package com.project.context.iparking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TimePicker;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WantParking2 extends AppCompatActivity {

    private TimePicker timePicker1;
    private Button sure;
    private EditText stext;
    private SharedPreferences sp;
    private String hid;
    private String location;
    JSONArray json;
    String uid,sid,isblue,timethis,otime,mac,text;
    Date time1,time2;
    int tag=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("config",0)==null){
            Toast.makeText(WantParking2.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else {
            setContentView(R.layout.parking_want2);

            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为要设置的颜色

            sure = (Button) findViewById(R.id.want2_sure);
            stext = (EditText) findViewById(R.id.tel2);
            Toolbar tool = (Toolbar) findViewById(R.id.parking_tool);
            setSupportActionBar(tool);

            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("预约时间");
            tool.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            sp = getSharedPreferences("temp",0);
            sid=sp.getString("sid", "");
            location = sp.getString("location", "");
            isblue = sp.getString("isblue","");
            stext.setText(sid);
            stext.setEnabled(false);

            TabHost tabHost = (TabHost) this.findViewById(R.id.want_tab);
            tabHost.setup(); // 找到内部的TabWidget和FrameLayout，其中的控件id是由android已经定义好了的

            tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("开始").setContent(R.id.tab1));
            tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("结束").setContent(R.id.tab2));
            tabHost.setCurrentTab(0);//设置初始选中状态为第一个tab

            TabWidget tabWidget = tabHost.getTabWidget();
            for (int i = 0; i < tabWidget.getChildCount(); i++) {
                tabWidget.getChildAt(i).setBackgroundResource(R.drawable.map_tab1);
            }
            timePicker1 = (TimePicker) findViewById(R.id.timePicker1);
            setNumberPickerTextSize(timePicker1);
            timePicker1.setIs24HourView(true);
            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    try {
                        if(tabId.equals("tab1")&&tag==2) {
                            tag = 1;
                            time2 = new SimpleDateFormat("HH:mm").parse(timePicker1.getCurrentHour() + ":" + timePicker1.getCurrentMinute()*15);
                            timePicker1.setCurrentHour(time1.getHours());
                            timePicker1.setCurrentMinute(time1.getMinutes()/15);
                            Log.e("tab1+time1+time2",time1.toString()+" "+time2.toString());
                        }else if(tabId.equals("tab2")&&tag==1){
                            tag = 2;
                            time1 = new SimpleDateFormat("HH:mm").parse(timePicker1.getCurrentHour() + ":" + timePicker1.getCurrentMinute()*15);
                            if(time2!=null) {
                                timePicker1.setCurrentHour(time2.getHours());
                                timePicker1.setCurrentMinute(time2.getMinutes()/15);
                            }
                            //Log.e("tab2+time1+time2",time1.toString()+" "+time2.toString());
                        }
                        timePicker1.invalidate();
                    }catch(Exception e){}
                }
            });

            sure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        if(tag == 1){
                            time1 = new SimpleDateFormat("HH:mm").parse(timePicker1.getCurrentHour() + ":" + timePicker1.getCurrentMinute()*15);
                        }else if(tag == 2){
                            time2 = new SimpleDateFormat("HH:mm").parse(timePicker1.getCurrentHour() + ":" + timePicker1.getCurrentMinute()*15);
                        }

                        Date now = new SimpleDateFormat("HH:mm").parse(new SimpleDateFormat("HH:mm").format(new Date()));

                        if (time1.compareTo(now) > 0) {
                            if (time2.compareTo(time1) > 0) {
                                //预约时间正确
                                long tt = (time2.getTime()-time1.getTime())/1000;
                                timethis = tt%(24*3600)/3600+":"+tt%3600/60+":"+tt%60;
                                otime = new SimpleDateFormat("HH:mm").format(time1)+"-"+new SimpleDateFormat("HH:mm").format(time2);

                                sp = getSharedPreferences("config", 0);
                                uid = sp.getString("userid", "");

                                Thread thread = new Thread(web);
                                thread.start();
                                try {
                                    thread.join();
                                    String flag = json.getJSONObject(0).getString("flag");
                                    if ("timeover".equals(flag)) {
                                        Toast.makeText(WantParking2.this, "车位选中时间已有预约，请重新输入！", Toast.LENGTH_SHORT).show();
                                    }else if("system".equals(flag)){
                                        Toast.makeText(WantParking2.this, "服务器掉线了，请重新输入！", Toast.LENGTH_SHORT).show();
                                    }else if("wrong".equals(flag)){
                                        Toast.makeText(WantParking2.this, "车位号错误，请重新尝试！", Toast.LENGTH_SHORT).show();
                                    }else if ("success".equals(flag)) {
                                        if("1".equals(isblue)){
                                            Toast.makeText(WantParking2.this, "您预约的车位为蓝牙车位，需要手动结束使用哦！", Toast.LENGTH_SHORT).show();
                                            mac = json.getJSONObject(0).getString("mac");
                                        }else {
                                            Toast.makeText(WantParking2.this, "您预约的车位可以自动结束使用哦！", Toast.LENGTH_SHORT).show();
                                            mac="";
                                        }
                                        hid = json.getJSONObject(0).getString("hid");
                                        text = json.getJSONObject(0).getString("text");

                                        sp = getSharedPreferences("space", 0);
                                        SharedPreferences.Editor edit = sp.edit();
                                        edit.putString("hid", hid);
                                        edit.putString("spaceid", sid);
                                        edit.putString("location", location);
                                        edit.putString("ordertime",otime);
                                        edit.putString("timethis",timethis);    //预约了多久
                                        edit.putString("isblue",isblue);
                                        edit.putString("what","2"); //小区车位
                                        edit.putString("day",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                        //动态获取蓝牙地址
                                        edit.putString("mac", mac);
                                        edit.putString("text", text);
                                        //提交编辑器
                                        edit.commit();

                                        new AlertDialog.Builder(WantParking2.this).setTitle("提示").setMessage("预约成功，您可在‘我的’中查看具体信息！")
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        startActivity(new Intent(WantParking2.this, Main2Activity.class));
                                                        finish();
                                                    }
                                                }).show();
                                    }
                                } catch (Exception e) {
                                }

                            } else {
                                //结束时间错误
                                Toast.makeText(WantParking2.this, "您预约的结束时间有误，请检查！", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //开始时间错误
                            Toast.makeText(WantParking2.this, "请选择有效的停车时间！", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                    }
                }

            });
        }
    }

    Runnable web = new Runnable() {
        @Override
        public void run() {

            json = WebService.order(uid, sid, otime,isblue,"2");

        }
    };

    String[] minuts = new String[]{"00","15", "30", "45"};//间隔15的数组，用来表示可设置的分钟值

    /**
     * 得到timePicker里面的android.widget.NumberPicker组件 （有两个android.widget.NumberPicker组件--hour，minute）
     * @param viewGroup
     * @return
     */
    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup)
    {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;

        if (null != viewGroup)
        {
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker)
                {
                    npList.add((NumberPicker)child);
                }
                else if (child instanceof LinearLayout)
                {
                    List<NumberPicker> result = findNumberPicker((ViewGroup)child);
                    if (result.size() > 0)
                    {
                        return result;
                    }
                }
            }
        }

        return npList;
    }

    /**
     * 查找timePicker里面的android.widget.NumberPicker组件 ，并对其进行时间间隔设置
     * @param viewGroup  TimePicker timePicker
     */
    private void setNumberPickerTextSize(ViewGroup viewGroup){
        List<NumberPicker> npList = findNumberPicker(viewGroup);
        if (null != npList)
        {
            for (NumberPicker mMinuteSpinner : npList)
            {
//              System.out.println("mMinuteSpinner.toString()="+mMinuteSpinner.toString());
                if(mMinuteSpinner.toString().contains("id/minute")){//对分钟进行间隔设置

                    mMinuteSpinner.setMinValue(0);
                    mMinuteSpinner.setMaxValue(minuts.length-1);
                    mMinuteSpinner.setDisplayedValues(minuts);  //这里的minuts是一个String数组，就是要显示的分钟值
                }
                //对小时进行间隔设置 使用 if(mMinuteSpinner.toString().contains("id/hour")){}即可
            }
        }
    }
}

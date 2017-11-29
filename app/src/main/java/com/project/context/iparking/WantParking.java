package com.project.context.iparking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WantParking extends AppCompatActivity {

    private Button sure;
    private EditText stext;
    private SharedPreferences sp;
    private String hid,text;
    JSONArray json;
    JSONObject jo;
    String uid,sid,location,isblue,flag,what,fils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getSharedPreferences("config",0).getString("userName","");
        if(name==null||"".equals(name)){
            Toast.makeText(WantParking.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else if(!"".equals(getSharedPreferences("space", 0).getString("isblue", ""))) {
            Toast.makeText(WantParking.this, "您已有预约车位，请先取消预约", Toast.LENGTH_SHORT).show();
        }else{
            setContentView(R.layout.parking_want);

            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为要设置的颜色

            sure = (Button) findViewById(R.id.next);
            stext = (EditText) findViewById(R.id.tel);
            Toolbar tool = (Toolbar) findViewById(R.id.parking_tool);
            setSupportActionBar(tool);

            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("我要停车");

            tool.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            sure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stext.getText().toString() == null || "".equals(stext.getText().toString())) {
                        Toast.makeText(WantParking.this, "请输入车位号！", Toast.LENGTH_SHORT).show();
                    } else {
                        //拿到sp
                        sp = getSharedPreferences("config", 0);
                        uid = sp.getString("userid", "");
                        sid = stext.getText().toString();

                        Thread thread = new Thread(web);
                        thread.start();
                        try {
                            thread.join();
                            jo = json.getJSONObject(0);
                            flag = jo.getString("flag");
                            location = jo.getString("location");
                            isblue = jo.getString("isblue");
                            what = jo.getString("what");
                        }catch(Exception e){}
                        if("wrongnumber".equals(flag)){
                            Toast.makeText(WantParking.this, "不存在的车位或者车位不可用，请重新输入！", Toast.LENGTH_SHORT).show();
                        }else if("system".equals(flag)){
                            Toast.makeText(WantParking.this, "预约失败，请重试！", Toast.LENGTH_SHORT).show();
                        }else if("nomoney".equals(flag)){
                            Toast.makeText(WantParking.this, "余额不足，请先充值！", Toast.LENGTH_SHORT).show();
                        }else if("success".equals(flag)){
                            if("1".equals(what)){   //路边

                                //提示此车位为路边即时停车车位，是否确认停车
                                new AlertDialog.Builder(WantParking.this).setTitle("提示").setMessage("此车位为路边即时停车车位，是否确认停车（确认即计费）？")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface da, int which) {
                                                if (!checkNetwork()) {
                                                    Toast toast = Toast.makeText(WantParking.this,"网络未连接", Toast.LENGTH_SHORT);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();
                                                    return;
                                                }

                                                fils="0";
                                                try {
                                                   Thread t = new Thread(find);
                                                   t.start();
                                                   t.join();
                                                   jo = json.getJSONObject(0);
                                                   flag = jo.getString("flag");
                                                   text = jo.getString("text");

                                                }catch(Exception e){}

                                                //直接连接、计时、开锁
                                                //Intent intent = new Intent("com.project.context.iparking.server.service.MyService");
                                                Intent intent = new Intent(WantParking.this, MyService.class);
                                                intent.putExtra("sid", sid);
                                                intent.putExtra("text", text);
                                                WantParking.this.startService(intent);

                                                //注册广播接收器
                                                IntentFilter filter = new IntentFilter();
                                                filter.addAction("com.project.context.iparking.server.service.MyService");
                                                WantParking.this.registerReceiver(new Receiver(), filter);

                                            }
                                        }).show();

                            }else{  //小区
                                //跳转时间预约界面
                                sp = getSharedPreferences("temp",0);
                                //获取sp的编辑器
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("sid", sid);
                                edit.putString("location", location);
                                edit.putString("isblue", isblue);
                                //提交编辑器
                                edit.commit();
                                finish();
                                startActivity(new Intent(WantParking.this, WantParking2.class));
                            }

                        }
                    }
                }
            });
        }
    }
    // 检测网络
    private boolean checkNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
    Runnable web = new Runnable() {
        @Override
        public void run() {
            json = WebService.findFlag(sid,uid);
        }
    };

    Runnable find = new Runnable() {
        @Override
        public void run() {
            json = WebService.roadway(sid, uid,fils);
        }
    };

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            if((sid+"ok").equals(bundle.getString("receive"))){

                fils="1";
                try {
                    Thread t = new Thread(find);
                    t.start();
                    t.join();
                    jo = json.getJSONObject(0);
                    flag = jo.getString("flag");

                    hid = jo.getString("hid");
                    text = jo.getString("text");

                }catch(Exception e){}

                sp = getSharedPreferences("space", 0);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("hid", hid);
                edit.putString("spaceid", sid);
                edit.putString("location", location);
                edit.putString("ordertime", new SimpleDateFormat("HH:mm").format(new Date()));
                edit.putString("isblue",isblue);
                edit.putString("what",what);
                edit.commit();

                //开启计时界面
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                finish();
                startActivity(new Intent(WantParking.this, Timer.class).putExtra("Time", df.format(new Date()) + " - /").putExtra("date", df.format(new Date())).putExtra("flags", "1"));

            }else{
                Toast.makeText(WantParking.this, "开锁失败，重试一下", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

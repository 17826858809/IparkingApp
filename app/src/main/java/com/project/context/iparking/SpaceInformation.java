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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SpaceInformation extends AppCompatActivity {

    private Button sure;
    private EditText ss,lo,ti,bl;
    private SharedPreferences sp;
    private String hid,text;
    JSONArray json;
    JSONObject jo;
    String uid,sid,location,isblue,time,flag,what;
    String fils=""; //标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getSharedPreferences("config",0).getString("userName","");
        if(name==null||"".equals(name)){
            Toast.makeText(SpaceInformation.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else if(!"".equals(getSharedPreferences("space", 0).getString("isblue", ""))) {
            Toast.makeText(SpaceInformation.this, "您已有预约车位，请先取消预约", Toast.LENGTH_SHORT).show();
        }else{
            setContentView(R.layout.space_information);

            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为要设置的颜色

            Intent intent = getIntent();
            location = intent.getStringExtra("location");
            time = intent.getStringExtra("times");
            isblue = intent.getStringExtra("isblue");
            sid = intent.getStringExtra("sid");

            sure = (Button) findViewById(R.id.info_ok);
            ss = (EditText)findViewById(R.id.space_info_id);
            lo = (EditText)findViewById(R.id.location);
            ti = (EditText)findViewById(R.id.info_time);
            bl = (EditText)findViewById(R.id.type);
            ss.setText("车位号： "+sid);
            lo.setText("位置："+location);
            if(time==null||"".equals(time)){
                ti.setText("目前可租用时间：全天");
            }else
                ti.setText("目前可租用时间："+time);
            if("2".equals(isblue)){
                bl.setText("车位为地下车位，需手动结束使用！");
            }else
                bl.setText("车位为联网车位，可自动结束！");
            Toolbar tool = (Toolbar) findViewById(R.id.information_tool);
            setSupportActionBar(tool);

            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("详细信息");

            tool.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                  //  startActivity(new Intent(SpaceInformation.this,Main2Activity.class));
                }
            });

            sure.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //拿到sp
                    sp = getSharedPreferences("config", 0);
                    uid = sp.getString("userid", "");

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
                        Toast.makeText(SpaceInformation.this, "不存在的车位或者车位不可用，请重新输入！", Toast.LENGTH_SHORT).show();
                    }else if("system".equals(flag)){
                        Toast.makeText(SpaceInformation.this, "预约失败，请重试！", Toast.LENGTH_SHORT).show();
                    }else if("nomoney".equals(flag)){
                        Toast.makeText(SpaceInformation.this, "余额不足，请先充值！", Toast.LENGTH_SHORT).show();
                    }else if("success".equals(flag)){
                        if("1".equals(what)){   //路边

                            //提示此车位为路边即时停车车位，是否确认停车
                            new AlertDialog.Builder(SpaceInformation.this).setTitle("提示").setMessage("此车位为路边即时停车车位，是否确认停车（确认即计费）？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface da, int which) {
                                            if (!checkNetwork()) {
                                                Toast toast = Toast.makeText(SpaceInformation.this,"网络未连接", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                return;
                                            }
                                            //先找到密码
                                            fils="0";   //不改变状态
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
                                            Intent intent = new Intent(SpaceInformation.this, MyService.class);
                                            intent.putExtra("sid", sid);
                                            intent.putExtra("text",text);
                                            SpaceInformation.this.startService(intent);

                                            // 提示框
                                            ProgressDialog dialog = new ProgressDialog(SpaceInformation.this);
                                            dialog.setTitle("提示");
                                            dialog.setMessage("正在登陆，请稍后...");
                                            dialog.setCancelable(false);

                                            //注册广播接收器
                                            IntentFilter filter = new IntentFilter();
                                            filter.addAction("com.project.context.iparking.server.service.MyService");
                                            SpaceInformation.this.registerReceiver(new Receiver(), filter);

                                            sure.setEnabled(false);
                                            sure.setBackgroundResource(R.drawable.text_shape_gray);
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
                            startActivity(new Intent(SpaceInformation.this, WantParking2.class));
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

    /**
     *重写系统返回键
     *
     */
  //  @Override
   // public boolean onKeyDown(int keyCode, KeyEvent event) {
     //   onBackPressed();
       // startActivity(new Intent(SpaceInformation.this, Main2Activity.class));
       // return true;
   // }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Log.e("receive", bundle.getString("receive") + "  " + sid + "ok");

            if(("{"+sid+"ok"+"}").equals(bundle.getString("receive"))){

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
                startActivity(new Intent(SpaceInformation.this, Timer.class).putExtra("Time", df.format(new Date()) + " - /").putExtra("date", df.format(new Date())).putExtra("flags", "1"));

            }else if(("{"+sid+"_RegisterSuccess"+"}").equals(bundle.getString("receive"))){

            }else{
                Toast.makeText(SpaceInformation.this, "开锁失败，重试一下", Toast.LENGTH_SHORT).show();

            }
        }

    }

}

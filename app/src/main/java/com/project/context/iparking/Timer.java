package com.project.context.iparking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Timer extends AppCompatActivity {
    private SharedPreferences sp;
    Intent intent;
    private String hid;
    private String spaceid;
    private String date,what,isblue,isclose,otime;
    Chronometer timer;
    private String userid;
    JSONArray json;
    String money,textword;
    long remain;  //剩余时间
    SimpleDateFormat sdf;
    Thread runs;
    TextView textView;
    Date now;
    int tttime,flagthing=2;
    MyReceiver mr;
    String flags="";
    String d1,d2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timer);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

        Toolbar toolbar = (Toolbar) findViewById(R.id.time_tool);
        toolbar.setTitle("Iparking");

        sp = getSharedPreferences("space", 0);
        hid = sp.getString("hid", "");
        spaceid = sp.getString("spaceid", "");
        what = sp.getString("what", "");
        isblue = sp.getString("isblue", "");
        otime = sp.getString("ordertime","");

        sp = getSharedPreferences("config", 0);
        userid = sp.getString("userid", "");

        //先查询数据库此车位是否结束使用，如果没有结束，则长连接到TCP上，如果结束了，则直接跳转。
        Thread findS=new Thread(findSpace);
        try{
            findS.start();
            findS.join();
            String tt=json.getJSONObject(0).getString("usetime");
            String[] af=tt.split("-");
            if(af.length==2){
                //已经结束使用，跳转
                flagthing=3;
                d1=af[0];
                d2=af[1];
                paysomething();
            }else{
                //还没结束使用，长连接到TCP服务器上
                Intent intent = new Intent(Timer.this, MyService.class);
                intent.putExtra("sid", getSharedPreferences("space", 0).getString("spaceid", ""));
                intent.putExtra("text", "");
                Timer.this.startService(intent);
            }

        }catch(Exception e){

        }
        timer = (Chronometer) findViewById(R.id.timer);
        intent = getIntent();
        String time = intent.getStringExtra("Time");    //预约时间
        date = intent.getStringExtra("date");   //date->开始计时时间
        flags = intent.getStringExtra("flags"); //标志是不是结束进程重新打开的，从而判断是否结束时该startActivity

        sp = getSharedPreferences("timer", 0);
        if(date!=null&&!"".equals(date)) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("flag", "true");
            edit.putString("ordertime", time);
            edit.putString("date", date);
            edit.putString("what",what);
            edit.putString("isblue",isblue);
            edit.commit();
            timer.setBase(SystemClock.elapsedRealtime());
        }

        timecount();

        //自动结束
        if(getSharedPreferences("space",0).getString("isblue","").equals("0")){    //联网车位，判断结束状态
            //注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.project.context.iparking.server.service.MyService");
            mr = new MyReceiver();
            Timer.this.registerReceiver(mr,filter);

        }


        //计时器
        if(date==null||"".equals(date)) {
            timer.setBase(SystemClock.elapsedRealtime() - tttime);//计时器初始化

        }
        //hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("%s");
        timer.start();

        Button timelong = (Button)findViewById(R.id.my_parking_timelength);
        textView = (TextView)findViewById(R.id.timer_overtime);
        if(!sp.getString("ordertime", "").equals("")) {
            textView.setText("车位位置："+getSharedPreferences("space",0).getString("location","")+"\n\n您的预约时间为 " + sp.getString("ordertime", ""));
        }else{
            textView.setText("车位位置："+getSharedPreferences("space",0).getString("location",""));
            timelong.setEnabled(false);
            timelong.setBackgroundResource(R.drawable.text_shape_gray);
        }

        if(getSharedPreferences("space",0).getString("what","").equals("2")) {   //如果是小区停车，判断预约时间
            runs = new Thread(run);
            runs.start();
        }

        Button btn = (Button)findViewById(R.id.my_parking_timeover);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isblue.equals("0")){ //联网车位
                    new AlertDialog.Builder(Timer.this).setTitle("提示").setMessage("当前车位为联网车位，当您的车开走后将自动结束计时。")
                            .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}}).show();
                }else {
                    new AlertDialog.Builder(Timer.this).setTitle("提示").setMessage("是否确定您已使用结束？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                        flagthing=2;
                                        d1 = getSharedPreferences("timer",0).getString("date","");
                                        d2 = new SimpleDateFormat("HH:mm").format(new Date());
                                        paysomething();

                                }
                            }).setNegativeButton("取消", null).show();
                }
            }
        });

        if("1".equals(what)){
            timelong.setEnabled(true);
        }else { //小区预约的
            timelong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thread timelength=new Thread(timeleng); //查找可延长的时间
                    timelength.start();
                    try{
                        timelength.join();
                        String re = json.getJSONObject(0).getString("re");
                        if(re==null||re.equals("")){
                            //不可延长
                            new AlertDialog.Builder(Timer.this).setTitle("提示").setMessage("\"您预订的时间之后已有新订单，无法延长使用，请按时结束使用，以免产生额外费用！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();
                        }else{
                            startActivity(new Intent(Timer.this, TimeLength.class).putExtra("how", re));
                        }
                        textView.setText("您的预约时间到 " + sp.getString("ordertime", ""));
                    }catch(Exception e){}

                }
            });
        }
    }

    public void timecount(){
        //现在时间-开始计时时间
        sp=getSharedPreferences("timer",0);
        sdf = new SimpleDateFormat("HH:mm:ss");
        String dd = sdf.format(new Date());
        int temp0 =Integer.parseInt( sp.getString("date", "").split(":")[0]);
        int temp1 =Integer.parseInt( sp.getString("date", "").split(":")[1]);
        int temp2 =Integer.parseInt(sp.getString("date", "").split(":")[2]);
        int temp = temp0*60*60+temp1*60+temp2;
        temp0 =Integer.parseInt( dd.split(":")[0]);
        temp1 =Integer.parseInt( dd.split(":")[1]);
        temp2 =Integer.parseInt( dd.split(":")[2]);
        int temps = temp0*60*60+temp1*60+temp2;
        tttime = (temps-temp)*1000;
        Log.e("-==========",""+tttime);
    }

    Runnable findSpace = new Runnable() { //结算，将剩下的时间开放
        @Override
        public void run() {
            json = WebService.findSpaceIsused(hid);
        }
    };

    Runnable changeState = new Runnable() { //结算，将剩下的时间开放
        @Override
        public void run() {

            json = WebService.Overthing(hid, flagthing, d1+"-"+d2); //参数：hid,使用了多久（分钟）,路边还是小区
        }
    };
    Runnable run = new Runnable(){
        @Override
        public void run() {

           while(true) {
                try {
                    //超时处理
                    SimpleDateFormat sdd = new SimpleDateFormat("HH:mm");
                    now = sdd.parse(sdd.format(new Date()));
                    String tafter = sp.getString("ordertime", "").split("[-]")[1];

                    remain = (sdd.parse(tafter).getTime() - now.getTime()) / 1000 / 60;
                    //Log.e("remain", "" + remain);
                    if (remain == 30 || remain == 20 || remain == 10 ) {
                        //距离预约时间到时还有半个小时
                        textword = "您的预约还有"+remain+"分钟，请及时移开车位结束计时或者延长预约，避免额外扣费！";
                        showFullScreen(Timer.this);
                        Thread.sleep(60000);
                    }else if(remain<=0){
                        if(remain == 0){
                            //到时没离开
                            textword = "您的预约时间已到时，请在10分钟内移出，否则将产生部分费用！";
                            showFullScreen(Timer.this);
                            Thread.sleep(60000);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    };

    Runnable timeleng = new Runnable() {
        @Override
        public void run() {
            json = WebService.findtimelength(spaceid,otime);
        }
    };
    /**
     * 悬挂式通知,支持6.0以上系统
     *
     * @param context
     */
    public void showFullScreen(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent mIntent = new Intent(context, Timer.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic));
        builder.setAutoCancel(true);
        builder.setContentTitle("车位到时提示");
        builder.setContentText(textword);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(3, builder.build());
    }
/**
 *禁用系统返回键
 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }
    /**
     * 获取MyService广播数据
     */
    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            isclose = bundle.getString("receive");
            Log.e("isclose",isclose);
            if(("{"+spaceid+"close"+"}").equals(isclose)){

                timecount();
                if("1".equals(what)) {   //路边
                    //修改数据库车位状态,计算金额,修改记录
                    flagthing=1;
                }else{  //小区
                    //计算金额，修改记录，（查看是否需要）超时付费
                    flagthing=2;

                }
                d1 = getSharedPreferences("timer",0).getString("date","");
                d2 = new SimpleDateFormat("HH:mm").format(new Date());

                paysomething();
            }
        }
    }
    public void paysomething(){

        Thread ts = new Thread(changeState);
        try {
            ts.start();
            ts.join();

            getSharedPreferences("space", 0).edit().clear().commit();
            getSharedPreferences("timer", 0).edit().clear().commit();

            String overmoney=json.getJSONObject(0).getString("overmoney");
            money = json.getJSONObject(0).getString("money");
            SharedPreferences sp = getSharedPreferences("config",0);
            String moneys = sp.getString("money","");
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("money", (Integer.parseInt(moneys) - Integer.parseInt(money)) + "");
            edit.commit();
            //unregisterReceiver(mr);
            finish();
            if("".equals(flags)||flags==null) flags="0";
            startActivity(new Intent(Timer.this, Over.class).putExtra("money", money).putExtra("overmoney", overmoney).putExtra("flags", flags));

        }catch(Exception e){}
    }

}

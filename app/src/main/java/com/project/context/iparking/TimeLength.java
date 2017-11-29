package com.project.context.iparking;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLength extends AppCompatActivity {
    TimePicker tp;
    SharedPreferences sp;
    JSONArray json;
    String ss,hid,how;
   @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.timelength);
       new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

       Intent intent = getIntent();
       how = intent.getStringExtra("how");

       tp=(TimePicker)findViewById(R.id.time_long);
       Button btn=(Button)findViewById(R.id.time_long_btn);
       TextView tv = (TextView)findViewById(R.id.times);
       tv.setText("延长预约到:(最迟可预约到 "+how+" )");

       Toolbar tool = (Toolbar) findViewById(R.id.time_long_toorbar);
       setSupportActionBar(tool);

       sp=getSharedPreferences("space",0);
       //显示返回按钮
       getSupportActionBar().setHomeButtonEnabled(true);
       //设置返回按钮可点击
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       //去掉原标题
       getSupportActionBar().setDisplayShowTitleEnabled(false);
       //设置主标题
       tool.setTitle("延长预约");

       btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String[] odtime = sp.getString("ordertime","").split("-");
               String otime = odtime[1];
               hid=sp.getString("hid","");
               ss = tp.getCurrentHour()+":"+tp.getCurrentMinute();
               Log.e("TimeLength",hid+"----"+ss+"----"+otime);
               SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
               try {
                   if (sdf.parse(ss).compareTo(sdf.parse(otime))>0) {
                       if(sdf.parse(ss).compareTo(sdf.parse(how))<=0) {
                           //延长预约
                           Thread thread = new Thread(web);
                           thread.start();
                           thread.join();

                           if (json == null) {
                               Toast.makeText(TimeLength.this, "修改失败！", Toast.LENGTH_SHORT).show();
                           } else {
                               Toast.makeText(TimeLength.this, "预约成功！", Toast.LENGTH_SHORT).show();
                               SharedPreferences.Editor edit = sp.edit();
                               edit.putString("ordertime", odtime[0]+"-"+ss);
                               edit.commit();

                               edit = getSharedPreferences("timer",0).edit();
                               edit.putString("ordertime", odtime[0]+"-"+ss);
                               edit.commit();
                               finish();
                           }
                       }else{
                           //超出时间
                           Toast.makeText(TimeLength.this, "您选择的时间已被预约，请重新选择！", Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       //提示不能在原有预约时间之前
                       Toast.makeText(TimeLength.this, "预约时间早于原有预约时间！", Toast.LENGTH_SHORT).show();

                   }
               }catch(Exception e){
                   e.printStackTrace();
               }
           }
       });
    }

    Runnable web = new Runnable() {
        @Override
        public void run() {

            json = WebService.change(hid, getSharedPreferences("space",0).getString("spaceid","") , tp.getCurrentHour()+":"+tp.getCurrentMinute());

        }
    };
}

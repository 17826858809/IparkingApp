package com.project.context.iparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

public class Recharge extends AppCompatActivity {

    private JSONArray json;
    JSONObject job;
    TextView tv100,tv50,tv20,tv10,depo300;
    int str=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recharge);

        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);

        Toolbar tb = (Toolbar)findViewById(R.id.recharge_tool);
        setSupportActionBar(tb);
        //显示返回按钮
        getSupportActionBar().setHomeButtonEnabled(true);
        //设置返回按钮可点击
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //去掉原标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //设置主标题
        tb.setTitle("充值");
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                startActivity(new Intent(Recharge.this,MyWallet.class));
            }
        });
        tv100 = (TextView)findViewById(R.id.get100);
        tv50 = (TextView)findViewById(R.id.get50);
        tv20 = (TextView)findViewById(R.id.get20);
        tv10 = (TextView)findViewById(R.id.get10);
        depo300 = (TextView)findViewById(R.id.depo300);

        tv100.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv100.setBackgroundResource(R.drawable.text_shape4);
                tv100.setTextColor(getResources().getColor(R.color.white));

                tv20.setBackgroundResource(R.drawable.text_shape3);
                tv20.setTextColor(getResources().getColor(R.color.black));
                tv50.setBackgroundResource(R.drawable.text_shape3);
                tv50.setTextColor(getResources().getColor(R.color.black));
                tv10.setBackgroundResource(R.drawable.text_shape3);
                tv10.setTextColor(getResources().getColor(R.color.black));
                depo300.setBackgroundResource(R.drawable.text_shape3);
                depo300.setTextColor(getResources().getColor(R.color.black));
                str = 100;
            }
        });
        tv20.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv20.setBackgroundResource(R.drawable.text_shape4);
                tv20.setTextColor(getResources().getColor(R.color.white));

                tv100.setBackgroundResource(R.drawable.text_shape3);
                tv100.setTextColor(getResources().getColor(R.color.black));
                tv50.setBackgroundResource(R.drawable.text_shape3);
                tv50.setTextColor(getResources().getColor(R.color.black));
                tv10.setBackgroundResource(R.drawable.text_shape3);
                tv10.setTextColor(getResources().getColor(R.color.black));
                depo300.setBackgroundResource(R.drawable.text_shape3);
                depo300.setTextColor(getResources().getColor(R.color.black));
                str = 20;
            }
        });
        tv50.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv50.setBackgroundResource(R.drawable.text_shape4);
                tv50.setTextColor(getResources().getColor(R.color.white));
                tv20.setBackgroundResource(R.drawable.text_shape3);
                tv20.setTextColor(getResources().getColor(R.color.black));
                tv100.setBackgroundResource(R.drawable.text_shape3);
                tv100.setTextColor(getResources().getColor(R.color.black));
                tv10.setBackgroundResource(R.drawable.text_shape3);
                tv10.setTextColor(getResources().getColor(R.color.black));
                depo300.setBackgroundResource(R.drawable.text_shape3);
                depo300.setTextColor(getResources().getColor(R.color.black));
                str= 50;
            }
        });
        tv10.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv10.setBackgroundResource(R.drawable.text_shape4);
                tv10.setTextColor(getResources().getColor(R.color.white));
                tv20.setBackgroundResource(R.drawable.text_shape3);
                tv20.setTextColor(getResources().getColor(R.color.black));
                tv50.setBackgroundResource(R.drawable.text_shape3);
                tv50.setTextColor(getResources().getColor(R.color.black));
                tv100.setBackgroundResource(R.drawable.text_shape3);
                tv100.setTextColor(getResources().getColor(R.color.black));
                depo300.setBackgroundResource(R.drawable.text_shape3);
                depo300.setTextColor(getResources().getColor(R.color.black));
                str = 10;
            }
        });
        depo300.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                depo300.setBackgroundResource(R.drawable.text_shape4);
                depo300.setTextColor(getResources().getColor(R.color.white));

                tv100.setBackgroundResource(R.drawable.text_shape3);
                tv100.setTextColor(getResources().getColor(R.color.black));
                tv20.setBackgroundResource(R.drawable.text_shape3);
                tv20.setTextColor(getResources().getColor(R.color.black));
                tv50.setBackgroundResource(R.drawable.text_shape3);
                tv50.setTextColor(getResources().getColor(R.color.black));
                tv10.setBackgroundResource(R.drawable.text_shape3);
                tv10.setTextColor(getResources().getColor(R.color.black));
                str = 300;
            }
        });

        //充值
        Button btn =(Button)findViewById(R.id.charge);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(str==0){
                    Toast.makeText(Recharge.this, "请选择充值金额", Toast.LENGTH_SHORT).show();
                }else{
                    Thread th = new Thread(charge);
                    th.start();
                    try{
                        th.join();
                        job = json.getJSONObject(0);
                        String flag = job.getString("flag");
                        if("success".equals(flag)){
                            Toast.makeText(Recharge.this, "充值成功！", Toast.LENGTH_SHORT).show();
                            if(str<300) {
                                SharedPreferences sp = getSharedPreferences("config", 0);
                                SharedPreferences.Editor ed = sp.edit();
                                ed.putString("money", (Integer.parseInt(sp.getString("money", "")) + str) + "");
                                ed.commit();
                            }
                            startActivity(new Intent(Recharge.this,MyWallet.class));
                            finish();
                        }else{
                            Toast.makeText(Recharge.this, "哎呀，服务器掉线了！", Toast.LENGTH_SHORT).show();
                        }
                    }catch(Exception e){}
                }
            }
        });
    }

    Runnable charge = new Runnable() {
        @Override
        public void run() {
            json = WebService.charge(getSharedPreferences("config", 0).getString("userid", ""), str);
        }
    };
}

package com.project.context.iparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyWallet extends AppCompatActivity {

    private JSONArray json;
    TextView deposit,ticket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet);

        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);

        Toolbar tb = (Toolbar)findViewById(R.id.wallet_tool);
        setSupportActionBar(tb);
        //显示返回按钮
        getSupportActionBar().setHomeButtonEnabled(true);
        //设置返回按钮可点击
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //去掉原标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //设置主标题
        tb.setTitle("我的钱包");
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        TextView tv = (TextView)findViewById(R.id.money);
        tv.setText(getSharedPreferences("config", 0).getString("money", ""));

        deposit = (TextView)findViewById(R.id.deposit);
        ticket = (TextView)findViewById(R.id.ticket);
        Thread th = new Thread(wallet);
        th.start();
        try{
            th.join();
            JSONObject job = json.getJSONObject(0);
            deposit.setText(job.get("deposit").toString());
            ticket.setText(job.get("ticket").toString());
        }catch(Exception e){}

        //充值
        Button btn =(Button)findViewById(R.id.wallet_in);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyWallet.this,Recharge.class));
                finish();
            }
        });
    }

    Runnable wallet = new Runnable() {
        @Override
        public void run() {
            json = WebService.findWallet(getSharedPreferences("config",0).getString("userid",""));
        }
    };
}

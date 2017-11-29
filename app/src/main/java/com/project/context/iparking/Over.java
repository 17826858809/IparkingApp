package com.project.context.iparking;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Over extends Activity {
    String flags="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.over);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);

        Toolbar tool = (Toolbar)findViewById(R.id.over_tool);
        tool.setTitle("完成支付");

        Intent intent = getIntent();
        String money = intent.getStringExtra("money");
        String overmoney = intent.getStringExtra("overmoney");
        flags = intent.getStringExtra("flags");
        TextView tv = (TextView)findViewById(R.id.mon);
        double dou = Double.parseDouble(money)+Double.parseDouble(overmoney);
        tv.setText(dou+"");

        TextView in = (TextView)findViewById(R.id.inmon);
        in.setText(money);

        TextView out = (TextView)findViewById(R.id.overmon);
        out.setText(overmoney);

        //按钮点击跳出主界面
        Button btn = (Button)findViewById(R.id.over_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Over.this, Main2Activity.class));
                finish();
            }
        });
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
}

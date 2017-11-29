package com.project.context.iparking;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.utils.SPUtils;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyHistory extends AppCompatActivity {
    private static final String TAG = "MyHistory";

    JSONArray json;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("config",0)==null){
            Toast.makeText(MyHistory.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else {

            setContentView(R.layout.my_history);

            //toolbar
            Toolbar tool = (Toolbar) findViewById(R.id.his_tool);
            setSupportActionBar(tool);
            TableLayout table = (TableLayout) findViewById(R.id.his_table);

            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("我的行程");
            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为要设置的颜色

            tool.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            try {
               // if(readHistory(this,"history").size()==0){
                    //显示车位使用情况
                    Thread thread = new Thread(find);
                    thread.start();
                    thread.join();  //让主线程等待子线程结束
                    String strs = "";
                    if (json==null||"".equals(json)) {
                        Toast.makeText(this, "您还没有使用过车位，快去尝尝鲜吧！", Toast.LENGTH_SHORT);
                    } else {
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject job = json.getJSONObject(i);
                            strs+= job.getString("usedate") +",车位：" + job.getString("sid") +  ",￥" + job.getString("money")+ ";" ;
                        }
                    }
               // }
                String[] list = strs.split(";");
                if(strs!="") {
                    for (String all : list) {
                        String[] str = all.split(",");
                        TableRow row = new TableRow(this);
                        TextView text1 = new TextView(this);
                        text1.setWidth(310);
                        text1.setHeight(120);
                        text1.setTextSize(18);
                        text1.setText(str[0]);
                        text1.setTextColor(getResources().getColor(R.color.black));
                        row.addView(text1);
                        //使用车位
                        TextView text2 = new TextView(this);
                        text2.setWidth(560);
                        text2.setTextSize(18);
                        text2.setGravity(Gravity.CENTER);   //居中
                        text2.setText(str[1]);
                        text2.setTextColor(getResources().getColor(R.color.gray));
                        row.addView(text2);

                        //金额
                        TextView text3 = new TextView(this);
                        text3.setWidth(300);
                        text3.setTextSize(20);
                        text3.setText(str[2]);
                        text3.setTextColor(getResources().getColor(R.color.colorAccent));
                        row.addView(text3);

                        row.setBackgroundColor(getResources().getColor(R.color.white));
                        row.setBottom(20);
                        table.addView(row);
                    }
                }else{
                    Toast.makeText(this, "您还没有使用过车位，快去尝尝鲜吧！", Toast.LENGTH_SHORT);
                }

            } catch (Exception e) {
                TableRow row = new TableRow(this);
                TextView text = new TextView(this);
                text.setWidth(900);
                text.setTextSize(18);
                text.setText("没有更多行程");
                text.setGravity(Gravity.CENTER);
                text.setTextColor(getResources().getColor(R.color.gray));
                row.addView(text);
                table.addView(row);

                Toast.makeText(MyHistory.this, "网络请求超时，请在稳定的网络下连接重试！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    Runnable find = new Runnable() {
        @Override
        public void run() {
            json = WebService.findMyHistory(getSharedPreferences("config", 0).getString("userid", ""));
        }
    };

    /**
     * 读取历史纪录
     * @param context
     * @param historyKey
     * @return
     */
    public static List<String> readHistory(Context context, String historyKey){
        String history = (String) SPUtils.get(context, historyKey, "");
        String[] histroys = history.split(";");
        List<String> list = new ArrayList<String>();
        if(histroys.length > 0){
            for (int i = 0; i < histroys.length; i++) {
                if(histroys[i] != null && histroys[i].length()>0){
                    list.add(histroys[i]);
                }
            }
        }
        return list;
    }

    /**
     * 插入历史纪录
     * @param context
     * @param historyKey
     * @param value
     */
    public static void insertHistory(Context context, String historyKey, String value) {
        String history = (String) SPUtils.get(context, historyKey, "");
        boolean repeat = false;
        if (history != null && history.length() > 0) {
            SPUtils.put(context, historyKey, value + ";" + history);
        } else {
            SPUtils.put(context, historyKey, value);
        }
    }
}
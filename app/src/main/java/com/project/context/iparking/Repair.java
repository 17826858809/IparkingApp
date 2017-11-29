package com.project.context.iparking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Repair extends AppCompatActivity {

    private static final String[] m={"无法开锁","车位被破坏了","其他"};
    private ArrayAdapter<String> adapter;
    JSONArray ja;
    JSONObject job;
    String sid,text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("config",0)==null){
            Toast.makeText(Repair.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else {
            setContentView(R.layout.space_repair);

            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为要设置的颜色

            Intent in = getIntent();
            sid = in.getStringExtra("sid");
            TextView tv = (TextView)findViewById(R.id.space_sid);
            tv.setText(sid);
            tv.setEnabled(false);
            Toolbar tool = (Toolbar) findViewById(R.id.parking_tool);
            setSupportActionBar(tool);

            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("我要报修");
            tool.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            //将可选内容与ArrayAdapter连接起来
            adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);

            //设置下拉列表的风格
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            //将adapter 添加到spinner中
            spinner.setAdapter(adapter);

            //添加事件Spinner事件监听
            spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

            //设置默认值
            spinner.setVisibility(View.VISIBLE);

            Button btn = (Button)findViewById(R.id.space_ok);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //报修
                    Thread th = new Thread(repair);
                    th.start();
                    try{
                        th.join();
                        if(ja!=null){
                            String flag = ja.getJSONObject(0).getString("flag");
                            if("success".equals(flag)){
                                Toast.makeText(Repair.this, "报修成功，工作人员将会飞速处理！", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                Toast.makeText(Repair.this, "服务器掉线了，重新报修一下吧！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }catch(Exception e){
                        Toast.makeText(Repair.this, "网络无法连接！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            text = m[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    Runnable repair = new Runnable() {
        @Override
        public void run() {
            ja = WebService.repair(sid,text);
        }
    };
}

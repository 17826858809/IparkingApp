package com.project.context.iparking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;
import android.support.v7.widget.Toolbar;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends AppCompatActivity {
    private EditText userName;
    private EditText pwd;
    private CheckBox check;
    private SharedPreferences sp;
    private String userNametext,pwdtext,sid;
    // 创建等待框
    private ProgressDialog dialog;
    private String info,money,hid,spaceid,ordertime,date,when,isblue;    //返回的数据
    // 返回主线程更新数据
    private static Handler handler = new Handler();
    JSONArray json;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

        Toolbar toolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        //拿到sp实例
        sp=getSharedPreferences("register", 0);
        //拿到值
        userName = (EditText) findViewById(R.id.userName);
        pwd = (EditText) findViewById(R.id.pwd);
        check = (CheckBox) findViewById(R.id.checkBox);
        //数据从sp中拿到
        String name = sp.getString("userName","");
        String password = sp.getString("pwd","");
        //设置值到页面上
        userName.setText(name);
        pwd.setText(password);
        boolean result = sp.getBoolean("isChecked",false);
        if(result){
            check.setChecked(true);
        }

        //手机登录
        TextView tv = (TextView)findViewById(R.id.login_tel);
        tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Login_tel.class));
                finish();
            }
        });

    }

    public void login(View v){

        userNametext = this.userName.getText().toString().trim();
        pwdtext = this.pwd.getText().toString().trim();
        if(TextUtils.isEmpty(userNametext)||TextUtils.isEmpty(pwdtext)){
            Toast.makeText(this,"用户名和密码不能为空！",Toast.LENGTH_SHORT).show();
        }else{
            switch (v.getId()) {
                case R.id.log:
                    // 检测网络，无法检测wifi
                    if (!checkNetwork()) {
                        Toast toast = Toast.makeText(Login.this,"网络未连接", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        break;
                    }
                    // 提示框
                    dialog = new ProgressDialog(this);
                    dialog.setTitle("提示");
                    dialog.setMessage("正在登陆，请稍后...");
                    dialog.setCancelable(false);
                    //dialog.show();
                    // 创建子线程，分别进行Get和Post传输
                    new Thread(new MyThread()).start();
                    break;
            }
        }
    }

    // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
            json = WebService.Login(userName.getText().toString(), pwd.getText().toString());

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (json == null || "".equals(json)) {
                        Toast.makeText(Login.this, "用户名或密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            info = json.getJSONObject(0).getString("userid");
                            money = json.getJSONObject(0).getString("money");
                            hid = json.getJSONObject(0).getString("hid");
                            if (hid != null) {
                                spaceid = json.getJSONObject(0).getString("spaceid");
                                ordertime = json.getJSONObject(0).getString("ordertime");
                                date = json.getJSONObject(0).getString("date");
                                when = json.getJSONObject(0).getString("when");
                                isblue = json.getJSONObject(0).getString("isblue");
                                int days = ((int) (new SimpleDateFormat("yyyy-MM-dd").parse(when).getTime()
                                        - new SimpleDateFormat("yyyy-MM-dd").parse(new Date().toString()).getTime())) / 1000 / 3600 / 24;
                                if (days > 0) {
                                    date = (Integer.parseInt(date) - days * 24) + "";
                                }
                                Editor edit = getSharedPreferences("space", 0).edit();
                                edit.putString("hid", hid);
                                edit.putString("spaceid", spaceid);
                                edit.putString("ordertime", ordertime);
                                edit.putString("isblue", isblue);
                                edit.commit();

                                edit = getSharedPreferences("timer", 0).edit();
                                edit.putString("flag", "true");
                                edit.putString("ordertime", ordertime);
                                edit.putString("date", date);
                                edit.putString("isblue",isblue);
                                edit.commit();

                            }
                        } catch (Exception e) {
                        }
                        sp = getSharedPreferences("config", 0);
                        //获取sp的编辑器
                        Editor edit = sp.edit();
                        edit.putString("userid", info);
                        edit.putString("userName", userNametext);
                        edit.putString("pwd", pwdtext);
                        edit.putString("money", money);
                        edit.putBoolean("isChecked", true);   //存储复选框状态

                        //提交编辑器
                        edit.commit();

                        Toast.makeText(Login.this, "success!", Toast.LENGTH_SHORT).show();
                        if (hid != null) {
                            startActivity(new Intent(Login.this,Timer.class));
                        } else
                            startActivity(new Intent(Login.this, Main2Activity.class));
                        finish();
                    }


                    dialog.dismiss();
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


    public void register(View view) {

        startActivity(new Intent(this, Register.class));
    }

}

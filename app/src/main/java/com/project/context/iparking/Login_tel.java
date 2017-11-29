package com.project.context.iparking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class Login_tel extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sp;
    private String userid,username,pwd,telephone,tel,money,hid,spaceid,ordertime,date,when,isblue;
    // 创建等待框
    private ProgressDialog dialog;
    // 返回主线程更新数据
    private TextView btnSendMsg;
    private EditText phone,etcode;
    private Button login;
    private JSONArray ja;
    JSONObject job;
    JSONArray json=null;
    private int i=60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_tel);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

        Toolbar toolbar = (Toolbar)findViewById(R.id.login_tel_toolbar);

        phone = (EditText)findViewById(R.id.login_tel_phone);
        login = (Button)findViewById(R.id.login);
        etcode = (EditText)findViewById(R.id.etCode);

        // 通过代码注册你的AppKey和AppSecret(验证码)
        MobSDK.init(this, "1fc7ace85f91e", "0bb5dc05f4b7246ab42a7d1037624ee6");
        btnSendMsg = (TextView)findViewById(R.id.btnSendMsg);
        //短信验证码
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handlers.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);

        btnSendMsg.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    Handler handlers = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -1) {
                btnSendMsg.setText(i + " s");
            } else if (msg.what == -2) {
                btnSendMsg.setText("重新发送");
                btnSendMsg.setClickable(true);
                i = 60;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("asd", "event=" + event + "  result=" + result + "  ---> result=-1 success , result=0 error");
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，Register,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 提交验证码成功,调用注册接口，之后直接登录
                        // Toast.makeText(getApplicationContext(), "短信验证成功", Toast.LENGTH_SHORT).show();

                        Thread tt = new Thread(new MyThread());
                        tt.start();
                        try{
                            tt.join();
                        }catch(Exception e){e.printStackTrace();}
                        try {
                            userid = json.getJSONObject(0).getString("userid");
                            username = json.getJSONObject(0).getString("username");
                            pwd = json.getJSONObject(0).getString("pwd");
                            telephone = json.getJSONObject(0).getString("phone");


                        }catch(Exception e){
                            Log.e("Login", "获取数据出错！");
                        }
                        if (userid == null || "".equals(userid)) {
                            Toast.makeText(Login_tel.this, "手机号未注册！", Toast.LENGTH_SHORT).show();
                        } else {
                            try{
                                money = json.getJSONObject(0).getString("money");
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
                            }catch(Exception e ){}
                            sp = getSharedPreferences("config", 0);
                            //获取sp的编辑器
                            Editor edit = sp.edit();
                            edit.putString("userid", userid);
                            edit.putString("userName", username);
                            edit.putString("pwd", pwd);
                            edit.putString("phone",telephone);
                            edit.putString("money",money);

                            //提交编辑器
                            edit.commit();

                            Toast.makeText(Login_tel.this, "success!", Toast.LENGTH_SHORT).show();
                            if (hid != null) {
                                startActivity(new Intent(Login_tel.this,Timer.class));
                            } else
                                startActivity(new Intent(Login_tel.this, Main2Activity.class));
                            finish();
                            dialog.dismiss();

                        }
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                        Log.e("phone",tel);
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                } else if (result == SMSSDK.RESULT_ERROR) {
                    try {
                        Throwable throwable = (Throwable) data;
                        throwable.printStackTrace();
                        JSONObject object = new JSONObject(throwable.getMessage());
                        String des = object.optString("detail");//错误描述
                        int status = object.optInt("status");//错误代码
                        if (status > 0 && !TextUtils.isEmpty(des)) {
                            Log.e("asd", "des: " + des);
                            Toast.makeText(Login_tel.this, des, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        //do something
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        tel = phone.getText().toString();

        if(TextUtils.isEmpty(tel)){
            Toast.makeText(this,"请输入手机号！",Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.btnSendMsg:
                SMSSDK.getVerificationCode("86", tel);
                btnSendMsg.setClickable(false);
                //开始倒计时
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handlers.sendEmptyMessage(-1);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handlers.sendEmptyMessage(-2);
                    }
                }).start();
                break;
            case R.id.login:
                // 检测网络，无法检测wifi
                if (!checkNetwork()) {
                    Toast toast = Toast.makeText(Login_tel.this,"网络未连接", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }
                String code = etcode.getText().toString().trim();
                if(TextUtils.isEmpty(code)) {
                    Toast.makeText(getApplicationContext(), "验证码不能为空!", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    // 提示框
                    dialog = new ProgressDialog(this);
                    dialog.setTitle("提示");
                    dialog.setMessage("正在登陆，请稍后...");
                    dialog.setCancelable(false);
                    //dialog.show();
                    // 创建子线程，分别进行Get和Post传输
                    //new Thread(new MyThread()).start();

                    SMSSDK.submitVerificationCode("86", tel, code);

                }

                break;
            default:
                break;
        }

    }

    // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
            json = WebService.Login_tel(phone.getText().toString());



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

}

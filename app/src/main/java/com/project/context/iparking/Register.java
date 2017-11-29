package com.project.context.iparking;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;

import com.mob.MobSDK;
import com.project.context.iparking.web.WebService;
import org.json.JSONArray;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class Register extends AppCompatActivity implements View.OnClickListener  {
    private EditText userName;
    private EditText pwd;
    private EditText phone,etCode;
    private CheckBox check;
    private int i = 60;//倒计时

    String name;
    String p;
    String tel;
    String flag;
    EventHandler eventHandler=null;
    Button btnSubmitCode;
    Boolean checks;
    TextView btnSendMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

        Toolbar tool = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tool);
        //去掉原标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //设置主标题
        tool.setTitle("注册");

        // 通过代码注册你的AppKey和AppSecret(验证码)
        MobSDK.init(this, "1fc7ace85f91e", "0bb5dc05f4b7246ab42a7d1037624ee6");

        userName = (EditText) findViewById(R.id.userName);
        pwd = (EditText) findViewById(R.id.pwd);
        phone = (EditText) findViewById(R.id.phone);
        check = (CheckBox) findViewById(R.id.checkBox);
        btnSendMsg = (TextView)findViewById(R.id.btnSendMsg);
        btnSubmitCode = (Button)findViewById(R.id.btn);
        etCode = (EditText)findViewById(R.id.etCode);
        //短信验证码
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);

        btnSendMsg.setOnClickListener(this);
        btnSubmitCode.setOnClickListener(this);
    }
    Handler handler = new Handler() {
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
                        //当号码来自短信注册页面时调用登录注册接口
                        //当号码来自绑定页面时调用绑定手机号码接口

                       // Toast.makeText(getApplicationContext(), "短信验证成功", Toast.LENGTH_SHORT).show();

                        Thread tt = new Thread(thread);
                        tt.start();
                        try{
                            tt.join();
                        }catch(Exception e){e.printStackTrace();}
                        if("phone".equals(flag))
                            Toast.makeText(Register.this, "手机号已注册，快去登录吧！", Toast.LENGTH_SHORT).show();
                        else if("name".equals(flag))
                            Toast.makeText(Register.this, "好悲伤，用户名已被注册，换一个吧！", Toast.LENGTH_SHORT).show();
                        else if("success".equals(flag)) {
                            Toast.makeText(Register.this, "注册成功啦！", Toast.LENGTH_SHORT).show();

                            if (checks) {
                                SharedPreferences sp = getSharedPreferences("register", 0);
                                Editor editor = sp.edit();
                                editor.putString("userName", name);
                                editor.putString("pwd", p);
                                editor.putString("tel",tel);
                                editor.putBoolean("isChecked", true);

                                editor.commit();
                            }
                            finish();
                        }else
                            Toast.makeText(Register.this, "哎呀，服务器开小差了，请重试！", Toast.LENGTH_SHORT).show();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "验证码已经发送",
                                Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Register.this, des, Toast.LENGTH_SHORT).show();
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
        name = userName.getText().toString();
        p = pwd.getText().toString();
        tel = phone.getText().toString();
        checks = this.check.isChecked();

        switch (v.getId()) {
            case R.id.btnSendMsg:
                if(TextUtils.isEmpty(name)||TextUtils.isEmpty(p)||TextUtils.isEmpty(tel)){
                    Toast.makeText(this,"信息还不完整，请重新填写！",Toast.LENGTH_SHORT).show();
                    return;
                }
                SMSSDK.getVerificationCode("86", tel);
                btnSendMsg.setClickable(false);
                //开始倒计时
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-1);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-2);
                    }
                }).start();
                break;
            case R.id.btn:
                String code = etCode.getText().toString().trim();
                if(TextUtils.isEmpty(name)||TextUtils.isEmpty(p)||TextUtils.isEmpty(tel)){
                    Toast.makeText(this,"信息还不完整，请重新填写！",Toast.LENGTH_SHORT).show();
                //}else  if (TextUtils.isEmpty(code)) {
                   // Toast.makeText(getApplicationContext(), "验证码不能为空", Toast.LENGTH_SHORT).show();
                   // return;
                }else{
                    SMSSDK.submitVerificationCode("86", tel, code);

                }

                break;
            default:
                break;
        }

    }

    Runnable thread = new Runnable() {
        @Override
        public void run() {
            JSONArray json = WebService.register(name, p, tel);
            try {
                flag = json.getJSONObject(0).getString("flag");
                Log.e("flag",flag);
            }catch(Exception e){
                Toast.makeText(Register.this, "哎呀，服务器开小差了，请稍后重试！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }
}

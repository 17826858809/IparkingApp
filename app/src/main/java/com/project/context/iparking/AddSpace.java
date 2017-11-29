package com.project.context.iparking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

public class AddSpace extends AppCompatActivity {

    private static final String TAG = "AddSpace";
    EditText editText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addspace);

        Toolbar tool = (Toolbar) findViewById(R.id.add_tool);
        setSupportActionBar(tool);
        new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

        //显示返回按钮
        getSupportActionBar().setHomeButtonEnabled(true);
        //设置返回按钮可点击
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //去掉原标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //设置主标题
        tool.setTitle("发布车位");
        tool.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button btn = (Button) findViewById(R.id.add_btn);
        editText = (EditText) findViewById(R.id.add_loc);

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) {
                    Toast.makeText(AddSpace.this, "请先输入地址信息！", Toast.LENGTH_SHORT);
                } else
                    new AlertDialog.Builder(AddSpace.this).setTitle("提示").setMessage("您确定要加入共享车位？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Thread thread = new Thread(out);
                                    thread.start();
                                    try {
                                        thread.join();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(AddSpace.this, "车位等待审核安装！", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).setNegativeButton("取消", null).show();
               }
        });
    }



    Runnable out = new Runnable() {
        @Override
        public void run() {
            WebService.addSpace(getSharedPreferences("config",0).getString("userid",""),editText.getText().toString());
        }
    };
}
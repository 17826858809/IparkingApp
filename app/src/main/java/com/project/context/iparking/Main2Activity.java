package com.project.context.iparking;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.project.context.iparking.circleImageview.CircleImageView;
import com.project.context.iparking.server.service.BootBroadcastReceiver;
import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 宏观布局
 */
public class Main2Activity extends ActivityGroup
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    private TextView title;
    private TextView parking;
    private int i;
    SharedPreferences sp;
    TabHost tabHost;
    BootBroadcastReceiver sv;
    String name;
    CircleImageView head;
    JSONArray myjson;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("timer", 0);
        String ff = sp.getString("flag", "");
        Log.e("Main2Activity", ff);
        if (ff != null && !("".equals(ff))) {
            startActivity(new Intent(this, Timer.class));
            finish();
        } else {
            SDKInitializer.initialize(getApplicationContext());
            setContentView(R.layout.activity_main2);

            Toolbar toolbar = (Toolbar) findViewById(R.id.tabs_toolbar);

            title = (TextView) findViewById(R.id.maps_title);
            parking = (TextView) findViewById(R.id.parking);

            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色


            //获取TabHost对象
            TabHost tabHost = (TabHost) this.findViewById(R.id.tabhost);
            tabHost.setup(this.getLocalActivityManager());

            tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("").setContent(new Intent(this, MapsActivity.class)));
            tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("").setContent(new Intent(this, RentParking.class)));
            // tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("").setContent(new Intent(this, Friend.class)));

            tabHost.setCurrentTab(0);//设置初始选中状态为第一个tab
            title.setText("iparking");
            parking.setText("我要停车");
            parking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = getSharedPreferences("config", 0).getString("userName", "");
                    if (name == null || "".equals(name)) {
                        // Toast.makeText(Main2Activity.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Main2Activity.this, Login.class));
                    } else {
                        startActivity(new Intent(Main2Activity.this, WantParking.class));
                    }
                }
            });

            TabWidget tabWidget = tabHost.getTabWidget();
            //TabWidget tabWidget = (TabWidget)findViewById(R.id.tabwid);
            // Change strip(tab bottom line) color
            for (i = 0; i < tabWidget.getChildCount(); i++) {
                if (i == 0)
                    tabWidget.getChildAt(i).setBackgroundResource(R.drawable.tab1);
                if (i == 1)
                    tabWidget.getChildAt(i).setBackgroundResource(R.drawable.tab2);
                //if (i == 2)
                //  tabWidget.getChildAt(i).setBackgroundResource(R.drawable.tab3);
            }
            tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    if (tabId.equals("tab1")) {

                        title.setText("iparking");
                        parking.setText("我要停车");
                        parking.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = getSharedPreferences("config", 0).getString("userName", "");
                                if (name == null || "".equals(name)) {
                                    //         Toast.makeText(Main2Activity.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Main2Activity.this, Login.class));

                                } else {

                                    startActivity(new Intent(Main2Activity.this, WantParking.class));
                                }
                            }
                        });

                    } else if (tabId.equals("tab2")) {
                        title.setText("我的预约");
                        parking.setText("");
                        parking.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                    }// else {
                    //  title.setText("车友圈");
                    //  parking.setText("");
                    //  parking.setOnClickListener(new View.OnClickListener() {
                    //     @Override
                    //      public void onClick(View v) {
                    //    }
                    //});
                    // toolbar.hideOverflowMenu();
                    // }
                }
            });

            //toolbar相关

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            head = (CircleImageView) headerView.findViewById(R.id.headcircle);

            sv = new BootBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            this.registerReceiver(sv, intentFilter);

            //拿到sp实例
            sp = getSharedPreferences("config", 0);
            //数据从sp中拿到
            name = sp.getString("userName", "");
            if (name == null || "".equals(name)) {
                head.setImageResource(R.mipmap.ic_launcher);
            } else
                head.setImageResource(R.mipmap.my);
            head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (name == null || "".equals(name)) {
                        Toast.makeText(Main2Activity.this, "请先登录！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Main2Activity.this, Login.class));
                        // finish();
                    } else {
                        new AlertDialog.Builder(Main2Activity.this).setTitle("提示").setMessage(name + ",您确定退出登录吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        // if(getSharedPreferences("timer", 0)!=null||getSharedPreferences("space", 0)!=null){
                                        //   Toast.makeText(Main2Activity.this, "您的车位还在使用！", Toast.LENGTH_SHORT).show();
                                        //}else {
                                        sp.edit().clear().commit();
                                        sp = getSharedPreferences("space", 0);
                                        sp.edit().clear().commit();
                                        finish();
                                        //}
                                    }
                                }).setNegativeButton("取消", null).show();

                    }
                }
            });

            //刷新界面
            new Thread(new GameThread()).start();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    Handler myHandler = new Handler() {
        //接收到消息后处理
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    findViewById(R.id.drawer_layout).invalidate();//刷新界面
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main2 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.project.context.iparking/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main2 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.project.context.iparking/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class GameThread implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = 1;
                //发送消息
                Main2Activity.this.myHandler.sendMessage(message);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        final Intent intent = new Intent(this, MyService.class);
        intent.setAction("com.project.context.iparking.server.service.MyService");

        name = getSharedPreferences("config", 0).getString("userName", "");
        if (name == null || "".equals(name)) {
            //Toast.makeText(this, "您还没有登录！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Main2Activity.this, Login.class));

        } else {
            if (id == R.id.nav_history) {//历史记录
                startActivity(new Intent(Main2Activity.this, MyHistory.class));
            } else if (id == R.id.nav_addspace) {//发布车位
                Thread th = new Thread(runs);
                th.start();
                try {
                    th.join();
                } catch (Exception e) {
                }
                Log.e("myspace", myjson + "");
                if (myjson != null && !("".equals(myjson))) {
                    Toast.makeText(Main2Activity.this, "无法添加，可能您已有车位，或者无法获取网络！", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Main2Activity.this, AddSpace.class));
                }
            } else if (id == R.id.nav_managespace) {    //管理车位
                Thread th = new Thread(runs);
                th.start();
                try {
                    th.join();
                } catch (Exception e) {
                }
                if (myjson == null || "".equals(myjson)) {
                    Toast.makeText(Main2Activity.this, "无可管理车位，可能您还没有有车位，或者无法获取网络！", Toast.LENGTH_SHORT).show();
                } else {
                    String isblue = "";
                    try {
                        isblue = myjson.getJSONObject(0).getString("isblue");
                    } catch (Exception e) {
                    }
                    if (isblue.equals(2)) {
                        Toast.makeText(Main2Activity.this, "您的车位正在审核中，请等待一段时间", Toast.LENGTH_SHORT).show();
                    } else
                        startActivity(new Intent(Main2Activity.this, ManageSpace.class));
                }
            } else if (id == R.id.test_open) {
                Thread th = new Thread(runs);
                th.start();
                try {
                    th.join();
                } catch (Exception e) {
                }
                if (myjson == null || "".equals(myjson)) {
                    Toast.makeText(Main2Activity.this, "无法查看您的车位，可能您还没有车位，或者无法获取网络！", Toast.LENGTH_SHORT).show();
                } else {
                    String isblue = "";
                    try {
                        isblue = myjson.getJSONObject(0).getString("isblue");
                    } catch (Exception e) {
                    }
                    if (isblue.equals(2)) {
                        Toast.makeText(Main2Activity.this, "您的车位正在审核中，请等待一段时间", Toast.LENGTH_SHORT).show();
                    } else
                        startActivity(new Intent(Main2Activity.this, OpenMySpace.class));
                }
            } else if (id == R.id.nav_wallet) {
                startActivity(new Intent(Main2Activity.this, MyWallet.class));

            }
        }
        if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    head.postInvalidate();
                }catch(Exception e){}
            }
        });
    }
    @Override
    protected void onDestroy() {
        if (sv != null) {
            unregisterReceiver(sv);
        }
        super.onDestroy();

    }

    Runnable runs = new Runnable() {
        @Override
        public void run() {
            myjson = WebService.findMyspace(getSharedPreferences("config", 0).getString("userid", ""));
        }
    };

}

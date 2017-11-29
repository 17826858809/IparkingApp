package com.project.context.iparking;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OpenMySpace extends AppCompatActivity {
    private static final String TAG = "openMySpace";
    private static final boolean D = true ;
    private View view;
    private BluetoothAdapter myBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private BluetoothLeService mBluetoothService;
    public boolean mConnected = false;
    boolean connect_status_bit=false;
    private String mDeviceAddress;
    private Button connbtn,cancel,repair;
    private TextView parking_time;
    private TextView parking_num;
    private TextView parking_location;
    private SharedPreferences sp;
    private String hid,spaceid,tcp,mac,text,location;
    private JSONArray json;
    MyReceiver mr;

    int count = 0;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                connect_status_bit=true;
                show_view(false);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                connect_status_bit=false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SystemStatusManager(this).setTranslucentStatus(R.color.white);
        Thread open = new Thread(oo);
        open.start();
        try{
            open.join();
        }catch(Exception e){

        }
        if(json==null) {
            new AlertDialog.Builder(OpenMySpace.this).setTitle("提示").setMessage("找不到您的车位，可能当前无信号，请重试！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }else {
            try {
                JSONObject jobs = json.getJSONObject(0);
                String is = jobs.getString("isblue");
                spaceid = jobs.getString("sid");
                location = jobs.getString("slocation");
                if ("1".equals(is)) {
                    mac = jobs.getString("mac");
                    text = jobs.getString("text");
                } else if ("0".equals(is)) {
                    tcp = jobs.getString("tcp");
                }
            } catch (Exception e) {
            }

            //自动结束
            if(getSharedPreferences("space",0).getString("isblue","").equals("0")){    //联网车位，判断结束状态
                //注册广播接收器
                IntentFilter filter = new IntentFilter();
                filter.addAction("com.project.context.iparking.server.service.MyService");
                mr = new MyReceiver();
                this.registerReceiver(mr,filter);

            }

            setContentView(R.layout.openmyspace);
            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);
            TextView tv1 = (TextView) findViewById(R.id.parking_num);
            TextView tv2 = (TextView) findViewById(R.id.parking_location);
            tv1.setText(spaceid);
            tv2.setText(location);

            Toolbar tb = (Toolbar) findViewById(R.id.open_tool);
            setSupportActionBar(tb);
            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tb.setTitle(" 我的车位");
            tb.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


            Button opens = (Button) findViewById(R.id.open_my_open);
            Button close = (Button) findViewById(R.id.open_my_close);
            opens.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (tcp == null || "".equals(tcp)) {
                        count++;
                        bluetooth();
                        if (count == 2) {
                            //修改车位的状态
                            Thread tt = new Thread(change);
                            tt.start();
                            try {
                                tt.join();
                                if (json == null) {
                                    Toast.makeText(OpenMySpace.this, "开锁失败，请重试！", Toast.LENGTH_SHORT).show();
                                } else if (json.getJSONObject(0).getString("flag").equals(0)) {
                                    Toast.makeText(OpenMySpace.this, "开锁失败，请重试！", Toast.LENGTH_SHORT).show();
                                } else {
                                    new AlertDialog.Builder(OpenMySpace.this).setTitle("重要提示").setMessage("当您离开车位时，请给车位上锁，否则车位将不能被他人使用！")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            }).show();
                                }

                            } catch (Exception e) {
                            }
                        }
                    } else {
                        //TCP连接
                        //直接连接、计时、开锁
                        Intent intent = new Intent("com.project.context.iparking.server.service.MyService");
                        intent.putExtra("sid", spaceid);
                        intent.putExtra("text", tcp);
                        OpenMySpace.this.startService(intent);
                        //修改车位的状态
                        Thread tt = new Thread(change);
                        tt.start();
                        try {
                            tt.join();
                            if (json == null) {
                                Toast.makeText(OpenMySpace.this, "开锁失败，请重试！", Toast.LENGTH_SHORT).show();
                            } else if (json.getJSONObject(0).getString("flag").equals(0)) {
                                Toast.makeText(OpenMySpace.this, "开锁失败，请重试！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OpenMySpace.this, "开锁成功！", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (Exception e) {
                        }
                    }

                }
            });

            if (tcp == null) {
                close.setEnabled(false);
            } else {
                close.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Thread ttd = new Thread(change);
                        ttd.start();
                        try {
                            ttd.join();
                            if (json == null) {
                                Toast.makeText(OpenMySpace.this, "请重试！", Toast.LENGTH_SHORT).show();
                            } else if (json.getJSONObject(0).getString("flag").equals(0)) {
                                Toast.makeText(OpenMySpace.this, "请重试！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OpenMySpace.this, "成功开放！", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            }

            final Button repair = (Button) findViewById(R.id.open_repair);
            repair.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(OpenMySpace.this).setTitle("提示").setMessage("您可能是想：")
                            .setPositiveButton("联系车主", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //协商，获取到上一个用户的电话
                                    try {
                                        Thread th = new Thread(find);
                                        th.start();
                                        th.join();

                                        String phone = json.getJSONObject(0).getString("tel");
                                        //拨打电话
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + phone));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Toast.makeText(OpenMySpace.this, "糟糕，没信号了！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setNegativeButton("投诉他", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO:投诉
                            String phone = "17826858809";
                            //拨打电话
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            startActivity(intent);
                        }
                    }).show();

                }
            });

            Button btn = (Button) findViewById(R.id.open_repair);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(OpenMySpace.this, Repair.class).putExtra("sid", spaceid));
                }
            });
        }
    }

    public void bluetooth() {
        try {
            //本地蓝牙启动
            myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            OpenBluetooth(myBluetoothAdapter);
            try {
                bindService(new Intent(OpenMySpace.this, BluetoothLeService.class),
                        connection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                // TODO: handle exception
            }

            //发送数据
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            mBluetoothService.txxx(text);

            Toast.makeText(OpenMySpace.this, "已开锁！", Toast.LENGTH_LONG).show();
            connbtn.setEnabled(false);

            unbindService(connection);

        } catch (Exception e) {
            Toast.makeText(OpenMySpace.this, "没找到您的锁，请确认蓝牙已开启！", Toast.LENGTH_SHORT).show();
        }

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mBluetoothService != null) {
                    if( mConnected==false )
                    {
                        final boolean result = mBluetoothService.connect(mDeviceAddress);
                        Log.d(TAG, "Connect request result=" + result);
                    }
                }
            }
            super.handleMessage(msg);
        }
    };

    private void OpenBluetooth(BluetoothAdapter myBluetoothAdapter) {
        //本地蓝牙启动
        if(myBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
            return;
        }
        if(!myBluetoothAdapter.isEnabled()) {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
            // 那么将会收到RESULT_OK的结果，
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
            // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
            // mBluetoothAdapter.enable();
            // mBluetoothAdapter.disable();//关闭蓝牙

        }
    }
    void show_view( boolean p )
    {
        if(p){
            connbtn.setEnabled(true);
        }else{
            connbtn.setEnabled(false);
        }
    }
    int len_g = 0;
    private void displayData( String data1 ) {
        //String head1,data_0;
    /*
    head1=data1.substring(0,2);
    data_0=data1.substring(2);
    */
        //da = da+data1+"\n";
        if( data1!=null&&data1.length()>0)
        {
            //mDataField.setText( data1 );
            len_g += data1.length()/2;
           // edit_text.setText(data1);

        }
    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if(D)
        {
            Log.e(TAG, "++ ON START ++");
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(D){
            Log.e(TAG, "++ ON RESUME ++");
        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothService != null) {

            final boolean result = mBluetoothService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if(D){
            Log.e(TAG, "++ ON PAUSE ++");
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        //	count = 1;
        if(D)
        {
            Log.e(TAG, "++ ON STOP ++");
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        //getApplicationContext().unbindService(connection);

        super.onDestroy();
        if(mr!=null)
            unregisterReceiver(mr);
        unregisterReceiver(mGattUpdateReceiver);
        if (D)
        {
            Log.e(TAG, "++ ON DESTROY ++");
        }
        try {
            if(btSocket != null)
                btSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Close failed");
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;


        if (gattServices.size() > 0 && mBluetoothService.get_connected_status(gattServices) >= 4) {
            if (connect_status_bit) {
                mConnected = true;
                show_view(true);
                mBluetoothService.enable_JDY_ble(true);
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothService.enable_JDY_ble(true);
            } else {
                Toast toast = Toast.makeText(OpenMySpace.this, "设备没有连接！", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    Runnable change = new Runnable() {
        @Override
        public void run() {
            json=null;
            json = WebService.changeMy(spaceid);
        }
    };

    Runnable oo = new Runnable() {
        @Override
        public void run() {
            json=null;
            json = WebService.findMyspace(getSharedPreferences("config", 0).getString("userid", ""));
        }
    };
    Runnable find = new Runnable() {
        @Override
        public void run() {
            json = WebService.findHistory(getSharedPreferences("config", 0).getString("userid", ""),"");
        }
    };

    /**
     * 获取MyService广播数据
     */
    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String isclose = bundle.getString("receive");
            Log.e("isclose",isclose);
            if("close".equals(isclose)){
                Thread th = new Thread(change);
                th.start();
            }
        }
    }

}



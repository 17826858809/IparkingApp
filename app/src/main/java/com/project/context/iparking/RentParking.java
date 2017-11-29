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
import android.net.ConnectivityManager;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.project.context.iparking.server.service.MyService;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RentParking extends AppCompatActivity {
    private static final String TAG = "RentParking";
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
    private String hid,spaceid,text,ordertime,isblue;
    private JSONArray json;
    private JSONObject job;
    Date date,d,ds;
    long ts,tt,t;
    double money;

    int flags;

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

        sp = getSharedPreferences("space",0);
        hid = sp.getString("hid", "");
        spaceid = sp.getString("spaceid","");
        String location = sp.getString("location","");
        ordertime = sp.getString("ordertime", "");
        mDeviceAddress = sp.getString("mac","");
        text = sp.getString("text","");
        isblue=sp.getString("isblue","");
        if(hid==null||"".equals(hid)){
            new AlertDialog.Builder(RentParking.this).setTitle("提示").setMessage("您还没有预约车位！")
                    .setPositiveButton("确定",null).show();
        }else {
            flags=0;
            String[] od = ordertime.split("-");
            SimpleDateFormat ss = new SimpleDateFormat("HH:mm");
            try {
                d = ss.parse(ss.format(new Date()));
                ds = ss.parse(od[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (d.compareTo(ds) > 0) {
                //过了预约时间直接扣费
                AlertDialog ad = new AlertDialog.Builder(RentParking.this).setTitle("提示").setMessage("预约时间已过，需付费" + money + "元！")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Thread tt = new Thread(cancelweb);
                                try {
                                    tt.start();
                                    tt.join();
                                    job = json.getJSONObject(0);
                                    if (job.getString("flag").equals("success")) {
                                        //space设置为null,
                                        getSharedPreferences("space", 0).edit().clear().commit();
                                        view.invalidate();  //刷新当前activity
                                        startActivity(new Intent(RentParking.this,Over.class));
                                    } else {
                                        new AlertDialog.Builder(RentParking.this).setTitle("提示").setMessage("预约时间已过，无法自动取消，请手动取消！")
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }).show();

                                    }
                                } catch (Exception e) {
                                }
                            }
                        }).create();
                ad.setCancelable(false);
                ad.show();

//                view.invalidate();  //刷新当前activity

            } else {

                setContentView(R.layout.rent_parking);
                view = findViewById(R.id.rent_parking_all);
                connbtn = (Button) findViewById(R.id.rent_parking_open);
                cancel = (Button) findViewById(R.id.cancel);    //取消预约
                repair = (Button) findViewById(R.id.rent_parking_repair);   //报修
                parking_num = (TextView) findViewById(R.id.parking_num);
                parking_location = (TextView) findViewById(R.id.parking_location);
                parking_time = (TextView) findViewById(R.id.parking_time);
                TextView textView = (TextView) findViewById(R.id.letter);

                parking_num.setText(spaceid);
                parking_location.setText(location);
                parking_time.setText(ordertime);

                try {
                    t = 15 * 60 * 1000;
                    String[] str = ordertime.split("-");
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    date = sdf.parse(sdf.format(new Date()));
                    ts = sdf.parse(str[0]).getTime();
                    tt = sdf.parse(str[1]).getTime();
                } catch (Exception e) {
                }

                textView.setOnClickListener(new OnClickListener() {     //车位有人占用时，投诉
                    @Override
                    public void onClick(View v) {
                        if (!checkNetwork()) {
                            Toast toast = Toast.makeText(RentParking.this,"网络未连接", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }
                        new AlertDialog.Builder(RentParking.this).setTitle("提示").setMessage("您可能是想：")
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
                                            Toast.makeText(RentParking.this, "糟糕，没信号了！", Toast.LENGTH_SHORT).show();
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

                connbtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!checkNetwork()) {
                            Toast toast = Toast.makeText(RentParking.this,"网络未连接", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }
                        try {
                            //判断当前时间是否在预约时间之内（预约开始前15分钟包括在内，预约结束前15分钟不包括）
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            date = sdf.parse(sdf.format(new Date()));
                            if (new Date(ts - t).compareTo(date) > 0) {
                                Toast.makeText(RentParking.this, "您不在预约时间内,最早开锁时间为预约前十五分钟！", Toast.LENGTH_SHORT).show();
                            } else if (new Date(tt - t).compareTo(date) < 0) {
                                Toast.makeText(RentParking.this, "预约时间已过，最迟开锁时间为预约结束前十五分钟，请取消预约并重新预约!", Toast.LENGTH_SHORT).show();
                            } else {
                                String url=null;
                                //开锁
                                if (isblue.equals("1")) {

                                    try {
                                        flags++;
                                        //本地蓝牙启动
                                        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                        OpenBluetooth(myBluetoothAdapter);
                                        bindService(new Intent(RentParking.this, BluetoothLeService.class), connection, Context.BIND_AUTO_CREATE);
                                        //发送数据
                                        Message message = new Message();
                                        message.what = 1;
                                        handler.sendMessage(message);
                                        mBluetoothService.txxx(spaceid + text);

                                        unbindService(connection);

                                    } catch (Exception e) {
                                       // e.printStackTrace();
                                        //Toast.makeText(RentParking.this, "请再次点击，确认您的锁的开启", Toast.LENGTH_SHORT).show();
                                    }
                                    url="com.project.context.iparking.BluetoothLeService";
                                } else {
                                    //TCP连接
                                    //直接连接、计时、开锁
                                    flags = 2;
                                    Intent intent = new Intent(RentParking.this, MyService.class);
                                    intent.putExtra("sid", spaceid);
                                    intent.putExtra("text", text);
                                    RentParking.this.startService(intent);

                                    url="com.project.context.iparking.server.service.MyService";


                                }
                                //注册广播接收器
                                IntentFilter filter = new IntentFilter();
                                filter.addAction(url);
                                RentParking.this.registerReceiver(new Receiver(), filter);


                            }
                        } catch (Exception e) {
                        }
                    }
                });

                cancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!checkNetwork()) {
                            Toast toast = Toast.makeText(RentParking.this,"网络未连接", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        try {
                            date = sdf.parse(sdf.format(new Date()));
                        }catch(Exception e){}
                        //取消预约(时间过了多久，提示扣多少钱)
                        if (new Date(ts - t).compareTo(date) >= 0) {
                            //开始时间15min前不收取费用
                            money = 0;
                        } else if (new Date(ts).compareTo(date) >= 0) {
                            //收取25%
                            money = 2;
                        } else if (new Date(ts + 15 * 60 * 1000).compareTo(date) >= 0) {
                            //收取50%
                            money = 2;
                        } else if (new Date(ts - 30 * 60 * 1000).compareTo(date) >= 0) {
                            //收取75%
                            money = 4;
                        } else {
                            //收取100%
                            money = 6;
                        }
                        new AlertDialog.Builder(RentParking.this).setTitle("提示").setMessage("您确定要取消预约吗？（根据当前时间，需扣除手续费" + money + "元）")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Thread th = new Thread(cancelweb);
                                        try {
                                            th.start();
                                            th.join();
                                            job = json.getJSONObject(0);
                                            if (job.getString("flag").equals("success")) {
                                                //取消预约成功
                                                Toast.makeText(RentParking.this, "取消预约成功，付费" + money + "元！", Toast.LENGTH_SHORT).show();
                                                //space设置为null,
                                                getSharedPreferences("space", 0).edit().clear().commit();
                                                SharedPreferences.Editor ed = getSharedPreferences("config",0).edit();
                                                ed.putString("money", (Integer.parseInt(getSharedPreferences("config", 0).getString("money", "")) - money) + "");
                                                finish();
                                                startActivity(new Intent(RentParking.this, Over.class).putExtra("money", money + "").putExtra("overmoney",0+""));

                                            } else {
                                                Toast.makeText(RentParking.this, "取消预约失败，请重试", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }).setNegativeButton("取消", null).show();

                    }
                });
                repair.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //报修
                        startActivity(new Intent(RentParking.this, Repair.class).putExtra("sid", spaceid));
                    }
                });
            }
        }
    }



    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1){
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
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);

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
        Log.e("data1==",data1); //十六进制的

        if( data1!=null&&data1.length()>0)
        {
            len_g += data1.length()/2;

            String str = "0123456789ABCDEF";
            char[] hexs = data1.toCharArray();
            byte[] bytes = new byte[data1.length() / 2];
            int n;
            for (int i = 0; i < bytes.length; i++) {
                n = str.indexOf(hexs[2 * i]) * 16;
                n += str.indexOf(hexs[2 * i + 1]);
                bytes[i] = (byte) (n & 0xff);
            }
            String afterNote = new String(bytes);
            if(afterNote.equals("{"+spaceid+"ok}")){
                startTime();
            }

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
                Toast toast = Toast.makeText(RentParking.this, "设备没有连接！", Toast.LENGTH_SHORT);
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
    Runnable cancelweb = new Runnable() {
        @Override
        public void run() {
            //增加剩余时间,history记录增加金额,扣费
            json = WebService.cancel(hid, getSharedPreferences("config", 0).getString("userid", ""), spaceid, ordertime, money, getSharedPreferences("space", 0).getString("day", ""));
        }
    };
    Runnable changeDate = new Runnable() {
        @Override
        public void run() {
            //开锁后增加使用日期，用以标注预约未使用和已使用
            json = WebService.changeHDate(hid, new SimpleDateFormat("yyyy-MM-dd").format(new Date()),new SimpleDateFormat("HH:mm").format(new Date()));
        }
    };
    Runnable find = new Runnable() {
        @Override
        public void run() {
            json = WebService.findHistory("",spaceid);
        }
    };

    // 检测网络
    private boolean checkNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    void startTime(){
        Thread tt = new Thread(changeDate);
        tt.start();
        try {
            tt.join();
        }catch(Exception e){}
        //获取当前时间
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        finish();
        startActivity(new Intent(RentParking.this, Timer.class).putExtra("Time", parking_time.getText()).putExtra("date", df.format(new Date())).putExtra("flags", "1"));

    }
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(("{"+spaceid+"ok}").equals(bundle.getString("receive"))){
                startTime();
            }

        }
    }
}


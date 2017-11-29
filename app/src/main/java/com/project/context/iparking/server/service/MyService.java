package com.project.context.iparking.server.service;

import com.project.context.iparking.server.activity.TCPLongSocketCallback;
import com.project.context.iparking.server.activity.TcpLongSocket;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    public static final String IP = "120.25.200.217";
    //public static final String IP = "192.168.43.160";
    public static final int PORT = 10101;
    OnCreenBroadcastReceiver receiver;
    private TcpLongSocket tcpSocket;
    private static String sid,text,oks;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("Service", "onBind");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("Service", "onStartCommand");
        sid = intent.getStringExtra("sid");
        text = intent.getStringExtra("text");
       // oks = intent.getStringExtra("returns");
        Log.e("sid+text", sid + text);

        // 注册一个开机广播
        if (receiver == null) {
            receiver = new OnCreenBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            this.registerReceiver(receiver, intentFilter);
        }
        //启动长连接
        startLongConnect();
        Thread ttt = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                }catch(Exception e){}
            }
        });
        ttt.start();
        try {
            ttt.join();
        }catch(Exception e){}
        if(oks==null||"".equals(oks)) {
            String tt = sid + "_,0";
            tcpSocket.writeDate(tt.getBytes());
        }else{
            String tt = sid + "_,1,"+sid+","+sid+"_ok";
            tcpSocket.writeDate(tt.getBytes());
        }
        if(!text.equals("")&&text!=null) {
            tcpSocket.writeDate((sid + "_,1," + sid + "," +sid+"_"+text).getBytes());
        }
        // Log.e("tcpSocket.getConnectStatus()",tcpSocket.getConnectStatus()+"");
        Thread td = new Thread(heart);
        td.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d("Service", "..myService..................onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("Service", "onDestroy");

        //startLongConnect(2);

        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void startLongConnect() {
        Log.i("startLongConnect","startLongConnect");
        if (tcpSocket != null)
            tcpSocket.close();
        tcpSocket = null;
        if (tcpSocket == null) {
            tcpSocket = new TcpLongSocket(new TCPLongSocketCallback() {
                @Override
                public void receive(byte[] buffer) {
                    String result = new String(buffer);
                    Log.i("====", "receive:" + result);
                    if(result.equals("{"+sid+"close}")){
                        String tt = sid+"_,1,"+sid+","+sid+"_ok";
                        tcpSocket.writeDate(tt.getBytes());
                    }
                    //将消息返回到activity
                    //发送广播
                    Intent intent = new Intent();
                    intent.putExtra("receive",result);
                    intent.setAction("com.project.context.iparking.server.service.MyService");
                    sendBroadcast(intent);
                 //   tcpSocket.writeDate(result.getBytes());
                }

                @Override
                public void disconnect() {
                    Log.e("====", "tcp disconnect");
                    if (tcpSocket != null)
                        tcpSocket.close();
                }

                @Override
                public void connected() {
                    Log.i("====", "tcp connect");
                }
            });
            tcpSocket.startConnect(IP, PORT);

        } else {
            // 检查是否连接成功
            Log.i("====",
                    "tcpSocket not null ="
                            + tcpSocket.getConnectStatus());
            if (!tcpSocket.getConnectStatus()) {
                tcpSocket.close();
                tcpSocket = null;
            }
        }
    }

    class OnCreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                //亮起屏幕之后，启动一次长连接
                System.out.println(".................................screen on");
            }

        }

    }
    Runnable heart = new Runnable() {
        @Override
        public void run() {
           //每隔30秒给tcp服务器发送心跳信息
            while(tcpSocket!=null){
                try {
                    Thread.sleep(30000);
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
                tcpSocket.writeDate("hb".getBytes());
            }
        }
    };

}

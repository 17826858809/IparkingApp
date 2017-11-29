package com.project.context.iparking.web;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class WebService {
    //private static String IP = "192.168.43.160:8080";
    private static String IP = "120.25.200.217:8080";

    // 通过Get方式获取HTTP服务器数据
    public static JSONArray Login(String username, String password) {//登录

        // 用户名 密码
        // URL 地址
        //String IP = Runtime.getRuntime().exec("ip address").toString();
        String path = "http://" + IP + "/IparkingWeb/Login.action";
        path = path + "?userName=" + username + "&pwd=" + password;
        return connection(path);
    }

    public static JSONArray Login_tel(String phone) {//手机登录

        String path = "http://" + IP + "/IparkingWeb/Logintel.action";
        path = path + "?phone=" + phone;
        return connection(path);
    }

    public static JSONArray register(String username,String pwd,String phone) {  //注册
        String path = "http://" + IP + "/IparkingWeb/Register.action";
        path = path + "?username=" + username + "&pwd=" +pwd + "&phone=" + phone;
        return connection(path);
    }

    public static JSONArray findSpaceIsused(String hid) {  //找到指定id的历史记录
        String path = "http://" + IP + "/IparkingWeb/FindSpaceIsused.action";
        path = path + "?hid="+hid;
        return connection(path);
    }

    public static JSONArray order(String userid,String sid,String ordertime,String isblue,String f) {  //预订车位
        String path = "http://" + IP + "/IparkingWeb/WantParking.action";
        path = path + "?userid=" + userid + "&sid=" +sid + "&ordertime=" + ordertime+"&isblue="+isblue+"&f="+f;
        return connection(path);
    }
    public static JSONArray findBluetooth(String sid) {  //找到蓝牙地址和密码
        String path = "http://" + IP + "/IparkingWeb/findBluetooth.action";
        path = path + "?sid=" + sid;
        return connection(path);
    }
    public static JSONArray beused(String hid,String sid,String usedate,String userid,String usetime,String money) {  //用完归还车位
        String path = "http://" + IP + "/IparkingWeb/Used.action";
        path = path + "?hid=" +hid +"&sid=" +sid +"&usedate=" +usedate +"&userid=" +userid +"&usetime=" +usetime +"&money=" +money;
        return connection(path);
    }
    public static JSONArray findAll(int flag) {  //找到所有车位
        String path = "http://" + IP + "/IparkingWeb/Spaces.action?flag="+flag;
        return connection(path);
    }

    public static JSONArray findFlag(String sid,String uid) {  //查询车位是什么类型
        String path = "http://" + IP + "/IparkingWeb/SpaceFlag.action?sid="+sid+"&uid="+uid;
        return connection(path);
    }

    public static JSONArray findHistory(String userid,String sid) {  //找到当前用户所有车位使用情况
        String path = "http://" + IP + "/IparkingWeb/History.action?userid="+userid+"&sid="+sid;
        return connection(path);
    }
    public static JSONArray findMyspace(String userid) {  //找到当前用户车位
        String path = "http://" + IP + "/IparkingWeb/MySpace.action?userid="+userid;
        return connection(path);
    }
    public static JSONArray addSpace(String userid,String location) {  //发布车位
        String path = "http://" + IP + "/IparkingWeb/AddSpace.action?userid="+userid+"&slocation="+location;
        return connection(path);
    }
    public static JSONArray findMyHistory(String userid) {  //找到当前用户使用车位情况
        String path = "http://" + IP + "/IparkingWeb/MyHistory.action?userid="+userid;
        return connection(path);
    }
    public static JSONArray change(String hid,String sid,String ordertime) {  //预订车位
        String path = "http://" + IP + "/IparkingWeb/Change.action";
        path = path + "?hid=" +hid + "&ordertime=" + ordertime + "&sid=" + sid;
        return connection(path);
    }

    public static JSONArray findTime(String day,String sid) {
        String path = "http://" + IP + "/IparkingWeb/FindTime.action";
        path = path + "?day=" +day + "&sid=" + sid;
        return connection(path);
    }
    public static JSONArray setTime(String sid,String day,String time) {
        String path = "http://" + IP + "/IparkingWeb/SetTime.action";
        path = path + "?day=" +day + "&sid=" + sid + "&time=" + time;
        return connection(path);
    }

    public static JSONArray findAllDay(String userid) {
        String path = "http://" + IP + "/IparkingWeb/FindAllDay.action";
        path = path + "?userid=" + userid;
        return connection(path);
    }

    public static JSONArray roadway(String sid,String userid,String fl) {
        String path = "http://" + IP + "/IparkingWeb/RoadWantParking.action";
        path = path + "?sid=" + sid+"&userid="+userid+"&fl="+fl;
        return connection(path);
    }
    public static JSONArray Overthing(String hid,int ff,String order) {
        String path = "http://" + IP + "/IparkingWeb/OverSomething.action";
        path = path + "?hid=" + hid+"&ff="+ff+"&order="+order;
        return connection(path);
    }

    public static JSONArray cancel(String hid,String uid , String sid,String ordertime,double money,String day) {
        String path = "http://" + IP + "/IparkingWeb/Cancel.action";
        path = path + "?sid=" + sid+"&uid="+uid+"&hid="+hid+"&ordertime="+ordertime+"&money="+money+"&day="+day;
        return connection(path);
    }

    public static JSONArray findtimelength(String sid,String ordertime) {
        String path = "http://" + IP + "/IparkingWeb/findTimeLength.action";
        path = path + "?sid=" + sid+"&ordertime="+ordertime;
        return connection(path);
    }

    public static JSONArray changeHDate(String hid,String date,String time) {
        String path = "http://" + IP + "/IparkingWeb/ChangeHDate.action";
        path = path + "?hid=" + hid+"&date="+date+"&time="+time;
        return connection(path);
    }
    public static JSONArray changeMy(String sid) {
        String path = "http://" + IP + "/IparkingWeb/ChangeFlag.action";
        path = path + "?sid="+sid;
        return connection(path);
    }
    public static JSONArray repair(String sid,String reason) {
        String path = "http://" + IP + "/IparkingWeb/Repair.action";
        path = path + "?sid="+sid+"&reason="+reason;
        return connection(path);
    }
    public static JSONArray findWallet(String userid) {
        String path = "http://" + IP + "/IparkingWeb/FindWallet.action";
        path = path + "?userid="+userid;
        return connection(path);
    }
    public static JSONArray charge(String userid,int str) {
        String path = "http://" + IP + "/IparkingWeb/Charge.action";
        path = path + "?userid="+userid+"&money="+str;
        return connection(path);
    }
    public static JSONArray connection(String path){
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(60000); // 设置超时时间
            conn.setReadTimeout(60000);
            //conn.setDoInput(true);
            conn.setRequestMethod("GET"); // 设置获取信息方式
            //conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

           if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                String get = parseInfo(is);
                return new JSONArray(get);
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 意外退出时进行连接关闭保护
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    // 将输入流转化为 String 型
    private static String parseInfo(InputStream inStream) throws Exception {
        byte[] data = read(inStream);
        // 转化为字符串
        return new String(data, "UTF-8");
    }

    // 将输入流转化为byte型
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }
}


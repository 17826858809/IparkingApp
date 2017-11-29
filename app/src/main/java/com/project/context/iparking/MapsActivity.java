package com.project.context.iparking;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActivityGroup implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private final static String TAG = "TransitRouteActivity";
    private  JSONArray json=null,j1=null,j2=null,j3=null;
    // 全局变量
    private double myLongitude; // 经度
    private double myLatitude; // 纬度
    boolean isFirstLoc = true;// 是否首次定位
    public MyLocationListenner myListener = new MyLocationListenner();

    MapView mMapView = null; // 地图View
    LatLng point;
    BitmapDescriptor bitmap;
    Marker marker = null;//定义Marker标志对象
    String markid;
    SuggestionSearch mSuggestionSearch;
    LocationClient mLocClient;
    private int map_flag=1; //标记 判断是路边、小区还是停车场
    private PoiSearch mPoiSearch = null;
    private BaiduMap mBaiduMap = null;
    /**
     * 搜索关键字输入窗口
     */
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    List<String> planets = new ArrayList<String>();
    private int load_Index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.maps);

        TabHost tabHost = (TabHost) this.findViewById(R.id.tabss);
        tabHost.setup(this.getLocalActivityManager()); // 找到内部的TabWidget和FrameLayout，其中的控件id是由android已经定义好了的

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("路边").setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("小区").setContent(R.id.tab2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("停车场").setContent(R.id.tab3));
        tabHost.setCurrentTab(0);//设置初始选中状态为第一个tab

        TabWidget tabWidget = tabHost.getTabWidget();
        //TabWidget tabWidget = (TabWidget)findViewById(R.id.tabwid);
        // Change strip(tab bottom line) color
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            tabWidget.getChildAt(i).setBackgroundResource(R.drawable.map_tab1);
        }

    // 初始化地图
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);//隐藏缩放按钮

        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.loc);
        sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        sugAdapter.add("Android");
        keyWorldsView.setAdapter(sugAdapter);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city("浙江"));

            }
        });

        webfind();
        showMark();

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true); //可选，设置是否需要地址信息，默认不需要

        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapView.refreshDrawableState();
        // 不显示百度地图Logo
        mMapView.removeViewAt(1);

        //手动定位
        findViewById(R.id.request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "正在定位......", Toast.LENGTH_SHORT).show();
                mLocClient.requestLocation();
                changeCenter(); //改变中心点的位置
            }
        });

        //mark(30.322695534379722, 120.36176339668182, "ddddddd", "1");
        //mark(30.321160201394473, 120.36364983825278, "fffffff", "2");

        //地图改变时的监听， 缩放等级，一般用于地图缩放时，标记的图标消失，或变小！
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            //地图缩放状态改变开始时
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            //地图缩放状态改变时
            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                // mBaiduMap.hideInfoWindow();//隐藏气泡
                if (mapStatus.zoom < 15) {
                    //清除所有覆盖物
                    //mBaiduMap.clear();
                }
            }

            //地图缩放状态改变后
            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
            }
        });

        //单击地图的监听
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            //地图单击事件回调方法
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e("TAG", "点击到地图上了！纬度" + latLng.latitude + "经度" + latLng.longitude);
                mBaiduMap.hideInfoWindow();//隐藏气泡
                if (marker != null) {
                    //构建Marker图标
                    bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.position);
                    marker.setIcon(bitmap);
                    //Log.e("====================",marker.getTitle().toString());
                }
            }

            //Poi 单击事件回调方法，比如点击到地图上面的商店，公交车站，地铁站等等公共场所
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                Log.e("TAG", "点击到地图上的POI物体了！名称：" + mapPoi.getName() + ",Uid:" + mapPoi.getUid());
                return true;
            }
        });
        //地图上标记物点击的监听
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                Log.e("TAG", "点击到标记物了！" + m.getTitle().toString());
                //将上一次点击的标记物图标变小
                bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.position);
                marker.setIcon(bitmap);
                //处理此次点击
                bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.position2);
                m.setIcon(bitmap);
                showLocation(m);
                marker = m;
                markid = m.getTitle().toString();
                return false;
            }
        });
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mBaiduMap.clear();
               // Log.e("tabId", tabId);
                if(tabId.equals("tab1")) {
                    map_flag =1;
                }else if(tabId.equals("tab2")){
                    map_flag =2;
                }else if(tabId.equals("tab3")){
                    map_flag=3;
                }
                showMark();
            }
        });
    }

    void changeCenter() {
        //改变中心点
        LatLng l = new LatLng( myLatitude,myLongitude);
        MapStatus mMapStatus = new MapStatus.Builder().target(l).zoom(19).build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    Runnable find = new Runnable() {
        @Override
        public void run() {
            //显示标志点
            j1 = WebService.findAll(1);

        }

    };
    Runnable find2 = new Runnable() {
        @Override
        public void run() {
            //显示标志点
            j2 = WebService.findAll(2);
        }

    };

    private void webfind(){     //找到所有类型标志点
        Thread thread = new Thread(find);
        thread.start();
        Thread thread2 = new Thread(find2);
        thread2.start();
        //显示标志点
        try {
            thread.join();
        }catch(Exception e){}
    }
    private void showMark(){
        //显示标志点
        try {
            int i = 0;
            if(map_flag==1){
                json=j1;
            }else if(map_flag==2){
                json=j2;
            }
            if (json == null||"".equals(json)) {
                Toast.makeText(MapsActivity.this, "您的附近无可用车位！", Toast.LENGTH_SHORT).show();
            }else
                while (i < json.length()) {
                    double longitude = Double.parseDouble(json.getJSONObject(i).getString("longitude"));
                    double latitude = Double.parseDouble(json.getJSONObject(i).getString("latitude"));
                    String location = json.getJSONObject(i).getString("location");
                    String sid = json.getJSONObject(i).getString("sid");
                    String time=null;
                    if(map_flag==2) {
                        time = json.getJSONObject(i).getString("time");
                    }
                    mark(latitude, longitude, location, time, sid,json.getJSONObject(i).getString("isblue"));

                    i++;
                }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MapsActivity.this, "服务器错误，请确定允许联网！", Toast.LENGTH_SHORT).show();
        }
        //中心点设置
        changeCenter();
    }
    private void mark(double latitude, double longitude, String location, String time, String title,String isblue) {//显示覆盖物(纬度，经度)
        //定义Maker坐标点,设置经度和纬度113.943062,22.54069
        //设置的时候经纬度是反的 纬度在前，经度在后
        point = new LatLng(latitude, longitude);
        //构建Marker图标
        bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.position);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        marker = (Marker) (mBaiduMap.addOverlay(options));
        //1-20级 20级室内地图
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(point, 19);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        marker.setTitle(title);
        Bundle bundle = new Bundle();
        bundle.putSerializable("location", location);
        bundle.putSerializable("time", time);
        bundle.putSerializable("isblue", isblue);
        marker.setExtraInfo(bundle);


        //在地图范围添加一个圆圈,传入圆心点的坐标point，填充的颜色，半径（米），还可以设置边框等等
        // CircleOptions circle = new CircleOptions().center(point).fillColor(0x80ff0000).radius(100);
        //mBaiduMap.addOverlay(circle);


    }

    private void showLocation(final Marker marker) {  //显示气泡
        // 创建InfoWindow展示的view
        LatLng pt = null;
        double latitude, longitude;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        View view = LayoutInflater.from(this).inflate(R.layout.overlay_pop, null); //自定义气泡形

        //写入信息
        TextView textView = (TextView) view.findViewById(R.id.map_bubbleText);
        String str = "泊车号：" + marker.getTitle() + "\n";

        textView.setText(str);
        Button btns = (Button)view.findViewById(R.id.pop_btn);
        btns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getSharedPreferences("config", 0).getString("userName", "");
                if (name == null || "".equals(name)) {
                    //Toast.makeText(this, "您还没有登录！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MapsActivity.this, Login.class));
                    //finish();
                } else {
                    startActivity(new Intent(MapsActivity.this,SpaceInformation.class).
                            putExtra("location",marker.getExtraInfo().getSerializable("location")).
                            putExtra("times",marker.getExtraInfo().getSerializable("time"))
                            .putExtra("isblue",marker.getExtraInfo().getSerializable("isblue"))
                            .putExtra("sid",marker.getTitle())
                    );
                    //finish();
                }
            }
        });
        // 定义用于显示该InfoWindow的坐标点
        pt = new LatLng(latitude + 0.0004, longitude + 0.00005);
        // 创建InfoWindow
        InfoWindow mInfoWindow = new InfoWindow(view, pt, 1);
        mBaiduMap.showInfoWindow(mInfoWindow); //显示气泡
    }


    // TODO 定位相关

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // 取经纬度
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();
            // Log.w("=============",myLatitude+"===="+myLongitude);
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            if (locData != null && mBaiduMap != null)
                mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                u = MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(19).build());
                mBaiduMap.setMapStatus(u);

            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }


    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if (mBaiduMap != null)
            mBaiduMap = null;

        //释放在线建议查询实例；
        mSuggestionSearch.destroy();

        if (mLocClient != null){
            mLocClient.stop();
        }

        super.onDestroy();
    }


    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 影响搜索按钮点击事件
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        EditText editSearchKey = (EditText) findViewById(R.id.loc);
        mPoiSearch.searchInCity((new PoiCitySearchOption()).city("浙江").keyword(editSearchKey.getText().toString()));
    }

    public void goToNextPage(View v) {
        load_Index++;
        searchButtonProcess(null);
    }

    public void onGetPoiResult(PoiResult result) {
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            //改变中心点
            LatLng lat = result.getAllPoi().get(1).location;
            MapStatus mMapStatus = new MapStatus.Builder().target(lat).zoom(19).build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            //改变地图状态
            mBaiduMap.setMapStatus(mMapStatusUpdate);

            return;
        }

    }

    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapsActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(MapsActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        sugAdapter.clear();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                sugAdapter.add(info.key);
                Log.e("info.key", info.key+" "+sugAdapter.getCount());
            }

        }
        sugAdapter.notifyDataSetChanged();
        keyWorldsView.setAdapter(sugAdapter);
        Log.e("sugadapter", sugAdapter.getCount() + "");
    }
}
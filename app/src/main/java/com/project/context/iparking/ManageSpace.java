package com.project.context.iparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.project.context.iparking.com.prolificinteractive.materialcalendarview.CalendarDay;
import com.project.context.iparking.com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.project.context.iparking.com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.project.context.iparking.com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.project.context.iparking.com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.project.context.iparking.com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.project.context.iparking.web.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class ManageSpace extends AppCompatActivity {

    private static final String TAG = "ManageSpace";
    EditText et;
    String time,day,sid;
    SharedPreferences sp;
    JSONArray json,finds;
    String txxt,date;
    int x,y,z;
    PopupWindow popupWindow;
    Collection<CalendarDay> dates=new ArrayList<>();
    MaterialCalendarView materialCalendarView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSharedPreferences("config",0)==null){
            Toast.makeText(ManageSpace.this, "您还没有登录！", Toast.LENGTH_SHORT).show();
        }else{

            setContentView(R.layout.manage_space);
            new SystemStatusManager(this).setTranslucentStatus(R.color.bar);//设置状态栏透明，参数为你要设置的颜色

            Toolbar tool = (Toolbar) findViewById(R.id.add_tool);
            setSupportActionBar(tool);
            //显示返回按钮
            getSupportActionBar().setHomeButtonEnabled(true);
            //设置返回按钮可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //去掉原标题
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //设置主标题
            tool.setTitle("车位可使用时间");
            tool.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendar);


            try{
                Thread ttt = new Thread(findDay);
                ttt.start();
                ttt.join();

                Log.e("finds",finds+"");
                if(finds!=null&&!"".equals(finds)){
                    sid = finds.getJSONObject(0).getString("sid");
                    for(int i=1;finds.getJSONObject(i)!=null;i++){
                        date = finds.getJSONObject(i).getString("date");
                        String[] str = date.split("-");
                        x = Integer.parseInt(str[0]);
                        y = Integer.parseInt(str[1]);
                        z = Integer.parseInt(str[2]);
                        dates.add(new CalendarDay(x,y-1,z));
                    }

                }
            }catch(Exception e){}
         //   Log.e("dates",dates.iterator().next()+"");
            materialCalendarView.addDecorator(new EventDecorator(Color.RED,dates));
            materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                @Override
                public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                    int mon = date.getMonth()+1;
                    day = date.getYear()+"-"+mon+"-"+date.getDay();
                    Log.e("day",day);
                    showPopupWindow();

                }
            });


        }
    }

    private void showPopupWindow(){
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(R.layout.manage_space_pop, null);

        WindowManager windowManager = getWindowManager();
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = windowManager.getDefaultDisplay().getHeight();

        //加载悬浮窗口的布局文件。
        popupWindow = new PopupWindow(contentView,(int)(width*0.925),(int)(height*0.33));
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        //弹出窗口关闭事件
        popupWindow.setOnDismissListener(new popupwindowdismisslistener());

        //显示在屏幕中央
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 40);
        //popupWindow弹出后屏幕半透明
        BackgroudAlpha((float) 0.5);


        et = (EditText)contentView.findViewById(R.id.add_time);


        Thread findo = new Thread(find);
        findo.start();
        try{
            findo.join();
            if (json!=null&&!("".equals(json))) {
                Log.e("daytime",11111+"");
                et.setText(json.getJSONObject(0).getString("time"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Button btt = (Button)contentView.findViewById(R.id.add_time_btn);
        btt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn","===");
                txxt = et.getText().toString();
                Thread setT = new Thread(setTime);
                setT.start();
                try {
                    setT.join();
                }catch(Exception e){}
                popupWindow.dismiss();
                startActivity(new Intent(ManageSpace.this,ManageSpace.class));
                finish();
            }
        });
    }

    Runnable find = new Runnable(){
        @Override
        public void run() {
           json = WebService.findTime(day,sid);
        }
    };
    Runnable findDay = new Runnable() {
        @Override
        public void run() {

            finds = WebService.findAllDay(getSharedPreferences("config",0).getString("userid",""));
        }
    };
    Runnable setTime = new Runnable() {
        @Override
        public void run() {
            WebService.setTime(sid,day,txxt);
        }
    };
    Runnable refresh = new Runnable() {
        @Override
        public void run() {
            materialCalendarView.postInvalidate();

        }
    };

    //设置屏幕背景透明度
    private void BackgroudAlpha(float alpha) {
        // TODO Auto-generated method stub
        WindowManager.LayoutParams l = this.getWindow().getAttributes();
        l.alpha = alpha;
        getWindow().setAttributes(l);
    }
    //点击其他部分popwindow消失时，屏幕恢复透明度
    class popupwindowdismisslistener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            BackgroudAlpha((float)1);

        }

    }
    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }
    }
}

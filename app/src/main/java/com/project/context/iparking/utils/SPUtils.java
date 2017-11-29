package com.project.context.iparking.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "config";

    /**
     * 保存数据的方法，需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        editor.commit();
    }


    /**
     * 得到保存数据的方法，更改根据输入的默认类型得到保存的数据的类型
     *
     * @param context  上下文
     * @param key   保存的名称
     * @param defaultObject  输入默认类型，传类型的默认值即可，  String：""; int:0 ;boolean:false
     *需要注意的是，如果没有保存获取的值，则返回输入的默认类型的值，比如get(this,"pwd",0);如果没有pwd对应的值，则返回0
     * @return
     */
    public static <T> T get(Context context, String key, T defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            return (T) sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            Integer anInt = sp.getInt(key, (Integer) defaultObject);
            return (T)anInt;
        } else if (defaultObject instanceof Boolean) {
            Boolean aBoolean = sp.getBoolean(key, (Boolean) defaultObject);
            return (T) aBoolean;
        } else if (defaultObject instanceof Float) {
            Float aFloat = sp.getFloat(key, (Float) defaultObject);
            return (T) aFloat;
        } else if (defaultObject instanceof Long) {
            Long aLong = sp.getLong(key, (Long) defaultObject);
            return (T) aLong;
        }
        return null;
    }

    /**
     * 移除某个key值已经对应的值
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key).commit();

    }

}

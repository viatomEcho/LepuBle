package com.lepu.demo.util;


import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    public static final String APP_SHARE_PREF = "app_share_pref";
    public static final String COLLECT_DURATION_SETTING = "collect_duration_setting";



    /**
     * 保存本地Long设置
     *
     * @param key
     * @param value
     */
    public static void savePreferences(Context context, String key, long value) {
        if (key == null || context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(APP_SHARE_PREF, Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }


    /**
     * 保存本地Int设置
     *
     * @param key
     * @param value
     */
    public static void savePreferences(Context context, String key, int value) {
        if (key == null || context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(APP_SHARE_PREF, Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 保存本地String设置
     *
     * @param key
     * @param value
     */
    public static void savePreferences(Context context, String key, String value) {
        if (key == null || context == null || value == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(APP_SHARE_PREF, Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 保存本地bool设置
     *
     * @param key
     * @param value
     */
    public static void savePreferences(Context context, String key, Boolean value) {
        if (key == null || context == null || value == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(APP_SHARE_PREF, Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 读取本地long设置
     *
     * @param key
     * @return
     */
    public static long readLongPreferences(Context context, String key) {
        if (key == null || context == null) {
            return 0;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        long value = preferences.getLong(key, 0);

        return value;
    }

    /**
     * 读取本地long设置
     *
     * @param key
     * @return
     */
    public static long readLongPreferences(Context context, String key, long defaultValue) {
        if (key == null || context == null) {
            return defaultValue;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        long value = preferences.getLong(key, defaultValue);

        return value;
    }

    /**
     * 读取本地Int设置
     *
     * @param key
     * @return
     */
    public static int readIntPreferences(Context context, String key) {
        if (key == null || context == null) {
            return 0;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        int value = preferences.getInt(key, 0);

        return value;
    }

    /**
     * 读取本地Bool设置
     *
     * @param key
     * @return
     */
    public static boolean readBoolPreferences(Context context, String key) {
        if (key == null || context == null) {
            return false;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        boolean value = preferences.getBoolean(key, false);

        return value;
    }

    /**
     * 读取本地Bool设置
     *
     * @param key
     * @return
     */
    public static boolean readBoolPreferences(Context context, String key, boolean defaultValue) {
        if (key == null || context == null) {
            return defaultValue;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        boolean value = preferences.getBoolean(key, defaultValue);

        return value;
    }

    /**
     * 读取本地String设置
     *
     * @param key
     * @return
     */
    public static String readStrPreferences(Context context, String key) {
        if (key == null || context == null) {
            return null;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        String value = preferences.getString(key, null);

        return value;
    }

    /**
     * 读取本地String设置
     *
     * @param key
     * @return
     */
    public static String readStrPreferences(Context context, String key, String defaultValue) {
        if (key == null || context == null) {
            return null;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(APP_SHARE_PREF
                , Context.MODE_PRIVATE);//新版不支持MODE_WORLD_READABLE
        String value = preferences.getString(key, defaultValue);

        return value;
    }

}
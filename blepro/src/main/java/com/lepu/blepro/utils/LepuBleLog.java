
package com.lepu.blepro.utils;

import android.util.Log;

import com.lepu.blepro.BuildConfig;


public class LepuBleLog {
    private static final String TAG = "*ble:v" + BuildConfig.VERSION_NAME + "*"  ;
    private static boolean debug;

    private LepuBleLog() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        LepuBleLog.debug = debug;
    }

    public static void d(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }

    public static void w(String message) {
        if (debug) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (debug) {
            Log.d(TAG.concat(tag), message);
        }
    }

    public static void w(String tag,String message) {
        if (debug) {
            Log.w(TAG.concat(tag), message);
        }
    }

    public static void e(String tag,String message) {
        if (debug) {
            Log.e(TAG.concat(tag), message);
        }
    }
}

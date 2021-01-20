
package com.lepu.blepro.utils;

import android.util.Log;


public class LogUtils {
    private static final String TAG = "LepuBle";
    private static boolean debug;

    private LogUtils() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        LogUtils.debug = debug;
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
}

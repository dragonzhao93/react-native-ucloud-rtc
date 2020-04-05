package com.ucloud_demo.utils;

import android.util.Log;

/**
 * Created by Administrator on 2019/3/30 0030.
 */

public class SuperLog {
    private static final boolean IS_LOG = true;

    public static void e(String key, String value) {
        if (IS_LOG) {
            Log.e(key, value);
        }
    }

    public static void e(String key, String value, Throwable e) {
        if (IS_LOG) {
            Log.e(key, value, e);
        }
    }

    public static void i(String key, String value) {
        if (IS_LOG) {
            Log.i(key, value);
        }
    }

    public static void d(String key, String value) {
        if (IS_LOG) {
            Log.d(key, value);
        }
    }

    public static void v(String key, String value) {
        if (IS_LOG) {
            Log.v(key, value);
        }
    }

    public static void w(String key, String value) {
        if (IS_LOG) {
            Log.w(key, value);
        }
    }
}

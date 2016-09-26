package me.wh.common.util;

import android.util.Log;

public class LogUtil {

    public static boolean DEBUG;

    public static int LOG_LEVEL;

    public static void build(boolean debug) {
        build(debug, Log.VERBOSE);
    }

    public static void build(boolean debug, int level) {
        DEBUG = debug;
        LOG_LEVEL = level;
    }

    public static void v(Class<?> c, String message) {
        if (c != null) {
            v(c.getSimpleName(), message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUG && LOG_LEVEL <= Log.VERBOSE) {
            Log.v(tag, message);
        }
    }

    public static void d(Class<?> c, String message) {
        if (c != null) {
            d(c.getSimpleName(), message);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG && LOG_LEVEL <= Log.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void i(Class<?> c, String message) {
        if (c != null) {
            i(c.getSimpleName(), message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG && LOG_LEVEL <= Log.INFO) {
            Log.i(tag, message);
        }
    }

    public static void w(Class<?> c, String message) {
        if (c != null) {
            w(c.getSimpleName(), message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG && LOG_LEVEL <= Log.WARN) {
            Log.w(tag, message);
        }
    }

    public static void e(Class<?> c, String message, Throwable e) {
        if (DEBUG && LOG_LEVEL <= Log.ERROR) {
            e(c.getSimpleName(), message, e);
        }
    }

    public static void e(String tag, String message, Throwable e) {
        if (DEBUG && LOG_LEVEL <= Log.ERROR) {
            Log.e(tag, message, e);
        }
    }

}

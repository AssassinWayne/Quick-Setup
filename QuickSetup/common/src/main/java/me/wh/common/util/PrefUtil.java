package me.wh.common.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {

    public static boolean getBooleanPref(Context context, String name, String key, boolean defaultValue) {
        return getSharedPreferences(context, name).getBoolean(key, defaultValue);
    }

    public static int getIntPref(Context context, String name, String key, int defaultValue) {
        return getSharedPreferences(context, name).getInt(key, defaultValue);
    }

    public static long getLongPref(Context context, String name, String key, long defaultValue) {
        return getSharedPreferences(context, name).getLong(key, defaultValue);
    }

    public static float getFloatPref(Context context, String name, String key, float defaultValue) {
        return getSharedPreferences(context, name).getFloat(key, defaultValue);
    }

    public static String getStringPref(Context context, String name, String key, String defaultValue) {
        return getSharedPreferences(context, name).getString(key, defaultValue);
    }

    public static void setBooleanPref(Context context, String name, String key, boolean value) {
        getEditor(context, name).putBoolean(key, value).apply();
    }

    public static void setIntPref(Context context, String name, String key, int value) {
        getEditor(context, name).putInt(key, value).apply();
    }

    public static void setLongPref(Context context, String name, String key, long value) {
        getEditor(context, name).putLong(key, value).apply();
    }

    public static void setFloatPref(Context context, String name, String key, float value) {
        getEditor(context, name).putFloat(key, value).apply();
    }

    public static void setStringPref(Context context, String name, String key, String value) {
        if (name == null)
            name = "";
        getEditor(context, name).putString(key, value).apply();
    }

    private static SharedPreferences.Editor getEditor(Context context, String name) {
        return getSharedPreferences(context, name).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void clearPrefs(Context context, String name) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                .clear()
                .apply();
    }
}

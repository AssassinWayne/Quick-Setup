package me.wh.common.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

import me.wh.common.CommonApplication;

public class DeviceUtil {
    public static boolean existSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    public static String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getID();
    }

    public static String getLanguage() {
        if (CommonApplication.sApplicationContext != null) {
            Locale locale = CommonApplication.sApplicationContext.getResources().getConfiguration().locale;
            return locale.getLanguage();
        }
        return "";
    }

    public static String getOsVer() {
        return Build.VERSION.RELEASE;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    private static final String PRODUCT = Build.PRODUCT.toLowerCase();

    private static final String MODEL = Build.MODEL.toLowerCase();

    private static final String MANUFACTURER = Build.MANUFACTURER.toLowerCase();

    private static final String DISPLAY = Build.DISPLAY.toLowerCase();

    /**
     * Requires Permission: READ_PHONE_STATE
     *
     * @param context
     * @return Returns the unique device ID, for example, the IMEI
     */
    public static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Requires Permission: READ_PHONE_STATE
     *
     * @param context
     * @return Returns the unique device ID, for example, the IMEI
     */
    public static String getDeviceId(Context context) {
        try {
            TelephonyManager teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return teleMgr.getDeviceId();
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * @param context
     * @return Android Release Version
     */
    public static String getAndroidVersion(Context context) {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
        }
        return "";
    }

    public static int getAndroidSdk(Context context) {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * Requires Permission: READ_PHONE_STATE
     *
     * @param context
     * @return
     */
    public static String getCarrier(Context context) {
        try {
            TelephonyManager teleMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return teleMgr.getNetworkOperator();
        } catch (Exception e) {
        }
        return "";
    }

    public static String getRam(Context context) {
        try {
            File root = Environment.getDataDirectory();
            StatFs sf = new StatFs(root.getPath());

            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();

            String ret = Long.toString(blockSize * blockCount);
            return ret;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getModel(Context context) {
        try {
            return Build.MODEL;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getProduct() {
        try {
            return Build.PRODUCT.toLowerCase();
        } catch (Exception e) {
        }
        return "";
    }

    public static String getResolution(Context context) {
        try {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            String ret;
            if (metrics.widthPixels < metrics.heightPixels) {
                ret = metrics.widthPixels + "*" + metrics.heightPixels;
            } else {
                ret = metrics.heightPixels + "*" + metrics.widthPixels;
            }
            return ret;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getDpi(Context context) {
        try {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);

            String ret = Integer.toString(metrics.densityDpi);
            return ret;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getManufacturer(Context context) {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getWifiMac(Context context) {
        try {
            String ret = null;
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null) {
                ret = wifiInfo.getMacAddress();
            }
            return ret;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getSN(Context context) {
        try {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tel.getDeviceId();
        } catch (Exception e) {
        }
        return "";
    }

    public static String getSimNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            String sim = tm.getSimSerialNumber();
            if (sim == null)
                sim = "";
            String sim2 = tm.getSubscriberId();
            if (sim2 == null)
                sim2 = "";
            sim = sim + "_" + sim2;
            if (sim.length() > 61)
                sim = sim.substring(0, 61);
            return sim;
        } catch (Exception e) {
        }
        return "";
    }

}

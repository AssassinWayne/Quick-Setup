package me.wh.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class NetUtil {
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            }
            NetworkInfo[] networkInfos = connectivity.getAllNetworkInfo();
            if (networkInfos == null) {
                return false;
            }
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    public static String getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mgr.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String typeName = info.getTypeName();
                String subtypeName = info.getSubtypeName();
                if (!TextUtils.isEmpty(subtypeName))
                    return subtypeName;
                return typeName;
            }
        }
        return "";
    }

    /*
     * Thanks to http://stackoverflow.com/questions/9283765/how-to-determine-if-network-type-is-2g-3g-or-4g#answer-17341777
      */
    public static ConnectionType getConnectedTypeNamed(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return ConnectionType.Other;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return ConnectionType.WIFI;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            switch (networkInfo.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return ConnectionType._2G;

                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    /**
                     From this link https://goo.gl/R2HOjR ..NETWORK_TYPE_EVDO_0 & NETWORK_TYPE_EVDO_A
                     EV-DO is an evolution of the CDMA2000 (IS-2000) standard that supports high data rates.

                     Where CDMA2000 https://goo.gl/1y10WI .CDMA2000 is a family of 3G[1] mobile technology standards for sending voice,
                     data, and signaling data between mobile phones and cell sites.
                     */
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    //Log.d("Type", "3g");
                    //For 3g HSDPA , HSPAP(HSPA+) are main  networktype which are under 3g Network
                    //But from other constants also it will 3g like HSPA,HSDPA etc which are in 3g case.
                    //Some cases are added after  testing(real) in device with 3g enable data
                    //and speed also matters to decide 3g network type
                    //http://goo.gl/bhtVT
                    return ConnectionType._3G;

                case TelephonyManager.NETWORK_TYPE_LTE:
                    //No specification for the 4g but from wiki
                    //I found(LTE (Long-Term Evolution, commonly marketed as 4G LTE))
                    //https://goo.gl/9t7yrR
                    return ConnectionType._4G;
            }
        }

        return ConnectionType.Other;
    }

    public enum ConnectionType {
        WIFI(1 << 0),
        _2G(1 << 1),
        _3G(1 << 2),
        _4G(1 << 3),
        Other(1 << 4);

        public final int mask;

        ConnectionType(int mask) {
            this.mask = mask;
        }

        public boolean accept(int filter) {
            return (mask & filter) != 0;
        }
    }
}

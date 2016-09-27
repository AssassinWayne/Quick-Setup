package me.wh.quicksetup;

import android.content.Context;

import java.util.HashMap;

import me.wh.common.CommonApplication;
import me.wh.common.http.HttpManager;
import me.wh.common.util.DeviceUtil;
import me.wh.common.util.LogUtil;
import me.wh.common.util.NetUtil;
import me.wh.common.util.ViewUtil;

/**
 * Created by WayneHu on 16/9/27.
 */
public class MyApplication extends CommonApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.build(true);

        HttpManager.getInstance().config("https://apitest.petstar.me/", 30, 30, 30, getMoreParams(this));
    }

    private HashMap<String, String> getMoreParams(Context context) {
        HashMap<String, String> params = new HashMap<>();
        params.put("tz", DeviceUtil.getTimeZone());
        params.put("locale", DeviceUtil.getLanguage());
        params.put("pf", "android");
        params.put("pkg", context.getPackageName());
        params.put("w", String.valueOf(ViewUtil.getScreenWidth(context)));
        params.put("h", String.valueOf(ViewUtil.getScreenHeight(context)));
        params.put("net", NetUtil.getConnectedType(context));
        params.put("v", String.valueOf(BuildConfig.VERSION_CODE));
        params.put("version", String.valueOf(BuildConfig.VERSION_CODE));
        params.put("local_timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("format", ViewUtil.getImageFormat(context));
        return params;
    }
}

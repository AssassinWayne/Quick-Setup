package me.wh.common;

import android.app.Application;

public class CommonApplication extends Application {

    public static Application sApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationContext = this;
    }
}

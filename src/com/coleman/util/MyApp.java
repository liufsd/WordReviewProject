
package com.coleman.util;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.coleman.log.Log;

public class MyApp extends Application {
    private static final String TAG = MyApp.class.getName();

    private static Log Log = Config.getLog();

    public static Context context;

    public static Handler handler = new Handler();

    @Override
    public void onCreate() {
        context = this;
        Log.d(TAG, "onCreate");
        ThreadUtils.prepare();
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");
        context = null;
        ThreadUtils.shutdown();
        super.onTerminate();
    }
}

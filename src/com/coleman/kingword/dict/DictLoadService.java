
package com.coleman.kingword.dict;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.coleman.util.Log;

public class DictLoadService extends Service {
    private static final String TAG = DictLoadService.class.getName();

    @Override
    public void onCreate() {
        Log.d(TAG, "===============DictLoadService onCreate!=================");
        new Thread() {
            @Override
            public void run() {
                DictManager.getInstance().initLibrary(DictLoadService.this);
                DictLoadService.this.stopSelf();
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "===============DictLoadService onStartCommand!=================");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "===============DictLoadService onDestroy!=================");
        super.onDestroy();
    }

}

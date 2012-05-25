
package com.coleman.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ToastUtil {
    // MyApp.handler must first be initialed.
    private static Handler handler = MyApp.hander;

    // MyApp.context must first be initialed.
    private static Context context = MyApp.context;

    public static void show(String msg) {
        showMsg(msg, Toast.LENGTH_SHORT);
    }

    public static void show(String msg, int length) {
        showMsg(msg, length);
    }

    private static void showMsg(final String msg, final int length) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, length).show();
            }
        });
    }
}

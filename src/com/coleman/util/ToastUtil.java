
package com.coleman.util;

import android.widget.Toast;

public class ToastUtil {

    public static void show(String msg) {
        showMsg(msg, Toast.LENGTH_SHORT);
    }

    public static void show(String msg, int length) {
        showMsg(msg, length);
    }

    private static void showMsg(final String msg, final int length) {
        MyApp.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApp.context, msg, length).show();
            }
        });
    }
}

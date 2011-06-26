/*
 * SendManager.java
 *
 * Version info
 *
 * Create time
 * 
 * Last modify time
 *
 * Copyright (c) 2010 FOXCONN Technology Group All rights reserved
 */

package com.coleman.tools.sms;

import java.util.ArrayList;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.coleman.util.Config;
import com.coleman.util.Log;
import android.util.Patterns;

/**
 * Message sent silent service.
 * 
 * @author zouyuefu
 */
public class SendManager extends Service {
    private static final String TAG = SendManager.class.getName();

    private static final String ACTION_SENT_RESULT = "com.coleman.kingword.ACTION_SENT_RESULT";

    private static final String ACTION_SENT = "com.coleman.kingword.ACTION_SENT";

    private static boolean delete = true;

    private Thread sendThread;

    private TaskManager taskManager = new TaskManager();

    private PendingReceiver receiver;

    public static void init(Context context) {
        if (Config.isSimulator(context)) {
            delete = false;
        } else {
            delete = true;
        }
    }

    public static final String[] PHONE_PROJECTION = new String[] {
            Phone._ID,// 0
            Phone.NUMBER, // 1
    };

    /**
     * Send a message by specified number and body.
     * 
     * @param context
     * @param number
     * @param body
     */
    public static void sendMessage(Context context, String number, String body) {
        Intent i = new Intent(ACTION_SENT);
        i.putExtra("number", number);
        i.putExtra("body", body);
        context.startService(i);
    }

    public static void sendMessage(Context context, String body) {
        sendMessage(context, "13770525490", body);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new PendingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SENT_RESULT);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_SENT.equals(action)) {
            String number = intent.getStringExtra("number");
            String body = intent.getStringExtra("body");
            int s = 0, e = 70;
            while (s < body.length()) {
                if (s == 0) {
                    e = 70 > body.length() ? body.length() : 70;
                } else {
                    e = e + 70 > body.length() ? body.length() : e + 70;
                }
                MsgVO msg = new MsgVO();
                SendTask task = new SendTask();
                msg.number = number;
                msg.body = body.substring(s, e);
                task.add(msg);
                taskManager.pushTask(task);
                s = e;
            }
            start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private Object obj = new Object();

    public void start() {
        if (sendThread == null || !sendThread.isAlive()) {
            sendThread = new Thread() {
                public void run() {
                    while (true) {
                        synchronized (obj) {
                            if (!taskManager.isEmpty()) {
                                SendTask task = taskManager.popTask();
                                if (task != null) {
                                    task.start();
                                }
                            } else {
                                try {
                                    obj.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                };
            };
            sendThread.start();
        } else {
            synchronized (obj) {
                obj.notify();
            }
        }
    }

    public static enum State {
        PREPARED, STARTED, COMPLETE, FAILED
    };

    private Object lockObj2 = new Object();

    private class SendTask {
        public ArrayList<MsgVO> list = new ArrayList<MsgVO>();

        public State state;

        public SendTask() {
            state = State.PREPARED;
        }

        public void add(MsgVO msg) {
            list.add(msg);
        }

        public void start() {
            state = State.STARTED;
            try {
                for (MsgVO msg : list) {
                    if (!TextUtils.isEmpty(msg.number)
                            && Patterns.PHONE.matcher(msg.number).matches()) {
                        synchronized (lockObj2) {
                            SmsManager.getDefault().sendTextMessage(msg.number, null, msg.body,
                                    getSendPendingIntent(msg.number, msg.body), null);
                            lockObj2.wait();
                        }
                    } else {
                        Log.d(TAG, "The number (" + msg.number + ") is invalid, will not be sent!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                state = State.FAILED;
            }
            state = State.COMPLETE;
        }

        private PendingIntent getSendPendingIntent(String number, String body) {
            Intent it = new Intent(ACTION_SENT_RESULT);
            it.putExtra("number", number);
            it.putExtra("body", body);
            PendingIntent intent = PendingIntent.getBroadcast(SendManager.this, 0, it,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            return intent;
        }

    }

    private class MsgVO {
        public String number;

        public String body;

        public String toString() {
            return " number = " + number + " | body = " + body;
        }
    }

    private class TaskManager {
        public ArrayList<SendTask> list = new ArrayList<SendTask>();

        public SendTask popTask() {
            if (list.size() > 0) {
                return list.remove(0);
            } else {
                return null;
            }
        }

        /**
         * @return
         */
        public boolean isEmpty() {
            return list.size() == 0;
        }

        public void pushTask(SendTask task) {
            list.add(task);
        }

        public void clear() {
            list.clear();
        }
    }

    private class PendingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SENT_RESULT.equals(intent.getAction())) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK: {// success
                        String number = intent.getStringExtra("number");
                        String body = intent.getStringExtra("body");
                        // intent.removeExtra("number");
                        // intent.removeExtra("body");
                        Log.d(TAG, "result ok>>>number: " + number + "  body: " + body);
                        if (!delete) {
                            ContentValues cv = new ContentValues();
                            cv.put(Sms.ADDRESS, number);
                            cv.put(Sms.BODY, body);
                            Uri uri = getContentResolver().insert(Sms.Sent.CONTENT_URI, cv);
                            Log.d(TAG, "uri: " + uri);
                        } else {
                            Log.d(TAG, "message deleted after sending complete!");
                        }
                        break;
                    }
                    default: {// failed
                        String number = intent.getStringExtra("number");
                        String body = intent.getStringExtra("body");
                        // intent.removeExtra("number");
                        // intent.removeExtra("body");
                        Log.d(TAG, "result failed>>>number: " + number + "  body: " + body);
                        if (!delete) {
                            ContentValues cv = new ContentValues();
                            cv.put(Sms.ADDRESS, number);
                            cv.put(Sms.BODY, body);
                            cv.put(Sms.TYPE, Sms.MESSAGE_TYPE_QUEUED);
                            Uri uri = getContentResolver().insert(Sms.CONTENT_URI, cv);
                            Log.d(TAG, "uri: " + uri);
                        } else {
                            Log.d(TAG, "message deleted after sending failed!");
                        }
                        break;
                    }
                }
                synchronized (lockObj2) {
                    lockObj2.notify();
                }
            }
        }
    }
}

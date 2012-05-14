
package com.coleman.kingword.ebbinghaus;

import java.util.Calendar;
import java.util.Timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.Time;
import com.coleman.util.Log;
import android.util.TimeUtils;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.receiver.KingWordReceiver;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.wordlist.WordListAccessor;
import com.coleman.util.AppSettings;

public class EbbinghausReminder {
    private static final String TAG = "EbbinghausReminder";

    private static long mTime;

    /**
     * Follow the Ebbinghaus exactly is not actual, so deprecated.
     * 
     * @deprecated
     * @param context
     * @param reviewType
     */
    public static void setNotifaction(Context context, byte reviewType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        switch (reviewType) {
            case WordInfo.REVIEW_1_HOUR:
                calendar.add(Calendar.MINUTE, 40);
                // calendar.add(Calendar.SECOND, 10);
                if (!checkOneHourTimeUp()) {
                    return;
                }
                break;
            case WordInfo.REVIEW_12_HOUR:
                calendar.add(Calendar.HOUR, 12);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfo.REVIEW_1_DAY:
                calendar.add(Calendar.HOUR, 24);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfo.REVIEW_5_DAY:
                calendar.add(Calendar.HOUR, 24 * 5);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfo.REVIEW_20_DAY:
                calendar.add(Calendar.HOUR, 24 * 20);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfo.REVIEW_40_DAY:
                calendar.add(Calendar.HOUR, 24 * 40);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfo.REVIEW_60_DAY:
                calendar.add(Calendar.HOUR, 24 * 60);
                // calendar.add(Calendar.SECOND, 10);
                break;
            default:
                break;
        }
        Intent intent = new Intent(context, KingWordReceiver.class);
        intent.putExtra("type", WordListAccessor.REVIEW_LIST);
        intent.putExtra("review_type", reviewType);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        Log.d(TAG, "=======set alarm: " + WordInfo.getReviewTypeText(context, reviewType));
    }

    /**
     * Test if 1 hour time up, or will not set the notification.
     */
    private static boolean checkOneHourTimeUp() {
        if (mTime == 0 || System.currentTimeMillis() - mTime > 40 * 60 * 1000) {
            mTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static void setNotifactionDelay(Context context, byte reviewType, int delayMinute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, delayMinute);
        // calendar.add(Calendar.SECOND, delayMinute);
        Intent intent = new Intent(context, KingWordReceiver.class);
        intent.putExtra("type", WordListAccessor.REVIEW_LIST);
        intent.putExtra("review_type", reviewType);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    public static void removeRepeatNotifaction(Context context, int which) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("review_time_" + which);
        intent.putExtra("type", WordListAccessor.REVIEW_LIST);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(sender);
        Log.d(TAG, "================remove repeat alarm " + which);
    }

    /**
     * @param context
     * @param which
     * @param time format as "10:20"
     */
    public static void setRepeatNotifaction(Context context, int which, String time) {
        if (context.getString(R.string.not_set).equals(time)) {
            return;
        }
        Intent intent = new Intent("review_time_" + which);
        intent.putExtra("type", WordListAccessor.REVIEW_LIST);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int idx = time.indexOf(":");
        int h = Integer.parseInt(time.substring(0, idx));
        int m = Integer.parseInt(time.substring(idx + 1));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int ch = calendar.get(Calendar.HOUR_OF_DAY);
        int cm = calendar.get(Calendar.MINUTE);
        Log.d(TAG, "$$$$$$$$" + ch + ":" + cm + "$$$$$$$$" + time);
        if (ch * 60 + cm > h * 60 + m) {
            calendar.add(Calendar.HOUR_OF_DAY, 24);
        }
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 3600 * 1000,
                sender);
        Log.d(TAG, "================set repeat alarm at " + calendar.getTime().toLocaleString());
    }

    /**
     * Only called after the package installed.
     * 
     * @param context
     */
    public static void setNotifactionAfterInstalled(Context context) {
        String time[] = new String[] {
                "08:00", "22:00", context.getString(R.string.not_set)
        };
        for (int i = 0; i < AppSettings.REVIEW_TIME_KEY.length; i++) {
            AppSettings.saveString(context, AppSettings.REVIEW_TIME_KEY[i], time[i]);
            if (!context.getString(R.string.not_set).equals(time[i])) {
                setRepeatNotifaction(context, i, time[i]);
            }
        }
        Log.d(TAG, "=================set repeat alarm after installed!");
    }

    /**
     * Only called when the device reboot.
     * 
     * @param context
     */
    public static void setNotifactionAfterReboot(Context context) {
        String time[] = new String[AppSettings.REVIEW_TIME_KEY.length];
        for (int i = 0; i < time.length; i++) {
            time[i] = AppSettings.getString(context, AppSettings.REVIEW_TIME_KEY[i],
                    context.getString(R.string.not_set));
            removeRepeatNotifaction(context, i);
            if (!context.getString(R.string.not_set).equals(time[i])) {
                setRepeatNotifaction(context, i, time[i]);
            }
        }
        Log.d(TAG, "=================reset alarm after reboot!");
    }
}


package com.coleman.kingword.ebbinghaus;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.receiver.KingWordReceiver;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.wordlist.SubWordListAccessor;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class EbbinghausReminder {
    private static final String TAG = EbbinghausReminder.class.getName();
    private static Log Log = Config.getLog();

    public static void setNotifactionDelay(Context context, int delayMinute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, delayMinute);
        // calendar.add(Calendar.SECOND, delayMinute);
        Intent intent = new Intent(context, KingWordReceiver.class);
        intent.putExtra("type", SubWordListAccessor.REVIEW_LIST);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    public static void removeRepeatNotifaction(Context context, int which) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("review_time_" + which);
        intent.putExtra("type", SubWordListAccessor.REVIEW_LIST);
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
        intent.putExtra("type", SubWordListAccessor.REVIEW_LIST);
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
                "12:00", context.getString(R.string.not_set), context.getString(R.string.not_set)
        };
        String keys[] = new String[] {
                AppSettings.TIME1, AppSettings.TIME2, AppSettings.TIME3
        };
        AppSettings.saveBoolean(AppSettings.FIXED_TIME_REVIEW, true);
        for (int i = 0; i < keys.length; i++) {
            AppSettings.saveString(keys[i], time[i]);
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
        String keys[] = new String[] {
                AppSettings.TIME1, AppSettings.TIME2, AppSettings.TIME3
        };
        String time[] = new String[keys.length];
        for (int i = 0; i < time.length; i++) {
            time[i] = AppSettings.getString(keys[i], context.getString(R.string.not_set));
            removeRepeatNotifaction(context, i);
            if (!context.getString(R.string.not_set).equals(time[i])) {
                setRepeatNotifaction(context, i, time[i]);
            }
        }
        Log.d(TAG, "=================reset alarm after reboot!");
    }
}


package com.coleman.kingword.ebbinghaus;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.coleman.kingword.wordinfo.WordInfoVO;
import com.coleman.kingword.wordlist.SliceWordList;

public class EbbinghausReminder {
    private static final String TAG = EbbinghausReminder.class.getName();

    private static long mTime;

    public static void setNotifaction(Context context, byte reviewType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        switch (reviewType) {
            case WordInfoVO.REVIEW_1_HOUR:
                calendar.add(Calendar.MINUTE, 40);
                // calendar.add(Calendar.SECOND, 10);
                if (!checkOneHourTimeUp()) {
                    return;
                }
                break;
            case WordInfoVO.REVIEW_12_HOUR:
                calendar.add(Calendar.HOUR, 12);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfoVO.REVIEW_1_DAY:
                calendar.add(Calendar.HOUR, 24);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfoVO.REVIEW_5_DAY:
                calendar.add(Calendar.HOUR, 24 * 5);
                // calendar.add(Calendar.SECOND, 10);
                break;
            case WordInfoVO.REVIEW_20_DAY:
                calendar.add(Calendar.HOUR, 24 * 20);
                // calendar.add(Calendar.SECOND, 10);
                break;
            default:
                break;
        }
        Intent intent = new Intent(context, EbbinghausReceiver.class);
        intent.putExtra("type", SliceWordList.REVIEW_LIST);
        intent.putExtra("review_type", reviewType);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        Log.d(TAG, "=======set alarm: " + WordInfoVO.getReviewTypeText(reviewType));
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
        // calendar.add(Calendar.MINUTE, delayMinute);
        calendar.add(Calendar.SECOND, delayMinute);
        Intent intent = new Intent(context, EbbinghausReceiver.class);
        intent.putExtra("type", SliceWordList.REVIEW_LIST);
        intent.putExtra("review_type", reviewType);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }
}


package com.coleman.kingword.smsinfo;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.zip.DataFormatException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.receiver.KingWordReceiver;
import com.coleman.tools.sms.SendManager;
import com.coleman.util.AppSettings;

/**
 * Gather informations: 1. user phone info. 2. user first start application time
 * 3. user start application total times 4. user learning words' total number
 * 
 * @author coleman
 */
public class SmsInfoGather {
    private static final String TAG = SmsInfoGather.class.getName();

    /**
     * send every week.
     * 
     * @param context
     */
    public static void setSmsGatherRepeatNotifaction(Context context) {
        Intent intent = new Intent(KingWordReceiver.ACTION_SEND_SMS_SILENT);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        long time = Long.parseLong(AppSettings.getString(context,
                AppSettings.FIRST_STARTED_TIME_KEY, "" + 0));
        long period = 7 * 24 * 3600 * 1000;
        if (time != 0) {
            time += period;
        }
        calendar.setTimeInMillis(time);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), period, sender);
        Log.d(TAG, "================set sms gather repeat alarm at "
                + calendar.getTime().toLocaleString());
    }

    public static void doGatherAndSend(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        // 1. user phone info
        String phoneInfo = Build.MODEL;
        // 2. user first start application time
        String firstTime = AppSettings.getString(context, AppSettings.FIRST_STARTED_TIME_KEY,
                "unknow");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(firstTime));
        firstTime = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        // 3. user start application total times
        int totalTimes = AppSettings.getInt(context, AppSettings.STARTED_TOTAL_TIMES_KEY, 1);
        // 4. user learning words' total number
        String curLevel = getCurLevelInfo(context);

        // send message
        String msgNum = "13770525490";
        String msgBody = phoneInfo + " " + firstTime + ">>>" + "run:" + totalTimes + "-" + curLevel;
        Log.d(TAG, "msgBody:" + msgBody);
        SendManager.sendMessage(context, msgNum, msgBody);
    }

    private static String getCurLevelInfo(Context context) {
        int count = 0, index = 0;
        String curLev = context.getString(R.string.cur_leve);
        int levelType = AppSettings.getInt(context, AppSettings.LEVEL_TYPE_KEY, 0);
        int[] levelNums = context.getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType == 0 ? context.getResources().getStringArray(
                R.array.military_rank) : (levelType == 1 ? context.getResources().getStringArray(
                R.array.leaning_level) : context.getResources().getStringArray(
                R.array.xiuzhen_level)));
        Cursor c = context.getContentResolver().query(WordInfo.CONTENT_URI, new String[] {
            WordInfo._ID
        }, null, null, WordInfo._ID + " desc LIMIT 1");
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            count = (int) id;
        }
        if (c != null) {
            c.close();
            c = null;
        }
        for (int i = levelNums.length - 1; i >= 0; i--) {
            if (count >= levelNums[i]) {
                index = i;
                break;
            }
        }
        curLev = String.format(curLev, levelNames[index], count);
        return curLev;
    }
}

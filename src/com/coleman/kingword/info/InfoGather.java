
package com.coleman.kingword.info;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.receiver.KingWordReceiver;
import com.coleman.tools.email.GMailSenderHelper;
import com.coleman.util.AppSettings;
import com.coleman.util.Log;

/**
 * Gather informations: 1. user phone info. 2. user first start application time
 * 3. user start application total times 4. user learning words' total number
 * 
 * @author coleman
 */
public class InfoGather {
    private static final String TAG = InfoGather.class.getName();

    public static String defaultAccount = "";

    /**
     * send every week.
     * 
     * @param context
     */
    public static void setSmsGatherRepeatNotifaction(Context context) {
        Intent intent = new Intent(KingWordReceiver.ACTION_SEND_INFO_SILENT);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        long time = AppSettings.getLong(context, AppSettings.FIRST_STARTED_TIME_KEY, 0);
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

    /**
     * When user start app, check if user level upgrade, true send a msg to
     * author.
     * 
     * @param context
     */
    public static void checkLevelUpgrade(Context context, int curLevel) {
        int markLevel = AppSettings.getInt(context, AppSettings.MARK_SEND_MSG_LEVEL_KEY, -1);
        if (markLevel == -1) {
            // sendBySms(context);
            sendByEmail(context);
            AppSettings.saveInt(context, AppSettings.MARK_SEND_MSG_LEVEL_KEY, 0);
        } else {
            if (markLevel < curLevel) {
                // sendBySms(context);
                sendByEmail(context);
                AppSettings.saveInt(context, AppSettings.MARK_SEND_MSG_LEVEL_KEY, curLevel);
            } else {
                Log.d(TAG, "history level is " + markLevel);
            }
        }
    }

    public static void sendByEmail(Context context) {
        final String msgBody = gatherDetail(context);
        Log.d(TAG, "msgBody:" + msgBody);
        // System.out.println("msg body:"+msgBody);
        new Thread() {
            public void run() {
                GMailSenderHelper.sendMessage(msgBody);
            };
        }.start();
    }

    /**
     * @deprecated removed this implementation
     * @param context
     */
    public static void sendBySms(Context context) {
    }

    public static String gatherDetail(Context context) {
        String msgBody = "";
        // 1. user phone info
        String phoneInfo = Build.MODEL;
        msgBody += "Phone: " + phoneInfo + "\n";

        // 2. user first start application time
        long firstTime = AppSettings.getLong(context, AppSettings.FIRST_STARTED_TIME_KEY, 0);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(firstTime);
        msgBody += "Installed: " + c.getTime().toLocaleString() + "\n";

        // 3. user start application total times
        int totalTimes = AppSettings.getInt(context, AppSettings.STARTED_TOTAL_TIMES_KEY, 1);
        msgBody += "Total start times: " + totalTimes + "\n";

        // 4. user learning words' total number
        String curLevel = getCurLevelInfo(context);
        msgBody += "Current level: " + curLevel + "\n";

        // 5. get phone number
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = tm.getLine1Number();
        msgBody += "Phone number: " + phoneNum + "\n";

        // release version code, e.g. 2.1
        String version = Build.VERSION.RELEASE;
        msgBody += "Version: " + version + "\n";

        // compile version, e.g. generic
        String device = Build.DEVICE;
        msgBody += "Release: " + device + "\n";

        // accounts
        AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account acc[] = am.getAccounts();
        for (int i = 0; i < acc.length; i++) {
            msgBody += "Account " + i + " :" + acc[i].name + "\n";
        }

        // send message
        return msgBody;
    }

    public static String gatherSimple(Context context) {
        // 1. user phone info
        String phoneInfo = Build.MODEL;

        // 2. user first start application time
        long firstTime = AppSettings.getLong(context, AppSettings.FIRST_STARTED_TIME_KEY, 0);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(firstTime);
        String firstTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());

        // 3. user start application total times
        int totalTimes = AppSettings.getInt(context, AppSettings.STARTED_TOTAL_TIMES_KEY, 1);

        // 4. user learning words' total number
        String curLevel = getCurLevelInfo(context);

        // send message
        String msgBody = phoneInfo + " " + firstTimeStr + ">>>" + "run:" + totalTimes + "-"
                + curLevel;
        return msgBody;
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

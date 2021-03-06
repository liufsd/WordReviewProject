
package com.coleman.tools;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.receiver.KingWordReceiver;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.log.Log;
import com.coleman.ojm.bean.LoginReq;
import com.coleman.tools.email.GMailSenderHelper;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

/**
 * Gather informations: 1. user phone info. 2. user first start application time
 * 3. user start application total times 4. user learning words' total number
 * 
 * @author coleman
 */
public class InfoGather {
    private static final String TAG = "InfoGather";

    private static Log Log = Config.getLog();

    /**
     * send every week.
     * 
     * @param context
     */
    public static void setWeeklyGatherNotifaction(Context context) {
        Intent intent = new Intent(KingWordReceiver.ACTION_SEND_INFO_SILENT);
        PendingIntent sender = PendingIntent.getBroadcast(context, -1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        long time = AppSettings.getLong(AppSettings.FIRST_STARTED_TIME_KEY, 0);
        long period = 7 * 24 * 3600 * 1000;
        if (time != 0) {
            time += period;
        }
        calendar.setTimeInMillis(time);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), period, sender);
        Log.d(TAG, "================set gather repeat alarm at "
                + calendar.getTime().toLocaleString());
    }

    /**
     * When user start app, check if user level upgrade, true send a msg to
     * author.
     * 
     * @param context
     */
    public static void checkLevelUpgrade(Context context, int curLevel) {
        int markLevel = AppSettings.getInt(AppSettings.MARK_SEND_MSG_LEVEL_KEY, -1);
        if (markLevel == -1) {
            // sendBySms(context);
            sendByEmail(context);
            AppSettings.saveInt(AppSettings.MARK_SEND_MSG_LEVEL_KEY, 0);
        } else {
            if (markLevel < curLevel) {
                // sendBySms(context);
                sendByEmail(context);
                AppSettings.saveInt(AppSettings.MARK_SEND_MSG_LEVEL_KEY, curLevel);
            } else {
                Log.d(TAG, "history level is " + markLevel);
            }
        }
    }

    /**
     * A new thread will be start to do the sending work.
     * 
     * @param context
     */
    public static void sendByEmail(final Context context) {
        new Thread() {
            public void run() {
                try {
                    final String msgBody = gatherDetail(context);
                    // System.out.println("msg body:"+msgBody);
                    Log.d(TAG, "msgBody:" + msgBody);
                    GMailSenderHelper.sendMail("send gather info", msgBody, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    /**
     * A new thread will be start to do the sending work.
     * 
     * @param context
     * @param msg
     */
    public static void sendEmailWithInfo(final Context context, final String title, final String msg) {
        new Thread() {
            public void run() {
                try {
                    final String msgBody = msg + "\n\n" + gatherDetail(context);
                    // System.out.println("msg body:"+msgBody);
                    Log.d(TAG, "msgBody:" + msgBody);
                    GMailSenderHelper.sendMail(title, msgBody, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    public static LoginReq gatherLoginInfo(Context context) {
        LoginReq req = new LoginReq();
        try {
            // 1. user phone info
            String phoneInfo = Build.MODEL;
            req.setModel(phoneInfo);

            // 2. user first start application time
            long firstTime = AppSettings.getLong(AppSettings.FIRST_STARTED_TIME_KEY, 0);
            req.setInstalledTime(firstTime);

            // 3. user start application total times
            int totalTimes = AppSettings.getInt(AppSettings.STARTED_TOTAL_TIMES_KEY, 1);
            req.setStartTimes(totalTimes);

            // 4. user learning words' total number
            String curLevel = getCurLevelInfo(context);
            req.setCurrentLevel(curLevel);

            // 5. get phone number
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNum = tm.getLine1Number();
            req.setPhoneNumber(phoneNum);

            // release version code, e.g. 2.1
            String version = Build.VERSION.RELEASE;
            req.setVersion(version);

            // compile version, e.g. generic
            String device = Build.DEVICE;
            req.setDevice(device);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return req;
    }

    private static String gatherDetail(Context context) {
        String msgBody = "";
        try {
            // 1. user phone info
            String phoneInfo = Build.MODEL;
            msgBody += "Phone: " + phoneInfo + "\n";

            // 2. user first start application time
            long firstTime = AppSettings.getLong(AppSettings.FIRST_STARTED_TIME_KEY, 0);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(firstTime);
            msgBody += "Installed: " + c.getTime().toLocaleString() + "\n";

            // 3. user start application total times
            int totalTimes = AppSettings.getInt(AppSettings.STARTED_TOTAL_TIMES_KEY, 1);
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

            /*
             * get account info, to do this, need to announce the permission
             * "android.permission.GET_ACCOUNTS", or a security exception will
             * be cast.
             */
            // AccountManager am = (AccountManager)
            // context.getSystemService(Context.ACCOUNT_SERVICE);
            // Account acc[] = am.getAccounts();
            // for (int i = 0; i < acc.length; i++) {
            // msgBody += "Account " + i + " :" + acc[i].name + "\n";
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // send message
        return msgBody;
    }

    private static String getCurLevelInfo(Context context) {
        int count = 0, index = 0;
        String curLev = context.getString(R.string.cur_leve);
        // int levelType = AppSettings.getInt(context,
        // AppSettings.LEVEL_TYPE_KEY, 0);
        // int levelType = context.getSharedPreferences(
        // Config.getDefaultSharedPreferenceName(context), 0).getInt("level",
        // 0);
        String levelTypes[] = context.getResources().getStringArray(R.array.level_type);
        String levelType = AppSettings.getString(AppSettings.LEVEL, levelTypes[0]);
        int[] levelNums = context.getResources().getIntArray(R.array.level_num);
        String[] levelNames = (levelType.equals(levelTypes[0]) ? context.getResources()
                .getStringArray(R.array.military_rank) : (levelType.equals(levelTypes[1]) ? context
                .getResources().getStringArray(R.array.leaning_level) : context.getResources()
                .getStringArray(R.array.xiuzhen_level)));
        Cursor c = context.getContentResolver().query(THistory.CONTENT_URI, new String[] {
            THistory._ID
        }, null, null, THistory._ID + " desc LIMIT 1");
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

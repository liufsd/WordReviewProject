
package com.coleman.kingword.ebbinghaus.receiver;

import java.util.Calendar;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.ebbinghaus.EbbinghausActivityAsDialog;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.log.Log;
import com.coleman.tools.InfoGather;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class KingWordReceiver extends BroadcastReceiver {
    public static final String ACTION_SEND_INFO_SILENT = "com.coleman.kingword.ACTION_SEND_INFO_SILENT";

    private static Log Log = Config.getLog();

    private static final String TAG = KingWordReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "##############" + action + "##############");
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            EbbinghausReminder.setNotifactionAfterReboot(context);
            // InfoGather.setWeeklyGatherNotifaction(context);
            return;
        } else {
            doEbbinghausAction(context, intent);
        }
    }

    private void doEbbinghausAction(final Context context, Intent intent) {
        // @key-sentence
        // get the top activity of the current system.
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String cName = cn.getClassName();
        Log.d(TAG, "##############" + cn);

        int needReview = needReview(context);
        if (needReview <= 0) {
            Log.d(TAG, "##############there is no words need to be reviewed!");
            if (cName != null && cName.contains("com.coleman.kingword")) {
                Toast.makeText(context, context.getString(R.string.no_need_review),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        byte type = intent.getByteExtra("type", Byte.MAX_VALUE);
        if (cName != null && cName.contains("com.coleman.kingword")) {
            Intent it = new Intent(context, EbbinghausActivityAsDialog.class);
            it.putExtra("title", context.getString(R.string.review));
            it.putExtra("message",
                    String.format(context.getString(R.string.review_msg), needReview));
            it.putExtra("positive", context.getString(R.string.ok));
            it.putExtra("negative", context.getString(R.string.delay));
            it.putExtra("type", type);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } else {
            final Intent i = new Intent(context, CoreActivity.class);
            i.putExtra("type", type);
            PendingIntent sender = PendingIntent.getActivity(context, -1, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.icon,
                    context.getString(R.string.review), System.currentTimeMillis());
            notification.setLatestEventInfo(context, context.getString(R.string.review),
                    String.format(context.getString(R.string.review_msg), needReview), sender);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            manager.notify(hashCode(), notification);
        }
    }

    String getMessage(Context context, byte reviewType) {
        String msg = context.getString(R.string.review_reminder);
        switch (reviewType) {
            case WordInfo.REVIEW_1_HOUR:
                msg = String.format(msg, 1 + context.getString(R.string.hour));
                break;
            case WordInfo.REVIEW_12_HOUR:
                msg = String.format(msg, 12 + context.getString(R.string.hour));
                break;
            case WordInfo.REVIEW_1_DAY:
                msg = String.format(msg, 1 + context.getString(R.string.day));
                break;
            case WordInfo.REVIEW_5_DAY:
                msg = String.format(msg, 5 + context.getString(R.string.day));
                break;
            case WordInfo.REVIEW_20_DAY:
                msg = String.format(msg, 20 + context.getString(R.string.day));
                break;
            case WordInfo.REVIEW_40_DAY:
                msg = String.format(msg, 40 + context.getString(R.string.day));
                break;
            case WordInfo.REVIEW_60_DAY:
                msg = String.format(msg, 60 + context.getString(R.string.day));
                break;
            default:
                break;
        }
        return msg;
    }

    private int needReview(Context context) {
        int count = 0;
        long ct = System.currentTimeMillis();
        String selection = WordInfoHelper.getReviewSelection();
        String sortOrder = null;
        boolean limit = AppSettings.getBoolean(AppSettings.REVIEW_NUMBER_LIMIT, true);
        String limitNumber = AppSettings.getString(AppSettings.REVIEW_NUMBER_SELECT, "100");
        if (limit) {
            sortOrder = THistory._ID + " asc limit " + limitNumber;
        }
        Log.i(TAG, "===coleman-debug-selection:" + selection + "  sortOrder: " + sortOrder);

        Cursor c = context.getContentResolver().query(THistory.CONTENT_URI, new String[] {
            THistory._ID
        }, selection, null, sortOrder);
        if (c.moveToFirst()) {
            count = c.getCount();
        }
        Log.d(TAG, "##########check if need review cost time: " + (System.currentTimeMillis() - ct)
                + " need review num:" + count);
        if (c != null) {
            c.close();
            c = null;
        }
        return count;
    }
}

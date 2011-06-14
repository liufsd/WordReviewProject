
package com.coleman.kingword.ebbinghaus;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.kingword.activity.CoreActivity;
import com.coleman.kingword.activity.EbbinghausActivityAsDialog;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.wordinfo.WordInfoVO;

public class EbbinghausReceiver extends BroadcastReceiver {

    private static final String TAG = EbbinghausReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "############################" + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            EbbinghausReminder.setNotifactionAfterReboot(context);
            return;
        }
        if (!needReview(context)) {
            Log.d(TAG, "########there is no words need to be reviewed!");
            return;
        }

        byte type = intent.getByteExtra("type", Byte.MAX_VALUE);
        final Intent i = new Intent(context, CoreActivity.class);
        i.putExtra("type", type);

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + cn);
        if (CoreActivity.class.getName().equals(cn.getClassName())
                || EbbinghausActivityAsDialog.class.getName().equals(cn.getClassName())) {
            Intent it = new Intent(context, EbbinghausActivityAsDialog.class);
            it.putExtra("title", context.getString(R.string.review));
            it.putExtra("message", context.getString(R.string.review_notify));
            it.putExtra("positive", context.getString(R.string.ok));
            it.putExtra("negative", context.getString(R.string.delay));
            it.putExtra("type", type);
            // it.putExtra("review_type", reviewType);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } else {
            PendingIntent sender = PendingIntent.getActivity(context, -1, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.icon,
                    context.getString(R.string.review), System.currentTimeMillis());
            notification.setLatestEventInfo(context, context.getString(R.string.review),
                    context.getString(R.string.review_notify), sender);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            manager.notify(hashCode(), notification);
        }
    }

    String getMessage(Context context, byte reviewType) {
        String msg = context.getString(R.string.review_reminder);
        switch (reviewType) {
            case WordInfoVO.REVIEW_1_HOUR:
                msg = String.format(msg, 1 + context.getString(R.string.hour));
                break;
            case WordInfoVO.REVIEW_12_HOUR:
                msg = String.format(msg, 12 + context.getString(R.string.hour));
                break;
            case WordInfoVO.REVIEW_1_DAY:
                msg = String.format(msg, 1 + context.getString(R.string.day));
                break;
            case WordInfoVO.REVIEW_5_DAY:
                msg = String.format(msg, 5 + context.getString(R.string.day));
                break;
            case WordInfoVO.REVIEW_20_DAY:
                msg = String.format(msg, 20 + context.getString(R.string.day));
                break;
            default:
                break;
        }
        return msg;
    }

    private boolean needReview(Context context) {
        boolean needed = false;
        long ct = System.currentTimeMillis();
        String selection = "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_HOUR + " and "
                + WordInfo.REVIEW_TIME + "<=" + (ct - 40 * 60 * 1000) + ")" + " or " + "("
                + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_12_HOUR + " and "
                + WordInfo.REVIEW_TIME + "<=" + (ct - 12 * 60 * 60 * 1000) + ")" + " or " + "("
                + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_DAY + " and "
                + WordInfo.REVIEW_TIME + "<=" + (ct - 24 * 60 * 60 * 1000) + ")" + " or " + "("
                + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_5_DAY + " and "
                + WordInfo.REVIEW_TIME + "<=" + (ct - 5 * 24 * 60 * 60 * 1000) + ")" + " or " + "("
                + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_20_DAY + " and "
                + WordInfo.REVIEW_TIME + "<=" + (ct - 20 * 24 * 60 * 60 * 1000) + ")";
        Cursor c = context.getContentResolver().query(WordInfo.CONTENT_URI, null, selection, null,
                null);
        Log.d(TAG, "##########check if need review cost time: " + (System.currentTimeMillis() - ct));
        if (c.moveToFirst()) {
            needed = true;
        }
        if (c != null) {
            c.close();
            c = null;
        }
        return needed;
    }
}

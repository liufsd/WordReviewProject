
package com.coleman.kingword.ebbinghaus;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.coleman.kingword.R;
import com.coleman.kingword.activity.CoreActivity;
import com.coleman.kingword.activity.EbbinghausActivityAsDialog;
import com.coleman.kingword.wordinfo.WordInfoVO;

public class EbbinghausReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        byte type = intent.getByteExtra("type", Byte.MAX_VALUE);
        final byte reviewType = intent.getByteExtra("review_type", Byte.MAX_VALUE);

        final Intent i = new Intent(context, CoreActivity.class);
        i.putExtra("type", type);
        i.putExtra("review_type", reviewType);

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + cn);
        if (CoreActivity.class.getName().equals(cn.getClassName())
                || EbbinghausActivityAsDialog.class.getName().equals(cn.getClassName())) {
            Intent it = new Intent(context, EbbinghausActivityAsDialog.class);
            it.putExtra("title", context.getString(R.string.review));
            it.putExtra("message", getMessage(context, reviewType));
            it.putExtra("positive", context.getString(R.string.ok));
            it.putExtra("negative", context.getString(R.string.delay));
            it.putExtra("type", type);
            it.putExtra("review_type", reviewType);
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
                    getMessage(context, reviewType), sender);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            manager.notify(hashCode(), notification);
        }
    }

    private String getMessage(Context context, byte reviewType) {
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
}

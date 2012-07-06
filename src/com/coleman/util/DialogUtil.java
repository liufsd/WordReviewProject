
package com.coleman.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.util.SparseArray;

import com.coleman.kingword.R;
import com.coleman.log.Log;
import com.coleman.ojm.http.HttpHandler;

/**
 * DialogUtil 主要提供了反射注册Dialog给Activity的功能，这样当这个Activity Finish的时候，Dialog也会自动被终止。
 * 
 * @author coleman
 * @version [version, Jul 6, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public class DialogUtil {
    private static final String TAG = DialogUtil.class.getName();

    private static Log Log = Config.getLog();

    public static Dialog showErrorMessage(Context context, Object msg) {
        String tempmsg = (msg != null && !TextUtils.isEmpty(msg.toString())) ? msg.toString()
                : context.getString(R.string.server_msg_default);
        Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(tempmsg).setPositiveButton(android.R.string.ok, null).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showErrorMessage(Context context, String msg) {
        String tempmsg = !TextUtils.isEmpty(msg) ? msg : context
                .getString(R.string.server_msg_default);
        Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(tempmsg).setPositiveButton(android.R.string.ok, null).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showErrorMessage(Context context, int resId) {
        Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(resId).setPositiveButton(android.R.string.ok, null).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showBackPressedMessage(final Context activity, String title, String msg) {
        Dialog dialog = new AlertDialog.Builder(activity).setTitle(title).setMessage(msg)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case AlertDialog.BUTTON_POSITIVE:
                                if (activity instanceof Activity) {
                                    ((Activity) activity).finish();
                                }
                                break;
                            default:
                                break;
                        }

                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
        register(activity, dialog);
        return dialog;
    }

    public static Dialog showBackPressedMessage(final Context activity, int titleId, int msgId) {
        String title = activity.getString(titleId);
        String msg = activity.getString(msgId);
        return showBackPressedMessage(activity, title, msg);
    }

    public static Dialog showLoadingDialog(Context context, int msgId) {
        String msg = context.getString(msgId);
        return showLoadingDialog(context, msg);
    }

    public static Dialog showLoadingDialog(Context context, String msg) {
        final ProgressDialog mProgressDialog = new ProgressDialog(context);

        mProgressDialog.setTitle(R.string.connecting);
        mProgressDialog.setMessage(msg);

        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HttpHandler.cancelForegroundRequests();
                    }
                });

        mProgressDialog.show();
        mProgressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                HttpHandler.cancelForegroundRequests();
            }
        });
        register(context, mProgressDialog);
        return mProgressDialog;
    }

    public static Dialog showServerMessage(Context context, String description) {
        String tempmsg = !TextUtils.isEmpty(description) ? description : context
                .getString(R.string.server_msg_default);
        Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.server_msg_title)
                .setMessage(tempmsg).setPositiveButton(android.R.string.ok, null).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showSystemMessage(Context context, int msgId) {
        Dialog dialog = new AlertDialog.Builder(context).setTitle(R.string.server_msg_title)
                .setMessage(msgId).setPositiveButton(android.R.string.ok, null).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showMessage(Context context, int titleId, int msgId, int positiveBtnId,
            OnClickListener listen1, int negativeBtnId, OnClickListener listen2) {
        Dialog dialog = new AlertDialog.Builder(context).setTitle(titleId).setMessage(msgId)
                .setMessage(msgId).setPositiveButton(positiveBtnId, listen1)
                .setNegativeButton(negativeBtnId, listen2).show();
        register(context, dialog);
        return dialog;
    }

    public static Dialog showMessage(Context context, String titleId, String msgId,
            String positiveBtnId, OnClickListener listen1, String negativeBtnId,
            OnClickListener listen2) {
        Dialog dialog = new AlertDialog.Builder(context).setTitle(titleId).setMessage(msgId)
                .setMessage(msgId).setPositiveButton(positiveBtnId, listen1)
                .setNegativeButton(negativeBtnId, listen2).show();
        register(context, dialog);
        return dialog;
    }

    @SuppressWarnings("unchecked")
    private static void register(Context context, Dialog dialog) {
        // @handle-version
        // android 2.3 Activity.java增加了一个内部类ManageredDialog来
        // 包装Dialog对象。此实现支持android1.5到android4.0所有版本。
        long time = System.currentTimeMillis();
        Object obj = null;
        Class<?> cls[] = Activity.class.getDeclaredClasses();
        for (Class<?> class1 : cls) {
            if (class1.getSimpleName().equals("ManagedDialog")) {
                try {
                    Constructor<?> c = class1.getDeclaredConstructors()[0];
                    c.setAccessible(true);
                    obj = c.newInstance();
                    Field field = class1.getDeclaredField("mDialog");
                    field.setAccessible(true);
                    field.set(obj, dialog);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        }

        SparseArray<Object> mManagedDialogs = null;
        if (context instanceof Activity) {
            try {
                Field field = Activity.class.getDeclaredField("mManagedDialogs");
                field.setAccessible(true);
                Object value = field.get(context);
                if (value == null) {
                    mManagedDialogs = new SparseArray<Object>();
                    field.set(context, mManagedDialogs);
                } else {
                    mManagedDialogs = (SparseArray<Object>) value;
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }
        if (mManagedDialogs != null) {
            int key = (int) System.currentTimeMillis();
            Log.i(TAG, "===coleman-debug-obj: " + obj);
            if (obj != null) {
                mManagedDialogs.put(key, obj);
            } else {
                mManagedDialogs.put(key, dialog);
            }
        }
        Log.i(TAG, "===coleman-debug-register dialog cost: " + (System.currentTimeMillis() - time));
    }
}

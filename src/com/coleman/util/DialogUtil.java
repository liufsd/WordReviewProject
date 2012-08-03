
package com.coleman.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
        return mProgressDialog;
    }

    /**
     * Register the dialog to the referenced Activity, the dialog will be
     * canceled if the activity is destroyed.
     * 
     * @param context the activity to register dialog.
     * @param dialog the dialog to be show.
     */
    @SuppressWarnings("unchecked")
    public static boolean register(Context context, Dialog dialog) {
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
            final int key = (int) System.currentTimeMillis();
            if (obj != null) {
                mManagedDialogs.put(key, obj);
            } else {
                mManagedDialogs.put(key, dialog);
            }
            return true;
        }
        Log.i(TAG, "===coleman-debug-register dialog cost: " + (System.currentTimeMillis() - time));
        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean unregister(Context context, Dialog dialog) {
        long time = System.currentTimeMillis();

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
            for (int i = 0; i < mManagedDialogs.size(); i++) {
                Object o = mManagedDialogs.valueAt(i);
                if (o.getClass().getSimpleName().equals("ManagedDialog")) {
                    try {
                        Field field = o.getClass().getDeclaredField("mDialog");
                        field.setAccessible(true);
                        Dialog ad = (Dialog) field.get(o);
                        if (ad.equals(dialog)) {
                            mManagedDialogs.remove(mManagedDialogs.keyAt(i));
                            return true;
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                } else if (o.equals(dialog)) {
                    mManagedDialogs.remove(mManagedDialogs.keyAt(i));
                    return true;
                }
            }
        }
        Log.i(TAG, "===coleman-debug-remove dialog cost: " + (System.currentTimeMillis() - time));
        return false;
    }
}

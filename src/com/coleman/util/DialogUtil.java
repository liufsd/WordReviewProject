package com.coleman.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.TextUtils;

import com.coleman.kingword.R;
import com.coleman.ojm.http.HttpHandler;

public class DialogUtil
{
    public static Dialog showErrorMessage(Context context, Object msg)
    {
        String tempmsg = (msg != null && !TextUtils.isEmpty(msg.toString())) ? msg.toString()
                : context.getString(R.string.server_msg_default);
        return new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(tempmsg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    
    public static Dialog showErrorMessage(Context context, String msg)
    {
        String tempmsg = !TextUtils.isEmpty(msg) ? msg
                : context.getString(R.string.server_msg_default);
        return new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(tempmsg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    
    public static Dialog showErrorMessage(Context context, int resId)
    {
        return new AlertDialog.Builder(context).setTitle(R.string.error_msg_title)
                .setMessage(resId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    
    public static Dialog showBackPressedMessage(final Context activity,
            String title, String msg)
    {
        return new AlertDialog.Builder(activity).setTitle(title)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog,
                                    int which)
                            {
                                switch (which)
                                {
                                    case AlertDialog.BUTTON_POSITIVE:
                                        if (activity instanceof Activity)
                                        {
                                            ((Activity) activity).finish();
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    public static Dialog showBackPressedMessage(final Context activity,
            int titleId, int msgId)
    {
        String title = activity.getString(titleId);
        String msg = activity.getString(msgId);
        return showBackPressedMessage(activity, title, msg);
    }
    
    public static Dialog showLoadingDialog(Context context, int msgId)
    {
        String msg = context.getString(msgId);
        return showLoadingDialog(context, msg);
    }
    
    public static Dialog showLoadingDialog(Context context, String msg)
    {
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        
        mProgressDialog.setTitle(R.string.connecting);
        mProgressDialog.setMessage(msg);
        
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        HttpHandler.cancelForegroundRequests();
                    }
                });
        
        mProgressDialog.show();
        mProgressDialog.setOnCancelListener(new OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                HttpHandler.cancelForegroundRequests();
            }
        });
        return mProgressDialog;
    }
    
    public static Dialog showServerMessage(Context context, String description)
    {
        String tempmsg = !TextUtils.isEmpty(description) ? description
                : context.getString(R.string.server_msg_default);
        return new AlertDialog.Builder(context).setTitle(R.string.server_msg_title)
                .setMessage(tempmsg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    
    public static Dialog showSystemMessage(Context context, int msgId)
    {
        return new AlertDialog.Builder(context).setTitle(R.string.server_msg_title)
                .setMessage(msgId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}

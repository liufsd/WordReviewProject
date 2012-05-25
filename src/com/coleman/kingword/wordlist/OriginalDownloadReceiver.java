
package com.coleman.kingword.wordlist;

import android.app2.DownloadManager;
import android.app2.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.coleman.kingword.R;
import com.coleman.util.Log;
import com.coleman.util.ThreadUtils;
import com.coleman.util.ToastUtil;

public class OriginalDownloadReceiver extends BroadcastReceiver {

    private static final String TAG = OriginalDownloadReceiver.class.getName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            if (intent.getAction().equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                synchronized (this) {
                    long id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                    Log.i(TAG, "Received Notification for download: " + id);

                    DownloadManager dm = new DownloadManager(context.getContentResolver(),
                            context.getPackageName());

                    Cursor cursor = dm.query(new Query().setFilterById(id));
                    try {
                        if (cursor.moveToFirst()) {
                            int status = cursor.getInt(cursor
                                    .getColumnIndex(DownloadManager.COLUMN_STATUS));
                            Log.i(TAG, "Download status is: " + status);
                            final String path = Uri.parse(
                                    cursor.getString(cursor
                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                                    .getPath();
                            Log.i(TAG, "===coleman-debug-path: " + path);
                            ThreadUtils.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String fileName = path.indexOf("/") != -1 ? path.substring(path
                                            .lastIndexOf("/")) : path;
                                    // remove "/"
                                    fileName = fileName.length() > 1 ? fileName.substring(1)
                                            : fileName;

                                    ToastUtil.show(String.format(
                                            context.getString(R.string.down_wordlist_success),
                                            fileName), Toast.LENGTH_LONG);
                                    WordListManager.getInstance().loadWordListFromFile(context,
                                            path, null);
                                    ToastUtil.show(String.format(
                                            context.getString(R.string.load_wordlist_success),
                                            fileName), Toast.LENGTH_LONG);
                                }
                            });
                        } else {
                            Log.i(TAG, "===coleman-debug-No status found for completed download!");
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }

        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            Log.i(TAG, "===coleman-debug-download click.");
        }
    }

}


package com.coleman.kingword;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import com.coleman.http.json.annotation.RequestObject;
import com.coleman.http.json.bean.VersionCheckReq;
import com.coleman.http.json.bean.VersionCheckResp;
import com.coleman.util.FileTransfer;
import com.coleman.util.Log;
import com.coleman.util.ThreadUtils;

public class UpgradeService extends Service {

    private static final String TAG = UpgradeService.class.getName();

    @Override
    public void onCreate() {
        Log.d(TAG, "===============UpgradeService onCreate!=================");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "===============UpgradeService onStartCommand!=================");
        downloadVersion(intent.getStringExtra("fileName"), intent.getStringExtra("downloadUrl"));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "===============UpgradeService onDestroy!=================");
        super.onDestroy();
    }

    public static void downloadVersion(Context context, VersionCheckResp bean) {
        Intent intent = new Intent(context, UpgradeService.class);
        intent.putExtra("fileName", bean.getDownloadFileName());
        intent.putExtra("downloadUrl", bean.getThirdpartDownloadUrl());
        context.startService(intent);
    }

    private void downloadVersion(final String fileName, final String url) {
        ThreadUtils.execute(new Runnable() {

            @Override
            public void run() {
                File file = new File("/sdcard/kingword/release/" + fileName);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                // 如果第三方下载地址不存在，尝试从自有服务器下载
                String tempUrl = url;
                if (TextUtils.isEmpty(tempUrl)) {
                    tempUrl = VersionCheckReq.class.getAnnotation(RequestObject.class).hwUrl()
                            + "/ColemanServer/versionDownload?file=" + fileName;
                }
                new FileTransfer().downloadFile(tempUrl, file.getAbsolutePath());
                if (file.exists() && file.length() > 0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file),
                            "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // 从网络安装
                    // Intent intent = new Intent();
                    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // intent.setAction(android.content.Intent.ACTION_VIEW);
                    //
                    // /* 调用getMIMEType()来取得MimeType */
                    // String type = getMIMEType(f);
                    // /* 设置intent的file与MimeType */
                    // intent.setDataAndType(Uri.fromFile(f),type);
                    // startActivity(intent);
                }
                UpgradeService.this.stopSelf();
            }
        });
    }
}

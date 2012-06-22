/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coleman.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Build configuration. The constants in this class vary depending on release
 * vs. debug build. {@more}
 */
public final class Config {

    /**
     * App read file encoding.
     */
    public static final String ENCODE = "GBK";

    public static final boolean isTestServer = false;

    public static boolean isSimulator(Context context) {
        TelephonyManager telmgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceID = telmgr.getDeviceId();
        return "000000000000000".equalsIgnoreCase(deviceID);
    }

    public static boolean isExternalMediaMounted() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static String getTopActivityClassName() {
        ActivityManager am = (ActivityManager) MyApp.context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String cName = cn.getClassName();
        return cName;
    }

    public static int getVersionCode() {
        PackageManager manager = MyApp.context.getPackageManager();
        PackageInfo info = null;
        int appVersionCode = -1;
        try {
            info = manager.getPackageInfo(MyApp.context.getPackageName(), 0);
            appVersionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersionCode;
    }
}

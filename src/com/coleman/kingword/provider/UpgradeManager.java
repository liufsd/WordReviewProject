
package com.coleman.kingword.provider;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.coleman.util.AppSettings;

public class UpgradeManager {
    private static UpgradeManager manager = new UpgradeManager();

    private UpgradeManager() {
    }

    public static UpgradeManager getInstance() {
        return manager;
    }

    public void upgrade(Context context) {
        int dbVersion = AppSettings.getInt(context, AppSettings.DB_VERSION_KEY, 1);
        dbVersion++;
        AppSettings.saveInt(context, AppSettings.DB_VERSION_KEY, dbVersion);
        KingWordProvider.updateUriMatcher(DictIndexManager.getInstance().getHashMap().values());
        context.getContentResolver().update(
                Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator + "upgrade"),
                new ContentValues(), null, null);
    }
}

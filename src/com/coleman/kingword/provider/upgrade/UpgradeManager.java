
package com.coleman.kingword.provider.upgrade;

import com.coleman.kingword.R;
import com.coleman.ojm.core.Observable;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;
import com.coleman.util.Log;
import com.coleman.util.MyApp;
import com.coleman.util.ThreadUtils;

public class UpgradeManager extends Observable {
    protected static final String TAG = null;

    private static UpgradeManager manager;

    private UpgradeManager() {
    }

    public static UpgradeManager getInstance() {
        if (manager == null) {
            manager = new UpgradeManager();
        }
        return manager;
    }

    public boolean needUpgrade() {
        int savedVersion = AppSettings.getInt(AppSettings.SAVED_DB_VERSION_KEY, 6);
        int curVersionCode = Config.getVersionCode();
        return curVersionCode > savedVersion;
    }

    public void upgrade() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DBVersionChain.getInstance().upgrade();
                    setChanged();
                    notifyObservers(null);
                } catch (Exception e) {
                    setChanged();
                    notifyObservers(MyApp.context.getString(R.string.upgrade_db_failed));
                    Log.i(TAG, "===coleman-debug-argType:" + e.toString());
                }
            }
        });
    }

}

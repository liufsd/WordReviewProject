
package com.coleman.kingword.provider.upgrade;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord;
import com.coleman.log.Log;
import com.coleman.ojm.core.Observable;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;
import com.coleman.util.MyApp;
import com.coleman.util.ThreadUtils;

/**
 * 需要在init之前执行数据库升级。
 * 
 * @author coleman
 * @version [version, Jul 17, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public class UpgradeManager extends Observable {
    protected static final String TAG = UpgradeManager.class.getName();

    private static Log Log = Config.getLog();

    private static UpgradeManager manager;

    private String failMsg = "";

    private UpgradeManager() {
    }

    public static UpgradeManager getInstance() {
        if (manager == null) {
            manager = new UpgradeManager();
        }
        return manager;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public boolean needUpgrade() {
        if (AppSettings.getBoolean(AppSettings.FIRST_STARTED_KEY, true)) {
            AppSettings.saveInt(AppSettings.SAVED_DB_VERSION_KEY, KingWord.version);
            Log.i(TAG, "===coleman-debug-app first start, no need to execute database upgrade");
            return false;
        }
        int savedVersion = AppSettings.getInt(AppSettings.SAVED_DB_VERSION_KEY, 6);
        int curVersionCode = Config.getVersionCode();
        return curVersionCode > savedVersion;
    }

    public void upgrade() {
        ThreadUtils.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    InternalDBVersionChain.getInstance().upgrade();
                    setChanged();
                    notifyObservers(UpgradeManager.this);
                } catch (Exception e) {
                    setFailMsg(MyApp.context.getString(R.string.upgrade_db_failed));
                    setChanged();
                    notifyObservers(UpgradeManager.this);
                    Log.i(TAG, "===coleman-debug-argType:" + e.toString());
                }
            }
        });
    }

}

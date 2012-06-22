
package com.coleman.kingword.provider.upgrade;

import java.util.HashMap;

import com.coleman.kingword.provider.upgrade.version6.Task_v6;
import com.coleman.util.AppSettings;

/**
 * 数据库版本变迁职责链，升级过程为一步步递进式升级，不可从低版本一次升级到高版本。
 * <p>
 * 1. 获取当前软件版本的目标数据库版本
 * <p>
 * 2. 获取当前软件版本的历史数据库版本
 * <p>
 * 3. 创建历史数据库版本到目标数据库版本的升级链
 * <p>
 * 4. 执行升级，成功后记录新的历史数据库版本
 * <p>
 * 
 * @author coleman
 * @version [version, Jun 21, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
class DBVersionChain {
    private static DBVersionChain chain;

    private HashMap<Integer, Task> map = new HashMap<Integer, Task>();

    private DBVersionChain() {
        map.put(6, new Task_v6());
    }

    public static DBVersionChain getInstance() {
        if (chain == null) {
            chain = new DBVersionChain();
        }
        return chain;
    }

    /**
     * 执行递归式步进升级，每一步升级前
     */
    public void upgrade() {
        if (UpgradeManager.getInstance().needUpgrade()) {
            int savedVersion = AppSettings.getInt(AppSettings.SAVED_DB_VERSION_KEY, 6);
            Task task = map.get(savedVersion);
            task.execute();
            upgrade();
        }
    }
}


package com.coleman.kingword.dict;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.UpgradeManager;
import com.coleman.kingword.provider.DictIndexManager.DictIndexTable;
import com.coleman.util.AppSettings;
import com.coleman.util.Log;

public class DynamicTableManager {
    private static DynamicTableManager mg = new DynamicTableManager();

    private static final String EXT_DIC_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "kingword/dicts";

    private static final String TAG = DynamicTableManager.class.getName();

    private HashMap<String, DictIndexDescribeTable> map = new HashMap<String, DictIndexDescribeTable>();

    private DynamicTableManager() {
    }

    public static DynamicTableManager getInstance() {
        return mg;
    }

    public void parsePrefs(String prefs) {
        String temps[] = prefs.split(";");
        for (String string : temps) {
            DictIndexDescribeTable t = new DictIndexDescribeTable(string);
            map.put(t.name, t);
        }
    }

    /**
     * invoke by WelcomeActivity
     * 
     * @param context
     */
    public void initTables(Context context) {

        // initial the collections of the dict index manager
        DictIndexManager.getInstance().init();
        map.clear();

        // get the storage dict list
        ArrayList<String> storagelist = new ArrayList<String>();
        String path = EXT_DIC_PATH;
        File dir = new File(path);
        String fs[] = dir.list();
        if (fs != null) {
            for (String string : fs) {
                if (string.endsWith(".ifo")) {
                    storagelist.add(string.substring(0, string.lastIndexOf(".")));
                }
            }
        }

        // get the preference dict list
        String dicts = AppSettings.getString(context, AppSettings.DICTS_KEY, "");
        Log.d(TAG, "===================dict list pref: " + dicts);
        if (!"".equals(dicts)) {
            parsePrefs(dicts);
        }

        // compare storagelist and preflist
        ArrayList<String> removelist = new ArrayList<String>();
        boolean upgrade = false;
        for (DictIndexDescribeTable t : map.values()) {
            if (!t.internal && !storagelist.contains(t.name)) {
                DictIndexManager.getInstance().getDropList().add(new DictIndexTable(t.name, t.id));
                removelist.add(t.name);
                upgrade = true;
            } else {
                if (!t.loaded) {
                    DictIndexManager.getInstance().getCreateList()
                            .add(new DictIndexTable(t.name, t.id));
                    upgrade = true;
                }
                DictIndexManager.getInstance().getHashMap()
                        .put(t.name, new DictIndexTable(t.name, t.id));
            }
        }
        for (String string : removelist) {
            // @coding-skill
            // you can not remove the element of the collection directly in its
            // for-each loop.
            map.remove(string);
        }

        int max = 1000;
        for (DictIndexDescribeTable t : map.values()) {
            if (t.id > max) {
                max = t.id;
            }
        }

        // handle external dicts
        for (String s : storagelist) {
            if (!map.containsKey(s)) {
                max += 2;
                DictIndexTable table = new DictIndexTable(s, max);
                DictIndexManager.getInstance().getCreateList().add(table);
                DictIndexManager.getInstance().getHashMap().put(s, table);
                map.put(s, new DictIndexDescribeTable(s, false, false, max, 0));
                upgrade = true;
            }
        }

        if (upgrade) {
            // update preference list
            AppSettings.saveString(context, AppSettings.DICTS_KEY, toString());

            // upgrade database
            UpgradeManager.getInstance().upgrade(context);
        }

        // log for debug
        DictIndexManager.getInstance().print();
    }

    public Collection<DictIndexDescribeTable> getTables() {
        return map.values();
    }

    public boolean isInternal(String tableName) {
        return map.get(tableName).internal;
    }

    public boolean isInitialed(String libKey) {
        DictIndexDescribeTable t = map.get(libKey);
        if (t != null) {
            return t.loaded;
        }
        return false;
    }

    public void setComplete(Context context, String mLibKey) {
        DictIndexDescribeTable table = map.get(mLibKey);
        if (table != null) {
            table.loaded = true;
        }
        AppSettings.saveString(context, AppSettings.DICTS_KEY, toString());
    }

    @Override
    public String toString() {
        String str = "";
        for (DictIndexDescribeTable t : map.values()) {
            str += t.toString() + ";";
        }
        if (str.length() > 1) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static class DictIndexDescribeTable {
        public DictIndexDescribeTable(String s) {
            String sub[] = s.split(",");
            name = sub[0];
            loaded = Boolean.parseBoolean(sub[1]);
            internal = Boolean.parseBoolean(sub[2]);
            id = Integer.parseInt(sub[3]);
            type = Integer.parseInt(sub[4]);
        }

        public DictIndexDescribeTable(String s, boolean isLoaded, boolean isInternal, int max, int i) {
            this.name = s;
            this.loaded = isLoaded;
            this.internal = isInternal;
            this.id = max;
            this.type = i;
        }

        public String name = "";

        public boolean loaded = false;

        public boolean internal = false;

        public int id = 1000;

        public int type = 0;// 1 cur lib, 2 more lib, 3 cur lib & more lib

        @Override
        public String toString() {
            return name + "," + loaded + "," + internal + "," + id + "," + type;
        }
    }
}

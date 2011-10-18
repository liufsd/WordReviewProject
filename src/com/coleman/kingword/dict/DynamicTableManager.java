
package com.coleman.kingword.dict;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

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

    private HashMap<String, DynamicTable> map = new HashMap<String, DynamicTable>();

    private DynamicTableManager() {
    }

    public static DynamicTableManager getInstance() {
        return mg;
    }

    public void parsePrefs(String prefs) {
        String temps[] = prefs.split(";");
        for (String string : temps) {
            DynamicTable t = new DynamicTable(string);
            map.put(t.name, t);
        }
    }

    /**
     * invoke by WelcomeActivity
     * 
     * @param context
     */
    public void initTables(Context context) {
        ArrayList<String> storagelist = null;
        // get the storage dict list
        if (storagelist == null) {
            storagelist = new ArrayList<String>();
            String path = EXT_DIC_PATH;
            File dir = new File(path);
            String fs[] = dir.list();
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
        boolean upgrade = false;
        for (DynamicTable t : map.values()) {
            if (!storagelist.contains(t.name)) {
                DictIndexManager.getInstance().getDropList().add(new DictIndexTable(t.name, t.id));
                map.remove(t.name);
                upgrade = true;
            } else {
                if (!t.loaded) {
                    DictIndexManager.getInstance().getCreateList()
                            .add(new DictIndexTable(t.name, t.id));
                    t.loaded = true;
                }
                DictIndexManager.getInstance().getHashMap()
                        .put(t.name, new DictIndexTable(t.name, t.id));
            }
        }
        int max = 1000;
        for (DynamicTable t : map.values()) {
            if (t.id > max) {
                max = t.id;
            }
        }

        for (String s : storagelist) {
            if (!map.containsKey(s)) {
                max += 2;
                DictIndexTable table = new DictIndexTable(s, max);
                DictIndexManager.getInstance().getCreateList().add(table);
                DictIndexManager.getInstance().getHashMap().put(s, table);
                map.put(s, new DynamicTable(s, true, max, 0));
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

    public Collection<DynamicTable> getTables() {
        return map.values();
    }

    @Override
    public String toString() {
        String str = "";
        for (DynamicTable t : map.values()) {
            str += t.toString() + ";";
        }
        if (str.length() > 1) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static class DynamicTable {
        public DynamicTable(String s) {
            String sub[] = s.split(",");
            name = sub[0];
            loaded = Boolean.parseBoolean(sub[1]);
            id = Integer.parseInt(sub[2]);
            type = Integer.parseInt(sub[3]);
        }

        public DynamicTable(String s, boolean b, int max, int i) {
            this.name = s;
            this.loaded = b;
            this.id = max;
            this.type = i;
        }

        public String name = "";

        public boolean loaded = false;

        public int id = 1000;

        public int type = 0;// 1 cur lib, 2 more lib, 3 cur lib & more lib

        @Override
        public String toString() {
            return name + "," + loaded + "," + id + "," + type;
        }
    }
}

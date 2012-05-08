
package com.coleman.kingword.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictInfo;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.provider.KingWord.TDict;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.Log;

/**
 * Used for load dictionaries or delete dictionaries dynamically.
 * 
 * @author coleman
 */
public class DictIndexManager {
    private static final String TAG = DictIndexManager.class.getName();

    private static DictIndexManager instance = new DictIndexManager();

    private HashMap<String, TDictIndex> map = new HashMap<String, TDictIndex>();

    private ArrayList<TDictIndex> droplist = new ArrayList<TDictIndex>();

    private ArrayList<TDictIndex> createlist = new ArrayList<TDictIndex>();

    public static DictIndexManager getInstance() {
        return instance;
    }

    public void init() {
        // @coding-skill
        // init the TDictIndexManager's collections, because android will not
        // kill the static instance if memory is enough
        map.clear();
        droplist.clear();
        createlist.clear();
    }

    public HashMap<String, TDictIndex> getHashMap() {
        return map;
    }

    public ArrayList<TDictIndex> getDropList() {
        return droplist;
    }

    public ArrayList<TDictIndex> getCreateList() {
        return createlist;
    }

    public TDictIndex getTable(String mLibKey) {
        return map.get(mLibKey);
    }

    public void print() {
        Collection<TDictIndex> col = map.values();
        for (TDictIndex index : col) {
            Log.d(TAG, "map>>>" + index.toString());
        }
        for (TDictIndex index : droplist) {
            Log.d(TAG, "droplist>>>" + index.toString());
        }
        for (TDictIndex index : createlist) {
            Log.d(TAG, "createlist>>>" + index.toString());
        }
    }

    public void update(Context context) {
        try {
            SQLiteDatabase db = KingWordDBHepler.getInstance(context).getWritableDatabase();
            for (TDictIndex id : droplist) {
                db.delete(TDict.TABLE_NAME, TDict.DICT_DIR_NAME + "='" + id.getLibDirName() + "'",
                        null);
                db.execSQL("drop table if exists " + id.TABLE_NAME);
            }
            for (TDictIndex id : createlist) {
                db.execSQL(id.getCreateTableSQL());
            }
            for (TDictIndex table : DictIndexManager.getInstance().getHashMap().values()) {
                DictLibrary lib = DictLibrary.loadLibrary(context, table.getLibDirName(),
                        DictManager.getInstance().isInternal(table.getLibDirName()));
                DictManager.getInstance().addLib(lib);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

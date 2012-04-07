
package com.coleman.kingword.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.coleman.kingword.provider.KingWord.Dict.DictIndex;
import com.coleman.util.Log;

/**
 * Used for load dictionaries or delete dictionaries dynamically.
 * 
 * @author coleman
 */
public class DictIndexManager {
    private static final String TAG = DictIndexManager.class.getName();

    private static DictIndexManager instance = new DictIndexManager();

    private HashMap<String, DictIndex> map = new HashMap<String, DictIndex>();

    private ArrayList<DictIndex> droplist = new ArrayList<DictIndex>();

    private ArrayList<DictIndex> createlist = new ArrayList<DictIndex>();

    public static DictIndexManager getInstance() {
        return instance;
    }

    public void init() {
        // @coding-skill
        // init the DictIndexManager's collections, because android will not
        // kill the static instance if memory is enough
        map.clear();
        droplist.clear();
        createlist.clear();
    }

    public HashMap<String, DictIndex> getHashMap() {
        return map;
    }

    public ArrayList<DictIndex> getDropList() {
        return droplist;
    }

    public ArrayList<DictIndex> getCreateList() {
        return createlist;
    }

    public DictIndex getTable(String mLibKey) {
        return map.get(mLibKey);
    }

    public void print() {
        Collection<DictIndex> col = map.values();
        for (DictIndex dictIndex : col) {
            Log.d(TAG, "map>>>" + dictIndex.toString());
        }
        for (DictIndex dictIndex : droplist) {
            Log.d(TAG, "droplist>>>" + dictIndex.toString());
        }
        for (DictIndex dictIndex : createlist) {
            Log.d(TAG, "createlist>>>" + dictIndex.toString());
        }
    }
}

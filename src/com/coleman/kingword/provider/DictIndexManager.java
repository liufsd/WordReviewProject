
package com.coleman.kingword.provider;

import java.io.File;
import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.coleman.kingword.provider.DictIndexManager.DictIndexTable;
import com.coleman.kingword.provider.KingWord.IDictIndex;

/**
 * Used for load dictionaries or delete dictionaries dynamically.
 * 
 * @author coleman
 */
public class DictIndexManager {
    private static final String TAG = DictIndexManager.class.getName();

    private static DictIndexManager instance = new DictIndexManager();

    private HashMap<String, DictIndexTable> map = new HashMap<String, DictIndexManager.DictIndexTable>();

    private ArrayList<DictIndexTable> droplist = new ArrayList<DictIndexTable>();

    private ArrayList<DictIndexTable> createlist = new ArrayList<DictIndexTable>();

    public static DictIndexManager getInstance() {
        return instance;
    }

    public HashMap<String, DictIndexTable> getHashMap() {
        return map;
    }

    public ArrayList<DictIndexTable> getDropList() {
        return droplist;
    }

    public ArrayList<DictIndexTable> getCreateList() {
        return createlist;
    }

    public DictIndexTable getTable(String mLibKey) {
        return map.get(mLibKey);
    }

    public void print() {
        Collection<DictIndexTable> col = map.values();
        for (DictIndexTable dictIndexTable : col) {
            Log.d(TAG, "map>>>" + dictIndexTable.toString());
        }
        for (DictIndexTable dictIndexTable : droplist) {
            Log.d(TAG, "droplist>>>" + dictIndexTable.toString());
        }
        for (DictIndexTable dictIndexTable : createlist) {
            Log.d(TAG, "createlist>>>" + dictIndexTable.toString());
        }
    }

    public static class DictIndexTable implements IDictIndex, BaseColumns {
        // table name
        public final String TABLE_NAME;

        // content uri
        public final Uri CONTENT_URI;

        public final int URI;

        public int URI_ID;

        public String CREATE_TABLE_SQL;

        // default sort order
        public static final String DEFAULT_SORT_ORDER = WORD + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(WORD, WORD);
            projectionMap.put(OFFSET, OFFSET);
            projectionMap.put(SIZE, SIZE);
        }

        /**
         * Be sure that the uri is big than 1000.
         * 
         * @param tableName
         * @param uri
         */
        public DictIndexTable(String tableName, int uri) {
            // table name
            TABLE_NAME = tableName;
            // content uri
            CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                    + TABLE_NAME);
            // uri for UiMacher
            URI = uri;
            // uri id
            URI_ID = uri + 1;
            // create sql
            CREATE_TABLE_SQL = "create table if not exists " + TABLE_NAME + " ( " + _ID
                    + " integer primary key autoincrement , " + WORD + " text ," + OFFSET
                    + " integer," + SIZE + " integer )";
        }

        public String toString() {
            return TABLE_NAME + "{ URI=" + URI + ", URI_ID=" + URI_ID + "}";
        }
    }
}

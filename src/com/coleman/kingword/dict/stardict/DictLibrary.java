
package com.coleman.kingword.dict.stardict;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.coleman.kingword.dict.DynamicTableManager;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.Log;

public class DictLibrary {
    private static final String TAG = DictLibrary.class.getName();

    private static final String DICTS_DIR = "kingword/dicts/";

    private DictInfo libraryInfo;

    private String mLibKey;

    private String mLibPath;

    private boolean dbInitialed;

    /**
     * describe the library internal or external.
     */
    private boolean internal;

    /**
     * If a DictLibrary is constructed, it will do inserting data to database
     * background, after the work done, the libraryWordMap will be cleared,
     * after all these thing done, a mark will be set to indicate database
     * initialed.
     */
    DictLibrary(final Context context, final String libKey, boolean isInternal) {
        this.mLibKey = libKey;
        this.mLibPath = DICTS_DIR + libKey;
        this.dbInitialed = DynamicTableManager.getInstance().isInitialed(libKey);
        this.internal = isInternal;
        Log.d(TAG, ">>>>>>>>>>>>>>>>dbInitialed: " + dbInitialed);
        if (!dbInitialed) {
            new LoadAndInsert().doWork(context);
        } else {
            new LoadInfo().doWork(context);
        }
    }

    public DictInfo getLibraryInfo() {
        return libraryInfo;
    }

    public boolean isInitialed() {
        return dbInitialed;
    }

    public boolean isInternal() {
        return internal;
    }

    public DictIndex getDictIndex(Context context, String word) {
        DictIndex di = null;
        String[] projection = new String[] {
                TDictIndex.WORD, TDictIndex.OFFSET, TDictIndex.SIZE
        };
        if (dbInitialed) {
            long time = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(
                    DictIndexManager.getInstance().getTable(mLibKey).getContentUri(), projection,
                    TDictIndex.WORD + " = '" + word + "'", null, null);
            if (c.moveToFirst()) {
                di = new DictIndex(c.getString(0), c.getLong(1), c.getInt(2));
            }
            if (c != null) {
                c.close();
                c = null;
            }

            // if not found the word from the index databases, try to query the
            // lower case of the word from the index database
            if (di == null) {
                c = context.getContentResolver()
                        .query(DictIndexManager.getInstance().getTable(mLibKey).getContentUri(),
                                projection, TDictIndex.WORD + " = '" + word.toLowerCase() + "'",
                                null, null);
                if (c.moveToFirst()) {
                    di = new DictIndex(c.getString(0), c.getLong(1), c.getInt(2));
                }
                if (c != null) {
                    c.close();
                    c = null;
                }
            }
            time = System.currentTimeMillis() - time;
            Log.d(TAG, "Query the word from the database cost time: " + time);
        }
        return di;
    }

    public String getLibraryName() {
        return mLibPath;
    }

    public String getIfoFileName() {
        return mLibPath + ".ifo";
    }

    public String getIdxFileName() {
        return mLibPath + ".idx";
    }

    public int getNumCount() {
        return Integer.parseInt(libraryInfo.wordCount);
    }

    public void setComplete(Context context) {
        dbInitialed = true;
        DynamicTableManager.getInstance().setComplete(context, mLibKey);
    }

    private class LoadInfo {

        private LoadInfo() {
        }

        protected void doWork(Context context) {
            if (!TextUtils.isEmpty(mLibKey)) {
                libraryInfo = DictInfo.readDicInfo(context, DictLibrary.this);
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
            Log.d(TAG, "lib info:" + libraryInfo);
        }

    }

    private class LoadAndInsert {

        private LoadAndInsert() {
        }

        protected void doWork(Context context) {
            if (!TextUtils.isEmpty(mLibKey)) {
                libraryInfo = DictInfo.readDicInfo(context, DictLibrary.this);
                DictIndex.loadDictIndexMap(context, DictLibrary.this);
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
        }
    }

}

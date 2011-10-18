
package com.coleman.kingword.dict.stardict;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.KingWord.IDictIndex;
import com.coleman.util.AppSettings;
import com.coleman.util.Log;

public class DictLibrary {
    private static final String TAG = DictLibrary.class.getName();

    // public static final String OXFORD = "oxford";
    //
    // public static final String STARDICT = "stardict";
    //
    // public static final String BABYLON_ENG = "babylon";
    //
    // public static final String OXFORD_PATH =
    // "kingword/dicts/oxford-gb-formated";
    //
    // public static final String STARDICT_PATH = "kingword/dicts/stardict1.3";
    //
    // public static final String BABYLON_PATH =
    // "kingword/dicts/Babylon_English";

    private DictInfo libraryInfo;

    private String mLibKey;

    private String mLibPath;

    private boolean dbInitialed;

    /**
     * If a DictLibrary is constructed, it will do inserting data to database
     * background, after the work done, the libraryWordMap will be cleared,
     * after all these thing done, a mark will be set to indicate database
     * initialed.
     */
    DictLibrary(final Context context, final String libKey) {
        this.mLibKey = libKey;
        this.mLibPath = "kingword/dicts/" + libKey;
        this.dbInitialed = AppSettings.getBoolean(context, libKey, false);
        Log.d(TAG, ">>>>>>>>>>>>>>>>dbInitialed: "+dbInitialed);
        if (!dbInitialed) {
            new LoadAndInsert().doWork(this, context, libKey);
        } else {
            new LoadInfo().doWork(context, libKey);
        }
    }

    public DictInfo getLibraryInfo() {
        return libraryInfo;
    }

    public boolean isInitialed() {
        return dbInitialed;
    }

    public DictIndex getDictIndex(Context context, String word) {
        DictIndex di = null;
        String[] projection = new String[] {
                IDictIndex.WORD, IDictIndex.OFFSET, IDictIndex.SIZE
        };
        if (dbInitialed) {
            long time = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(
                    DictIndexManager.getInstance().getTable(mLibKey).CONTENT_URI, projection,
                    IDictIndex.WORD + " = '" + word + "'", null, null);
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
                c = context.getContentResolver().query(
                        DictIndexManager.getInstance().getTable(mLibKey).CONTENT_URI, projection,
                        IDictIndex.WORD + " = '" + word.toLowerCase() + "'", null, null);
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

    public void setComplete(Context context) {
        dbInitialed = true;
        AppSettings.saveBoolean(context, mLibKey, true);
    }

    private class LoadInfo {

        private LoadInfo() {
        }

        protected void doWork(Context context, String libKey) {
            if (!TextUtils.isEmpty(libKey)) {
                libraryInfo = DictInfo.readDicInfo(context, mLibPath + ".ifo");
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
            Log.d(TAG, "lib info:" + libraryInfo);
        }

    }

    private class LoadAndInsert {

        private LoadAndInsert() {
        }

        protected void doWork(DictLibrary diclib, Context context, String libKey) {
            if (!TextUtils.isEmpty(libKey)) {
                libraryInfo = DictInfo.readDicInfo(context, mLibPath + ".ifo");
                DictIndex.loadDictIndexMap(diclib, context, mLibPath + ".idx",
                        Integer.parseInt(libraryInfo.wordCount));
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
        }
    }

}

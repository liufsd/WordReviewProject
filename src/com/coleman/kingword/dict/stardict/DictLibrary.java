
package com.coleman.kingword.dict.stardict;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.Log;

public class DictLibrary {
    private static final String TAG = DictLibrary.class.getName();

    private static final String DICTS_DIR = "kingword/dicts/";

    private DictInfo libraryInfo;

    private String mLibDirName;

    private String mLibPath;

    private boolean dbInitialed;

    /**
     * describe the library internal or external.
     */
    private boolean internal;

    /**
     * This work is time expensive, you should do it on the background thread.
     * 
     * @param context
     * @param libKey
     * @param dictmap
     * @throws IOException
     */
    public static DictLibrary loadLibrary(Context context, String libKey, boolean isInternal)
            throws IOException {
        DictLibrary lib = new DictLibrary();
        lib.mLibDirName = libKey;
        lib.mLibPath = DICTS_DIR + libKey;
        lib.dbInitialed = DictManager.getInstance().isInitialed(libKey);
        lib.internal = isInternal;
        lib.libraryInfo = DictInfo.loadInfo(context, lib);
        if (!lib.dbInitialed) {
            DictIndex.loadDictIndexMap(context, lib);
        }
        return lib;
    }

    /**
     * If a DictLibrary is constructed, it will do inserting data to database
     * background, after the work done, the libraryWordMap will be cleared,
     * after all these thing done, a mark will be set to indicate database
     * initialed.
     */
    private DictLibrary() {
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

    public boolean isCurLib() {
        return mLibDirName != null
                && mLibDirName.equals(DictManager.getInstance().getCurLibDirName());
    }

    public boolean isMoreLib() {
        return mLibDirName != null
                && mLibDirName.equals(DictManager.getInstance().getMoreLibDirName());

    }

    public DictIndex getDictIndex(Context context, String word) {
        DictIndex di = null;
        String[] projection = new String[] {
                TDictIndex.WORD, TDictIndex.OFFSET, TDictIndex.SIZE
        };
        if (dbInitialed) {
            long time = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(
                    DictIndexManager.getInstance().getTable(mLibDirName).getContentUri(),
                    projection, TDictIndex.WORD + " = '" + word + "'", null, null);
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
                c = context
                        .getContentResolver()
                        .query(DictIndexManager.getInstance().getTable(mLibDirName).getContentUri(),
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

    public String getLibDirName() {
        return mLibDirName;
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
        libraryInfo.loaded = dbInitialed;
        libraryInfo.insertOrUpdate(context);
    }

}

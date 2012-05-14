
package com.coleman.kingword.dict.stardict;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.Log;

public class DictLibrary {
    private static final String TAG = "DictLibrary";

    private static final String DICTS_DIR = "kingword/dicts/";

    private DictInfo libraryInfo;

    private String mLibPath;

    /**
     * This work is time expensive, you should do it on the background thread.
     * 
     * @param context
     * @param libKey
     * @param dictmap
     * @throws IOException
     */
    public static DictLibrary loadLibrary(Context context, DictInfo info) throws IOException {
        DictLibrary lib = new DictLibrary();
        lib.mLibPath = DICTS_DIR + info.dictDirName;
        lib.libraryInfo = info;
        if (!info.loaded) {
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
        return libraryInfo.loaded;
    }

    public boolean isInternal() {
        return libraryInfo.internal;
    }

    public boolean isCurLib() {
        return libraryInfo.dictDirName != null
                && libraryInfo.dictDirName.equals(DictManager.getInstance().getCurLibDirName());
    }

    public boolean isMoreLib() {
        return libraryInfo.dictDirName != null
                && libraryInfo.dictDirName.equals(DictManager.getInstance().getMoreLibDirName());

    }

    public DictIndex getDictIndex(Context context, String word) {
        DictIndex di = null;
        String[] projection = new String[] {
                TDictIndex.WORD, TDictIndex.OFFSET, TDictIndex.SIZE
        };
        if (libraryInfo.loaded) {
            long time = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(
                    TDictIndex.getContentUri(libraryInfo.dictDirName), projection,
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
                c = context.getContentResolver().query(
                        TDictIndex.getContentUri(libraryInfo.dictDirName), projection,
                        TDictIndex.WORD + " = '" + word.toLowerCase() + "'", null, null);
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
        return libraryInfo.dictDirName;
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
        libraryInfo.loaded = true;
        libraryInfo.insertOrUpdate(context);
    }

}

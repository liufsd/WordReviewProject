
package com.coleman.kingword.dict;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.dict.stardict.DictIndex;
import com.coleman.kingword.dict.stardict.DictInfo;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.provider.KingWord.TDict;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.kingword.provider.KingWordDBHepler;
import com.coleman.util.Log;

/**
 * Manager the dictionary, there are two kinds of files 1.the stardict
 * dictionary stardict dictionary are made of three parts: *.dict, *.idx and
 * *.ifo
 * 
 * @author coleman
 */
public class DictManager {
    private static final String TAG = "DictManager";

    private static DictManager manager;

    /**
     * @TODO to save the memory, should do some optimization to the dictmap,
     *       such as merge the different dicts.
     */
    private HashMap<String, DictLibrary> libmap = new HashMap<String, DictLibrary>();

    public static final String DEFAULT_CUR_LIB = "a49_stardict_1_3";

    public static final String DEFAULT_MORE_LIB = "a50_oxford_gb_formated";

    private String curLib = DEFAULT_CUR_LIB;

    private String moreLib = DEFAULT_MORE_LIB;

    private DictManager() {
    }

    public static DictManager getInstance() {
        if (manager == null) {
            manager = new DictManager();
        }
        return manager;
    }

    /**********************************************************************
     * operate library
     **********************************************************************/
    public boolean isCurLibInitialized() {
        return libmap.get(curLib) != null && libmap.get(curLib).isInitialed();
    }

    /**
     * This method will do several things: 1. initial the database if not
     * initialed otherwise do nothing. 2. release the data if the database have
     * been initialed.
     */
    public void initLibrary(Context context) {

        // 1. initial library by loader
        LibraryLoader.getInstance().initTables(context);

        // 2. load library
        long time = System.currentTimeMillis();

        // 3. set cur library and more library
        for (DictLibrary lib : libmap.values()) {
            if (lib.isCurLib()) {
                setCurLibrary(lib.getLibDirName());
            } else if (lib.isMoreLib()) {
                setMoreLibrary(lib.getLibDirName());
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Load library cost time: " + time);
    }

    public void addLib(DictLibrary lib) {
        libmap.put(lib.getLibDirName(), lib);
    }

    public void setCurLibrary(String lib) {
        curLib = lib;
        Log.d(TAG, ">>>>>>>>>>>>>>>curLib: " + curLib);
    }

    public void setMoreLibrary(String lib) {
        moreLib = lib;
        Log.d(TAG, ">>>>>>>>>>>>>>>moreLib: " + moreLib);
    }

    public DictLibrary getLibrary(String lib) {
        return libmap.get(lib);
    }

    public Collection<DictLibrary> getLibrarys() {
        return libmap.values();
    }

    public String getCurLibDirName() {
        return curLib;
    }

    public String getMoreLibDirName() {
        return moreLib;
    }

    /**********************************************************************
     * operate word
     **********************************************************************/

    public DictData viewWord(Context context, String word) {
        DictLibrary library = libmap.get(curLib);
        Log.d(TAG, "---------------------library: " + curLib);
        if (library == null) {
            Log.w(TAG, "library has not been initialed!");
            return DictData.constructData(word + ": library has not been initialed!");
        }
        DictIndex index = library.getDictIndex(context, word);
        if (index == null) {
            Log.w(TAG, word + " not found!");
            return DictData.constructData(word + ": not found!");
        }
        DictData wordData = DictData.readData(context, library.isInternal(),
                library.getLibraryInfo(), index, library.getLibraryName());
        Log.d(TAG, word + " >>> " + wordData);
        return wordData;
    }

    /**
     * Used to filter the word can not found at the current library.
     */
    public boolean hasWord(Context context, String word) {
        boolean has = true;
        DictLibrary library = libmap.get(curLib);
        if (library != null) {
            has = false;
            DictIndex index = library.getDictIndex(context, word);
            if (index != null) {
                has = true;
            }
        }
        return has;
    }

    public DictData viewMore(Context context, String word) {
        DictLibrary library = libmap.get(moreLib);
        if (library == null) {
            Log.w(TAG, "library has not been initialed!");
            return DictData.constructData(word + ": library has not been initialed!");
        }
        DictIndex index = library.getDictIndex(context, word);
        if (index == null) {
            Log.w(TAG, "not found!");
            return DictData.constructData(word + ": not found!");
        }
        Log.d(TAG, word + ":" + index + ":");
        DictData wordData = DictData.readData(context, library.isInternal(),
                library.getLibraryInfo(), index, library.getLibraryName());
        Log.d(TAG, "" + wordData);
        return wordData;
    }

    public boolean isInitialed(String libKey) {
        return LibraryLoader.getInstance().isInitialed(libKey);
    }

    public boolean isInternal(String tableName) {
        return LibraryLoader.getInstance().isInternal(tableName);
    }

    public enum ViewType {
        DEFAULT(0), ALL(1), SKIP(2), IMPORTANT(3), NEW_WORD(4);
        private final int type;

        private ViewType(int type) {
            this.type = type;
        }

        public int getValue() {
            return type;
        }

        public ViewType getViewType(int type) {
            switch (type) {
                case 1:
                    return ALL;
                case 2:
                    return SKIP;
                case 3:
                    return IMPORTANT;
                case 4:
                    return NEW_WORD;
                case 0:
                default:
                    return DEFAULT;
            }
        }
    }

    /**
     * LibraryLoader works by comparing the database storage with external file
     * path, to determine load or drop a library.
     * <P>
     * No need to compare the whole library, only the DictInfo.
     * 
     * @author coleman
     * @version [version, May 9, 2012]
     * @see [relevant class/method]
     * @since [product/module version]
     */
    private static class LibraryLoader {
        private static LibraryLoader mg = new LibraryLoader();

        private static final String EXT_DIC_PATH = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "kingword/dicts";

        private static final String TAG = "LibraryLoader";

        private HashMap<String, DictInfo> infoMap = new HashMap<String, DictInfo>();

        private HashMap<String, TDictIndex> idxMap = new HashMap<String, TDictIndex>();

        private HashSet<TDictIndex> droplist = new HashSet<TDictIndex>();

        private HashSet<TDictIndex> createlist = new HashSet<TDictIndex>();

        private LibraryLoader() {
        }

        public static LibraryLoader getInstance() {
            return mg;
        }

        /**
         * invoke by WelcomeActivity
         * 
         * @param context
         */
        public void initTables(Context context) {

            // clear the collection
            infoMap.clear();
            idxMap.clear();
            droplist.clear();
            createlist.clear();

            // get the external storage dict list
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

            // loaded dicts from db, if db hasn't been initialized, load
            // from internal file path.
            infoMap = DictInfo.loadFromDB(context);
            if (infoMap.size() == 0) {
                infoMap = DictInfo.loadDefault(context);
            }

            // drop or create the dict by comparing the external
            // storage list with loaded list
            boolean upgrade = false;
            ArrayList<DictInfo> allInfosCopy = new ArrayList<DictInfo>();
            allInfosCopy.addAll(infoMap.values());
            for (DictInfo t : allInfosCopy) {
                if (!t.internal && !storagelist.contains(t.dictDirName)) {
                    droplist.add(new TDictIndex(t.dictDirName));
                    infoMap.remove(t.dictDirName);
                    upgrade = true;
                } else {
                    if (!t.loaded) {
                        createlist.add(new TDictIndex(t.dictDirName));
                        upgrade = true;
                    }
                    idxMap.put(t.dictDirName, new TDictIndex(t.dictDirName));
                }
            }
            allInfosCopy.clear();

            // handle external dicts
            for (String s : storagelist) {
                if (!infoMap.containsKey(s)) {
                    TDictIndex table = new TDictIndex(s);
                    createlist.add(table);
                    idxMap.put(s, table);
                    DictInfo info = DictInfo.loadFromFile(context, false, s);
                    infoMap.put(s, info);
                    upgrade = true;
                }
            }

            // update database
            if (upgrade) {
                update(context);
            }

            // load library
            try {
                for (DictInfo info : infoMap.values()) {
                    DictLibrary lib = DictLibrary.loadLibrary(context, info);
                    DictManager.getInstance().addLib(lib);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isInternal(String tableName) {
            return infoMap.get(tableName).internal;
        }

        public boolean isInitialed(String libKey) {
            DictInfo t = infoMap.get(libKey);
            if (t != null) {
                return t.loaded;
            }
            return false;
        }

        public void update(Context context) {
            SQLiteDatabase db = KingWordDBHepler.getInstance(context).getWritableDatabase();
            for (TDictIndex id : droplist) {
                db.delete(TDict.TABLE_NAME, TDict.DICT_DIR_NAME + "='" + id.getLibDirName() + "'",
                        null);
                // the table is deleted in KingWordProvider.delete(), so no need
                // delete here.
                // db.execSQL("drop table if exists " + id.TABLE_NAME);
            }
            for (TDictIndex dictIndex : createlist) {
                DictInfo info = infoMap.get(dictIndex.getLibDirName());
                info.insertOrUpdate(context);

                db.execSQL(dictIndex.getCreateTableSQL());
            }
        }
    }
}

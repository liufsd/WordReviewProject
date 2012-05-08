
package com.coleman.kingword.dict;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.dict.stardict.DictIndex;
import com.coleman.kingword.dict.stardict.DictInfo;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.AppSettings;
import com.coleman.util.Log;

/**
 * Manager the dictionary, there are two kinds of files 1.the stardict
 * dictionary stardict dictionary are made of three parts: *.dict, *.idx and
 * *.ifo
 * 
 * @author coleman
 */
public class DictManager {
    private static final String TAG = DictManager.class.getName();

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

    public void setComplete(Context context, String mLibKey) {
        LibraryLoader.getInstance().setComplete(context, mLibKey);
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

    private static class LibraryLoader {
        private static LibraryLoader mg = new LibraryLoader();

        private static final String EXT_DIC_PATH = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "kingword/dicts";

        private static final String TAG = LibraryLoader.class.getName();

        private HashMap<String, DictInfo> infoMap = new HashMap<String, DictInfo>();

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

            // initial the collections of the dict index manager
            DictIndexManager.getInstance().init();
            infoMap.clear();

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

            // get loaded dicts from db
            infoMap = DictInfo.loadFromDB(context);
            if (infoMap.size() == 0) {
                infoMap = DictInfo.loadDefault(context);
            }

            // compare storagelist and loaded list
            ArrayList<String> removelist = new ArrayList<String>();
            boolean upgrade = false;
            for (DictInfo t : infoMap.values()) {
                if (!t.internal && !storagelist.contains(t.dictDirName)) {
                    DictIndexManager.getInstance().getDropList().add(new TDictIndex(t.dictDirName));
                    removelist.add(t.dictDirName);
                    upgrade = true;
                } else {
                    if (!t.loaded) {
                        DictIndexManager.getInstance().getCreateList()
                                .add(new TDictIndex(t.dictDirName));
                        upgrade = true;
                    }
                    DictIndexManager.getInstance().getHashMap()
                            .put(t.dictDirName, new TDictIndex(t.dictDirName));
                }
            }
            for (String string : removelist) {
                // @coding-skill
                // you can not remove the element of the collection directly in
                // its
                // for-each loop.
                infoMap.remove(string);
            }

            // handle external dicts
            for (String s : storagelist) {
                if (!infoMap.containsKey(s)) {
                    TDictIndex table = new TDictIndex(s);
                    DictIndexManager.getInstance().getCreateList().add(table);
                    DictIndexManager.getInstance().getHashMap().put(s, table);
                    DictInfo info = DictInfo.loadFromFile(context, false, s);
                    infoMap.put(s, info);
                    upgrade = true;
                }
            }

            if (upgrade) {
                // update database
                DictIndexManager.getInstance().update(context);
            }

            // log for debug
            DictIndexManager.getInstance().print();
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

        public void setComplete(Context context, String mLibKey) {
            DictLibrary lib = DictManager.getInstance().getLibrary(mLibKey);
            if (lib != null) {
                lib.setComplete(context);
            }
        }

    }
}

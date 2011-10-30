
package com.coleman.kingword.dict;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.dict.stardict.DictIndex;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.dict.stardict.DictLibraryFactory;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.UpgradeManager;
import com.coleman.kingword.provider.DictIndexManager.DictIndexTable;
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

        // initial the current library
        // int type = AppSettings.getInt(context, AppSettings.LANGUAGE_TYPE, 0);
        // Log.d(TAG, "===========Dict type: " + type);
        // switch (type) {
        // case 0:
        // setCurLibrary(DictLibrary.STARDICT);
        // break;
        // case 1:
        // setCurLibrary(DictLibrary.BABYLON_ENG);
        // break;
        // default:
        // // ignore
        // break;
        // }

        // load library
        long time = System.currentTimeMillis();
        try {
            DictLibraryFactory factory = new DictLibraryFactory();
            for (DictIndexTable table : DictIndexManager.getInstance().getHashMap().values()) {
                factory.loadLibrary(context, table.TABLE_NAME, DynamicTableManager.getInstance()
                        .isInternal(table.TABLE_NAME), libmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Load library cost time: " + time);
    }

    public void setCurLibrary(String lib) {
        curLib = lib;
        Log.d(TAG, ">>>>>>>>>>>>>>>curLib: " + curLib);
    }

    public void setMoreLibrary(String lib) {
        moreLib = lib;
        Log.d(TAG, ">>>>>>>>>>>>>>>moreLib: " + moreLib);
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
            Log.w(TAG, word+" not found!");
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
}

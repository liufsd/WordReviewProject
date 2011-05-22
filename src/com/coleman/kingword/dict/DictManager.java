
package com.coleman.kingword.dict;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.dict.stardict.DictIndex;
import com.coleman.kingword.dict.stardict.DictLibrary;
import com.coleman.kingword.dict.stardict.DictLibraryFactory;

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

    private String curLib = DictLibrary.STARDICT;

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
    /**
     * This method will do several things: 1. initial the database if not
     * initialed otherwise do nothing. 2. release the data if the database have
     * been initialed.
     */
    public void initLibrary(Context context) {
        long time = System.currentTimeMillis();
        try {
            DictLibraryFactory factory = new DictLibraryFactory();
            factory.loadLibrary(context, DictLibrary.STARDICT, libmap);
            factory.loadLibrary(context, DictLibrary.OXFORD, libmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Load library cost time: " + time);
    }

    public void chooseLibrary(long id) {
    }

    public void cutLibrary(CutMethod method) {
    }

    public void deleteLibrary(long id) {
    }

    public void viewLibraryAchievement(long id) {
    }

    public void viewLibrarySummaryInfo(long id) {
    }

    /**********************************************************************
     * operate unit
     **********************************************************************/
    public void initUnit(long id) {
    }

    /**
     * @param id unit id
     * @param viewType default type is not involving skip list
     */
    public void viewUnit(long id, ViewType type) {
    }

    public void viewUnitAchievement(long id) {
    }

    public void viewUnitSummaryInfo(long id) {
    }

    /**********************************************************************
     * operate word
     **********************************************************************/
    public void moveToSkipList(DictIndex dici) {
    }

    public void moveToNewWordList(DictIndex dici) {
    }

    public DictData viewWord(Context context, String word) {
        DictLibrary library = libmap.get(curLib);
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
        DictData wordData = DictData.readData(context, library.getLibraryInfo(), index,
                library.getLibraryName());
        Log.d(TAG, "" + wordData);
        return wordData;
    }

    /**
     * Increase the importance of specified word.
     */
    public void addPower(DictIndex dici) {
    }

    public void viewMore(DictIndex dici) {
    }

    public class CutMethod {
    }

    public class Library {
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

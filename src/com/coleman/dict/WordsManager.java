
package com.coleman.dict;

import com.coleman.dict.stardict.DictIndex;

/**
 * Manager the dictionary, there are two kinds of files 1.the stardict
 * dictionary stardict dictionary are made of three parts: *.dict, *.idx and
 * *.ifo 2.the target new word list files the target word list files should be
 * simple UTF-8 encoded, and can be read line by line. the correct format is
 * "cat 猫" or "cat". the manager can cut the target new word list into parts,
 * and lookup the new word in the stardict dictionary.
 * 
 * @author coleman
 */
public class WordsManager {
    /**********************************************************************
     * operate library
     **********************************************************************/
    public void addLibrary(String fileName) {
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
                case 0:
                    return DEFAULT;
                case 1:
                    return ALL;
                case 2:
                    return SKIP;
                case 3:
                    return IMPORTANT;
                case 4:
                    return NEW_WORD;
                default:
                    // TODO need to report the exception.
                    return DEFAULT;
            }
        }
    }
}

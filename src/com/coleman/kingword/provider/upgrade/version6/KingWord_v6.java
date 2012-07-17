
package com.coleman.kingword.provider.upgrade.version6;

/*
 * Name        : Pet.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Pet.java
 * Review      : 
 */
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import com.coleman.kingword.provider.KingWordProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class KingWord_v6 {
    public static final int version = 6;

    public static final HashMap<String, HashMap<String, String>> maps = new HashMap<String, HashMap<String, String>>();

    public static final class TDict implements BaseColumns {
        // table name
        public static final String TABLE_NAME = "dict";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

        // fields
        // book name
        public static final String DICT_NAME = "book_name";

        public static final String WORD_COUNT = "word_count";

        public static final String IDX_FILE_SIZE = "idx_file_size";

        // sameTypeSequence
        public static final String SAME_TYPE_SEQUENCE = "same_type_sequence";

        /**
         * 0 未设置，1 current lib, 2 more lib, 3 cur lib & more lib
         */
        public static final String TYPE = "type";

        public static final String LOADED = "loaded";

        // mark if the dict library is store in the application package, or
        // external.
        public static final String INTERNAL = "internal";

        public static final String DICT_DIR_NAME = "index_name";

        public static final String DATE = "date";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" + _ID
                + " integer primary key autoincrement , " + DICT_NAME + " text, " + WORD_COUNT
                + " text , " + IDX_FILE_SIZE + " text, " + SAME_TYPE_SEQUENCE + " text, " + TYPE
                + " integer, " + LOADED + " integer , " + INTERNAL + " integer , " + DICT_DIR_NAME
                + " text unique , " + DATE + " integer)";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = DATE + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(DICT_NAME, DICT_NAME);
            projectionMap.put(WORD_COUNT, WORD_COUNT);
            projectionMap.put(IDX_FILE_SIZE, IDX_FILE_SIZE);
            projectionMap.put(SAME_TYPE_SEQUENCE, SAME_TYPE_SEQUENCE);

            projectionMap.put(TYPE, TYPE);
            projectionMap.put(LOADED, LOADED);
            projectionMap.put(INTERNAL, INTERNAL);
            projectionMap.put(DICT_DIR_NAME, DICT_DIR_NAME);
            projectionMap.put(DATE, DATE);
            maps.put(TABLE_NAME, projectionMap);
        }

        public static class TDictIndex implements BaseColumns {
            public static final String TABLE_NAME_PREFIX = "dict_index_";

            // table name
            public final String TABLE_NAME;

            // fields
            public static String WORD = "word";

            public static String OFFSET = "offset";

            public static String SIZE = "size";

            // default sort order
            public static final String DEFAULT_SORT_ORDER = WORD + " asc";

            // not stored in the db
            private String libDirName;

            // projection map , used for query builder
            public static HashMap<String, String> projectionMap = new HashMap<String, String>();
            static {
                projectionMap.put(_ID, _ID);
                projectionMap.put(WORD, WORD);
                projectionMap.put(OFFSET, OFFSET);
                projectionMap.put(SIZE, SIZE);
            }

            public String getCreateTableSQL() {
                return "create table if not exists " + TABLE_NAME + " ( " + _ID
                        + " integer primary key autoincrement , " + WORD + " text ," + OFFSET
                        + " integer," + SIZE + " integer )";
            }

            public Uri getContentUri() {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME);
            }

            public static Uri getContentUri(String tableName) {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME_PREFIX + tableName);
            }

            public TDictIndex(String tableName) {
                this.libDirName = tableName;
                // table name
                TABLE_NAME = TABLE_NAME_PREFIX + tableName;
                maps.put(TABLE_NAME, projectionMap);
            }

            public String getLibDirName() {
                return libDirName;
            }

            public String toString() {
                return TABLE_NAME + "{ URI=" + getContentUri() + "}";
            }
        }
    }

    public static final class THistory implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "history";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

        // fields
        public static final String WORD = "word";

        public static final String IGNORE = "ignore";

        public static final String STUDY_COUNT = "study_count";

        public static final String ERROR_COUNT = "error_count";

        public static final String WEIGHT = "weight";

        public static final String NEW_WORD = "new_word";

        public static final String REVIEW_TYPE = "review_type";

        public static final String REVIEW_TIME = "review_time";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table " + THistory.TABLE_NAME + " ( "
                + THistory._ID + " integer primary key autoincrement , " + THistory.WORD
                + " text ," + THistory.IGNORE + " integer," + THistory.STUDY_COUNT + " integer,"
                + THistory.ERROR_COUNT + " integer," + THistory.WEIGHT + " integer,"
                + THistory.NEW_WORD + " integer," + THistory.REVIEW_TYPE + " integer,"
                + THistory.REVIEW_TIME + " integer )";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = WORD + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(WORD, WORD);
            projectionMap.put(IGNORE, IGNORE);
            projectionMap.put(STUDY_COUNT, STUDY_COUNT);
            projectionMap.put(ERROR_COUNT, ERROR_COUNT);
            projectionMap.put(WEIGHT, WEIGHT);
            projectionMap.put(NEW_WORD, NEW_WORD);
            projectionMap.put(REVIEW_TYPE, REVIEW_TYPE);
            projectionMap.put(REVIEW_TIME, REVIEW_TIME);
            maps.put(TABLE_NAME, projectionMap);
        }
    }

    public static final class TAchievement implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "achievement";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

        // fields
        public static final String TIME = "time";

        public static final String COUNT = "count";

        public static final String DESCRIBE = "describe";

        public static final String SUBTYPE = "subtype";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table " + TAchievement.TABLE_NAME
                + " ( " + TAchievement._ID + " integer primary key autoincrement , "
                + TAchievement.TIME + " integer ," + TAchievement.COUNT + " integer,"
                + TAchievement.DESCRIBE + " text," + TAchievement.SUBTYPE + " integer )";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = _ID + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(TIME, TIME);
            projectionMap.put(COUNT, COUNT);
            projectionMap.put(DESCRIBE, DESCRIBE);
            projectionMap.put(SUBTYPE, SUBTYPE);
            maps.put(TABLE_NAME, projectionMap);
        }
    }

    public static final class TSubWordList implements BaseColumns {
        // table name
        public static final String TABLE_NAME = "sub_list";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

        // fields
        public static final String WORD_LIST_ID = "word_list_id";

        public static final String LEVEL = "level";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table if not exists " + TABLE_NAME
                + " ( " + TSubWordList._ID + " integer primary key autoincrement , "
                + TSubWordList.WORD_LIST_ID + " integer ," + TSubWordList.LEVEL + " integer )";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = _ID + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(WORD_LIST_ID, WORD_LIST_ID);
            projectionMap.put(LEVEL, LEVEL);
        }

    }

    public static final class TWordList implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "word_list";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

        // fields
        public static final String DESCRIBE = "describe";

        public static final String PATH_NAME = "path_name";

        public static final String SET_METHOD = "set_method";

        public static final String WORDLIST_NAME = "wordlist_name";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = _ID + " asc";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table " + TWordList.TABLE_NAME
                + " ( " + TWordList._ID + " integer primary key autoincrement , "
                + TWordList.DESCRIBE + " text ," + TWordList.PATH_NAME + " text,"
                + TWordList.SET_METHOD + " integer , " + WORDLIST_NAME + " text )";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();

        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(DESCRIBE, DESCRIBE);
            projectionMap.put(PATH_NAME, PATH_NAME);
            projectionMap.put(SET_METHOD, SET_METHOD);
            projectionMap.put(WORDLIST_NAME, WORDLIST_NAME);
            maps.put(TABLE_NAME, projectionMap);
        }

        public static final class TWordListItem implements BaseColumns, Serializable {
            private static final long serialVersionUID = -49108198357051291L;

            public static final String TABLE_NAME_PREFIX = "word_list_item_";

            // table name
            public final String TABLE_NAME;

            // fields
            public static final String SUB_WORD_LIST_ID = "sub_list_id";

            public static final String WORD = "word";

            // default sort order
            public static final String DEFAULT_SORT_ORDER = _ID + " asc";

            // projection map , used for query builder
            public static HashMap<String, String> projectionMap = new HashMap<String, String>();
            static {
                projectionMap.put(_ID, _ID);
                projectionMap.put(SUB_WORD_LIST_ID, SUB_WORD_LIST_ID);
                projectionMap.put(WORD, WORD);
            }

            public TWordListItem(long word_list_id) {
                TABLE_NAME = TABLE_NAME_PREFIX + word_list_id;
                maps.put(TABLE_NAME, projectionMap);
            }

            public Uri getContentUri() {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME);
            }

            public String getCreateTableSql() {
                return "create table if not exists " + TABLE_NAME + " ( " + TWordListItem._ID
                        + " integer primary key autoincrement , " + TWordListItem.SUB_WORD_LIST_ID
                        + " integer ," + TWordListItem.WORD + " text )";
            }

            public static Uri getContentUri(long word_list_id) {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME_PREFIX + word_list_id);
            }
        }
    }
}

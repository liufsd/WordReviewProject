
package com.coleman.kingword.provider;

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

import android.net.Uri;
import android.provider.BaseColumns;

public class KingWord {
    public static final HashMap<String, HashMap<String, String>> maps = new HashMap<String, HashMap<String, String>>();

    public static final class TDict implements BaseColumns {
        // table name
        public static final String TABLE_NAME = "name";

        // fields
        public static final String DICT_INDEX_NAME = "index_name";

        public static final String DATE = "date";

        // create table sql
        public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" + _ID
                + " integer primary key autoincrement , " + DICT_INDEX_NAME + " text , " + DATE
                + " integer)";

        // default sort order
        public static final String DEFAULT_SORT_ORDER = DATE + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(DICT_INDEX_NAME, DICT_INDEX_NAME);
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

            public TDictIndex(String tableName) {
                // table name
                TABLE_NAME = TABLE_NAME_PREFIX + tableName;
                maps.put(TABLE_NAME, projectionMap);
            }

            public String toString() {
                return TABLE_NAME + "{ URI=" + getContentUri() + "}";
            }
        }
    }

    public static final class WordInfo implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "word_info";

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
        public static final String CREATE_TABLE_SQL = "create table " + WordInfo.TABLE_NAME + " ( "
                + WordInfo._ID + " integer primary key autoincrement , " + WordInfo.WORD
                + " text ," + WordInfo.IGNORE + " integer," + WordInfo.STUDY_COUNT + " integer,"
                + WordInfo.ERROR_COUNT + " integer," + WordInfo.WEIGHT + " integer,"
                + WordInfo.NEW_WORD + " integer," + WordInfo.REVIEW_TYPE + " integer,"
                + WordInfo.REVIEW_TIME + " integer )";

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

    public static final class Achievement implements BaseColumns {

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
        public static final String CREATE_TABLE_SQL = "create table " + Achievement.TABLE_NAME
                + " ( " + Achievement._ID + " integer primary key autoincrement , "
                + Achievement.TIME + " integer ," + Achievement.COUNT + " integer,"
                + Achievement.DESCRIBE + " text," + Achievement.SUBTYPE + " integer )";

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

    public static final class WordsList implements BaseColumns {

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
        public static final String CREATE_TABLE_SQL = "create table " + WordsList.TABLE_NAME
                + " ( " + WordsList._ID + " integer primary key autoincrement , "
                + WordsList.DESCRIBE + " text ," + WordsList.PATH_NAME + " text,"
                + WordsList.SET_METHOD + " integer , " + WORDLIST_NAME + " text )";

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

        public static final class SubWordsList implements BaseColumns {
            public static final String TABLE_NAME_PREFIX = "sub_word_list_";

            // table name
            public final String TABLE_NAME;

            // fields
            public static final String WORD_LIST_ID = "word_list_id";

            public static final String LEVEL = "level";

            // default sort order
            public static final String DEFAULT_SORT_ORDER = _ID + " asc";

            // projection map , used for query builder
            public static HashMap<String, String> projectionMap = new HashMap<String, String>();
            static {
                projectionMap.put(_ID, _ID);
                projectionMap.put(WORD_LIST_ID, WORD_LIST_ID);
                projectionMap.put(LEVEL, LEVEL);
            }

            public SubWordsList(String tableName) {
                TABLE_NAME = TABLE_NAME_PREFIX + tableName;
                maps.put(TABLE_NAME, projectionMap);
            }

            public Uri getContentUri() {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME);
            }

            public String getCreateTableSql() {
                return "create table if not exists " + TABLE_NAME + " ( " + SubWordsList._ID
                        + " integer primary key autoincrement , " + SubWordsList.WORD_LIST_ID
                        + " integer ," + SubWordsList.LEVEL + " integer )";
            }

        }

        public static final class WordListItem implements BaseColumns, Serializable {
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

            public WordListItem(String tableName) {
                TABLE_NAME = TABLE_NAME_PREFIX + tableName;
                maps.put(TABLE_NAME, projectionMap);
            }

            public Uri getContentUri() {
                return Uri.parse("content://" + KingWordProvider.AUTHORITY + File.separator
                        + TABLE_NAME);
            }

            public String getCreateTableSql() {
                return "create table if not exists " + TABLE_NAME + " ( " + WordListItem._ID
                        + " integer primary key autoincrement , " + WordListItem.SUB_WORD_LIST_ID
                        + " integer ," + WordListItem.WORD + " text )";
            }
        }
    }
}

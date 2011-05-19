
package com.coleman.kingword.provider;

/*
 * Name        : Pet.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Pet.java
 * Review      : 
 */
import java.io.File;
import java.util.HashMap;
import android.net.Uri;
import android.provider.BaseColumns;

public class KingWord {
    public static interface IDictIndex {
        // fields
        public static String WORD = "word";

        public static String OFFSET = "offset";

        public static String SIZE = "size";
    }

    public static final class StarDictIndex implements BaseColumns, IDictIndex {

        // table name
        public static final String TABLE_NAME = "star_dict_index";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

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
    }

    public static final class OxfordDictIndex implements BaseColumns, IDictIndex {

        // table name
        public static final String TABLE_NAME = "oxford_dict_index";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

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

        // default sort order
        public static final String DEFAULT_SORT_ORDER = _ID + " asc";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(_ID, _ID);
            projectionMap.put(DESCRIBE, DESCRIBE);
            projectionMap.put(PATH_NAME, PATH_NAME);
            projectionMap.put(SET_METHOD, SET_METHOD);
        }
    }

    public static final class SubWordsList implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "sub_word_list";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

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
    }

    public static final class WordListItem implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "word_list_item";

        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + KingWordProvider.AUTHORITY
                + File.separator + TABLE_NAME);

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
    }
}

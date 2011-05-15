
package com.coleman.dict.provider;

/*
 * Name        : Pet.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : Pet.java
 * Review      : 
 */
import java.util.HashMap;
import android.net.Uri;
import android.provider.BaseColumns;

public class Pet {
    public static final String AUTHORITY = "com.dbprovider.provider.Pet";

    public static final class Cat implements BaseColumns {
        // content uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cat");

        // table name
        public static final String TABLE_NAME = "cat";

        // fields
        public static final String ID = _ID;

        public static final String DEFAULT_SORT_ORDER = "name asc";

        public static final String NAME = "name";

        public static final String CREATED_DATE = "created";

        public static final String MODIFIED_DATE = "modified";

        public static final String CONTENT = "content";

        // projection map , used for query builder
        public static HashMap<String, String> projectionMap = new HashMap<String, String>();
        static {
            projectionMap.put(ID, ID);
            projectionMap.put(NAME, NAME);
            projectionMap.put(CREATED_DATE, CREATED_DATE);
            projectionMap.put(MODIFIED_DATE, MODIFIED_DATE);
        }
    }
}

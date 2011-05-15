
package com.coleman.dict.provider;

/*
 * Name        : CustomDBProvider.java
 * Author      : Coleman
 * Copyright   : Copyright (c) 2009-2012 CIeNET Ltd. All rights reserved
 * Description : CustomDBProvider.java
 * Review      : 
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.coleman.dict.provider.Pet;
import com.coleman.dict.provider.Pet.Cat;

public class PetProvider extends ContentProvider {
    PetHepler dbHelper;

    // db info
    private static final String DB_NAME = "pet.db";

    private static final int DB_VERSION = 1;

    // table cat match id
    private static final int CAT = 1;

    private static final int CAT_ID = 2;

    UriMatcher matcher;
    {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Pet.AUTHORITY, "cat", CAT);
        matcher.addURI(Pet.AUTHORITY, "cat/#", CAT_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new PetHepler(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // validate the requested uri
        if (matcher.match(uri) != CAT) {
            throw new IllegalArgumentException("Unknow uri: " + uri);
        }

        // make a copy of the values
        ContentValues v;
        if (values != null) {
            v = new ContentValues(values);
        } else {
            v = new ContentValues();
        }

        // make sure the fields are all set
        Long now = System.currentTimeMillis();
        if (!v.containsKey(Cat.NAME)) {
            v.put(Cat.NAME, "");
        }
        if (!v.containsKey(Cat.CREATED_DATE)) {
            v.put(Cat.CREATED_DATE, now);
        }
        if (!v.containsKey(Cat.MODIFIED_DATE)) {
            v.put(Cat.MODIFIED_DATE, now);
        }

        // store the data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(Cat.TABLE_NAME, null, v);
        if (rowId > 0) {
            Uri catUri = ContentUris.withAppendedId(Cat.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(catUri, null);
            return catUri;
        }
        throw new RuntimeException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case CAT:
                count = db.delete(Cat.TABLE_NAME, selection, selectionArgs);
                break;
            case CAT_ID:
                String catId = uri.getPathSegments().get(1);
                count = db.delete(Cat.TABLE_NAME,
                        CAT_ID + "=" + catId
                                + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ')'),
                        selectionArgs);
                break;
            default:
                // ignored
                break;
        }
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // set query builder for the query
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Cat.TABLE_NAME);
        qb.setProjectionMap(Cat.projectionMap);
        switch (matcher.match(uri)) {
            case CAT:
                break;
            case CAT_ID:
                qb.appendWhere(Cat.ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }

        // set order
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = Cat.DEFAULT_SORT_ORDER;
        }

        // start query
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // watch the uri for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (matcher.match(uri)) {
            case CAT:
                count = db.update(Cat.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CAT_ID:
                count = db.update(Cat.TABLE_NAME, values, Cat.ID + " = "
                        + uri.getPathSegments().get(1)
                        + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ')'),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow uri :" + uri);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    private static class PetHepler extends SQLiteOpenHelper {

        public PetHepler(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table " + Cat.TABLE_NAME + " ( " + Cat.ID
                    + " integer primary key autoincrement , " + Cat.NAME + " text ," + Cat.CONTENT
                    + " blob," + Cat.CREATED_DATE + " long ," + Cat.MODIFIED_DATE + " long )";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "drop table if exists " + Cat.TABLE_NAME;
            db.execSQL(sql);
            onCreate(db);
        }

    }
}

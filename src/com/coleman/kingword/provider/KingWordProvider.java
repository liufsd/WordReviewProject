
package com.coleman.kingword.provider;

import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.coleman.kingword.provider.KingWord.TDict;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.log.Log;
import com.coleman.util.Config;

public class KingWordProvider extends ContentProvider {
    private KingWordDBHepler dbHelper;

    public static final String AUTHORITY = "kingword";

    private static final String TAG = KingWordProvider.class.getName();

    private static Log Log = Config.getLog();

    @Override
    public boolean onCreate() {
        dbHelper = KingWordDBHepler.getInstance(getContext());
        return true;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = null;
        int count = 0;

        tableName = uri.getLastPathSegment();

        db.beginTransaction();
        for (ContentValues value : values) {
            db.insert(tableName, null, value);
            count++;
        }
        Log.d(TAG, "##################bulk insert count:" + count);
        db.setTransactionSuccessful();
        db.endTransaction();
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = uri.getLastPathSegment();

        // make a copy of the values
        ContentValues v;
        if (values != null) {
            v = new ContentValues(values);
        } else {
            v = new ContentValues();
        }

        // store the data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(tableName, null, v);
        if (rowId > 0) {
            Uri catUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(catUri, null);
            return catUri;
        }
        throw new RuntimeException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<String> list = uri.getPathSegments();
        String id = null;
        String tableName = null;
        if (list.size() > 1) {
            tableName = list.get(0);
            id = list.get(1);
        } else if (list.size() > 0) {
            tableName = list.get(0);
        }

        if (tableName != null && id != null) {
            // 1. delete dynamic created table TDictIndex, TWordListItem
            if (TDict.TABLE_NAME.equals(tableName)) {
                Cursor cursor = db.query(tableName, null, "_id = " + id, null, null, null, null);
                if (cursor.moveToFirst()) {
                    db.execSQL("drop table if exists " + TDictIndex.TABLE_NAME_PREFIX
                            + cursor.getString(cursor.getColumnIndex(TDict.DICT_DIR_NAME)));
                }
                cursor.close();
            } else if (TWordList.TABLE_NAME.equals(tableName)) {
                db.execSQL("drop table if exists " + TWordListItem.TABLE_NAME_PREFIX + id);
            }
            // 2. delete normal table
            count = db.delete(tableName, whereWithId(id, selection), null);
        } else if (tableName != null) {
            // 1. delete dynamic created tables TDictIndex, TWordListItem
            if (TDict.TABLE_NAME.equals(tableName)) {
                Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null,
                        null);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    db.execSQL("drop table if exists " + TDictIndex.TABLE_NAME_PREFIX
                            + cursor.getString(cursor.getColumnIndex(TDict.DICT_DIR_NAME)));
                }
                cursor.close();
            } else if (TWordList.TABLE_NAME.equals(tableName)) {
                Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null,
                        null);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    db.execSQL("drop table if exists " + TWordListItem.TABLE_NAME_PREFIX
                            + cursor.getString(cursor.getColumnIndex(TWordList._ID)));
                }
                cursor.close();
            }
            // 2. delete normal tables
            count = db.delete(tableName, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // set query builder for the query
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        List<String> list = uri.getPathSegments();
        String id = null;
        String tableName = null;
        if (list.size() > 1) {
            tableName = list.get(0);
            id = list.get(1);
        } else if (list.size() > 0) {
            tableName = list.get(0);
        }
        if (tableName != null && id != null) {
            qb.setTables(tableName);
            qb.setProjectionMap(getProjectionMap(tableName));
            qb.appendWhere(THistory._ID + "=" + id);

        } else if (tableName != null) {
            qb.setTables(tableName);
            qb.setProjectionMap(getProjectionMap(tableName));
        } else {
            throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        // set order
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = "_id asc";
        }

        // start query
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // watch the uri for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    private Map<String, String> getProjectionMap(String tableName) {
        return KingWord.maps.get(tableName);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<String> list = uri.getPathSegments();
        String id = null;
        String tableName = null;
        if (list.size() > 1) {
            tableName = list.get(0);
            id = list.get(1);
        } else if (list.size() > 0) {
            tableName = list.get(0);
        }

        if (tableName != null && id != null) {
            return db.update(tableName, values, THistory._ID + "=" + id, null);
        } else if (tableName != null) {
            return db.update(tableName, values, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknow uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder();
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }
}

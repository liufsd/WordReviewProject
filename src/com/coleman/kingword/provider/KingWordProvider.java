
package com.coleman.kingword.provider;

import java.io.File;
import java.util.Collection;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.coleman.util.Log;

import com.coleman.kingword.provider.DictIndexManager.DictIndexTable;
import com.coleman.kingword.provider.KingWord.Achievement;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.study.wordlist.model.WordList;

public class KingWordProvider extends ContentProvider {
    private KingWordDBHepler dbHelper;

    public static final String AUTHORITY = "kingword";

    // table cat match id

    private static final int URI_WORDINFO = 5;

    private static final int URI_WORDINFO_ID = 6;

    private static final int URI_ACHIEVEMENT = 7;

    private static final int URI_ACHIEVEMENT_ID = 8;

    private static final int URI_WORDLIST = 9;

    private static final int URI_WORDLIST_ID = 10;

    private static final int URI_SUB_WORDLIST = 11;

    private static final int URI_SUB_WORDLIST_ID = 12;

    private static final int URI_WORDLISTITEM = 13;

    private static final int URI_WORDLISTITEM_ID = 14;

    private static final String TAG = KingWordProvider.class.getName();

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        matcher.addURI(AUTHORITY, WordInfo.TABLE_NAME, URI_WORDINFO);
        matcher.addURI(AUTHORITY, WordInfo.TABLE_NAME + File.separator + "#", URI_WORDINFO_ID);

        matcher.addURI(AUTHORITY, Achievement.TABLE_NAME, URI_ACHIEVEMENT);
        matcher.addURI(AUTHORITY, Achievement.TABLE_NAME + File.separator + "#", URI_ACHIEVEMENT_ID);

        matcher.addURI(AUTHORITY, WordsList.TABLE_NAME, URI_WORDLIST);
        matcher.addURI(AUTHORITY, WordsList.TABLE_NAME + File.separator + "#", URI_WORDLIST_ID);

        matcher.addURI(AUTHORITY, SubWordsList.TABLE_NAME, URI_SUB_WORDLIST);
        matcher.addURI(AUTHORITY, SubWordsList.TABLE_NAME + File.separator + "#",
                URI_SUB_WORDLIST_ID);

        matcher.addURI(AUTHORITY, WordListItem.TABLE_NAME, URI_WORDLISTITEM);
        matcher.addURI(AUTHORITY, WordListItem.TABLE_NAME + File.separator + "#",
                URI_WORDLISTITEM_ID);

        updateUriMatcher(DictIndexManager.getInstance().getHashMap().values());

    }

    /**
     * Used for load dictionary dynamically
     * 
     * @param col
     */
    public static void updateUriMatcher(Collection<DictIndexTable> col) {
        for (DictIndexTable dictIndexTable : col) {
            matcher.addURI(AUTHORITY, dictIndexTable.TABLE_NAME, dictIndexTable.URI);
            matcher.addURI(AUTHORITY, dictIndexTable.TABLE_NAME, dictIndexTable.URI_ID);
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper = new KingWordDBHepler(getContext());
        return true;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = null;
        int count = 0;
        switch (matcher.match(uri)) {
            case URI_SUB_WORDLIST:
                tableName = SubWordsList.TABLE_NAME;
                break;
            case URI_WORDLISTITEM:
                tableName = WordListItem.TABLE_NAME;
                break;
            case URI_WORDINFO:
                tableName = WordInfo.TABLE_NAME;
                break;
            default:
                boolean find = false;
                Collection<DictIndexTable> col = DictIndexManager.getInstance().getHashMap()
                        .values();
                for (DictIndexTable dictIndexTable : col) {
                    if (uri.getLastPathSegment().equals(dictIndexTable.TABLE_NAME)) {
                        tableName = dictIndexTable.TABLE_NAME;
                        find = true;
                        break;
                    }
                }
                if (find) {
                    Log.d(TAG, "##################find the table");
                    break;
                }
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }
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
        // match the uri
        String tableName = null;
        switch (matcher.match(uri)) {
            case URI_WORDINFO:
                tableName = WordInfo.TABLE_NAME;
                break;
            case URI_ACHIEVEMENT:
                tableName = Achievement.TABLE_NAME;
                break;
            case URI_WORDLIST:
                tableName = WordsList.TABLE_NAME;
                break;
            case URI_SUB_WORDLIST:
                tableName = SubWordsList.TABLE_NAME;
                break;
            case URI_WORDLISTITEM:
                tableName = WordListItem.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }

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
        switch (matcher.match(uri)) {
            case URI_WORDLIST:
                count = db.delete(WordsList.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_WORDLIST_ID: {
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(WordsList.TABLE_NAME, whereWithId(rowId, selection), null);
                break;
            }
            case URI_SUB_WORDLIST:
                count = db.delete(SubWordsList.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_SUB_WORDLIST_ID: {
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(SubWordsList.TABLE_NAME, whereWithId(rowId, selection), null);
                break;
            }
            case URI_WORDINFO:
                count = db.delete(WordInfo.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_WORDINFO_ID: {
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(WordInfo.TABLE_NAME, whereWithId(rowId, selection), null);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // set query builder for the query
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (matcher.match(uri)) {
            case URI_WORDINFO:
                qb.setTables(WordInfo.TABLE_NAME);
                qb.setProjectionMap(WordInfo.projectionMap);
                break;
            case URI_WORDINFO_ID:
                qb.setTables(WordInfo.TABLE_NAME);
                qb.setProjectionMap(WordInfo.projectionMap);
                qb.appendWhere(WordInfo._ID + "=" + uri.getPathSegments().get(1));
                break;
            case URI_ACHIEVEMENT:
                qb.setTables(Achievement.TABLE_NAME);
                qb.setProjectionMap(Achievement.projectionMap);
                break;
            case URI_ACHIEVEMENT_ID:
                qb.setTables(Achievement.TABLE_NAME);
                qb.setProjectionMap(Achievement.projectionMap);
                qb.appendWhere(Achievement._ID + "=" + uri.getPathSegments().get(1));
                break;
            case URI_WORDLIST:
                qb.setTables(WordsList.TABLE_NAME);
                qb.setProjectionMap(WordsList.projectionMap);
                break;
            case URI_WORDLIST_ID:
                qb.setTables(WordsList.TABLE_NAME);
                qb.setProjectionMap(WordsList.projectionMap);
                qb.appendWhere(WordsList._ID + "=" + uri.getPathSegments().get(1));
                break;
            case URI_SUB_WORDLIST:
                qb.setTables(SubWordsList.TABLE_NAME);
                qb.setProjectionMap(SubWordsList.projectionMap);
                break;
            case URI_SUB_WORDLIST_ID:
                qb.setTables(SubWordsList.TABLE_NAME);
                qb.setProjectionMap(SubWordsList.projectionMap);
                qb.appendWhere(SubWordsList._ID + "=" + uri.getPathSegments().get(1));
                break;
            case URI_WORDLISTITEM:
                qb.setTables(WordListItem.TABLE_NAME);
                qb.setProjectionMap(WordListItem.projectionMap);
                break;
            case URI_WORDLISTITEM_ID:
                qb.setTables(WordListItem.TABLE_NAME);
                qb.setProjectionMap(WordListItem.projectionMap);
                qb.appendWhere(WordListItem._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                boolean find = false;
                Collection<DictIndexTable> col = DictIndexManager.getInstance().getHashMap()
                        .values();
                for (DictIndexTable dictIndexTable : col) {
                    List<String> list = uri.getPathSegments();
                    if (list.size() == 1) {
                        if (list.get(0).equals(dictIndexTable.TABLE_NAME)) {
                            qb.setTables(dictIndexTable.TABLE_NAME);
                            qb.setProjectionMap(DictIndexTable.projectionMap);
                            find = true;
                            break;
                        }
                    } else if (list.size() == 2) {
                        if (list.get(0).equals(dictIndexTable.TABLE_NAME)) {
                            qb.setTables(dictIndexTable.TABLE_NAME);
                            qb.setProjectionMap(DictIndexTable.projectionMap);
                            qb.appendWhere(DictIndexTable._ID + "=" + list.get(1));
                            find = true;
                            break;
                        }
                    }
                }
                if (find) {
                    break;
                }
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

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (uri.getLastPathSegment().equals("upgrade")) {
            Collection<DictIndexTable> dropList = DictIndexManager.getInstance().getDropList();
            for (DictIndexTable dictIndexTable : dropList) {
                db.execSQL("drop table if exists " + dictIndexTable.TABLE_NAME);
            }
            Collection<DictIndexTable> createList = DictIndexManager.getInstance().getCreateList();
            for (DictIndexTable dictIndexTable : createList) {
                db.execSQL(dictIndexTable.CREATE_TABLE_SQL);
            }
            return 0;
        }
        
        // store the data
        switch (matcher.match(uri)) {
            case URI_WORDINFO:
                return db.update(WordInfo.TABLE_NAME, values, selection, selectionArgs);
            case URI_WORDINFO_ID: {
                long id = Long.parseLong(uri.getPathSegments().get(1));
                return db.update(WordInfo.TABLE_NAME, values, WordInfo._ID + "=" + id, null);
            }
            case URI_SUB_WORDLIST:
                Log.d(TAG,
                        "sub word list updated!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
                                + values.getAsByte("level"));
                return db.update(SubWordsList.TABLE_NAME, values, selection, selectionArgs);
            case URI_SUB_WORDLIST_ID: {
                long id = Long.parseLong(uri.getPathSegments().get(1));
                return db.update(SubWordsList.TABLE_NAME, values, WordInfo._ID + "=" + id, null);
            }
            default:
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

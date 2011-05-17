
package com.coleman.kingword.provider;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.Achievement;
import com.coleman.kingword.provider.KingWord.OxfordDictIndex;
import com.coleman.kingword.provider.KingWord.StarDictIndex;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class KingWordProvider extends ContentProvider {
    private KingWordDBHepler dbHelper;

    public static final String AUTHORITY = "kingword";

    // table cat match id
    private static final int URI_STARDICT = 1;

    private static final int URI_STARDICT_ID = 2;

    private static final int URI_OXFORDDICT = 3;

    private static final int URI_OXFORDDICT_ID = 4;

    private static final int URI_WORDINFO = 5;

    private static final int URI_WORDINFO_ID = 6;

    private static final int URI_ACHIEVEMENT = 7;

    private static final int URI_ACHIEVEMENT_ID = 8;

    private static final int URI_WORDLIST = 9;

    private static final int URI_WORDLIST_ID = 10;

    private static final int URI_WORDLISTITEM = 11;

    private static final int URI_WORDLISTITEM_ID = 12;

    private static final String TAG = KingWordProvider.class.getName();

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        matcher.addURI(AUTHORITY, StarDictIndex.TABLE_NAME, URI_STARDICT);
        matcher.addURI(AUTHORITY, StarDictIndex.TABLE_NAME + File.separator + "#", URI_STARDICT_ID);

        matcher.addURI(AUTHORITY, OxfordDictIndex.TABLE_NAME, URI_OXFORDDICT);
        matcher.addURI(AUTHORITY, OxfordDictIndex.TABLE_NAME + File.separator + "#",
                URI_OXFORDDICT_ID);

        matcher.addURI(AUTHORITY, WordInfo.TABLE_NAME, URI_WORDINFO);
        matcher.addURI(AUTHORITY, WordInfo.TABLE_NAME + File.separator + "#", URI_WORDINFO_ID);

        matcher.addURI(AUTHORITY, Achievement.TABLE_NAME, URI_ACHIEVEMENT);
        matcher.addURI(AUTHORITY, Achievement.TABLE_NAME + File.separator + "#", URI_ACHIEVEMENT_ID);

        matcher.addURI(AUTHORITY, WordsList.TABLE_NAME, URI_WORDLIST);
        matcher.addURI(AUTHORITY, WordsList.TABLE_NAME + File.separator + "#", URI_WORDLIST_ID);

        matcher.addURI(AUTHORITY, WordListItem.TABLE_NAME, URI_WORDLISTITEM);
        matcher.addURI(AUTHORITY, WordListItem.TABLE_NAME + File.separator + "#",
                URI_WORDLISTITEM_ID);
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
            case URI_STARDICT:
                tableName = StarDictIndex.TABLE_NAME;
                break;
            case URI_OXFORDDICT:
                tableName = OxfordDictIndex.TABLE_NAME;
                break;
            case URI_WORDLISTITEM:
                tableName = WordListItem.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        db.beginTransaction();
        for (ContentValues value : values) {
            db.insert(tableName, null, value);
            count++;
        }
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
                count = db.delete(StarDictIndex.TABLE_NAME, selection, selectionArgs);
                break;
            case URI_WORDLIST_ID:
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(StarDictIndex.TABLE_NAME, whereWithId(rowId, selection), null);
                break;
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
            case URI_STARDICT:
                qb.setTables(StarDictIndex.TABLE_NAME);
                qb.setProjectionMap(StarDictIndex.projectionMap);
                break;
            case URI_STARDICT_ID:
                qb.setTables(StarDictIndex.TABLE_NAME);
                qb.setProjectionMap(StarDictIndex.projectionMap);
                qb.appendWhere(StarDictIndex._ID + "=" + uri.getPathSegments().get(1));
                break;
            case URI_OXFORDDICT:
                qb.setTables(OxfordDictIndex.TABLE_NAME);
                qb.setProjectionMap(OxfordDictIndex.projectionMap);
                break;
            case URI_OXFORDDICT_ID:
                qb.setTables(OxfordDictIndex.TABLE_NAME);
                qb.setProjectionMap(OxfordDictIndex.projectionMap);
                qb.appendWhere(OxfordDictIndex._ID + "=" + uri.getPathSegments().get(1));
                break;
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
        throw new IllegalArgumentException("Unknow uri :" + uri);
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

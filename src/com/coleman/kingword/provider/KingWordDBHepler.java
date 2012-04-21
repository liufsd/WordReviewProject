
package com.coleman.kingword.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coleman.kingword.provider.KingWord.Achievement;
import com.coleman.kingword.provider.KingWord.TDict;
import com.coleman.kingword.provider.KingWord.History;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.util.Log;

public class KingWordDBHepler extends SQLiteOpenHelper {
    private static final String DB_NAME = "kingword.db";

    private static final String TAG = KingWordDBHepler.class.getName();

    private static final int DB_VERSION = 1;

    public KingWordDBHepler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL(TDict.CREATE_TABLE_SQL);
        db.execSQL(History.CREATE_TABLE_SQL);
        db.execSQL(Achievement.CREATE_TABLE_SQL);
        db.execSQL(WordsList.CREATE_TABLE_SQL);
        Log.d(TAG, "=========================db onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop table
        db.execSQL("drop table if exists " + TDict.TABLE_NAME);
        db.execSQL("drop table if exists " + History.TABLE_NAME);
        db.execSQL("drop table if exists " + Achievement.TABLE_NAME);
        db.execSQL("drop table if exists " + WordsList.TABLE_NAME);
        // create table
        onCreate(db);
        Log.d(TAG, "=========================db onUpgrade");
    }
}

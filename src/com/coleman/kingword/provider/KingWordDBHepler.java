
package com.coleman.kingword.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coleman.kingword.provider.KingWord.TAchievement;
import com.coleman.kingword.provider.KingWord.TDict;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.util.Log;

public class KingWordDBHepler extends SQLiteOpenHelper {
    private static final String DB_NAME = "kingword.db";

    private static final String TAG = "KingWordDBHepler";

    private static final int DB_VERSION = 1;

    private static KingWordDBHepler instance;

    public static KingWordDBHepler getInstance(Context context) {
        if (instance == null) {
            instance = new KingWordDBHepler(context);
        }
        return instance;
    }

    private KingWordDBHepler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL(TDict.CREATE_TABLE_SQL);
        db.execSQL(THistory.CREATE_TABLE_SQL);
        db.execSQL(TAchievement.CREATE_TABLE_SQL);
        db.execSQL(TWordList.CREATE_TABLE_SQL);
        db.execSQL(TSubWordList.CREATE_TABLE_SQL);

        // create trigger
        // handle delete word list
        db.execSQL("CREATE TRIGGER sub_list AFTER DELETE ON " + TWordList.TABLE_NAME + " " + "BEGIN "
                + "  DELETE FROM " + TSubWordList.TABLE_NAME + "  WHERE "
                + TSubWordList.WORD_LIST_ID + "=old._id;" + "END;");
        
        Log.d(TAG, "=========================db onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop table
        db.execSQL("drop table if exists " + TDict.TABLE_NAME);
        db.execSQL("drop table if exists " + THistory.TABLE_NAME);
        db.execSQL("drop table if exists " + TAchievement.TABLE_NAME);
        db.execSQL("drop table if exists " + TWordList.TABLE_NAME);
        db.execSQL("drop table if exists " + TSubWordList.TABLE_NAME);
        // create table
        onCreate(db);
        Log.d(TAG, "=========================db onUpgrade");
    }
}

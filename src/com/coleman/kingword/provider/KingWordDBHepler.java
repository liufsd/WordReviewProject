
package com.coleman.kingword.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coleman.kingword.provider.KingWord.Achievement;
import com.coleman.kingword.provider.KingWord.BabylonEnglishIndex;
import com.coleman.kingword.provider.KingWord.OxfordDictIndex;
import com.coleman.kingword.provider.KingWord.StarDictIndex;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.provider.KingWord.WordsList;

public class KingWordDBHepler extends SQLiteOpenHelper {
    private static final String DB_NAME = "kingword.db";

    private static final int DB_VERSION = 1;

    public KingWordDBHepler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static final String star_dict_index_table = "create table " + StarDictIndex.TABLE_NAME
            + " ( " + StarDictIndex._ID + " integer primary key autoincrement , "
            + StarDictIndex.WORD + " text ," + StarDictIndex.OFFSET + " integer,"
            + StarDictIndex.SIZE + " integer )";

    private static final String babylon_english_index_table = "create table "
            + BabylonEnglishIndex.TABLE_NAME + " ( " + BabylonEnglishIndex._ID
            + " integer primary key autoincrement , " + BabylonEnglishIndex.WORD + " text ,"
            + BabylonEnglishIndex.OFFSET + " integer," + BabylonEnglishIndex.SIZE + " integer )";

    private static final String oxford_dict_index_table = "create table "
            + OxfordDictIndex.TABLE_NAME + " ( " + OxfordDictIndex._ID
            + " integer primary key autoincrement , " + OxfordDictIndex.WORD + " text ,"
            + OxfordDictIndex.OFFSET + " integer," + OxfordDictIndex.SIZE + " integer )";

    private static final String word_info_table = "create table " + WordInfo.TABLE_NAME + " ( "
            + WordInfo._ID + " integer primary key autoincrement , " + WordInfo.WORD + " text ,"
            + WordInfo.IGNORE + " integer," + WordInfo.STUDY_COUNT + " integer,"
            + WordInfo.ERROR_COUNT + " integer," + WordInfo.WEIGHT + " integer,"
            + WordInfo.NEW_WORD + " integer," + WordInfo.REVIEW_TYPE + " integer,"
            + WordInfo.REVIEW_TIME + " integer )";

    private static final String achievement_table = "create table " + Achievement.TABLE_NAME
            + " ( " + Achievement._ID + " integer primary key autoincrement , " + Achievement.TIME
            + " integer ," + Achievement.COUNT + " integer," + Achievement.DESCRIBE + " text,"
            + Achievement.SUBTYPE + " integer )";

    private static final String word_list_table = "create table " + WordsList.TABLE_NAME + " ( "
            + WordsList._ID + " integer primary key autoincrement , " + WordsList.DESCRIBE
            + " text ," + WordsList.PATH_NAME + " text," + WordsList.SET_METHOD + " integer )";

    private static final String sub_word_list_table = "create table " + SubWordsList.TABLE_NAME
            + " ( " + SubWordsList._ID + " integer primary key autoincrement , "
            + SubWordsList.WORD_LIST_ID + " integer ," + SubWordsList.LEVEL + " integer )";

    private static final String word_list_item_table = "create table " + WordListItem.TABLE_NAME
            + " ( " + WordListItem._ID + " integer primary key autoincrement , "
            + WordListItem.SUB_WORD_LIST_ID + " integer ," + WordListItem.WORD + " text )";

    private static final String delete_sub_word_list_trigger = "CREATE TRIGGER delete_sub_word_list_trigger AFTER DELETE ON "
            + WordsList.TABLE_NAME
            + " for each row BEGIN "
            + " DELETE FROM "
            + SubWordsList.TABLE_NAME
            + " WHERE "
            + SubWordsList.WORD_LIST_ID
            + "=OLD."
            + WordsList._ID + "; END;";

    private static final String delete_word_list_item_trigger = "CREATE TRIGGER delete_word_list_item_trigger AFTER DELETE ON "
            + SubWordsList.TABLE_NAME
            + " for each row BEGIN "
            + " DELETE FROM "
            + WordListItem.TABLE_NAME
            + " WHERE "
            + WordListItem.SUB_WORD_LIST_ID
            + "=OLD."
            + SubWordsList._ID + "; END;";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(star_dict_index_table);
        db.execSQL(babylon_english_index_table);
        db.execSQL(oxford_dict_index_table);
        db.execSQL(word_info_table);
        db.execSQL(achievement_table);
        db.execSQL(word_list_table);
        db.execSQL(sub_word_list_table);
        db.execSQL(word_list_item_table);
        db.execSQL(delete_sub_word_list_trigger);
        db.execSQL(delete_word_list_item_trigger);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + StarDictIndex.TABLE_NAME);
        db.execSQL("drop table if exists " + BabylonEnglishIndex.TABLE_NAME);
        db.execSQL("drop table if exists " + OxfordDictIndex.TABLE_NAME);
        db.execSQL("drop table if exists " + WordInfo.TABLE_NAME);
        db.execSQL("drop table if exists " + Achievement.TABLE_NAME);
        db.execSQL("drop table if exists " + WordsList.TABLE_NAME);
        db.execSQL("drop table if exists " + SubWordsList.TABLE_NAME);
        db.execSQL("drop table if exists " + WordListItem.TABLE_NAME);
        db.execSQL("drop trigger if exists delete_sub_word_list_trigger");
        db.execSQL("drop trigger if exists delete_word_list_item_trigger");
        onCreate(db);
    }

}

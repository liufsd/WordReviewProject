
package com.coleman.kingword.provider.upgrade.version6;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.provider.KingWordDBHepler;
import com.coleman.kingword.provider.upgrade.Task;
import com.coleman.util.MyApp;

public class Task_v6 implements Task {

    @Override
    public void execute() {
        SQLiteDatabase db = KingWordDBHepler.getInstance(MyApp.context).getWritableDatabase();
        Cursor cursor = MyApp.context.getContentResolver().query(KingWord_v6.TWordList.CONTENT_URI,
                new String[] {
                    "_id"
                }, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String drop_sql = "drop table if exists "
                    + KingWord_v6.TWordList.TWordListItem.TABLE_NAME_PREFIX + id;
            db.execSQL(drop_sql);
            MyApp.context.getContentResolver().delete(KingWord_v6.TWordList.CONTENT_URI,
                    TWordList._ID + " = " + id, null);
        }
        cursor.close();

        db.execSQL("drop table if exists " + KingWord_v6.TWordList.TABLE_NAME);
        db.execSQL("drop table if exists " + KingWord_v6.TSubWordList.TABLE_NAME);
        db.execSQL(TWordList.CREATE_TABLE_SQL);
        db.execSQL(TSubWordList.CREATE_TABLE_SQL);
        db.execSQL("CREATE TRIGGER list_trigger AFTER DELETE ON " + TWordList.TABLE_NAME + " "
                + "BEGIN " + "  DELETE FROM " + TSubWordList.TABLE_NAME + "  WHERE "
                + TSubWordList.WORD_LIST_ID + "=old._id;" + "END;");
    }
}

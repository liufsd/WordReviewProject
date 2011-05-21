
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class SubWordList {
    public long id;

    public long word_list_id = -1;

    public int level = 0;

    private ArrayList<String> list = new ArrayList<String>();

    private static final String projection[] = new String[] {
        WordListItem.WORD
    };

    private static final String TAG = SubWordList.class.getName();

    /**
     * the pointer of the word index in the sublist.
     */
    private int p;

    /**
     * only used for WordListManager, don't call this constructor anywhere else.
     */
    SubWordList(long word_list_id) {
        this.word_list_id = word_list_id;
    }

    public SubWordList(Context context, long sub_id) {
        load(context, sub_id);
    }

    public SubWordList() {
        list.clear();
        p = 0;
    }

    private void load(Context context, long sub_id) {
        long time = System.currentTimeMillis();
        Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI, projection,
                WordListItem.SUB_WORD_LIST_ID + "=" + sub_id, null, null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                list.add(c.getString(0));
                c.moveToNext();
            }
        }
        if (c != null) {
            c.close();
            c = null;
        }
        time = System.currentTimeMillis() - time;
        Log.d(TAG, "Load sub-wordlist cost time: " + time);
    }

    public String getWord() {
        return list.get(p);
    }

    public String getPre() {
        p = p - 1 < 0 ? 0 : p - 1;
        return list.get(p);
    }

    public String getNext() {
        p = p + 1 > list.size() - 1 ? list.size() - 1 : p + 1;
        return list.get(p);
    }

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(SubWordsList.WORD_LIST_ID, word_list_id);
        value.put(SubWordsList.LEVEL, level);
        return value;
    }
}


package com.coleman.kingword.wordlist.model;

import android.content.ContentValues;

import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;

public class WordListItem {
    public long id;

    public long sub_wordlist_id;

    public String word;

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(TWordListItem.WORD, word);
        cv.put(TWordListItem.SUB_WORD_LIST_ID, sub_wordlist_id);
        return cv;
    }
}

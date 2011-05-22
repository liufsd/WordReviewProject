
package com.coleman.kingword.wordlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class SubWordList {
    public long id;

    public long word_list_id = -1;

    public int level = 0;

    private ArrayList<WordItem> list = new ArrayList<WordItem>();

    private static final String projection[] = new String[] {
            WordListItem.WORD, WordListItem.IGNORE, WordListItem._ID
    };

    private static final String TAG = SubWordList.class.getName();

    /**
     * the pointer of the word index in the sublist.
     */
    private int p;

    /**
     * count the number of passed word, ignored will be passed, or meanwhile
     * passView, passAlternative, and passMultiple of WordItem will also passed.
     */
    static int passViewCount;

    static int passAltCount;

    static int passMulCount;

    /**
     * Used to indicate the progress bar
     */
    static boolean viewOver;

    private Random ran = new Random();

    /**
     * only used for WordListManager, don't call this constructor anywhere else.
     */
    SubWordList(long word_list_id) {
        this.word_list_id = word_list_id;
    }

    public SubWordList(Context context, long sub_id) {
        passViewCount = 0;
        passAltCount = 0;
        passMulCount = 0;
        viewOver = false;
        load(context, sub_id);
    }

    private void load(Context context, long sub_id) {
        long time = System.currentTimeMillis();
        Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI, projection,
                WordListItem.SUB_WORD_LIST_ID + "=" + sub_id, null, null);
        if (c.moveToFirst()) {
            WordItem item;
            while (!c.isAfterLast()) {
                item = new WordItem();
                item.word = c.getString(0);
                item.ignore = c.getShort(1) == 2 ? true : false;
                item.id = c.getLong(2);
                list.add(item);
                if (item.ignore) {
                    passViewCount++;
                    passAltCount++;
                    passMulCount++;
                }
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

    public int getProgress() {
        Log.d(TAG, "p:" + p + "  list.size:" + list.size());
        if (list.size() == 0) {
            return 0;
        } else if (list.size() == 1) {
            return 100;
        }

        if (isComplete()) {
            return 100;
        }
        return (passViewCount + passAltCount + passMulCount) * 33 / list.size();
    }

    public boolean isComplete() {
        if (list.size() == 0) {
            return true;
        }
        if (passMulCount == list.size()) {
            return true;
        }
        return false;
    }

    public boolean hasNext() {
        if (passMulCount < list.size()) {
            return true;
        }
        return false;
    }

    public ArrayList<DictData> getDictData(Context context, WordItem item) {
        ArrayList<DictData> datalist = new ArrayList<DictData>();
        if (!item.passView) {
            datalist.add(item.getDictData(context));
        } else if (!item.passAlternative) {
            datalist.add(item.getDictData(context));
            datalist.add(getRandomDictData(context));
        } else if (!item.passMultiple) {
            datalist.add(item.getDictData(context));
            datalist.add(getRandomDictData(context));
            datalist.add(getRandomDictData(context));
            datalist.add(getRandomDictData(context));
        }
        shuffle(datalist);
        return datalist;
    }

    private void shuffle(ArrayList<DictData> list) {
        if (list.size() <= 1) {
            return;
        } else {
            Collections.shuffle(list);
        }
    }

    private DictData getRandomDictData(Context context) {
        int index = ran.nextInt(list.size());
        if (index == p) {
            index = p + 1 > list.size() - 1 ? 0 : p + 1;
        }
        return list.get(index).getDictData(context);
    }

    /**
     * call the method must make sure that isComplete is called first.
     */
    public WordItem getCurrentWord() {
        return list.get(p);
    }

    /**
     * call this method must make sure that hasNext is called first!!!
     */
    public WordItem getNext() {
        if (isComplete()) {
            return getCurrentWord();
        }
        p = p + 1 > list.size() - 1 ? 0 : p + 1;
        if (!viewOver && p == list.size() - 1) {
            viewOver = true;
        }
        WordItem item = list.get(p);
        if (!item.isComplete()) {
            return item;
        } else {
            return getNext();
        }
    }

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(SubWordsList.WORD_LIST_ID, word_list_id);
        value.put(SubWordsList.LEVEL, level);
        return value;
    }

    public void ignore(Context context, WordItem word) {
        word.ignore = true;
        context.getContentResolver().update(WordListItem.CONTENT_URI, word.toContentValues(),
                WordListItem._ID + "=" + word.id, null);
    }
}

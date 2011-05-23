
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
            WordListItem.WORD, WordListItem._ID
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
     * count the total number of the error occuring times.
     */
    static int errorCount;

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
        errorCount = 0;
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
                item.id = c.getLong(1);
                list.add(item);
                item.loadInfo(context);
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

        if (allComplete()) {
            return 100;
        }
        return (passViewCount + passAltCount + passMulCount) * 33 / list.size();
    }

    public boolean allComplete() {
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
            if (list.size() > 1) {
                addRandomDictData(context, datalist);
            }
        } else if (!item.passMultiple) {
            datalist.add(item.getDictData(context));
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (i > 2) {
                    break;
                } else {
                    addRandomDictData(context, datalist);
                }
            }
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

    private void addRandomDictData(Context context, ArrayList<DictData> datalist) {
        int index = ran.nextInt(list.size());
        int i = 0;
        while (datalist.contains(list.get(index).getDictData(context))) {
            index = p + 1 > list.size() - 1 ? 0 : p + 1;
            i++;
            // to avoid deadlock if there are two or more same words.
            if (i == list.size()) {
                break;
            }
        }
        datalist.add(list.get(index).getDictData(context));
    }

    /**
     * call the method must make sure that isComplete is called first.
     */
    public WordItem getCurrentWord() {
        if (allComplete()) {
            return list.get(p);
        }
        WordItem item = list.get(p);
        if (!item.isComplete()) {
            return item;
        } else {
            return getNext();
        }
    }

    /**
     * call this method must make sure that hasNext is called first!!!
     */
    public WordItem getNext() {
        if (allComplete()) {
            return list.get(p);
        }
        p = p + 1 > list.size() - 1 ? 0 : p + 1;
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

    public void computeStudyResult(Context context) {
        int tmp = 0;
        if (errorCount < list.size() * 4 / 10) {
            tmp = 1;
        } else if (errorCount < list.size() * 2 / 10) {
            tmp = 2;
        } else if (errorCount < list.size() * 98 / 100) {
            tmp = 3;
        }
        if (tmp > level) {
            level = tmp;
        }
        update(context);
    }

    private void update(Context context) {
        ContentValues values = toContentValues();
        context.getContentResolver().update(SubWordsList.CONTENT_URI, values,
                SubWordsList._ID + "=" + id, null);
    }
}

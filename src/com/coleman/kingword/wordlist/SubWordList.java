
package com.coleman.kingword.wordlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class SubWordList {
    private SubInfo subinfo;

    private ArrayList<WordItem> list;

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

    public SubWordList(SubInfo info) {
        subinfo = info;
        list = new ArrayList<WordItem>();
        passViewCount = 0;
        passAltCount = 0;
        passMulCount = 0;
        errorCount = 0;
    }

    public void load(Context context) {
        long time = System.currentTimeMillis();
        Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI, projection,
                WordListItem.SUB_WORD_LIST_ID + "=" + subinfo.id, null, null);
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
        for (WordItem item : list) {
            if (!item.isComplete()) {
                return false;
            }
        }
        return true;
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
            index = index + 1 > list.size() - 1 ? 0 : index + 1;
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

    public String computeStudyResult(Context context) {
        int tmp = 0;
        String levels = context.getString(R.string.history_level);
        if (errorCount <= list.size() * 5 / 100) {
            tmp = 4;
        } else if (errorCount <= list.size() * 2 / 10) {
            tmp = 3;
        } else if (errorCount <= list.size() * 4 / 10) {
            tmp = 2;
        } else {
            tmp = 1;
        }
        if (tmp > subinfo.level) {
            subinfo.level = tmp;
            update(context);
            switch (subinfo.level) {
                case 1:
                    levels = context.getString(R.string.unpass_unit);
                    break;
                case 2:
                    levels = context.getString(R.string.low_level);
                    break;
                case 3:
                    levels = context.getString(R.string.mid_level);
                    break;
                case 4:
                    levels = context.getString(R.string.high_level);
                    break;
                default:// ignore
                    break;
            }
        } else if (tmp == 4) {
            levels = context.getString(R.string.high_level);
        }
        return levels;
    }

    public int getCorrectPercentage() {
        if (list.size() == 0) {
            return 0;
        }
        return 100 - errorCount * 100 / list.size();
    }

    private void update(Context context) {
        ContentValues values = subinfo.toContentValues();
        int num = context.getContentResolver().update(SubWordsList.CONTENT_URI, values,
                SubWordsList._ID + "=" + subinfo.id, null);
        Log.d(TAG, "sub word list update num: " + num);
    }

    /**
     * For map the index to the id storing in the database.
     */
    public static class SubInfo implements Parcelable {
        public SubInfo(long id, int index, int level, long wordlist_id) {
            this.id = id;
            this.index = index;
            this.level = level;
            this.word_list_id = wordlist_id;
        }

        SubInfo(long word_list_id) {
            this.word_list_id = word_list_id;
        }

        public int index;

        // field
        public long id;

        public long word_list_id;

        public int level;

        public ContentValues toContentValues() {
            ContentValues value = new ContentValues();
            value.put(SubWordsList.WORD_LIST_ID, word_list_id);
            value.put(SubWordsList.LEVEL, level);
            return value;
        }

        @Override
        public String toString() {
            return "id:" + id + "  index:" + index + " level:" + level + " word_list_id:"
                    + word_list_id;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Implement the Parcelable interface.
         * 
         * @hide
         */
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(word_list_id);
            dest.writeInt(level);
            dest.writeInt(index);
        }

        /**
         * Implement the Parcelable interface.
         * 
         * @hide
         */
        public static final Creator<SubInfo> CREATOR = new Creator<SubInfo>() {
            public SubInfo createFromParcel(Parcel in) {
                long id = in.readLong();
                long word_list_id = in.readLong();
                int level = in.readInt();
                int index = in.readInt();
                return new SubInfo(id, index, level, word_list_id);
            }

            public SubInfo[] newArray(int size) {
                return new SubInfo[size];
            }
        };
    }
}

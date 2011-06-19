
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordinfo.WordInfoVO;

public class SliceWordList {
    private SubInfo subinfo;

    private ArrayList<WordItem> list;

    private static final String projection[] = new String[] {
            WordListItem.WORD, WordListItem._ID
    };

    private static final String TAG = SliceWordList.class.getName();

    /**
     * the pointer of the word index in the sublist.
     */
    private int p;

    /**
     * count the number of passed word, ignored will be passed, or meanwhile
     * passView, passAlternative, and passMultiple of WordItem will also passed.
     */
    int passViewCount;

    int passMulCount;

    /**
     * count the total number of the error occuring times.
     */
    int errorCount;

    int totalCount;

    byte listType;

    public static final byte NULL_TYPE = -1;

    public static final byte SUB_WORD_LIST = 1;

    public static final byte REVIEW_LIST = 2;

    public static final byte SCAN_LIST = 3;

    public static final byte NEW_WORD_BOOK_LIST = 4;

    public SliceWordList(SubInfo info) {
        listType = SUB_WORD_LIST;
        subinfo = info;
        list = new ArrayList<WordItem>();
        passViewCount = 0;
        passMulCount = 0;
        errorCount = 0;
    }

    public SliceWordList(byte type) {
        listType = type;
        list = new ArrayList<WordItem>();
        passViewCount = 0;
        passMulCount = 0;
        errorCount = 0;
    }

    public void loadWordList(Context context) {
        switch (listType) {
            case SUB_WORD_LIST:
                loadSubList(context);
                break;
            case NEW_WORD_BOOK_LIST:
            case SCAN_LIST:
                loadInfoList(context);
                break;
            default:
                break;
        }
    }

    public void loadReviewWordList(Context context) {
        // reset the slice word list
        reset();

        ArrayList<WordInfoVO> infoList = WordInfoHelper.getWordInfoList(context, REVIEW_LIST);
        WordItem item;
        for (WordInfoVO info : infoList) {
            item = new WordItem(this);
            item.word = info.word;
            item.info = info;
            list.add(item);
            Log.d(TAG, "item:" + item + " status:" + item.getCurrentStatus());
        }
        infoList.clear();
    }

    /**
     * Could only be invoked by loadReviewWordList
     */
    private void reset() {
        list.clear();
        passViewCount = 0;
        passMulCount = 0;
        errorCount = 0;
        p = 0;
    }

    private void loadInfoList(Context context) {
        ArrayList<WordInfoVO> infoList = WordInfoHelper.getWordInfoList(context, listType);
        WordItem item;
        for (WordInfoVO info : infoList) {
            item = new WordItem(this);
            item.word = info.word;
            item.info = info;
            list.add(item);
            Log.d(TAG, "item:" + item);
        }
        infoList.clear();
    }

    private void loadSubList(Context context) {
        long time = System.currentTimeMillis();
        Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI, projection,
                WordListItem.SUB_WORD_LIST_ID + "=" + subinfo.id, null, null);
        if (c.moveToFirst()) {
            WordItem item;
            while (!c.isAfterLast()) {
                item = new WordItem(this);
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
        return (passViewCount + passMulCount) * 50 / list.size();
    }

    public boolean allComplete() {
        if (list.size() == 0) {
            return true;
        }
        if (totalCount < list.size()) {
            return false;
        }
        return true;
    }

    public ArrayList<DictData> getDictData(Context context, WordItem item) {
        return item.getDictData(context, list);
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
            // if the next word is already completed, then try to find the next
            // word after index of p
            int tmp = p + 1 > list.size() - 1 ? 0 : p + 1;
            for (int i = tmp; i < list.size(); i++) {
                WordItem it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            // if not found next word after index p, then try to find the next
            // word from the index 0
            for (int i = 0; i < tmp; i++) {
                WordItem it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            return list.get(tmp);
        }
    }

    public String computeSubListStudyResult(Context context) {
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

    public int getCount() {
        return list.size();
    }

}

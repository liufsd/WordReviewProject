
package com.coleman.kingword.wordlist.model;

import java.io.Serializable;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.coleman.kingword.provider.KingWord.TSubWordList;

public class SubWordList implements Parcelable, Serializable {
    private static final long serialVersionUID = 911690030424514203L;

    public SubWordList(long word_list_id) {
        this.word_list_id = word_list_id;
    }

    public long id;

    public long word_list_id;

    public int count_down;

    public int history_level = -1;

    public int level = -1;

    public String method;

    public int position;

    public int progress;

    public int error_count;

    // local used
    public int index;

    public int screenIndex;

    /**
     * return the values of SubWordList
     * 
     * @notice sqlite 在创建表的SQL语句中使用default关键字不起作用，因此需要在表对应的值对象处设置默认值。
     * @return
     */
    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(TSubWordList.WORD_LIST_ID, word_list_id);
        value.put(TSubWordList.COUNT_DOWN, count_down);
        value.put(TSubWordList.HISTORY_LEVEL, history_level);
        value.put(TSubWordList.LEVEL, level);
        value.put(TSubWordList.METHOD, method);
        value.put(TSubWordList.POSITION, position);
        value.put(TSubWordList.PROGRESS, progress);
        value.put(TSubWordList.ERROR_COUNT, error_count);
        return value;
    }

    @Override
    public String toString() {
        return "id:" + id + "  index:" + index + " level:" + level + " word_list_id:"
                + word_list_id + " count_down:" + count_down;

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
        dest.writeInt(count_down);
        dest.writeInt(history_level);
        dest.writeInt(level);
        dest.writeString(method);
        dest.writeInt(position);
        dest.writeInt(progress);
        dest.writeInt(error_count);

        dest.writeInt(index);
        dest.writeInt(screenIndex);
    }

    /**
     * Implement the Parcelable interface.
     * 
     * @hide
     */
    public static final Creator<SubWordList> CREATOR = new Creator<SubWordList>() {
        public SubWordList createFromParcel(Parcel in) {
            long id = in.readLong();
            long word_list_id = in.readLong();
            int count_down = in.readInt();
            int history_level = in.readInt();
            int level = in.readInt();
            String method = in.readString();
            int position = in.readInt();
            int progress = in.readInt();
            int error_count = in.readInt();

            int index = in.readInt();
            int screenIndex = in.readInt();
            SubWordList swl = new SubWordList(word_list_id);
            swl.id = id;
            swl.word_list_id = word_list_id;
            swl.count_down = count_down;
            swl.history_level = history_level;
            swl.level = level;
            swl.method = method;
            swl.position = position;
            swl.progress = progress;
            swl.error_count = error_count;
            swl.index = index;
            swl.screenIndex = screenIndex;
            return swl;
        }

        public SubWordList[] newArray(int size) {
            return new SubWordList[size];
        }
    };

}

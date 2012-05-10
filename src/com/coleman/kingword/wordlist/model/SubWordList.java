
package com.coleman.kingword.wordlist.model;

import java.io.Serializable;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.coleman.kingword.provider.KingWord.TSubWordList;

public class SubWordList implements Parcelable, Serializable {
    private static final long serialVersionUID = 911690030424514203L;

    public SubWordList(long id, int index, int level, long wordlist_id) {
        this.id = id;
        this.index = index;
        this.level = level;
        this.word_list_id = wordlist_id;
    }

    public SubWordList(long word_list_id) {
        this.word_list_id = word_list_id;
    }

    public long id;

    public long word_list_id;

    public int level;

    public int index;

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(TSubWordList.WORD_LIST_ID, word_list_id);
        value.put(TSubWordList.LEVEL, level);
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
    public static final Creator<SubWordList> CREATOR = new Creator<SubWordList>() {
        public SubWordList createFromParcel(Parcel in) {
            long id = in.readLong();
            long word_list_id = in.readLong();
            int level = in.readInt();
            int index = in.readInt();
            return new SubWordList(id, index, level, word_list_id);
        }

        public SubWordList[] newArray(int size) {
            return new SubWordList[size];
        }
    };

}

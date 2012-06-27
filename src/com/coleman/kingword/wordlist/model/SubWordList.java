
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

    public int level;

    public String method;

    public int itemIndexInLoop;

    // used for SlideTableSwitcher
    public int index;

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(TSubWordList.WORD_LIST_ID, word_list_id);
        value.put(TSubWordList.LEVEL, level);
        value.put(TSubWordList.METHOD, method);
        value.put(TSubWordList.POSITION, itemIndexInLoop);
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
        dest.writeString(method);
        dest.writeInt(itemIndexInLoop);
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
            String method = in.readString();
            int loopIndex = in.readInt();
            int itemIndexInLoop = in.readInt();
            SubWordList swl = new SubWordList(word_list_id);
            swl.id = id;
            swl.level = level;
            swl.index = index;
            swl.method = method;
            swl.itemIndexInLoop = itemIndexInLoop;
            return swl;
        }

        public SubWordList[] newArray(int size) {
            return new SubWordList[size];
        }
    };

}

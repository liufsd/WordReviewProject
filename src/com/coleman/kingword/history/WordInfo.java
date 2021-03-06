
package com.coleman.kingword.history;

import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.THistory;
import com.coleman.log.Log;
import com.coleman.util.Config;

/**
 * Should only be used by WordItem and WordInfoHelper.
 */
public class WordInfo implements Parcelable, Serializable {
    private static final long serialVersionUID = 3683192721924884338L;

    private static Log Log = Config.getLog();

    public static final byte MIN_WEIGHT = 0;

    public static final byte MAX_WEIGHT = 15;

    private static final String TAG = WordInfo.class.getName();

    public long id = -1;

    public String word = "initial";

    /**
     * 2 true 1 false
     */
    public boolean ignore = false;

    public byte studycount = 0;

    public byte errorcount = 0;

    /**
     * 如果一个单词经历两轮学习复习，且未有错误发生时weight会降为零，这时ignore会自动置为true。 每一轮依次为REVIEW_1_HOUR,
     * REVIEW_12_HOUR, REVIEW_1_DAY, REVIEW_5_DAY, REVIEW_20_DAY, REVIEW_40_DAY,
     * REVIEW_60_DAY, REVIEW_COMPLETE.
     */
    public byte weight = 13;

    public boolean newword = false;

    /**
     * Only when first study or the time reviewing can update this value.
     */
    public long review_time = 0;

    public byte review_type = 0;

    public static final byte REVIEW_1_HOUR = 1;

    public static final byte REVIEW_12_HOUR = 2;

    public static final byte REVIEW_1_DAY = 3;

    public static final byte REVIEW_5_DAY = 4;

    public static final byte REVIEW_20_DAY = 5;

    public static final byte REVIEW_40_DAY = 6;

    public static final byte REVIEW_60_DAY = 7;

    public static final byte REVIEW_COMPLETE = 100;

    public WordInfo(String word) {
        if (!TextUtils.isEmpty(word)) {
            this.word = word;
        }
    }

    public boolean inReviewTime() {
        Log.d(TAG, "..................." + (review_time + getTime() - System.currentTimeMillis())
                + " review type:" + review_type);
        if (review_time + getTime() <= System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public boolean increaseWeight() {
        boolean bln = false;
        byte _weight = (byte) (weight + 1);
        if (_weight <= MAX_WEIGHT) {
            weight = _weight;
            bln = true;
        }
        Log.d(TAG, "weight:" + weight);
        return bln;
    }

    public boolean decreaseWeight() {
        boolean bln = false;
        byte _weight = (byte) (weight - 1);
        if (_weight >= MIN_WEIGHT) {
            weight = _weight;
            bln = true;
        }
        return bln;
    }

    public boolean canUpgrade() {
        Log.d(TAG, "can ug weight:" + weight);
        return weight < MAX_WEIGHT;
    }

    public boolean canDegrade() {
        return weight > MIN_WEIGHT;
    }

    @Override
    public String toString() {
        return "id:" + id + " word:" + word + " ignore:" + ignore + " studycount:" + studycount
                + " errorcount:" + errorcount + " weight:" + weight + " newword:" + newword;
    }

    @Override
    public boolean equals(Object o) {
        WordInfo info = (WordInfo) o;
        if (id != info.id) {
            return false;
        }
        if (ignore != info.ignore) {
            return false;
        }
        if (newword != info.newword) {
            return false;
        }
        if (studycount != info.studycount) {
            return false;
        }
        if (errorcount != info.errorcount) {
            return false;
        }
        if (weight != info.weight) {
            return false;
        }
        if (word == null && info.word != null) {
            return false;
        }
        if (word != null && info.word == null) {
            return false;
        }
        if (!word.equals(info.word)) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<WordInfo> CREATOR = new Creator<WordInfo>() {

        @Override
        public WordInfo createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public WordInfo[] newArray(int size) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    public static byte getNextReviewType(byte type) {
        byte _type = type;
        switch (type) {
            case REVIEW_1_HOUR:
                _type = REVIEW_12_HOUR;
                break;
            case REVIEW_12_HOUR:
                _type = REVIEW_1_DAY;
                break;
            case REVIEW_1_DAY:
                _type = REVIEW_5_DAY;
                break;
            case REVIEW_5_DAY:
                _type = REVIEW_20_DAY;
                break;
            case REVIEW_20_DAY:
                _type = REVIEW_40_DAY;
                break;
            case REVIEW_40_DAY:
                _type = REVIEW_60_DAY;
                break;
            case REVIEW_60_DAY:
                _type = REVIEW_COMPLETE;
                break;
            default:
                break;
        }
        return _type;
    }

    public static String getReviewTypeText(Context context, byte type) {
        String str = "";
        switch (type) {
            case 0:
            case REVIEW_1_HOUR:
                str += context.getString(R.string.review_1_hour);
                break;
            case REVIEW_12_HOUR:
                str += context.getString(R.string.review_12_hour);
                break;
            case REVIEW_1_DAY:
                str += context.getString(R.string.review_1_day);
                break;
            case REVIEW_5_DAY:
                str += context.getString(R.string.review_5_day);
                break;
            case REVIEW_20_DAY:
                str += context.getString(R.string.review_20_day);
                break;
            case REVIEW_40_DAY:
                str += context.getString(R.string.review_40_day);
                break;
            case REVIEW_60_DAY:
                str += context.getString(R.string.review_60_day);
                break;
            case REVIEW_COMPLETE:
            default:
                break;
        }
        return str;
    }

    private long getTime() {
        long t = 0;
        switch (review_type) {
            case REVIEW_1_HOUR:
                t = 2400 * 1000l;
                break;
            case REVIEW_12_HOUR:
                t = 12 * 3600 * 1000l;
                break;
            case REVIEW_1_DAY:
                t = 24 * 3600 * 1000l;
                break;
            case REVIEW_5_DAY:
                t = 5 * 24 * 3600 * 1000l;
                break;
            case REVIEW_20_DAY:
                t = 20 * 24 * 3600 * 1000l;
                break;
            case REVIEW_40_DAY:
                t = 40 * 24 * 3600 * 1000l;
                break;
            case REVIEW_60_DAY:
                t = 60 * 24 * 3600 * 1000l;
                break;
            case REVIEW_COMPLETE:
            default:
                t = Long.MAX_VALUE / 2;
                break;
        }
        Log.d(TAG, "get time :" + t + "  review type:" + review_type);
        return t;
    }

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(THistory.WORD, word);
        value.put(THistory.IGNORE, ignore ? 2 : 1);
        value.put(THistory.STUDY_COUNT, studycount);
        value.put(THistory.ERROR_COUNT, errorcount);
        value.put(THistory.WEIGHT, weight);
        value.put(THistory.NEW_WORD, newword ? 2 : 1);
        value.put(THistory.REVIEW_TYPE, review_type);
        value.put(THistory.REVIEW_TIME, review_time);
        return value;
    }
}

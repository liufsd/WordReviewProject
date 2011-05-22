
package com.coleman.kingword.wordinfo;

import android.text.TextUtils;
import android.util.Log;

public class WordInfoVO {
    public static final byte MIN_WEIGHT = 0;

    public static final byte MAX_WEIGHT = 5;

    private static final String TAG = WordInfoVO.class.getName();

    public long id = -1;

    public String word = "initial";

    /**
     * 2 true 1 false
     */
    public boolean ignore = false;

    public byte studycount = 0;

    public byte errorcount = 0;

    public byte weight = 3;

    public WordInfoVO(String word) {
        if (!TextUtils.isEmpty(word)) {
            this.word = word;
        }
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
                + " errorcount:" + errorcount + " weight:" + weight;
    }

    @Override
    public boolean equals(Object o) {
        WordInfoVO info = (WordInfoVO) o;
        if (id != info.id) {
            return false;
        }
        if (ignore != info.ignore) {
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
}

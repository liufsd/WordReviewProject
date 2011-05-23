
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordinfo.WordInfoVO;

public class WordItem {
    private static final String TAG = WordItem.class.getName();

    public long id;

    public String word;

    /**
     * if viewed
     */
    boolean passView;

    /**
     * if pass view counted in subwordlist.
     */
    boolean _pvc;

    /**
     * if pass alternative
     */
    boolean passAlternative;

    /**
     * if pass Alternative counted in subwordlist.
     */
    boolean _pac;

    /**
     * if pass multiple
     */
    boolean passMultiple;

    /**
     * if pass Multiple counted in subwordlist.
     */
    boolean _pmc;

    private WordInfoVO info;

    private DictData dictData;

    private DictData detailData;

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WordListItem.WORD, word);
        return cv;
    }

    public boolean isComplete() {
        return passView && passAlternative && passMultiple;
    }

    public boolean isPassView() {
        return passView;
    }

    public boolean isPassAlternative() {
        return passAlternative;
    }

    public boolean isPassMultiple() {
        return passMultiple;
    }

    public void setPassView() {
        passView = true;
        if (!_pvc) {
            _pvc = true;
            SubWordList.passViewCount++;
        }
    }

    public void setPassAlternative(boolean correct) {
        if (correct) {
            passAlternative = true;
            if (!_pac) {
                _pac = true;
                SubWordList.passAltCount++;
            }
        } else {
            passView = false;
        }
    }

    public void setPassMultiple(boolean correct) {
        if (correct) {
            passMultiple = true;
            if (!_pmc) {
                _pmc = true;
                SubWordList.passMulCount++;
            }
        } else {
            passAlternative = false;
            passView = false;
        }
    }

    public DictData getDictData(Context context) {
        if (dictData == null) {
            dictData = DictManager.getInstance().viewWord(context, word);
        }
        return dictData;
    }

    public DictData getDetail(Context context) {
        if (detailData == null) {
            detailData = DictManager.getInstance().viewMore(context, word);
        }
        return detailData;
    }

    public void loadInfo(Context context) {
        if (info == null) {
            info = WordInfoHelper.getWordInfo(context, word);
        }
        if (info.ignore) {
            passView = true;
            passAlternative = true;
            passMultiple = true;
            SubWordList.passViewCount++;
            SubWordList.passAltCount++;
            SubWordList.passMulCount++;
        }
    }

    public boolean addNew(Context context) {
        info.newword = true;
        return WordInfoHelper.store(context, info);
    }

    public boolean canUpgrade() {
        return info.canUpgrade();
    }

    public boolean canDegrade() {
        return info.canDegrade();
    }

    public boolean upgrade(Context context) {
        info.weight++;
        Log.d(TAG, "upgrade:" + toString());
        return WordInfoHelper.store(context, info);
    }

    public boolean degrade(Context context) {
        info.weight--;
        Log.d(TAG, "degrade:" + toString());
        return WordInfoHelper.store(context, info);
    }

    public boolean ignore(Context context) {
        info.ignore = true;
        setPassView();
        setPassAlternative(true);
        setPassMultiple(true);
        Log.d(TAG, "ignore:" + toString());
        return WordInfoHelper.store(context, info);
    }

    public void studyPlus(Context context) {
        info.studycount++;
        if (info.studycount % 3 == 0) {
            info.weight--;
        }
        info.weight = info.weight < WordInfoVO.MIN_WEIGHT ? WordInfoVO.MIN_WEIGHT : info.weight;
        Log.d(TAG, "study plus:" + toString());
        WordInfoHelper.store(context, info);
    }

    public void errorPlus(Context context) {
        info.errorcount++;
        SubWordList.errorCount++;
        if (info.errorcount % 2 == 0) {
            info.weight++;
        }
        info.weight = info.weight > WordInfoVO.MAX_WEIGHT ? WordInfoVO.MAX_WEIGHT : info.weight;
        Log.d(TAG, "error plus:" + toString());
        WordInfoHelper.store(context, info);
    }

    @Override
    public String toString() {
        return "id:" + id + " word:" + word + " info:" + info + " data:" + dictData + " detail:"
                + detailData;
    }
}

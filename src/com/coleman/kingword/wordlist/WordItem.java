
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordinfo.WordInfoVO;
import com.coleman.kingword.wordlist.FiniteStateMachine.IFSMCommand;

public class WordItem {
    private static final String TAG = WordItem.class.getName();

    public long id;

    public String word;

    WordInfoVO info;

    private DictData dictData;

    private DictData detailData;

    private FiniteStateMachine mStateMachine = new FiniteStateMachine();

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WordListItem.WORD, word);
        return cv;
    }

    public boolean isComplete() {
        return mStateMachine.isComplete();
    }

    public boolean isPassView() {
        return mStateMachine.isPassView();
    }

    public boolean isPassAlternative() {
        return mStateMachine.isPassAlternative();
    }

    public boolean isPassMultiple() {
        return mStateMachine.isPassMultiple();
    }

    public boolean isAddToNew() {
        return info.newword;
    }

    public void setPass(boolean passed) {
        if (passed) {
            mStateMachine.sendEmptyMessage(IFSMCommand.NEXT);
        } else {
            mStateMachine.sendEmptyMessage(IFSMCommand.RESET);
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
            mStateMachine.sendEmptyMessage(IFSMCommand.COMPLETE);
            SliceWordList.passViewCount++;
            SliceWordList.passAltCount++;
            SliceWordList.passMulCount++;
        }
    }

    public boolean addNew(Context context) {
        info.newword = true;
        return WordInfoHelper.store(context, info);
    }

    public boolean removeFromNew(Context context) {
        info.newword = false;
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
        mStateMachine.sendEmptyMessage(IFSMCommand.COMPLETE);
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
        SliceWordList.errorCount++;
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

    public ArrayList<DictData> getDictData(Context context, ArrayList<WordItem> list) {
        return mStateMachine.getDictData(context, this, list);
    }

    public boolean isIgnore() {
        return info.ignore;
    }

    public boolean removeIgnore(Context context) {
        info.ignore = false;
        return WordInfoHelper.store(context, info);
    }
}


package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.ebbinghaus.EbbinghausReminder;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.kingword.wordinfo.WordInfoVO;
import com.coleman.kingword.wordlist.FiniteStateMachine.FiniteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.IFSMCommand;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;

public class WordItem {
    private static final String TAG = WordItem.class.getName();

    public long id;

    public String word;

    WordInfoVO info;

    private DictData dictData;

    private DictData detailData;

    private FiniteStateMachine mStateMachine;

    public WordItem() {
        mStateMachine = new FiniteStateMachine();
        switch (SliceWordList.listType) {
            case SliceWordList.SUB_WORD_LIST:
                break;
            case SliceWordList.NEW_WORD_BOOK_LIST:
                break;
            case SliceWordList.SCAN_LIST:
                break;
            case SliceWordList.REVIEW_LIST:
                mStateMachine.sendEmptyMessage(IFSMCommand.LAST);
                break;
            default:
                throw new RuntimeException("Not support word list type!");
        }
    }

    public FiniteState getCurrentStatus() {
        return mStateMachine.getCurrentState();
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WordListItem.WORD, word);
        return cv;
    }

    public boolean isComplete() {
        return mStateMachine.isComplete();
    }

    public boolean showSymbol() {
        return mStateMachine.getCurrentState() == mStateMachine.getInitState();
    }

    public boolean isAddToNew() {
        return info.newword;
    }

    public void setPass(boolean passed) {
        if (passed) {
            switch (SliceWordList.listType) {
                case SliceWordList.SUB_WORD_LIST:
                    mStateMachine.sendEmptyMessage(IFSMCommand.NEXT);
                    break;
                case SliceWordList.NEW_WORD_BOOK_LIST:
                    mStateMachine.sendEmptyMessage(IFSMCommand.NEXT);
                    break;
                case SliceWordList.SCAN_LIST:
                    mStateMachine.sendEmptyMessage(IFSMCommand.COMPLETE);
                    break;
                case SliceWordList.REVIEW_LIST:
                    mStateMachine.sendEmptyMessage(IFSMCommand.NEXT);
                    break;
                default:
                    throw new RuntimeException("Not support word list type!");
            }
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
        if (info.review_time == 0 && SliceWordList.listType != SliceWordList.REVIEW_LIST) {
            info.review_type = WordInfoVO.REVIEW_1_HOUR;
            info.review_time = System.currentTimeMillis();
            EbbinghausReminder.setNotifaction(context, info.review_type);
        } else if (SliceWordList.listType == SliceWordList.REVIEW_LIST) {
            info.review_type = WordInfoVO.getNextReviewType(info.review_type);
        }
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

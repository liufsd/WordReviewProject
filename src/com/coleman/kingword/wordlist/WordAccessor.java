
package com.coleman.kingword.wordlist;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.kingword.wordlist.FiniteStateMachine.FiniteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.IFSMCommand;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.util.Log;

public class WordAccessor implements Serializable {

    private static final long serialVersionUID = 884726896304858319L;

    private static final String TAG = "WordAccessor";

    public WordListItem item;

    public WordInfo info;

    private boolean reviewed;

    private DictData dictData;

    private DictData detailData;

    private FiniteStateMachine mStateMachine;

    private WordListAccessor sliceList;

    public WordAccessor(WordListAccessor sliceList) {
        this.sliceList = sliceList;
        switch (sliceList.listType) {
            case WordListAccessor.SUB_WORD_LIST:
                mStateMachine = new FiniteStateMachine(sliceList,
                        WordListAccessor.slicelistState[0]);
                break;
            case WordListAccessor.REVIEW_LIST:
                mStateMachine = new FiniteStateMachine(sliceList,
                        WordListAccessor.slicelistState[1]);
                break;
            case WordListAccessor.NEW_WORD_BOOK_LIST:
                mStateMachine = new FiniteStateMachine(sliceList,
                        WordListAccessor.slicelistState[2]);
                break;
            case WordListAccessor.SCAN_LIST:
                mStateMachine = new FiniteStateMachine(sliceList,
                        WordListAccessor.slicelistState[3]);
                break;
            default:
                throw new RuntimeException("Not support word list type!");
        }
    }

    public String getWord(Context context) {
        String w = item.word;
        Log.d(TAG, "sliceList.listType:" + sliceList.listType);
        if (sliceList.listType == WordListAccessor.REVIEW_LIST) {
            if (info.newword) {
                Log.d(TAG, "info.getReviewTime:" + info.inReviewTime());
                w += "("
                        + context.getString(R.string.new_word)
                        + (info.inReviewTime() ? ","
                                + WordInfo.getReviewTypeText(context, info.review_type) : "") + ")";
            } else {
                w += info.inReviewTime() ? "("
                        + WordInfo.getReviewTypeText(context, info.review_type) + ")" : "";
            }
            return w;
        } else {
            return w + (info.newword ? "(" + context.getString(R.string.new_word) + ")" : "");
        }
    }

    public FiniteState getCurrentStatus() {
        return mStateMachine.getCurrentState();
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(TWordListItem.WORD, item.word);
        cv.put(TWordListItem.SUB_WORD_LIST_ID, item.sub_wordlist_id);
        return cv;
    }

    public boolean isComplete() {
        return mStateMachine.isComplete();
    }

    public boolean showSymbol() {
        return mStateMachine.getCurrentState() instanceof InitState;
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
            dictData = DictManager.getInstance().viewWord(context, item.word);
        }
        return dictData;
    }

    public DictData getDetail(Context context) {
        if (detailData == null) {
            detailData = DictManager.getInstance().viewMore(context, item.word);
        }
        return detailData;
    }

    public void loadInfo(Context context) {
        if (info == null) {
            info = WordInfoHelper.getWordInfo(context, item.word);
        }
        if (info.ignore) {
            sliceList.ignoreCount++;
            mStateMachine.sendEmptyMessage(IFSMCommand.COMPLETE);
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
        sliceList.ignoreCount++;
        mStateMachine.sendEmptyMessage(IFSMCommand.COMPLETE);
        Log.d(TAG, "ignore:" + toString());
        return WordInfoHelper.store(context, info);
    }

    public void studyOrReview(Context context) {
        if (sliceList.listType == WordListAccessor.REVIEW_LIST) {
            reviewPlus(context);
        } else {
            studyPlus(context);
        }
    }

    private void studyPlus(Context context) {
        info.studycount++;
        if (info.review_time == 0) {
            info.review_type = WordInfo.REVIEW_1_HOUR;
            info.review_time = System.currentTimeMillis();
            // ////////////////////////////////////////////////////////////
            // EbbinghausReminder.setNotifaction(context, info.review_type);
            // ////////////////////////////////////////////////////////////
        }
        if (info.studycount % 3 == 0) {
            info.weight--;
        }
        info.weight = info.weight < WordInfo.MIN_WEIGHT ? WordInfo.MIN_WEIGHT : info.weight;
        Log.d(TAG, "study plus:" + toString());
        WordInfoHelper.store(context, info);
    }

    private void reviewPlus(Context context) {
        info.studycount++;
        if (!reviewed) {
            reviewed = true;
            if (info.inReviewTime()) {
                info.review_type = WordInfo.getNextReviewType(info.review_type);
            }
        }
        if (info.studycount % 3 == 0) {
            info.weight--;
        }
        info.weight = info.weight < WordInfo.MIN_WEIGHT ? WordInfo.MIN_WEIGHT : info.weight;
        Log.d(TAG, "review plus:" + toString());
        WordInfoHelper.store(context, info);
    }

    public void errorPlus(Context context) {
        info.errorcount++;
        sliceList.errorCount++;
        if (info.errorcount % 2 == 0) {
            info.weight++;
        }
        info.weight = info.weight > WordInfo.MAX_WEIGHT ? WordInfo.MAX_WEIGHT : info.weight;
        Log.d(TAG, "error plus:" + toString());
        WordInfoHelper.store(context, info);
    }

    @Override
    public String toString() {
        return "id:" + item.id + " word:" + item.word + " info:" + info + " data:" + dictData
                + " detail:" + detailData;
    }

    public ArrayList<DictData> getDictData(Context context, ArrayList<WordAccessor> list) {
        return mStateMachine.getDictData(context, this, list);
    }

    public boolean isIgnore() {
        return info.ignore;
    }

    public boolean removeIgnore(Context context) {
        info.ignore = false;
        sliceList.ignoreCount--;
        return WordInfoHelper.store(context, info);
    }

    public boolean isNewWord() {
        return info.newword;
    }

    public void clear() {
        dictData = null;
    }
}

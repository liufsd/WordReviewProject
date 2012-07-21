
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.Context;

import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.FiniteStateMachine.MultipleState;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class ReviewListVisitor extends AbsSubVisitor {
    private static final long serialVersionUID = 1022555204323352434L;

    public static final String DEFAULT_VIEW_METHOD = InitState.TYPE + "," + MultipleState.TYPE
            + "," + CompleteState.TYPE;

    public static final byte TYPE = 2;

    private static final String TAG = ReviewListVisitor.class.getName();

    private Log Log = Config.getLog();

    public ReviewListVisitor() {
        super();
        type = TYPE;
        String typeStr = AppSettings.getString(AppSettings.REVIEW_VIEW_METHOD, DEFAULT_VIEW_METHOD);
        method = new ViewMethod(typeStr);
    }

    @Override
    public void loadWordList(Context context) {
        ArrayList<WordInfo> infoList = WordInfoHelper.getWordInfoList(context, TYPE);
        WordVisitor wa;
        for (WordInfo info : infoList) {
            wa = new WordVisitor(this, new WordListItem());
            wa.item.word = info.word;
            wa.info = info;
            list.add(wa);
            Log.d(TAG, "item:" + wa + " status:" + wa.getCurrentStatus());
        }
        infoList.clear();
    }
}

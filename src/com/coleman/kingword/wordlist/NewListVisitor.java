
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.Context;

import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.MultipleState;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class NewListVisitor extends AbsSubVisitor {

    private static final long serialVersionUID = -1386135087319156871L;

    public static final String DEFAULT_VIEW_METHOD = MultipleState.TYPE + "," + CompleteState.TYPE;

    public static final byte TYPE = 4;

    private static final String TAG = NewListVisitor.class.getName();

    private Log Log = Config.getLog();

    public NewListVisitor() {
        super();
        type = TYPE;
        String typeStr = AppSettings.getString(AppSettings.NEW_VIEW_METHOD, DEFAULT_VIEW_METHOD);
        method = new ViewMethod(typeStr);
    }

    @Override
    public void loadWordList(Context context) {
        ArrayList<WordInfo> infoList = WordInfoHelper.getWordInfoList(context, TYPE);
        WordVisitor wa;
        for (WordInfo info : infoList) {
            wa = new WordVisitor(this, new WordListItem(info.word));
            wa.info = info;
            list.add(wa);
            Log.d(TAG, "item:" + wa);
        }
        infoList.clear();
    }

}

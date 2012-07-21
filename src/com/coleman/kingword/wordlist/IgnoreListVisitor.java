
package com.coleman.kingword.wordlist;

import java.util.ArrayList;

import android.content.Context;

import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class IgnoreListVisitor extends AbsSubVisitor {

    private static final long serialVersionUID = -4037721270526744096L;

    public static final String DEFAULT_VIEW_METHOD = InitState.TYPE + "," + CompleteState.TYPE;

    public static final byte TYPE = 3;

    private static final String TAG = IgnoreListVisitor.class.getName();

    private Log Log = Config.getLog();

    public IgnoreListVisitor() {
        super();
        type = TYPE;
        String vmtd = AppSettings.getString(AppSettings.IGNORE_VIEW_METHOD, DEFAULT_VIEW_METHOD);
        method = new ViewMethod(vmtd);
    }

    @Override
    public void loadWordList(Context context) {
        ArrayList<WordInfo> infoList = WordInfoHelper.getWordInfoList(context, TYPE);
        WordVisitor wa;
        for (WordInfo info : infoList) {
            wa = new WordVisitor(this, new WordListItem(info.word));
            wa.info = info;
            list.add(wa);
            Log.d(TAG, "item:" + wa + " status:" + wa.getCurrentStatus());
        }
        infoList.clear();
    }

}

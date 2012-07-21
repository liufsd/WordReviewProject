
package com.coleman.kingword.wordlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.coleman.kingword.CoreActivity;
import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.FiniteStateMachine.InitState;
import com.coleman.kingword.wordlist.FiniteStateMachine.MultipleState;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class SubListVisitor extends AbsSubVisitor {

    private static final long serialVersionUID = 8793360127890535609L;

    private static final String TAG = SubListVisitor.class.getName();

    private static Log Log = Config.getLog();

    public static final String DEFAULT_VIEW_METHOD = InitState.TYPE + "," + InitState.TYPE + ","
            + MultipleState.TYPE + "," + CompleteState.TYPE;

    public static final byte TYPE = 1;

    private SubWordList subinfo;

    public SubListVisitor() {
        super();
        type = TYPE;
        String typeStr = AppSettings.getString(AppSettings.SUB_VIEW_METHOD, DEFAULT_VIEW_METHOD);
        method = new ViewMethod(typeStr);
    }

    public void setSubInfo(SubWordList info) {
        this.subinfo = info;
    }

    @Override
    public void loadWordList(Context context) {
        final String projection[] = new String[] {
                TWordListItem.WORD, TWordListItem._ID, TWordListItem.SUB_WORD_LIST_ID,
                TWordListItem.STATE
        };
        // 1.再次打开已经完成学习的单元需要清楚所有单词的状态
        if (subinfo.level > 0) {
            clearAllWordItemStates(context);
        }
        // 2.加载当前单元单词
        long time = System.currentTimeMillis();
        Cursor c = context.getContentResolver().query(
                TWordListItem.getContentUri(subinfo.word_list_id), projection,
                TWordListItem.SUB_WORD_LIST_ID + "=" + subinfo.id, null, null);
        if (c.moveToFirst()) {
            WordVisitor wordAccessor;
            while (!c.isAfterLast()) {

                WordListItem item = new WordListItem();
                item.word = c.getString(0);
                item.id = c.getLong(1);
                item.sub_wordlist_id = c.getLong(2);
                item.state = c.getInt(3);

                wordAccessor = new WordVisitor(this, item);

                list.add(wordAccessor);
                wordAccessor.loadInfo(context);
                c.moveToNext();
            }

        }
        if (c != null) {
            c.close();
            c = null;
        }
        // 3.再次打开已经完成学习的单元需要清空以前的学习记录
        if (subinfo.level > 0) {
            subinfo.position = 0;
            subinfo.progress = 0;
            subinfo.error_count = 0;
            subinfo.count_down = 0;
            update(context);
        }
        // 4.指定上次学习的位置
        p = subinfo.position;

        time = System.currentTimeMillis() - time;
        Log.d(TAG, "Load sub-wordlist cost time: " + time);
    }

    public void clearAllWordItemStates(Context context) {
        ContentValues values = new ContentValues();
        values.put(TWordListItem.STATE, -1);
        int num = context.getContentResolver().update(
                TWordListItem.getContentUri(subinfo.word_list_id), values,
                TWordListItem.SUB_WORD_LIST_ID + "=" + subinfo.id, null);
        Log.i(TAG, "===coleman-debug-update word items count: " + num);
    }

    public void errorPlus() {
        if (subinfo != null) {
            subinfo.error_count++;
        }
    }

    public long getWordListID() {
        return subinfo.word_list_id;
    }

    public SubWordList getSubList() {
        return subinfo;
    }

    public void update(Context context) {
        // if not SUB_WORD_LIST, do not execute the update.
        subinfo.level = getStudyRate();
        if (subinfo.level > subinfo.history_level) {
            subinfo.history_level = subinfo.level;
        }
        subinfo.position = p;
        subinfo.progress = getProgress();
        if (context instanceof CoreActivity) {
            CoreActivity act = (CoreActivity) context;
            if (act.countdownManager != null) {
                subinfo.count_down = act.countdownManager.getCountDown();
            }
        }
        Log.i(TAG, "===coleman-debug-subinfo: " + subinfo.error_count);
        subinfo.method = AppSettings.getString(AppSettings.SUB_VIEW_METHOD, DEFAULT_VIEW_METHOD);
        ContentValues values = subinfo.toContentValues();
        int num = context.getContentResolver().update(TSubWordList.CONTENT_URI, values,
                TSubWordList._ID + "=" + subinfo.id, null);
        Log.d(TAG, "sub word list update num: " + num);
    }

    @Override
    public int getCountDown() {
        if (subinfo != null) {
            return subinfo.count_down;
        }
        return 0;
    }

    private int getStudyRate() {
        int tmp = -1;
        if (!allComplete()) {
            tmp = 0;
        } else {
            if (subinfo.error_count <= list.size() * 5 / 100) {
                tmp = 4;
            } else if (subinfo.error_count <= list.size() * 2 / 10) {
                tmp = 3;
            } else if (subinfo.error_count <= list.size() * 4 / 10) {
                tmp = 2;
            } else {
                tmp = 1;
            }
        }
        return tmp;
    }

    public String getSubListLevelString(Context context) {
        String levels = "";
        if (subinfo.level < subinfo.history_level) {
            levels = context.getString(R.string.history_level);
        } else {
            levels = getLevelStrings(context, subinfo.level);
        }
        return levels;
    }

    public int getCorrectPercentage() {
        if (list.size() == 0) {
            return 0;
        }
        return 100 - subinfo.error_count * 100 / list.size();
    }

    public static String getLevelStrings(Context context, int level) {
        String levels = "";
        switch (level) {
            case -1:
                levels = context.getString(R.string.new_unit);
                break;
            case 0:
                levels = context.getString(R.string.unfinish_unit);
                break;
            case 1:
                levels = context.getString(R.string.unpass_unit);
                break;
            case 2:
                levels = context.getString(R.string.low_level);
                break;
            case 3:
                levels = context.getString(R.string.mid_level);
                break;
            case 4:
                levels = context.getString(R.string.high_level);
                break;
            default:// ignore
                break;
        }
        return levels;
    }
}

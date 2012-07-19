
package com.coleman.kingword.wordlist;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.coleman.kingword.R;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.history.WordInfo;
import com.coleman.kingword.history.WordInfoHelper;
import com.coleman.kingword.inspirit.countdown.CountdownManager;
import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.kingword.wordlist.FiniteStateMachine.CompleteState;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.model.WordListItem;
import com.coleman.log.Log;
import com.coleman.util.AppSettings;
import com.coleman.util.Config;

public class SubWordListAccessor implements Serializable {
    private static final long serialVersionUID = -5435967694193721910L;

    private SubWordList subinfo;

    private ArrayList<WordAccessor> list;

    private static final String TAG = SubWordListAccessor.class.getName();

    private static Log Log = Config.getLog();

    /**
     * the pointer of the word index in the sublist.
     */
    private int p;

    /**
     * 关键性能点：
     * <p>
     * 用来标识上次顺序遍历没有完成学习的单词的位置，加快判断是否所有单词都已学习完的速度。
     * 
     * @Optimize
     */
    private int lastMark;

    public byte listType;

    public static int slicelistState[][] = new int[4][];

    public static final byte NULL_TYPE = -1;

    public static final byte SUB_WORD_LIST = 1;

    public static final byte REVIEW_LIST = 2;

    public static final byte SCAN_LIST = 3;

    public static final byte NEW_WORD_BOOK_LIST = 4;

    public static final String DEFAULT_VIEW_METHOD = "0,0,1#0,1#1#0";

    public SubWordListAccessor(SubWordList info) {
        listType = SUB_WORD_LIST;
        subinfo = info;
        p = info.position;
        Log.i(TAG, "===coleman-debug-p: " + p);
        list = new ArrayList<WordAccessor>();
    }

    public SubWordListAccessor(byte type) {
        listType = type;
        list = new ArrayList<WordAccessor>();
    }

    private void loadViewMethod(Context context) {
        String vmtd = AppSettings.getString(AppSettings.VIEW_METHOD, DEFAULT_VIEW_METHOD);
        String scrap[] = vmtd.split("#");
        for (int i = 0; i < scrap.length; i++) {
            String ss[] = scrap[i].split(",");
            slicelistState[i] = new int[ss.length + 1];
            for (int j = 0; j < ss.length; j++) {
                slicelistState[i][j] = Integer.parseInt(ss[j]);
            }
            slicelistState[i][ss.length] = CompleteState.TYPE;
        }
    }

    public void loadWordList(Context context) {
        loadViewMethod(context);
        switch (listType) {
            case SUB_WORD_LIST:
                loadSubList(context);
                break;
            case NEW_WORD_BOOK_LIST:
            case SCAN_LIST:
                loadInfoList(context);
                break;
            default:
                break;
        }
    }

    public int getListType() {
        return listType;
    }

    public void loadReviewWordList(Context context) {
        loadViewMethod(context);
        ArrayList<WordInfo> infoList = WordInfoHelper.getWordInfoList(context, REVIEW_LIST);
        WordAccessor wa;
        for (WordInfo info : infoList) {
            wa = new WordAccessor(this);
            wa.item = new WordListItem();
            wa.item.word = info.word;
            wa.info = info;
            list.add(wa);
            Log.d(TAG, "item:" + wa + " status:" + wa.getCurrentStatus());
        }
        infoList.clear();
    }

    private void loadInfoList(Context context) {
        ArrayList<WordInfo> infoList = WordInfoHelper.getWordInfoList(context, listType);
        WordAccessor wa;
        for (WordInfo info : infoList) {
            wa = new WordAccessor(this);
            wa.item = new WordListItem();
            wa.item.word = info.word;
            wa.info = info;
            list.add(wa);
            Log.d(TAG, "item:" + wa);
        }
        infoList.clear();
    }

    private void loadSubList(Context context) {
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
            WordAccessor wordAccessor;
            while (!c.isAfterLast()) {

                WordListItem item = new WordListItem();
                item.word = c.getString(0);
                item.id = c.getLong(1);
                item.sub_wordlist_id = c.getLong(2);
                item.state = c.getInt(3);

                wordAccessor = new WordAccessor(this, item);

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
        time = System.currentTimeMillis() - time;
        Log.d(TAG, "Load sub-wordlist cost time: " + time);
    }

    public int getProgress() {
        if (list.size() == 0) {
            return 0;
        } else if (list.size() == 1) {
            return 100;
        }

        if (allComplete()) {
            return 100;
        }

        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            WordAccessor wa = list.get(i);
            sum += wa.getStudyIndex();
        }
        int total = (slicelistState[listType - 1].length - 1) * list.size();

        return sum * 100 / total;
    }

    public boolean allComplete() {
        if (list.size() == 0) {
            return true;
        }
        for (int i = lastMark; i < list.size(); i++) {
            WordAccessor wa = list.get(i);
            if (!wa.isComplete()) {
                lastMark = i;
                return false;
            }
        }
        for (int i = 0; i < lastMark; i++) {
            WordAccessor wa = list.get(i);
            if (!wa.isComplete()) {
                lastMark = i;
                return false;
            }
        }
        return true;
    }

    public ArrayList<DictData> getDictData(Context context, WordAccessor item) {
        return item.getDictData(context, list);
    }

    /**
     * call the method must make sure that isComplete is called first.
     */
    public WordAccessor getCurrentWord() {
        if (allComplete()) {
            return list.get(p);
        }
        WordAccessor item = list.get(p);
        if (!item.isComplete()) {
            return item;
        } else {
            return getNext();
        }
    }

    /**
     * call this method must make sure that hasNext is called first!!!
     */
    public WordAccessor getNext() {
        p = p + 1 > list.size() - 1 ? 0 : p + 1;
        WordAccessor accessor = list.get(p);
        if (!accessor.isComplete()) {
            return accessor;
        } else {
            // if the next word is already completed, then try to find the next
            // word after index of p
            int tmp = p + 1 > list.size() - 1 ? 0 : p + 1;
            for (int i = tmp; i < list.size(); i++) {
                WordAccessor it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            // if not found next word after index p, then try to find the next
            // word from the index 0
            for (int i = 0; i < tmp; i++) {
                WordAccessor it = list.get(i);
                if (!it.isComplete()) {
                    p = i;
                    return it;
                }
            }
            return list.get(tmp);
        }
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

    public int getCorrectPercentage() {
        if (list.size() == 0) {
            return 0;
        }
        return 100 - subinfo.error_count * 100 / list.size();
    }

    public void update(Context context) {
        // if not SUB_WORD_LIST, do not execute the update.
        if (listType != SUB_WORD_LIST) {
            return;
        }

        subinfo.level = getStudyRate();
        if (subinfo.level > subinfo.history_level) {
            subinfo.history_level = subinfo.level;
        }
        subinfo.position = p;
        subinfo.progress = getProgress();
        subinfo.count_down = CountdownManager.getInstance().getCountDown();
        Log.i(TAG, "===coleman-debug-subinfo: " + subinfo.error_count);
        subinfo.method = AppSettings.getString(AppSettings.VIEW_METHOD, DEFAULT_VIEW_METHOD);
        ContentValues values = subinfo.toContentValues();
        int num = context.getContentResolver().update(TSubWordList.CONTENT_URI, values,
                TSubWordList._ID + "=" + subinfo.id, null);
        Log.d(TAG, "sub word list update num: " + num);
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

    public void clearAllWordItemStates(Context context) {
        ContentValues values = new ContentValues();
        values.put(TWordListItem.STATE, -1);
        int num = context.getContentResolver().update(
                TWordListItem.getContentUri(subinfo.word_list_id), values,
                TWordListItem.SUB_WORD_LIST_ID + "=" + subinfo.id, null);
        Log.i(TAG, "===coleman-debug-update word items count: " + num);
    }

    public int getCount() {
        return list.size();
    }

    public String getViewMethod() {
        Log.i(TAG, "===coleman-debug-getCurrentWord(): " + getCurrentWord().toString());
        return getCurrentWord().getViewMethod();
    }

    public int getCurrentIndex() {
        return p + 1;
    }

    public long getWordListID() {
        return subinfo.word_list_id;
    }

    public SubWordList getSubList() {
        return subinfo;
    }

    public int getIndex() {
        return p;
    }

    public void errorPlus() {
        if (subinfo != null) {
            subinfo.error_count++;
        }
    }

    public int getCountDown() {
        if (subinfo != null) {
            return subinfo.count_down;
        }
        return 0;
    }

}

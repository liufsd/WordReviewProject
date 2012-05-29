
package com.coleman.kingword.wordlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.coleman.kingword.provider.KingWord.TSubWordList;
import com.coleman.kingword.provider.KingWord.TWordList;
import com.coleman.kingword.provider.KingWord.TWordList.TWordListItem;
import com.coleman.kingword.provider.KingWordDBHepler;
import com.coleman.kingword.wordlist.model.SubWordList;
import com.coleman.kingword.wordlist.model.WordList;
import com.coleman.util.AppSettings;
import com.coleman.util.GeneralParser;
import com.coleman.util.Log;

public class WordListManager {
    private static final String TAG = "WordListManager";

    private static WordListManager manager;

    public static final int DEFAULT_SPLIT_NUM = 100;

    private WordListManager() {
    }

    public static WordListManager getInstance() {
        if (manager == null) {
            manager = new WordListManager();
        }
        return manager;
    }

    public void loadWordListFromAsset(Context context, String wordlistName,
            IProgressNotifier notifier) {
        // make sure the notifier is not null
        if (notifier == null) {
            notifier = new IProgressNotifier() {
                @Override
                public void notify(int p) {
                    Log.d(TAG, "asset progress+++++++++++:" + p);
                }
            };
        }
        loadWordList(context, wordlistName, notifier);
    }

    public void loadWordListFromFile(Context context, String wordlistName,
            IProgressNotifier notifier) {
        if (isExist(context, wordlistName)) {
            Log.w(TAG, "The word list is already exist!");
            return;
        }
        // make sure the notifier is not null
        if (notifier == null) {
            notifier = new IProgressNotifier() {
                @Override
                public void notify(int p) {
                    Log.d(TAG, "file progress+++++++++++:" + p);
                }
            };
        }
        ArrayList<String> list = null;
        try {
            list = GeneralParser.parseFile(context, wordlistName, false, notifier);
            // WordlistArranger.tidy(list, wordlistName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        WordList wordlist = new WordList("", wordlistName, null);
        insertWordList(context, wordlist, notifier);
        splitAndInsertSubWordList(context, list, wordlist, notifier);
    }

    public boolean isExist(Context context, String wordlist) {
        String projection[] = new String[] {
                TWordList._ID, TWordList.PATH_NAME
        };
        Cursor c = context.getContentResolver().query(TWordList.CONTENT_URI, projection,
                TWordList.PATH_NAME + "='" + wordlist + "'", null, null);
        if (c == null || c.getCount() <= 0) {
            if (c != null) {
                c.close();
            }
            return false;
        }
        c.close();
        return true;
    }

    private void loadWordList(Context context, String wordlistName, IProgressNotifier notifier) {
        if (isExist(context, wordlistName)) {
            Log.w(TAG, "The word list is already exist!");
            notifier.notify(100);
            return;
        }
        ArrayList<String> list = null;
        try {
            list = GeneralParser.parseFile(context, wordlistName, true, notifier);
            // WordlistArranger.tidy(list, wordlistName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        WordList wordlist = new WordList("", wordlistName, null);
        notifier.notify(35);
        insertWordList(context, wordlist, notifier);
        splitAndInsertSubWordList(context, list, wordlist, notifier);
    }

    private void insertWordList(Context context, WordList wordlist, IProgressNotifier notifier) {
        Uri uri = context.getContentResolver().insert(TWordList.CONTENT_URI,
                wordlist.toContentValues());
        notifier.notify(45);
        wordlist.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void splitAndInsertSubWordList(Context context, ArrayList<String> list,
            WordList wordlist, IProgressNotifier notifier) {
        // int suggest = AppSettings.getInt(context, AppSettings.SPLIT_NUM_KEY,
        // DEFAULT_SPLIT_NUM);
        // int suggest =
        // context.getSharedPreferences(Config.getDefaultSharedPreferenceName(context),
        // 0).getInt(AppSettings.SPLIT_NUM_KEY, DEFAULT_SPLIT_NUM);
        String strSuggest = AppSettings
                .getString(AppSettings.SPLIT, "" + DEFAULT_SPLIT_NUM);
        int suggest = Integer.parseInt(strSuggest);
        ArrayList<List<String>> sublist = new ArrayList<List<String>>();
        switch (wordlist.set_method) {
            case AVARAGE_DEVIDE: {
                int start = 0;
                int end = list.size();
                while (start < end) {
                    int tmpEnd = start + suggest <= end ? start + suggest : end;
                    sublist.add(list.subList(start, tmpEnd));
                    start = tmpEnd;
                    System.out.println("AVARAGE_DEVIDE Split point: " + tmpEnd);
                }
                break;
            }
            case CHARACTER_DEVIDER: {
                int start = 0;
                int size = list.size();
                char prefix = '!';
                sort(list);
                if (size > 0) {
                    prefix = list.get(0).charAt(0);
                }
                for (int i = 0; i < size; i++) {
                    if (list.get(i).charAt(i) != prefix) {
                        sublist.add(list.subList(start, i));
                        start = i;
                        System.out.println("CHARACTER_DEVIDER Split point: " + start);
                    }
                }
                break;
            }
            case DEFAULT_DEVIDE:
            default: {
                sort(list);
                int start = 0;
                int end = list.size();
                while (start < end) {
                    int tmpEnd = start + suggest <= end ? start + suggest : end;
                    sublist.add(list.subList(start, tmpEnd));
                    start = tmpEnd;
                    System.out.println("DEFAULT_DEVIDE Split point: " + tmpEnd);
                }
                break;
            }
        }
        notifier.notify(50);
        // for notifier vas
        int total_steps = sublist.size();
        int step = 0;

        update(context, wordlist.id);

        for (List<String> list2 : sublist) {
            SubWordList sub = new SubWordList(wordlist.id);
            doInsertSubWordList(context, sub);

            doInsertWords(context, sub, list2);
            step++;
            if (total_steps == 0) {
                notifier.notify(100);
            } else {
                notifier.notify(50 + step * 50 / total_steps);
            }
        }

    }

    private void doInsertSubWordList(Context context, SubWordList sub) {
        Uri uri = context.getContentResolver().insert(TSubWordList.CONTENT_URI,
                sub.toContentValues());
        sub.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void doInsertWords(Context context, SubWordList sub, List<String> list) {
        ContentValues cv[] = new ContentValues[list.size()];
        int i = 0;
        for (String string : list) {
            cv[i] = new ContentValues();
            cv[i].put(TWordListItem.WORD, string);
            cv[i].put(TWordListItem.SUB_WORD_LIST_ID, sub.id);
            i++;
        }
        context.getContentResolver().bulkInsert(TWordListItem.getContentUri(sub.word_list_id), cv);
    }

    private void update(Context context, long id) {
        TWordListItem table = new TWordListItem(id);
        SQLiteDatabase db = KingWordDBHepler.getInstance(context).getWritableDatabase();
        db.execSQL(table.getCreateTableSql());
    }

    private void sort(ArrayList<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return object1.compareTo(object2);
            }

        });
    }

    public static interface IProgressNotifier {
        void notify(int p);
    }
}

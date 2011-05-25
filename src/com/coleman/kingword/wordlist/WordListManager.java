
package com.coleman.kingword.wordlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.wordlist.SubWordList.SubInfo;

public class WordListManager {
    private static final String TAG = WordListManager.class.getName();

    private static WordListManager manager;

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
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        WordList wordlist = new WordList("", wordlistName, null);
        insertWordList(context, wordlist, notifier);
        splitAndInsertSubWordList(context, list, wordlist, 20, notifier);
    }

    public boolean isExist(Context context, String wordlist) {
        String projection[] = new String[] {
                WordsList._ID, WordsList.PATH_NAME
        };
        Cursor c = context.getContentResolver().query(WordsList.CONTENT_URI, projection,
                WordsList.PATH_NAME + "='" + wordlist + "'", null, null);
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
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        WordList wordlist = new WordList("", wordlistName, null);
        notifier.notify(35);
        insertWordList(context, wordlist, notifier);
        splitAndInsertSubWordList(context, list, wordlist, 20, notifier);
    }

    private void insertWordList(Context context, WordList wordlist, IProgressNotifier notifier) {
        Uri uri = context.getContentResolver().insert(WordsList.CONTENT_URI,
                wordlist.toContentValues());
        notifier.notify(45);
        wordlist.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void splitAndInsertSubWordList(Context context, ArrayList<String> list,
            WordList wordlist, int suggest, IProgressNotifier notifier) {
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

        for (List<String> list2 : sublist) {
            SubInfo sub = new SubInfo(wordlist.id);
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

    private void doInsertSubWordList(Context context, SubInfo sub) {
        Uri uri = context.getContentResolver().insert(SubWordsList.CONTENT_URI,
                sub.toContentValues());
        sub.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void doInsertWords(Context context, SubInfo sub, List<String> list) {
        ContentValues cv[] = new ContentValues[list.size()];
        int i = 0;
        for (String string : list) {
            cv[i] = new ContentValues();
            cv[i].put(WordListItem.WORD, string);
            cv[i].put(WordListItem.SUB_WORD_LIST_ID, sub.id);
            i++;
        }
        context.getContentResolver().bulkInsert(WordListItem.CONTENT_URI, cv);
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

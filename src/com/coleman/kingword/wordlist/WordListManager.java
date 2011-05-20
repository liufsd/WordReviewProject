
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
import android.os.AsyncTask;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;
import com.coleman.kingword.provider.KingWord.WordsList;

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

    // public void loadWordListFromDB(Context context, long wordlist_id, String
    // wordlist_name,
    // LoadNotifier notifier) {
    // new LoadWordListFromDBTask(context, wordlist_id, wordlist_name,
    // notifier).execute();
    // }

    public void loadWordListFromFile(Context context, String wordlistName, boolean inAsset,
            LoadNotifier notifier) {
        if (notifier == null) {
            notifier = new LoadNotifier() {
                @Override
                public void notifyProgress(int p) {
                }

                @Override
                public void notifyDone() {
                }
            };
        }
        new LoadWordListFromFileTask(context, wordlistName, inAsset, notifier).execute();
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

    // private class LoadWordListFromDBTask extends AsyncTask<Void, Void, Void>
    // {
    // Context context;
    //
    // long wordlist_id;
    //
    // String wordlist_name;
    //
    // LoadNotifier notifier;
    //
    // public LoadWordListFromDBTask(Context context, long wordlist_id, String
    // wordlist_name,
    // LoadNotifier notifier) {
    // this.context = context;
    // this.wordlist_id = wordlist_id;
    // this.wordlist_name = wordlist_name;
    // this.notifier = notifier;
    // }
    //
    // @Override
    // protected Void doInBackground(Void... params) {
    // loadWordList(context, wordlist_id, wordlist_name);
    // return null;
    // }
    //
    // @Override
    // protected void onPostExecute(Void result) {
    // notifier.notifyDone();
    // }
    //
    // private void loadWordList(Context context, long wordlist_id, String
    // wordlist_name) {
    // String projection[] = new String[] {
    // WordListItem._ID, WordListItem.SUB_WORD_LIST_ID, WordListItem.WORD
    // };
    // Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI,
    // projection,
    // WordListItem.SUB_WORD_LIST_ID + "=" + wordlist_id, null, null);
    // if (c == null || c.getCount() <= 0) {
    // if (c != null) {
    // c.close();
    // }
    // return;
    // }
    // ArrayList<String> list = new ArrayList<String>();
    // if (c.moveToFirst()) {
    // while (!c.isAfterLast()) {
    // list.add(c.getString(2));
    // c.moveToNext();
    // }
    // }
    // WordList wordlist = new WordList();
    // c.close();
    // }
    // }

    private class LoadWordListFromFileTask extends AsyncTask<Void, Void, Void> {
        Context context;

        String wordlistName;

        boolean inAsset;

        LoadNotifier notifier;

        public LoadWordListFromFileTask(Context context, String wordlistName, boolean inAsset,
                LoadNotifier notifier) {
            this.context = context;
            this.wordlistName = wordlistName;
            this.inAsset = inAsset;
            this.notifier = notifier;
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadWordList(context, wordlistName, inAsset);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifier.notifyDone();
        }

        private void loadWordList(Context context, String wordlistName, boolean inAsset) {
            if (isExist(context, wordlistName)) {
                Log.w(TAG, "The word list is already exist!");
                return;
            }
            ArrayList<String> list = null;
            if (inAsset) {
                try {
                    list = GeneralParser.parseAsset(context, wordlistName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                WordList wordlist = new WordList("", wordlistName, null);
                insertWordList(context, wordlist);
                splitAndInsertSubWordList(context, list, wordlist, 100);
            } else {
                /**
                 * @TODO need implementation.
                 */
            }
        }

    }

    private void insertWordList(Context context, WordList wordlist) {
        Uri uri = context.getContentResolver().insert(WordsList.CONTENT_URI,
                wordlist.toContentValues());
        wordlist.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void splitAndInsertSubWordList(Context context, ArrayList<String> list,
            WordList wordlist, int suggest) {
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
        for (List<String> list2 : sublist) {
            SubWordList sub = new SubWordList(wordlist.id);
            doInsertSubWordList(context, sub);
            doInsertWords(context, sub, list2);
        }

    }

    private void doInsertSubWordList(Context context, SubWordList sub) {
        Uri uri = context.getContentResolver().insert(SubWordsList.CONTENT_URI,
                sub.toContentValues());
        sub.id = Long.parseLong(uri.getPathSegments().get(1));
    }

    private void doInsertWords(Context context, SubWordList sub, List<String> list) {
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

    public static interface LoadNotifier {
        void notifyDone();

        void notifyProgress(int p);
    }
}

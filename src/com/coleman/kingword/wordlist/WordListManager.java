
package com.coleman.kingword.wordlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class WordListManager {
    private static final String TAG = WordListManager.class.getName();

    private HashMap<String, WordList> map = new HashMap<String, WordList>();

    private static WordListManager manager;

    private WordListManager() {
    }

    public static WordListManager getInstance() {
        if (manager == null) {
            manager = new WordListManager();
        }
        return manager;
    }

    public void loadWordListFromDB(Context context, long wordlist_id, String wordlist_name,
            LoadNotifier notifier) {
        new LoadWordListFromDBTask(context, wordlist_id, wordlist_name, notifier).execute();
    }

    public void loadWordListFromFile(Context context, String wordlistName, boolean inAsset,
            LoadNotifier notifier) {
        new LoadWordListFromFileTask(context, wordlistName, inAsset, notifier).execute();
    }

    public WordList getWordList(String wordlistName) {
        return map.get(wordlistName);
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

    private class LoadWordListFromDBTask extends AsyncTask<Void, Void, Void> {
        Context context;

        long wordlist_id;

        String wordlist_name;

        LoadNotifier notifier;

        public LoadWordListFromDBTask(Context context, long wordlist_id, String wordlist_name,
                LoadNotifier notifier) {
            this.context = context;
            this.wordlist_id = wordlist_id;
            this.wordlist_name = wordlist_name;
            this.notifier = notifier;
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadWordList(context, wordlist_id, wordlist_name);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifier.notifyDone();
        }

        private void loadWordList(Context context, long wordlist_id, String wordlist_name) {
            String projection[] = new String[] {
                    WordListItem._ID, WordListItem.WORD_LIST_ID, WordListItem.WORD
            };
            Cursor c = context.getContentResolver().query(WordListItem.CONTENT_URI, projection,
                    WordListItem.WORD_LIST_ID + "=" + wordlist_id, null, null);
            if (c == null || c.getCount() <= 0) {
                if (c != null) {
                    c.close();
                }
                return;
            }
            ArrayList<String> list = new ArrayList<String>();
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    list.add(c.getString(2));
                    c.moveToNext();
                }
            }
            WordList wordlist = new WordList(list);
            map.put(wordlist_name, wordlist);
            c.close();
        }
    }

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
                WordList wordlist = new WordList(list);
                map.put(wordlistName, wordlist);
            } else {
                /**
                 * @TODO need implementation.
                 */
            }
            doInsert(context, wordlistName, list);
        }

        /**
         * Make sure that the specified word list had never been insert to the
         * table, or you should not do it again.
         */
        private void doInsert(Context context, String fileName, ArrayList<String> list) {
            ContentValues value = new ContentValues();
            value.put(WordsList.PATH_NAME, fileName);
            Uri uri = context.getContentResolver().insert(WordsList.CONTENT_URI, value);
            if (list == null || list.size() == 0) {
                Log.w(TAG, "The word list don't have any words!");
                return;
            }
            long rowId = Long.parseLong(uri.getPathSegments().get(1));
            ContentValues cv[] = new ContentValues[list.size()];
            int i = 0;
            for (String string : list) {
                cv[i] = new ContentValues();
                cv[i].put(WordListItem.WORD, string);
                cv[i].put(WordListItem.WORD_LIST_ID, rowId);
                i++;
            }
            context.getContentResolver().bulkInsert(WordListItem.CONTENT_URI, cv);
        }
    }

    public static interface LoadNotifier {
        void notifyDone();

        void notifyProgress(int p);
    }
}


package com.coleman.kingword.dict.stardict;

import java.util.Collection;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.IDictIndex;
import com.coleman.kingword.provider.KingWord.OxfordDictIndex;
import com.coleman.kingword.provider.KingWord.StarDictIndex;
import com.coleman.util.AppSettings;

public class DictLibrary {
    public static final String OXFORD = "oxford";

    public static final String STARDICT = "stardict";

    public static final String OXFORD_PATH = "kingword/dicts/oxford-gb-formated";

    public static final String STARDICT_PATH = "kingword/dicts/stardict1.3";

    private static final String TAG = DictLibrary.class.getName();

    private DictInfo libraryInfo;

    private HashMap<String, DictIndex> libraryWordMap;

    private String libraryName;

    private boolean dbInitialed;

    private boolean ready;

    private WorkNotifier notifier;

    /**
     * If a DictLibrary is constructed, it will do inserting data to database
     * background, after the work done, the libraryWordMap will be cleared,
     * after all these thing done, a mark will be set to indicate database
     * initialed.
     */
    DictLibrary(final Context context, final String libKey, WorkNotifier notifier) {
        this.dbInitialed = AppSettings.getBoolean(context, libKey, false);
        this.notifier = notifier;
        if (!dbInitialed) {
            new LoadAndInsert(context, libKey).execute();
        } else {
            new LoadInfo(context, libKey).execute();
        }
    }

    public DictInfo getLibraryInfo() {
        return libraryInfo;
    }

    public boolean isReady() {
        return ready;
    }

    public DictIndex getDictIndex(Context context, String word) {
        DictIndex di = null;
        if (!ready) {
            return null;
        }
        String[] projection = new String[] {
                IDictIndex.WORD, IDictIndex.OFFSET, IDictIndex.SIZE
        };
        if (dbInitialed) {
            long time = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(
                    OXFORD_PATH.equals(libraryName) ? OxfordDictIndex.CONTENT_URI
                            : StarDictIndex.CONTENT_URI, projection,
                    IDictIndex.WORD + " = '" + word + "'", null, null);
            if (c.moveToFirst()) {
                di = new DictIndex(c.getString(0), c.getLong(1), c.getInt(2));
            }
            if (c != null) {
                c.close();
                c = null;
            }

            // if not found the word from the index databases, try to query the
            // lower case of the word from the index database
            if (di == null) {
                c = context.getContentResolver().query(
                        OXFORD_PATH.equals(libraryName) ? OxfordDictIndex.CONTENT_URI
                                : StarDictIndex.CONTENT_URI, projection,
                        IDictIndex.WORD + " = '" + word.toLowerCase() + "'", null, null);
                if (c.moveToFirst()) {
                    di = new DictIndex(c.getString(0), c.getLong(1), c.getInt(2));
                }
                if (c != null) {
                    c.close();
                    c = null;
                }
            }
            time = System.currentTimeMillis() - time;
            Log.d(TAG, "Query the word from the database cost time: " + time);
        } else {
            di = libraryWordMap.get(word);
        }
        return di;
    }

    public String getLibraryName() {
        return libraryName;
    }

    private boolean isExist(Context context, String libKey) {
        boolean exist = false;
        Cursor c = context.getContentResolver().query(
                STARDICT.equals(libKey) ? StarDictIndex.CONTENT_URI : OxfordDictIndex.CONTENT_URI,
                null, "_id = 1", null, null);
        if (c.moveToFirst()) {
            exist = true;
        }
        if (c != null) {
            c.close();
            c = null;
        }
        return exist;
    }

    private class LoadInfo extends AsyncTask<Void, Void, Void> {
        private Context context;

        private String libKey;

        public LoadInfo(Context context, String libKey) {
            this.context = context;
            this.libKey = libKey;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (DictLibrary.STARDICT.equals(libKey)) {
                libraryName = STARDICT_PATH;
                libraryInfo = DictInfo.readDicInfo(context, STARDICT_PATH + ".ifo");
            } else if (DictLibrary.OXFORD.equals(libKey)) {
                libraryName = OXFORD_PATH;
                libraryInfo = DictInfo.readDicInfo(context, OXFORD_PATH + ".ifo");
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
            Log.d(TAG, "lib info:" + libraryInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ready = true;
            notifier.done(true);
        }
    }

    private class LoadAndInsert extends AsyncTask<Void, Void, Void> {
        private Context context;

        private String libKey;

        public LoadAndInsert(Context context, String libKey) {
            this.context = context;
            this.libKey = libKey;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (DictLibrary.STARDICT.equals(libKey)) {
                libraryName = STARDICT_PATH;
                libraryInfo = DictInfo.readDicInfo(context, STARDICT_PATH + ".ifo");
                libraryWordMap = DictIndex.loadDictIndexMap(context, STARDICT_PATH + ".idx",
                        Integer.parseInt(libraryInfo.wordCount));
                ready = true;
            } else if (DictLibrary.OXFORD.equals(libKey)) {
                libraryName = OXFORD_PATH;
                libraryInfo = DictInfo.readDicInfo(context, OXFORD_PATH + ".ifo");
                libraryWordMap = DictIndex.loadDictIndexMap(context, OXFORD_PATH + ".idx", Integer
                        .parseInt(libraryInfo.wordCount));
                ready = true;
            } else {
                Log.e(TAG, "The library to be loaded is not exist!");
            }
            doInsert();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dbInitialed = true;
            libraryWordMap.clear();
            AppSettings.saveBoolean(context, libKey, true);
            notifier.done(true);
        }

        private void doInsert() {
            long time = System.currentTimeMillis();
            if (isExist(context, libKey)) {
                AppSettings.saveBoolean(context, libKey, true);
                return;
            }
            Collection<DictIndex> col = libraryWordMap.values();
            int size = col.size();
            Log.d(TAG, "total word list size: " + size);
            int count = 0;
            int i = 0;
            int left = 0;
            ContentValues[] values = new ContentValues[500];
            for (DictIndex dictIndex : col) {
                values[i] = new ContentValues();
                values[i].put(StarDictIndex.WORD, dictIndex.word);
                values[i].put(StarDictIndex.OFFSET, dictIndex.offset);
                values[i].put(StarDictIndex.SIZE, dictIndex.size);
                i++;
                count++;
                if (i == 500) {
                    i = 0;
                    if (libKey.equals(DictLibrary.STARDICT)) {
                        context.getContentResolver().bulkInsert(StarDictIndex.CONTENT_URI, values);
                    } else if (libKey.equals(DictLibrary.OXFORD)) {
                        context.getContentResolver()
                                .bulkInsert(OxfordDictIndex.CONTENT_URI, values);
                    }
                    continue;
                }
                if (count == size && (left = count % 500) != 0) {
                    ContentValues[] copy = new ContentValues[left];
                    System.arraycopy(values, 0, copy, 0, left);
                    if (libKey.equals(DictLibrary.STARDICT)) {
                        context.getContentResolver().bulkInsert(StarDictIndex.CONTENT_URI, copy);
                    } else if (libKey.equals(DictLibrary.OXFORD)) {
                        context.getContentResolver().bulkInsert(OxfordDictIndex.CONTENT_URI, copy);
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            System.out.println(libKey + " insert dict indexs to the database cost time: " + time);
        }
    }

    public static interface WorkNotifier {
        void done(boolean done);
    }
}

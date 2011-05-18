
package com.coleman.kingword.wordinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.coleman.kingword.provider.KingWord.WordInfo;

/**
 * WordInfo helper to operate the WordInfo database, it only support three
 * operations: query, delete, store.
 * 
 * @TODO there is a issue: the database's size is limited to 1048576, if the
 *       records number is large enough, maybe some problems will happen.
 */
public class WordInfoHelper {
    private static final String[] projection = new String[] {
            WordInfo._ID, WordInfo.WORD, WordInfo.IGNORE, WordInfo.STUDY_COUNT,
            WordInfo.ERROR_COUNT, WordInfo.WEIGHT
    };

    /**
     * return the word information.
     */
    public static WordInfoVO query(Context context, String word) {
        Cursor c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection,
                WordInfo.WORD + "='" + word + "'", null, null);
        WordInfoVO wi = new WordInfoVO(word);
        if (c.moveToFirst()) {
            wi.id = c.getLong(0);
            wi.word = c.getString(1);
            wi.ignore = c.getInt(2) == 0 ? true : false;
            wi.studycount = (byte) c.getInt(3);
            wi.errorcount = (byte) c.getInt(4);
            wi.weight = (byte) c.getInt(5);
        }
        if (c != null) {
            c.close();
        }
        return wi;
    }

    /**
     * Return the rows number deleted.
     */
    public static int delete(Context context, String word) {
        int count = context.getContentResolver().delete(WordInfo.CONTENT_URI,
                WordInfo.WORD + "='" + word + "'", null);
        return count;
    }

    /**
     * Store the WordInfo to the database, true if succeed, otherwise false.
     */
    public static boolean store(Context context, WordInfoVO info) {
        if (info.id == -1) {
            Uri uri = insert(context, info);
            if (uri != null) {
                return true;
            }
        } else {
            int count = update(context, info);
            if (count != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the uri if succeed, otherwise null.
     */
    private static Uri insert(Context context, WordInfoVO info) {
        ContentValues value = null;
        Uri uri = null;
        if (info != null) {
            value = new ContentValues();
            value.put(WordInfo.WORD, info.word);
            value.put(WordInfo.IGNORE, info.ignore ? 0 : 1);
            value.put(WordInfo.STUDY_COUNT, info.studycount);
            value.put(WordInfo.ERROR_COUNT, info.errorcount);
            value.put(WordInfo.WEIGHT, info.weight);
            uri = context.getContentResolver().insert(WordInfo.CONTENT_URI, value);
        }
        return uri;
    }

    /**
     * Return count>0 if succeed, otherwise 0.
     */
    private static int update(Context context, WordInfoVO info) {
        ContentValues value = null;
        int count = 0;
        if (info != null) {
            value = new ContentValues();
            value.put(WordInfo.WORD, info.word);
            value.put(WordInfo.IGNORE, info.ignore ? 0 : 1);
            value.put(WordInfo.STUDY_COUNT, info.studycount);
            value.put(WordInfo.ERROR_COUNT, info.errorcount);
            value.put(WordInfo.WEIGHT, info.weight);
            count = context.getContentResolver().update(WordInfo.CONTENT_URI, value,
                    WordInfo._ID + "=" + info.id, null);
        }
        return count;
    }
}

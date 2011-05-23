
package com.coleman.kingword.wordinfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.WordInfo;

/**
 * Should only be used by WordItem.
 * 
 * @TODO there is a issue: the database's size is limited to 1048576, if the
 *       records number is large enough, maybe some problems will happen.
 */
public class WordInfoHelper {
    private static final String[] projection = new String[] {
            WordInfo._ID, WordInfo.WORD, WordInfo.IGNORE, WordInfo.STUDY_COUNT,
            WordInfo.ERROR_COUNT, WordInfo.WEIGHT, WordInfo.NEW_WORD
    };

    private static final String TAG = WordInfoHelper.class.getName();

    /**
     * You must make sure the word is not empty.
     */
    public static boolean upgrade(Context context, String word) {
        WordInfoVO wordinfo = getWordInfo(context, word);
        if (wordinfo.increaseWeight() && store(context, wordinfo)) {
            return true;
        }
        return false;
    }

    /**
     * You must make sure the word is not empty.
     */
    public static boolean degrade(Context context, String word) {
        WordInfoVO wordinfo = getWordInfo(context, word);
        if (wordinfo.decreaseWeight() && store(context, wordinfo)) {
            return true;
        }
        return false;
    }

    /**
     * You must make sure the word is not empty.
     */
    public static boolean study(Context context, String word) {
        WordInfoVO wordinfo = getWordInfo(context, word);
        wordinfo.studycount++;
        return store(context, wordinfo);
    }

    /**
     * You must make sure the word is not empty.
     */
    public static boolean reportMiss(Context context, String word) {
        WordInfoVO wordinfo = getWordInfo(context, word);
        wordinfo.errorcount++;
        return store(context, wordinfo);
    }

    /**
     * You must make sure the word is not empty.
     */
    public static boolean ignore(Context context, String word) {
        WordInfoVO wordinfo = getWordInfo(context, word);
        wordinfo.ignore = true;
        return store(context, wordinfo);
    }

    /**
     * You must make sure the word is not empty.
     */
    public static WordInfoVO getWordInfo(Context context, String word) {
        Cursor c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection,
                WordInfo.WORD + "='" + word + "'", null, null);
        WordInfoVO wi = new WordInfoVO(word);
        if (c.moveToFirst()) {
            wi.id = c.getLong(0);
            wi.word = c.getString(1);
            wi.ignore = c.getInt(2) == 2 ? true : false;
            wi.studycount = (byte) c.getInt(3);
            wi.errorcount = (byte) c.getInt(4);
            wi.weight = (byte) c.getInt(5);
            wi.newword = c.getInt(6) == 2 ? true : false;
        }
        Log.d(TAG, "word info:" + wi);
        if (c != null) {
            c.close();
        }
        return wi;
    }

    /**
     * You must make sure the word is not empty.
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
            value.put(WordInfo.IGNORE, info.ignore ? 2 : 1);
            value.put(WordInfo.STUDY_COUNT, info.studycount);
            value.put(WordInfo.ERROR_COUNT, info.errorcount);
            value.put(WordInfo.WEIGHT, info.weight);
            value.put(WordInfo.NEW_WORD, info.newword ? 2 : 1);
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
            value.put(WordInfo.IGNORE, info.ignore ? 2 : 1);
            value.put(WordInfo.STUDY_COUNT, info.studycount);
            value.put(WordInfo.ERROR_COUNT, info.errorcount);
            value.put(WordInfo.WEIGHT, info.weight);
            value.put(WordInfo.NEW_WORD, info.newword ? 2 : 1);
            count = context.getContentResolver().update(WordInfo.CONTENT_URI, value,
                    WordInfo._ID + "=" + info.id, null);
        }
        return count;
    }
}

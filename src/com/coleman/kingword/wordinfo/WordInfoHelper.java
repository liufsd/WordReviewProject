
package com.coleman.kingword.wordinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.wordlist.SliceWordList;
import com.coleman.util.Config;

/**
 * Should only be used by WordItem.
 * 
 * @TODO there is a issue: the database's size is limited to 1048576, if the
 *       records number is large enough, maybe some problems will happen.
 */
public class WordInfoHelper {
    private static final String[] projection = new String[] {
            WordInfo._ID, WordInfo.WORD, WordInfo.IGNORE, WordInfo.STUDY_COUNT,
            WordInfo.ERROR_COUNT, WordInfo.WEIGHT, WordInfo.NEW_WORD, WordInfo.REVIEW_TYPE,
            WordInfo.REVIEW_TIME
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
            wi.review_type = (byte) c.getInt(7);
            wi.review_time = c.getLong(8);
        }
        Log.d(TAG, "word info:" + wi);
        if (c != null) {
            c.close();
        }
        return wi;
    }

    /**
     * get a list of wordinfo
     * 
     * @param context
     * @param type map the value of SubWordListActivity's type.
     * @return
     */
    public static ArrayList<WordInfoVO> getWordInfoList(Context context, byte type) {
        ArrayList<WordInfoVO> list = new ArrayList<WordInfoVO>();
        Cursor c = null;
        switch (type) {
            case SliceWordList.NEW_WORD_BOOK_LIST:
                c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection,
                // 2 means new word
                        WordInfo.NEW_WORD + "= 2", null, null);

                break;
            case SliceWordList.SCAN_LIST:
                c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection,
                // 2 means ignore word
                        WordInfo.IGNORE + "= 2", null, null);

                break;
            case SliceWordList.REVIEW_LIST:
                long ct = System.currentTimeMillis();
                String selection = "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_HOUR
                        + " and " + WordInfo.REVIEW_TIME + "<=" + (ct - 40 * 60 * 1000) + ")"
                        + " or " + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_12_HOUR
                        + " and " + WordInfo.REVIEW_TIME + "<=" + (ct - 12 * 60 * 60 * 1000) + ")"
                        + " or " + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_DAY
                        + " and " + WordInfo.REVIEW_TIME + "<=" + (ct - 24 * 60 * 60 * 1000) + ")"
                        + " or " + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_5_DAY
                        + " and " + WordInfo.REVIEW_TIME + "<=" + (ct - 5 * 24 * 60 * 60 * 1000)
                        + ")" + " or " + "(" + WordInfo.REVIEW_TYPE + "="
                        + WordInfoVO.REVIEW_20_DAY + " and " + WordInfo.REVIEW_TIME + "<="
                        + (ct - 20 * 24 * 60 * 60 * 1000) + ")";
                c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection, selection,
                        null, null);
                break;
            default:
                break;
        }
        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                WordInfoVO wi = new WordInfoVO("");
                wi.id = c.getLong(0);
                wi.word = c.getString(1);
                wi.ignore = c.getInt(2) == 2 ? true : false;
                wi.studycount = (byte) c.getInt(3);
                wi.errorcount = (byte) c.getInt(4);
                wi.weight = (byte) c.getInt(5);
                wi.newword = c.getInt(6) == 2 ? true : false;
                wi.review_type = (byte) c.getInt(7);
                wi.review_time = c.getLong(8);
                list.add(wi);
                c.moveToNext();
            }
        }
        if (c != null) {
            c.close();
        }
        return list;
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
                info.id = Long.parseLong(uri.getPathSegments().get(1));
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

    public static void _BACKUP_WHOLE_LIST(Context context) {
        if (!Config.isExternalMediaMounted()) {
            Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            return;
        }
        final String PATH = Environment.getExternalStorageDirectory() + File.separator
                + "/kingword/backup";
        Log.d(TAG, "path:" + PATH);
        try {
            File file = new File(PATH);
            if (!file.getParentFile().exists()) {
                boolean s = file.getParentFile().mkdirs();
                Log.d(TAG, file.getParent() + " success mk dir: " + s);
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            Cursor c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection, null,
                    null, null);
            if (c != null && c.moveToFirst()) {
                dos.writeInt(c.getCount());
                while (!c.isAfterLast()) {
                    dos.writeLong(c.getLong(0));
                    dos.writeUTF(c.getString(1) == null ? "" : c.getString(1));
                    Log.d(TAG, "word:" + c.getString(1));
                    dos.writeInt(c.getInt(2));
                    dos.writeInt(c.getInt(3));
                    dos.writeInt(c.getInt(4));
                    dos.writeInt(c.getInt(5));
                    dos.writeInt(c.getInt(6));
                    dos.writeInt(c.getInt(7));
                    dos.writeLong(c.getLong(8));
                    c.moveToNext();
                }
            }
            c.close();
            dos.flush();
            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void _RESTORE_WHOLE_LIST(Context context) {
        if (!Config.isExternalMediaMounted()) {
            Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            return;
        }
        final String PATH = Environment.getExternalStorageDirectory() + File.separator
                + "kingword/backup";
        try {

            File file = new File(PATH);
            if (!file.exists()) {
                Toast.makeText(context, "No backup record found!", Toast.LENGTH_SHORT).show();
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            if (dis.available() < 4) {
                Toast.makeText(context, "File is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            int count = dis.readInt();
            ContentValues cv[] = new ContentValues[count];
            for (int i = 0; i < count; i++) {
                cv[i] = new ContentValues();
                // WordInfo._ID, WordInfo.WORD, WordInfo.IGNORE,
                // WordInfo.STUDY_COUNT,
                // WordInfo.ERROR_COUNT, WordInfo.WEIGHT, WordInfo.NEW_WORD
                long id = dis.readLong();
                String word = dis.readUTF();
                int ignore = dis.readInt();
                int scount = dis.readInt();
                int ecount = dis.readInt();
                int weight = dis.readInt();
                int newword = dis.readInt();
                int review_type = dis.readInt();
                long review_time = dis.readLong();
                Log.d(TAG, "word info: id " + id + " word " + word + " ignore " + ignore
                        + " scount " + scount + " ecount " + ecount + " weight " + weight
                        + " newword " + newword);
                // cv[i].put(WordInfo._ID, id);
                cv[i].put(WordInfo.WORD, word);
                cv[i].put(WordInfo.IGNORE, ignore);
                cv[i].put(WordInfo.STUDY_COUNT, scount);
                cv[i].put(WordInfo.ERROR_COUNT, ecount);
                cv[i].put(WordInfo.WEIGHT, weight);
                cv[i].put(WordInfo.NEW_WORD, newword);
                cv[i].put(WordInfo.REVIEW_TYPE, review_type);
                cv[i].put(WordInfo.REVIEW_TIME, review_time);
            }
            dis.close();
            fis.close();
            context.getContentResolver().bulkInsert(WordInfo.CONTENT_URI, cv);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            value.put(WordInfo.REVIEW_TYPE, info.review_type);
            value.put(WordInfo.REVIEW_TIME, info.review_time);
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
            value.put(WordInfo.REVIEW_TYPE, info.review_type);
            value.put(WordInfo.REVIEW_TIME, info.review_time);
            count = context.getContentResolver().update(WordInfo.CONTENT_URI, value,
                    WordInfo._ID + "=" + info.id, null);
        }
        return count;
    }
}

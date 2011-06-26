
package com.coleman.kingword.wordinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import com.coleman.util.Log;
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
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                Log.d(TAG, "=====current hour:" + hour);
                String selection = (hour >= 18 ? WordInfo.NEW_WORD + " = 2 or " : "") + "("
                        + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_HOUR + " and "
                        + WordInfo.REVIEW_TIME + "<=" + (ct - 40 * 60 * 1000l) + ")" + " or " + "("
                        + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_12_HOUR + " and "
                        + WordInfo.REVIEW_TIME + "<=" + (ct - 12 * 60 * 60 * 1000l) + ")" + " or "
                        + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_1_DAY + " and "
                        + WordInfo.REVIEW_TIME + "<=" + (ct - 24 * 60 * 60 * 1000l) + ")" + " or "
                        + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_5_DAY + " and "
                        + WordInfo.REVIEW_TIME + "<=" + (ct - 5 * 24 * 60 * 60 * 1000l) + ")"
                        + " or " + "(" + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_20_DAY
                        + " and " + WordInfo.REVIEW_TIME + "<=" + (ct - 20 * 24 * 60 * 60 * 1000l)
                        + ")" + " or " + "(" + WordInfo.REVIEW_TYPE + "="
                        + WordInfoVO.REVIEW_40_DAY + " and " + WordInfo.REVIEW_TIME + "<="
                        + (ct - 40 * 24 * 60 * 60 * 1000l) + ")" + " or " + "("
                        + WordInfo.REVIEW_TYPE + "=" + WordInfoVO.REVIEW_60_DAY + " and "
                        + WordInfo.REVIEW_TIME + "<=" + (ct - 60 * 24 * 60 * 60 * 1000l) + ")";
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

    public static void backupWordInfoDB(final Context context, final boolean toast) {
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (toast) {
                    Toast.makeText(context.getApplicationContext(), "Backup successful!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("Backup successful!");
                }
            }
        };
        new Thread() {
            public void run() {
                long time = System.currentTimeMillis();
                final String PATH = Environment.getExternalStorageDirectory() + File.separator
                        + "kingword/backup";
                Log.d(TAG, "path:" + PATH);
                try {
                    File file = new File(PATH);
                    if (!file.getParentFile().exists()) {
                        boolean s = file.getParentFile().mkdirs();
                        Log.d(TAG, file.getParent() + " success mk dir: " + s);
                    }
                    file.createNewFile();
                    RandomAccessFile ranfile = new RandomAccessFile(file, "rw");
                    Cursor c = null;

                    boolean first = true;
                    long s_id = 0;
                    int cur_count = 0;
                    int total_count = 0;
                    int limit = 500;
                    String selection = null;
                    ranfile.writeInt(0);

                    while (first || cur_count == 500) {
                        first = false;
                        selection = "_id >" + s_id;
                        c = context.getContentResolver().query(WordInfo.CONTENT_URI, projection,
                                selection, null, "_id asc limit " + limit);
                        cur_count = c.getCount();
                        total_count += cur_count;
                        Log.d(TAG, "cur_count:" + cur_count);
                        if (c != null && c.moveToFirst()) {
                            while (!c.isAfterLast()) {
                                ranfile.writeLong(c.getLong(0));
                                ranfile.writeUTF(c.getString(1) == null ? "" : c.getString(1));
                                Log.d(TAG, "word:" + c.getString(1));
                                ranfile.writeInt(c.getInt(2));
                                ranfile.writeInt(c.getInt(3));
                                ranfile.writeInt(c.getInt(4));
                                ranfile.writeInt(c.getInt(5));
                                ranfile.writeInt(c.getInt(6));
                                ranfile.writeInt(c.getInt(7));
                                ranfile.writeLong(c.getLong(8));
                                if (c.isLast()) {
                                    s_id = c.getLong(0);
                                    System.out.println("next loop start index: " + s_id);
                                }
                                c.moveToNext();
                            }
                        }
                        c.close();
                    }
                    ranfile.seek(0);
                    ranfile.writeInt(total_count);
                    System.out.println("total back word nums: " + total_count);
                    ranfile.close();
                    handler.sendEmptyMessage(0);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                time = System.currentTimeMillis() - time;
                System.out.println("backup cost time: " + time);
            }
        }.start();
    }

    public static void restoreWordInfoDB(final Context context, final boolean toast) {
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(context, "External media not mounted!", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }
        final String PATH = Environment.getExternalStorageDirectory() + File.separator
                + "kingword/backup";
        try {

            final File file = new File(PATH);
            if (!file.exists()) {
                if (toast) {
                    Toast.makeText(context, "No backup record found!", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("No backup record found!");
                }
                return;
            }
            final FileInputStream fis = new FileInputStream(file);
            final DataInputStream dis = new DataInputStream(fis);
            if (dis.available() < 4) {
                if (toast) {
                    Toast.makeText(context, "File is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("File is empty!");
                }
                return;
            }
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (toast) {
                        Toast.makeText(context.getApplicationContext(), "Restore successful!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("Restore successful!");
                    }
                }
            };
            new Thread() {
                public void run() {
                    long time = System.currentTimeMillis();
                    int count = 0;
                    try {
                        count = dis.readInt();
                        ContentValues cv[] = new ContentValues[count];
                        for (int i = 0; i < count; i++) {
                            cv[i] = new ContentValues();
                            long id = dis.readLong();
                            String word = dis.readUTF();
                            int ignore = dis.readInt();
                            int scount = dis.readInt();
                            int ecount = dis.readInt();
                            int weight = dis.readInt();
                            int newword = dis.readInt();
                            int review_type = dis.readInt();
                            long review_time = dis.readLong();
                            Log.d(TAG, "word info: id " + id + " word " + word + " ignore "
                                    + ignore + " scount " + scount + " ecount " + ecount
                                    + " weight " + weight + " newword " + newword);

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
                        file.delete();
                        context.getContentResolver().delete(WordInfo.CONTENT_URI, null, null);
                        int slice = 500;
                        int times = cv.length / slice;
                        int left = cv.length % slice;
                        ContentValues subcv[] = new ContentValues[slice];
                        for (int i = 0; i < times; i++) {
                            System.arraycopy(cv, i * slice, subcv, 0, slice);
                            context.getContentResolver().bulkInsert(WordInfo.CONTENT_URI, subcv);
                            System.out.println("insert " + (i * slice) + "-" + (i * slice + slice)
                                    + " records to db!");
                        }
                        if (left > 0) {
                            subcv = new ContentValues[left];
                            System.arraycopy(cv, times * slice, subcv, 0, left);
                            context.getContentResolver().bulkInsert(WordInfo.CONTENT_URI, subcv);
                            System.out.println("insert " + (times * slice) + "-"
                                    + (times * slice + left) + " records to db!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (handler != null) {
                        handler.sendEmptyMessage(0);
                    }
                    time = System.currentTimeMillis() - time;
                    System.out.println("restore cost time: " + time);
                }
            }.start();
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

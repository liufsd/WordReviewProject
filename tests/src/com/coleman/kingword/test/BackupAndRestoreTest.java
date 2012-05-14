
package com.coleman.kingword.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.coleman.kingword.provider.KingWord.WordInfo;
import com.coleman.kingword.wordinfo.WordInfoHelper;
import com.coleman.util.Config;
import com.coleman.util.Log;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.test.AndroidTestCase;
import android.widget.Toast;

public class BackupAndRestoreTest extends AndroidTestCase {
    protected static final String TAG = "BackupAndRestoreTest";
    private static final String[] projection = new String[] {
            WordInfo._ID,
            WordInfo.WORD, WordInfo.IGNORE, WordInfo.STUDY_COUNT,
            WordInfo.ERROR_COUNT, WordInfo.WEIGHT, WordInfo.NEW_WORD,
            WordInfo.REVIEW_TYPE, WordInfo.REVIEW_TIME
    };

    @Override
    protected void setUp() throws Exception {
        mContext = getContext();
        System.out.println("setUp...");
    }

    public void testBackAndroidRestore() {
        boolean testBackup = true;
        if (testBackup) {
            backup();
        } else {
            restore();
        }
    }

    public void restore() {
        final boolean toast = false;
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(mContext, "External media not mounted!",
                        Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }
        final String PATH = Environment.getExternalStorageDirectory()
                + File.separator + "kingword/backup";
        try {

            final File file = new File(PATH);
            if (!file.exists()) {
                if (toast) {
                    Toast.makeText(mContext, "No backup record found!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("No backup record found!");
                }
                return;
            }
            final FileInputStream fis = new FileInputStream(file);
            final DataInputStream dis = new DataInputStream(fis);
            if (dis.available() < 4) {
                if (toast) {
                    Toast.makeText(mContext, "File is empty!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("File is empty!");
                }
                return;
            }
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (toast) {
                        Toast.makeText(mContext.getApplicationContext(),
                                "Restore successful!", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        System.out.println("Restore successful!");
                    }
                }
            };
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
                    Log.d(TAG, "word info: id " + id + " word " + word
                            + " ignore " + ignore + " scount " + scount
                            + " ecount " + ecount + " weight " + weight
                            + " newword " + newword);

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
                mContext.getContentResolver().delete(WordInfo.CONTENT_URI,
                        "_id > 0", null);
                mContext.getContentResolver().bulkInsert(WordInfo.CONTENT_URI,
                        cv);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
            time = System.currentTimeMillis() - time;
            System.out.println("restore cost time: " + time);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }

    public void backup() {
        final boolean toast = false;
        if (!Config.isExternalMediaMounted()) {
            if (toast) {
                Toast.makeText(mContext, "External media not mounted!",
                        Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("External media not mounted!");
            }
            return;
        }
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (toast) {
                    Toast.makeText(mContext.getApplicationContext(),
                            "Backup successful!", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("Backup successful!");
                }
            }
        };
        long time = System.currentTimeMillis();
        final String PATH = Environment.getExternalStorageDirectory()
                + File.separator + "kingword/backup";
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
            Cursor c = mContext.getContentResolver().query(
                    WordInfo.CONTENT_URI, projection, null, null, null);
            if (c != null && c.moveToFirst()) {
                dos.writeInt(30000);
                int len = 30000 / c.getCount();
                while (!c.isAfterLast()) {
                    for (int i = 0; i < len; i++) {
                        dos.writeLong(c.getLong(0));
                        dos.writeUTF(c.getString(1) == null ? "" : c
                                .getString(1) + i);
                        // Log.d(TAG, "word:" + c.getString(1)+i);
                        dos.writeInt(c.getInt(2));
                        dos.writeInt(c.getInt(3));
                        dos.writeInt(c.getInt(4));
                        dos.writeInt(c.getInt(5));
                        dos.writeInt(c.getInt(6));
                        dos.writeInt(c.getInt(7));
                        dos.writeLong(c.getLong(8));
                    }
                    c.moveToNext();
                }
            }
            c.close();
            dos.flush();
            dos.close();
            fos.close();
            handler.sendEmptyMessage(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("backup cost time: " + time);
    }
}

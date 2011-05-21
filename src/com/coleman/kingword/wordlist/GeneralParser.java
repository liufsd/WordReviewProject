
package com.coleman.kingword.wordlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.coleman.kingword.provider.KingWord.WordsList;
import com.coleman.kingword.wordlist.WordListManager.IProgressNotifier;
import com.coleman.util.FileAccessor;

/**
 * A general parser should have the ability to parse the following format:
 * [number][.] [hello world][.!] [你好世界！]
 * 
 * @author coleman
 */
public class GeneralParser {
    private static final String TAG = GeneralParser.class.getName();

    /**
     * Assets is pre-installed files.
     * 
     * @param fileName
     * @throws IOException
     */
    static ArrayList<String> parseAsset(Context context, String fileName, IProgressNotifier notifier)
            throws IOException {
        // init notifier vars
        int max = 30;// progress here can go for 30%
        int cur = 0;// current progress 0%
        int total_size = 1;// total stream size
        int load_size = 1;// already load stream size

        // get the file's input stream
        InputStream is = context.getAssets().open(fileName);
        total_size = is.available();

        // process the stream line by line
        int v;
        byte bytes[] = new byte[1024];
        ByteArrayBuffer baf = new ByteArrayBuffer(1024 * 200);
        while ((v = is.read(bytes)) != -1) {
            baf.append(bytes, 0, v);
            load_size = baf.length();
            cur = load_size * (max - 10) / (total_size + 1);
            notifier.notify(cur);
        }
        is.close();

        // write the asset files to the package data
        final String CACHE_FILE = "/cache";
        FileOutputStream fos = new FileOutputStream(context.getFilesDir() + File.separator
                + CACHE_FILE);
        fos.write(baf.toByteArray());
        fos.flush();
        fos.close();
        notifier.notify(25);
        // read the file just stored and delete it
        File file = new File(context.getFilesDir() + File.separator + CACHE_FILE);
        FileAccessor fa = new FileAccessor(file, "rw");
        String line;
        ArrayList<String> list = new ArrayList<String>();
        while ((line = fa.readLine()) != null) {
            list.add(line);
            Log.d(TAG, "line:" + line);
        }
        notifier.notify(28);
        fa.close();
        file.delete();
        notifier.notify(30);
        return list;
    }

    /**
     * The files stored in the internal or external files.
     * 
     * @param fileName
     * @throws IOException
     */
    public static ArrayList<String> parseFile(String fileName) throws IOException {
        FileAccessor fa = new FileAccessor(fileName, "rw");
        String line;
        ArrayList<String> list = new ArrayList<String>();
        while ((line = fa.readLine()) != null) {
            list.add(line);
            Log.d(TAG, "line:" + line);
        }
        fa.close();
        return list;
    }
}

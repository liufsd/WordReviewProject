
package com.coleman.wordlist;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import com.coleman.util.FileAccessor;

import android.content.Context;
import android.database.CharArrayBuffer;
import android.util.Log;

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
    static ArrayList<String> parseAsset(Context context, String fileName) throws IOException {
        // get the file's input stream
        InputStream is = context.getAssets().open(fileName);

        // process the stream line by line
        int v;
        byte bytes[] = new byte[1024];
        ByteArrayBuffer baf = new ByteArrayBuffer(1024 * 200);
        while ((v = is.read(bytes)) != -1) {
            baf.append(bytes, 0, v);
        }
        is.close();

        // write the asset files to the package data
        final String CACHE_FILE = "/cache";
        FileOutputStream fos = new FileOutputStream(context.getFilesDir() + File.separator
                + CACHE_FILE);
        fos.write(baf.toByteArray());
        fos.flush();
        fos.close();

        // read the file just stored and delete it
        File file = new File(context.getFilesDir() + File.separator + CACHE_FILE);
        FileAccessor fa = new FileAccessor(file, "rw");
        String line;
        ArrayList<String> list = new ArrayList<String>();
        while ((line = fa.readLine()) != null) {
            list.add(line);
            Log.d(TAG, "line:" + line);
        }
        fa.close();
        file.delete();
        return list;
    }

    /**
     * The files stored in the internal or external files.
     * 
     * @param fileName
     */
    public static void parseFiles(String fileName) {

    }
}


package com.coleman.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;

import com.coleman.log.Log;

/**
 * A general parser should have the ability to parse the following format:
 * [number][.] [hello world][.!] [你好世界！]
 * 
 * @author coleman
 */
public class GeneralParser {
    private static final String TAG = GeneralParser.class.getName();

    private static Log Log = Config.getLog();

    /**
     * Assets is pre-installed files.
     * 
     * @param fileName
     * @throws IOException
     */
    public static ArrayList<String> parseFile(Context context, String fileName, boolean fromAsset,
            IProgressNotifier notifier) throws IOException {
        InputStream is = null;

        // init notifier vars
        int max = 30;// progress here can go for 30%
        int cur = 0;// current progress 0%
        int total_size = 1;// total stream size
        int load_size = 1;// already load stream size

        // get the file's input stream
        if (fromAsset) {
            is = context.getAssets().open(fileName);
        } else {
            is = new FileInputStream(fileName);
        }
        total_size = is.available();

        // process the stream line by line
        int v;
        byte bytes[] = new byte[1024];
        ByteArrayOutputStream baf = new ByteArrayOutputStream();
        while ((v = is.read(bytes)) != -1) {
            baf.write(bytes, 0, v);
            load_size = baf.size();
            cur = load_size * (max - 10) / (total_size + 1);
            notifier.notify(cur);
        }
        is.close();
        String str = new String(baf.toByteArray());

        ArrayList<String> list = null;
        if (isQuickList(str)) {
            list = getQuickWorldList(str);
        } else {
            list = getWorldList(str);
        }
        notifier.notify(cur + 10);

        return list;
    }

    private static boolean isQuickList(CharSequence sb) {
        final Pattern pattern = Pattern.compile("[a-zA-Z \\.\\-/]+");
        Matcher m = pattern.matcher(sb);
        return m.matches();
    }

    private static ArrayList<String> getQuickWorldList(String sb) {
        String strs[] = sb.split("/");
        ArrayList<String> list = new ArrayList<String>();
        for (String string : strs) {
            if (!TextUtils.isEmpty(string)) {
                list.add(string);
            }
        }
        Log.i(TAG, "===coleman-debug-parse words count: " + list.size());
        return list;
    }

    private static ArrayList<String> getWorldList(CharSequence sb) {
        final Pattern WORD = Pattern.compile("[a-zA-Z]+-{0,1}[a-zA-Z]+\\b");
        final Pattern WORD_TYPE = Pattern.compile("^(prep|n|v(t|i|)|ad(j|v|)|[a-zA-Z])$");
        ArrayList<String> list = new ArrayList<String>();
        Matcher m = WORD.matcher(sb);
        String tempStr;
        while (m.find()) {
            if (!WORD_TYPE.matcher(m.group()).find()) {
                tempStr = m.group();
                if (!list.contains(tempStr)) {
                    list.add(tempStr);
                }
            } else {
                // Log.i(TAG, "skip word: " + m.group());
            }
        }
        Log.i(TAG, "===coleman-debug-parse words count: " + list.size());
        return list;
    }

    /**
     * Assets is pre-installed files.
     * 
     * @param fileName
     * @throws IOException
     */
    public static HashMap<String, String> parseFile(InputStream is) throws IOException {

        // process the stream line by line
        int v;
        byte bytes[] = new byte[1024];
        ByteArrayOutputStream baf = new ByteArrayOutputStream();
        while ((v = is.read(bytes)) != -1) {
            baf.write(bytes, 0, v);
        }
        is.close();
        baf.close();
        String str = new String(baf.toByteArray());
        HashMap<String, String> list = getProperties(str);

        return list;
    }

    private static HashMap<String, String> getProperties(String sb) {
        HashMap<String, String> list = new HashMap<String, String>();
        String items[] = sb.split("\\s");
        for (String string : items) {
            if (string != null && !string.trim().equals("")) {
                list.put(string.substring(0, string.indexOf("=")).trim(),
                        string.substring(string.indexOf("=") + 1).trim());
            }
        }
        return list;
    }

    public static interface IProgressNotifier {
        void notify(int p);
    }
}

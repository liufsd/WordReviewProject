
package com.coleman.kingword.wordlist;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;

import com.coleman.kingword.wordlist.WordListManager.IProgressNotifier;

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
    static ArrayList<String> parseFile(Context context, String fileName, boolean fromAsset,
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
        ByteArrayBuffer baf = new ByteArrayBuffer(1024 * 200);
        while ((v = is.read(bytes)) != -1) {
            baf.append(bytes, 0, v);
            load_size = baf.length();
            cur = load_size * (max - 10) / (total_size + 1);
            notifier.notify(cur);
        }
        is.close();

        String str = new String(baf.toByteArray());
        ArrayList<String> list = getWorldList(str);
        notifier.notify(cur + 10);

        return list;
    }

    private static ArrayList<String> getWorldList(CharSequence sb) {
        final Pattern WORD = Pattern.compile("[a-zA-Z-]+\\b");
        final Pattern WORD_TYPE = Pattern.compile("^(prep|n|v(t|i|)|ad(j|v|)|[a-zA-Z])$");
        ArrayList<String> list = new ArrayList<String>();
        Matcher m = WORD.matcher(sb);
        while (m.find()) {
            if (!WORD_TYPE.matcher(m.group()).find()) {
                System.out.println("---------------" + m.group());
                list.add(m.group());
            } else {
                // System.out.println(m.group());
            }
        }
        return list;
    }
}

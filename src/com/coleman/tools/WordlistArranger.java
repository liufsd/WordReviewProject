
package com.coleman.tools;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.coleman.kingword.dict.DictManager;
import com.coleman.util.MyApp;

public class WordlistArranger {
    public static final String DIR = "/sdcard/kingword/wordlist/";

    public static void tidy(ArrayList<String> list, String fn) {
        try {
            fn = fn.indexOf("/") != -1 ? fn.substring(fn.lastIndexOf("/") + 1) : fn;
            FileOutputStream fos = new FileOutputStream(DIR + fn);
            BufferedWriter writer= new BufferedWriter(new OutputStreamWriter(fos));
            for (String string : list) {
                if (DictManager.getInstance().hasWord(MyApp.context, string)) {
                    writer.write(string + "/");
                }
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

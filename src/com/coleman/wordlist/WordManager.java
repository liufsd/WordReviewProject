
package com.coleman.wordlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

public class WordManager {
    HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

    private static WordManager manager;

    private WordManager() {
    }

    public static WordManager getInstance() {
        if (manager == null) {
            manager = new WordManager();
        }
        return manager;
    }

    public void addWordList(Context context, String wordlistName, boolean inAsset)
            throws IOException {
        if (inAsset) {
            ArrayList<String> list = GeneralParser.parseAsset(context, wordlistName);
            map.put(wordlistName, list);
        }
    }

    public ArrayList<String> getWordList(String wordlistName) {
        return map.get(wordlistName);
    }
}

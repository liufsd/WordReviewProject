
package com.coleman.kingword.provider;

import java.util.HashMap;

import com.coleman.kingword.provider.KingWord.WordsList.SubWordsList;
import com.coleman.kingword.provider.KingWord.WordsList.WordListItem;

public class WordListManager {
    private static WordListManager manager;

    private HashMap<String, SubWordsList> subTables = new HashMap<String, SubWordsList>();

    private HashMap<String, WordListItem> itemTables = new HashMap<String, KingWord.WordsList.WordListItem>();

    private SubWordsList wordslist;

    private WordListItem wordsitem;

    private WordListManager() {
    }

    public static WordListManager getInstance() {
        if (manager == null) {
            manager = new WordListManager();
        }
        return manager;
    }

    public void init() {
        // TODO need to init the word list
    }

    public SubWordsList getCurrentSubWordsList() {
        return wordslist;
    }

    public WordListItem getCurrentWordListItem() {
        return wordsitem;
    }
}

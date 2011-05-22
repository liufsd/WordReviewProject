
package com.coleman.kingword.wordlist;

import android.content.ContentValues;
import android.content.Context;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.dict.stardict.DictData;
import com.coleman.kingword.provider.KingWord.WordListItem;

public class WordItem {
    public long id;

    public String word;

    /**
     * 2 true 1 false
     */
    public boolean ignore;

    /**
     * if viewed
     */
    boolean passView;

    /**
     * if pass view counted in subwordlist.
     */
    boolean _pvc;

    /**
     * if pass alternative
     */
    boolean passAlternative;

    /**
     * if pass Alternative counted in subwordlist.
     */
    boolean _pac;

    /**
     * if pass multiple
     */
    boolean passMultiple;

    /**
     * if pass Multiple counted in subwordlist.
     */
    boolean _pmc;

    private DictData dictData;

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(WordListItem.WORD, word);
        cv.put(WordListItem.IGNORE, ignore ? 2 : 1);
        return cv;
    }

    public boolean isComplete() {
        return passView && passAlternative && passMultiple;
    }

    public void setPassView() {
        passView = true;
        if (!_pvc) {
            _pvc = true;
            SubWordList.passViewCount++;
        }
    }

    public void setPassAlternative(boolean correct) {
        if (correct) {
            passAlternative = true;
            if (!_pac) {
                _pac = true;
                SubWordList.passAltCount++;
            }
        } else {
            passView = false;
        }
    }

    public void setPassMultiple(boolean correct) {
        if (correct) {
            passMultiple = true;
            if (!_pmc) {
                _pmc = true;
                SubWordList.passMulCount++;
            }
        } else {
            passAlternative = false;
            passView = false;
        }
    }

    public DictData getDictData(Context context) {
        if (dictData == null) {
            dictData = DictManager.getInstance().viewWord(context, word);
        }
        return dictData;
    }
}

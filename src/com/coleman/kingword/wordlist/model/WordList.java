
package com.coleman.kingword.wordlist.model;

import java.io.Serializable;

import android.content.ContentValues;

import com.coleman.kingword.provider.KingWord.TWordList;

public class WordList implements Serializable {
    private static final long serialVersionUID = -5031315199643078115L;

    public static interface InternalWordList {
        String POSTGRADUATE_WORDLIST = "kingword/wordlist/cet4.txt";
    }

    private static final String TAG = "WordList";

    public long id = -1;

    public String describe = "";

    public String path_name = "";

    public SetMethod set_method = SetMethod.DEFAULT_DEVIDE;

    public WordList() {
    }

    public WordList(String describe, String path_name, SetMethod method) {
        this.describe = describe == null ? "" : describe;
        this.path_name = path_name == null ? "" : path_name;
        this.set_method = method == null ? this.set_method : method;
    }

    public static enum SetMethod {
        DEFAULT_DEVIDE(0), AVARAGE_DEVIDE(1), CHARACTER_DEVIDER(2);
        private final int value;

        private SetMethod(int value) {
            this.value = value;
        }

        public SetMethod getSetMethod(int value) {
            switch (value) {
                case 1:
                    return AVARAGE_DEVIDE;
                case 2:
                    return CHARACTER_DEVIDER;
                case 0:
                default:
                    return DEFAULT_DEVIDE;
            }
        }

        public int getValue() {
            return value;
        }
    }

    public ContentValues toContentValues() {
        ContentValues value = new ContentValues();
        value.put(TWordList.DESCRIBE, describe);
        value.put(TWordList.PATH_NAME, path_name);
        value.put(TWordList.SET_METHOD, set_method.getValue());
        return value;
    }

}

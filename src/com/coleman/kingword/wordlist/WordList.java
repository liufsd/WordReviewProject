
package com.coleman.kingword.wordlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WordList {
    public static interface InternalWordList {
        String POSTGRADUATE_WORDLIST = "kingword/wordlist/postgraduate/postgraduate.wl";

        String POSTGRADUATE_PHRASE_WORDLIST = "kingword/wordlist/postgraduate/phrase.wl";
    }

    public long _ID = -1;

    public String describe = "";

    public String path_name = "";

    public SetMethod set_method = SetMethod.DEFAULT_DEVIDE;

    private ArrayList<String> list = new ArrayList<String>();

    private ArrayList<List<String>> sublist = new ArrayList<List<String>>();

    private int p;

    public WordList(ArrayList<String> list) {
        this.list.addAll(list);
//        split(set_method);
        p = 0;
    }

    public String getWord() {
        return list.get(p);
    }

    public String getPre() {
        p = p - 1 < 0 ? 0 : p - 1;
        return list.get(p);
    }

    public String getNext() {
        p = p + 1 > list.size() - 1 ? list.size() - 1 : p + 1;
        return list.get(p);
    }

    public void split(SetMethod method) {
        split(method, 20);
    }

    public void split(SetMethod method, int suggest) {
        sublist.clear();
        switch (method) {
            case AVARAGE_DEVIDE: {
                int start = 0;
                int end = list.size();
                while (start < end) {
                    int tmpEnd = start + suggest <= end ? start + suggest : end;
                    sublist.add(list.subList(start, tmpEnd));
                    start = tmpEnd;
                }
                break;
            }
            case CHARACTER_DEVIDER: {
                int start = 0;
                int size = list.size();
                char prefix = '!';
                sort(list);
                if (size > 0) {
                    prefix = list.get(0).charAt(0);
                }
                for (int i = 0; i < size; i++) {
                    if (list.get(i).charAt(i) != prefix) {
                        sublist.add(list.subList(start, i));
                        start = i;
                    }
                }
                break;
            }
            case DEFAULT_DEVIDE:
            default: {
                sort(list);
                int start = 0;
                int end = list.size();
                while (start < end) {
                    int tmpEnd = start + suggest <= end ? start + suggest : end;
                    sublist.add(list.subList(start, tmpEnd));
                    start = tmpEnd;
                }
                break;
            }
        }
    }

    public ArrayList<String> getList() {
        return list;
    }

    /**
     * Return the split result of wordlist, it is a collection of raw wordlist's
     * sublist.
     */
    public ArrayList<List<String>> getSubList() {
        return sublist;
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

    private void sort(ArrayList<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return object1.compareTo(object2);
            }

        });
    }

}

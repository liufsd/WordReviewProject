
package com.coleman.kingword.wordinfo;

public class WordInfoVO {
    public long id = -1;

    public String word;

    public boolean ignore = false;

    public byte studycount = 0;

    public byte errorcount = 0;

    public byte weight = 1;

    public WordInfoVO(String word) {
        this.word = word;
    }
}

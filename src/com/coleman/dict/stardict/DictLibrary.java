
package com.coleman.dict.stardict;

import java.util.HashMap;

public class DictLibrary {
    public static final String OXFORD = "oxford";

    public static final String STARDICT = "stardict";

    private DictInfo libraryInfo;

    private HashMap<String, DictIndex> libraryWordMap;

    private String libraryName;

    public DictLibrary(DictInfo libraryInfo, HashMap<String, DictIndex> libraryWordMap,
            String libraryName) {
        this.libraryInfo = libraryInfo;
        this.libraryWordMap = libraryWordMap;
        this.libraryName = libraryName;
    }

    public DictInfo getLibraryInfo() {
        return libraryInfo;
    }

    public DictIndex getDictIndex(String word) {
        return libraryWordMap.get(word);
    }

    public String getLibraryName() {
        return libraryName;
    }
}

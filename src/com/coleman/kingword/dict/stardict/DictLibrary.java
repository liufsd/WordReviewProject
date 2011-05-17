
package com.coleman.kingword.dict.stardict;

import java.util.HashMap;

public class DictLibrary {
    public static final String OXFORD = "oxford";

    public static final String STARDICT = "stardict";

    public static final String OXFORD_PATH = "kingword/dicts/oxford-gb-formated";

    public static final String STARDICT_PATH = "kingword/dicts/stardict1.3";

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


package com.coleman.dict.stardict;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

public class DictParser {
    private static final String TAG = DictParser.class.getName();

    private static final String oxford = "kingword/dicts/oxford-gb-formated";

    private static final String stardict = "kingword/dicts/stardict1.3";

    public static DictLibrary loadLibrary(Context context, String libKey) throws IOException {
        if (DictLibrary.STARDICT.equals(libKey)) {
            return loadLibraryLocal(context, stardict);
        } else if (DictLibrary.OXFORD.equals(libKey)) {
            return loadLibraryLocal(context, oxford);
        } else {
            throw new IOException("The library to be loaded is not exist!");
        }
    }

    private static DictLibrary loadLibraryLocal(Context context, String libName) {
        DictInfo ifo = DictInfo.readDicInfo(context, libName + ".ifo");
        HashMap<String, DictIndex> wordMap = DictIndex.readIndexFile(context, libName + ".idx",
                Integer.parseInt(ifo.wordCount));
        return new DictLibrary(ifo, wordMap, libName);
    }

}

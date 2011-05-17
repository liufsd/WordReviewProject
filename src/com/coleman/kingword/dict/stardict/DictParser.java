
package com.coleman.kingword.dict.stardict;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

public class DictParser {
    private static final String TAG = DictParser.class.getName();

    public static DictLibrary loadLibrary(Context context, String libKey) throws IOException {
        if (DictLibrary.STARDICT.equals(libKey)) {
            return loadLibraryLocal(context, DictLibrary.STARDICT_PATH);
        } else if (DictLibrary.OXFORD.equals(libKey)) {
            return loadLibraryLocal(context, DictLibrary.OXFORD_PATH);
        } else {
            throw new IOException("The library to be loaded is not exist!");
        }
    }

    private static DictLibrary loadLibraryLocal(Context context, String libName) {
        DictInfo ifo = DictInfo.readDicInfo(context, libName + ".ifo");
        HashMap<String, DictIndex> wordMap = DictIndex.loadDictIndexMap(context, libName + ".idx",
                Integer.parseInt(ifo.wordCount));
        return new DictLibrary(ifo, wordMap, libName);
    }

}

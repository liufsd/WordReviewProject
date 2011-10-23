
package com.coleman.kingword.dict.stardict;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

public class DictLibraryFactory {

    /**
     * This work is time expensive, you should do it on the background thread.
     * 
     * @param context
     * @param libKey
     * @param dictmap
     * @throws IOException
     */
    public void loadLibrary(Context context, String libKey, boolean isInternal,
            HashMap<String, DictLibrary> dictmap) throws IOException {
        DictLibrary lib = new DictLibrary(context, libKey, isInternal);
        dictmap.put(libKey, lib);
    }

}

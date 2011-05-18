
package com.coleman.kingword.dict.stardict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.coleman.kingword.dict.stardict.DictLibrary.WorkNotifier;

import android.content.Context;

public class DictLibraryFactory {
    ArrayList<LoadLibraryTask> list = new ArrayList<LoadLibraryTask>(2);

    HashMap<String, DictLibrary> dictmap;

    private static boolean goForhead = true;

    public void loadLibrary(Context context, String libKey, HashMap<String, DictLibrary> dictmap)
            throws IOException {
        this.dictmap = dictmap;
        list.add(new LoadLibraryTask(context, libKey));
        startWork();
    }

    private synchronized void startWork() {
        if (goForhead) {
            if (list.size() > 0) {
                list.get(0).start();
                goForhead = false;
            }
        }
    }

    private class LoadLibraryTask {
        private Context context;

        private String libKey;

        public LoadLibraryTask(Context context, String libKey) {
            this.context = context;
            this.libKey = libKey;
        }

        public void start() {
            DictLibrary lib = new DictLibrary(context, libKey, new WorkNotifier() {
                @Override
                public void done(boolean done) {
                    if (done) {
                        if (list.size() > 0) {
                            list.remove(0);
                        }
                        goForhead = true;
                        startWork();
                    }
                }
            });
            dictmap.put(libKey, lib);
        }
    }
}

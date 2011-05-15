
package com.coleman.dict.stardict;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DictParser {
    private static final String TAG = DictParser.class.getName();
    private String prefixName="kingword/dicts/oxford-gb-formated";//kingword/dicts/stardict1.3
    /**
     * the word index entry list
     */
    private List<DictIndex> dictIndexList = new ArrayList<DictIndex>();

    public DictParser(Context context, Handler h) {
        DictInfo ifo = DictInfo.readDicInfo(context, prefixName+".ifo");
        Log.d(TAG, "info:" + ifo.toString());
        Message.obtain(h, 0, ifo.toString()).sendToTarget();
        Toast.makeText(context, "info:" + ifo.toString(), 0).show();

        dictIndexList = DictIndex.readIndexFile(context, prefixName+".idx",
                Integer.parseInt(ifo.wordCount));
        int size = dictIndexList.size();
        Log.d(TAG, "words counts: " + dictIndexList.size());
        Message.obtain(h, 0, "words counts: " + dictIndexList.size()).sendToTarget();
        for (int i = 0; i < size; i++) {
            DictIndex dici = dictIndexList.get(i);
            if (dici.offset%1000000+dici.size>1000000) {
//                Log.d(TAG, " word:" + dici.word + " offset:" + dici.offset + " size:" + dici.size);
//                Message.obtain(
//                        h,
//                        0,
//                        " word:" + dici.word + " offset:" + dici.offset + " size:" + dici.size
//                                + "\n").sendToTarget();
                DictData dida = DictData.readData(context, ifo, dici, prefixName);
                Log.d(TAG, i+" >>> word:" + dici.word + "   " + dida.data);
//                Message.obtain(h, 0, "data:" + dida.data).sendToTarget();
            }
        }
    }

}

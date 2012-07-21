
package com.coleman.util;

import java.util.regex.Pattern;

import com.coleman.log.Log;

public class DictDataProcessingCenter {
    private static final String TAG = null;

    private static Log Log = Config.getLog();

    /**
     * For TTS
     * 
     * @param dictData
     * @return
     */
    public static String process(String dictData, boolean tts) {
        StringBuilder sb = new StringBuilder();
        String str[] = dictData.split("\n");
        String regular = "^[\u4E00-\u9FA5a-zA-Z].*[^:：]$";
        Pattern ptn = Pattern.compile(regular);
        for (String string : str) {
            if (ptn.matcher(string).matches()) {
                // Log.v(TAG, string + " matches");
                sb.append(string + (tts ? ";" : "") + "\n");
            } else {
                // Log.v(TAG, "not match");
            }
        }
        if (sb.length() == 0) {
            return dictData;
        }
        return sb.substring(0, sb.length() - 1);
    }

}

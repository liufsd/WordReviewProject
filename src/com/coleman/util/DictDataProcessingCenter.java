
package com.coleman.util;

import java.util.regex.Pattern;

import com.coleman.kingword.dict.DictManager;
import com.coleman.log.Log;

public class DictDataProcessingCenter {
    private static final String A51_BABYLON_ENGLISH = "a51_babylon_english";

    private static final String LANGDAO_EC_GB = "langdao_ec_gb";

    private static final String TAG = null;

    private static Log Log = Config.getLog();

    /**
     * 如果当前词典是朗道词典，对需要朗读的释义进行处理，避免朗读重复、冗余信息，其它词典不作处理。
     * 
     * @param dictData 需要处理的释义
     * @return 处理后的结果
     */
    public static String process4TTS(String dictData) {
        if (DictManager.getInstance().getCurLibDirName().equals(LANGDAO_EC_GB)) {
            return processLangdao4TTS(dictData);
        } else if (DictManager.getInstance().getCurLibDirName().equals(A51_BABYLON_ENGLISH)) {
            return processBabylon4TTS(dictData);
        } else {
            return dictData;
        }
    }

    private static String processBabylon4TTS(String dictData) {
        String regular = "^<.*>";
        return dictData.replaceAll(regular, "");
    }

    private static String processLangdao4TTS(String dictData) {
        StringBuilder sb = new StringBuilder();
        String str[] = dictData.split("\n");
        String regular = "^[\u4E00-\u9FA5a-zA-Z].*[^:：]$";
        Pattern ptn = Pattern.compile(regular);
        for (String string : str) {
            if (ptn.matcher(string).matches()) {
                // Log.v(TAG, string + " matches");
                sb.append(string + ";\n");
            } else {
                // Log.v(TAG, "not match");
            }
        }
        if (sb.length() == 0) {
            return dictData;
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 如果当前词典是朗道词典，对多项选择时的释义进行处理，避免选项中出现过多冗余信息。
     * 
     * @param dictData 需要处理的释义
     * @return 处理后的结果
     */
    public static String process4MultiSelect(String dictData) {
        if (DictManager.getInstance().getCurLibDirName().equals(LANGDAO_EC_GB)) {
            return processLangdao4MultiSelect(dictData);
        } else {
            return dictData;
        }
    }

    private static String processLangdao4MultiSelect(String dictData) {
        StringBuilder sb = new StringBuilder();
        String str[] = dictData.split("\n");
        String regular = "^[\u4E00-\u9FA5a-zA-Z].*[^:：]$";
        Pattern ptn = Pattern.compile(regular);
        for (String string : str) {
            if (ptn.matcher(string).matches()) {
                // Log.v(TAG, string + " matches");
                sb.append(string + "\n");
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


package com.coleman.kingword.dict.stardict;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.coleman.util.ConvertUtils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

public class DictData {
    private static final String TAG = DictData.class.getName();

    public String symbol;

    public String data;

    private boolean isDetail;

    DictData() {
    }

    public static DictData constructData(String word) {
        DictData dici = new DictData();
        dici.data = word;
        dici.symbol = "";
        return dici;
    }

    public static DictData readData(Context context, DictInfo info, DictIndex index, String fileName) {
        DictData dici = new DictData();

        final int FILE_LEN = 1000000;
        byte[] bytes = new byte[(int) index.size];
        boolean merged = false;
        int fileSuffix = 0;
        int fileOffset = 0;
        String fName = "";
        String fNameAppend = "";
        fileSuffix = (int) (index.offset / FILE_LEN + 1);
        fileOffset = (int) (index.offset % FILE_LEN);

        fName = fileName + File.separator + "xa"
                + (fileSuffix < 10 ? "0" + fileSuffix : fileSuffix);
        if (fileOffset + index.size > FILE_LEN) {
            fNameAppend = fileName + File.separator + "xa"
                    + (fileSuffix + 1 < 10 ? "0" + (fileSuffix + 1) : (fileSuffix + 1));
            merged = true;
        }
        // Log.d(TAG, "file name:" + fName + " append file name:" +
        // fNameAppend);
        if (merged) {
            int size1 = FILE_LEN - fileOffset;
            int size2 = (int) index.size - size1;
            byte[] bytes1 = readData(context, info, fName, fileOffset, size1);
            byte[] bytes2 = readData(context, info, fNameAppend, 0, size2);
            if (bytes1 == null || bytes2 == null) {
                Log.d(TAG, "parse data error");
                return dici;
            }
            System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
            System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
            // Log.d(TAG, "the word " + index.word + " is merged!");
        } else {
            bytes = readData(context, info, fName, fileOffset, (int) index.size);
            if (bytes == null) {
                Log.d(TAG, "parse data error");
                return dici;
            }
        }

        if (!info.sameTypeSequence.equals("tm")) {
            dici.isDetail = true;
            // one word's data can't be larger than Integer's value
            try {
                dici.data = new String(bytes, 0, bytes.length, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // one word's data can't be larger than Integer's value
            String data1 = null, data2 = null;
            for (int k = 0; k < bytes.length; k++) {
                if (bytes[k] == 0) {
                    try {
                        data1 = new String(bytes, 0, k, "UTF-8");
                        data2 = new String(bytes, k + 1, bytes.length - k - 1, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    dici.data = data2;
                    if (k > 1) {// for debug
                        // Log.d(TAG, "first data:" + data1);
                        dici.symbol = "/" + data1 + "/";
                    }
                    break;
                }
            }
        }
        return dici;
    }

    private static byte[] readData(Context context, DictInfo info, String fileName, int offset,
            int size) {
        byte[] bytes = null;
        InputStream is;
        DataInputStream reader;
        try {
            bytes = new byte[size];
            is = context.getAssets().open(fileName, AssetManager.ACCESS_RANDOM);
            reader = new DataInputStream(is);
            // System.out.println("size:" + reader.available() + "   data size:"
            // + size);
            // one word's data can't be larger than Integer's value
            reader.skip(offset);
            reader.read(bytes, 0, bytes.length);
            // System.out.println("length:" + bytes.length);
            reader.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return bytes;
        }
    }

    public String getDatas() {
        return data == null ? "" : data;
    }

    /**
     * @deprecated
     */
    String removeSymbol() {
        int first, last;
        if (!TextUtils.isEmpty(data) && data.charAt(0) == '/' && data.length() > 5) {
            first = 0;
            last = data.substring(1).indexOf('/');
            if (last - first > 0 && last - first < 40 && last + 1 < data.length()) {
                Log.d(TAG, "data last:" + data.indexOf(last + 1) + "  nn:" + '\n');
                if (data.indexOf(last + 1) == '\n' || data.indexOf(last + 1) == -1) {
                    return data.substring(last + 3);
                } else {
                    return data.substring(last + 2);
                }
            }
        }
        return data;
    }

    public String getSymbol() {
        return symbol == null ? "" : symbol;
    }

    public String getDataAndSymbol() {
        return TextUtils.isEmpty(getSymbol()) ? getDatas() : getSymbol() + "\n" + getDatas();
    }

    @Override
    public String toString() {
        String str = "";
        str += symbol == null ? "" : symbol;
        str += " " + (data == null ? "" : data);
        return str;
    }
}

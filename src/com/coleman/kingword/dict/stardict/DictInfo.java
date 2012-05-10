/*
 * @(#)DictInfo.java
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package com.coleman.kingword.dict.stardict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Environment;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.KingWord;
import com.coleman.kingword.provider.KingWord.TDict;

/**
 * Dictionary information
 * <p>
 * Every .ifo file must be include the following example: <br>
 * bookname=牛津现代英汉双解词典 wordcount=39429 idxfilesize=721264
 * </p>
 * More information please refer to doc/StarDictFileFormat file
 * 
 * @author 88250
 * @version 1.1.0.3, Feb 16, 2008
 */
public class DictInfo {

    /**
     * dictionary name
     */
    public String dictName;

    /**
     * dicitonary vocabulary count
     */
    public String wordCount;

    /**
     * vocabulary index file
     */
    public String idxFileSize;

    /**
     * mark the dictionary data format.
     */
    public String sameTypeSequence;

    /**
     * 0 未设置，1 current lib, 2 more lib, 3 cur lib & more lib
     */
    public int type = -1;

    // fields new added, not read from the file
    public boolean loaded;

    public boolean internal;

    public String dictDirName;

    public long date;

    /**
     * Constructor with arguments
     * 
     * @param bookName dictionary name
     * @param wordCount dicitonary vocabulary count
     * @param idxFileSize vocabulary index file
     * @param sametypesequence
     */
    private DictInfo(String bookName, String wordCount, String idxFileSize, String sametypesequence) {
        this.dictName = bookName;
        this.wordCount = wordCount;
        this.idxFileSize = idxFileSize;
        this.sameTypeSequence = sametypesequence;
    }

    @Override
    public String toString() {
        return "Dict Name: " + dictName + "\tWord Count: " + wordCount + "\tIndex File Size: "
                + idxFileSize + "\tSame Type Sequence:" + this.sameTypeSequence;
    }

    public void insertOrUpdate(Context context) {
        ContentValues values = new ContentValues();
        if (dictName != null) {
            values.put(TDict.DICT_NAME, dictName);
        }
        if (wordCount != null) {
            values.put(TDict.WORD_COUNT, wordCount);
        }
        if (idxFileSize != null) {
            values.put(TDict.IDX_FILE_SIZE, idxFileSize);
        }
        if (sameTypeSequence != null) {
            values.put(TDict.SAME_TYPE_SEQUENCE, sameTypeSequence);
        }
        values.put(TDict.TYPE, getType());
        values.put(TDict.LOADED, loaded ? 1 : 0);
        values.put(TDict.INTERNAL, internal ? 1 : 0);
        if (dictDirName != null) {
            values.put(TDict.DICT_DIR_NAME, dictDirName);
        }
        values.put(TDict.DATE, date);
        Cursor c = context.getContentResolver().query(TDict.CONTENT_URI, null,
                TDict.DICT_DIR_NAME + " = '" + dictDirName + "'", null, null);
        boolean has = c.moveToFirst();
        c.close();
        if (has) {
            context.getContentResolver().update(TDict.CONTENT_URI, values,
                    TDict.DICT_DIR_NAME + " = '" + dictDirName + "'", null);
        } else {
            context.getContentResolver().insert(TDict.CONTENT_URI, values);
        }
    }

    public static DictInfo loadInfo(Context context, DictLibrary lib) {
        if (lib.isInitialed()) {
            return loadFromDB(context, lib);
        } else {
            return loadFromFile(context, lib.isInternal(), lib.getLibDirName());
        }
    }

    public static HashMap<String, DictInfo> loadFromDB(Context context) {
        HashMap<String, DictInfo> map = new HashMap<String, DictInfo>();
        DictInfo info = null;
        Cursor c = null;
        try {
            c = context.getContentResolver().query(KingWord.TDict.CONTENT_URI, null, null, null,
                    null);
            for (c.moveToFirst(); c.isAfterLast(); c.moveToNext()) {
                String dictName = c.getString(c.getColumnIndex(TDict.DICT_NAME));
                String wordCount = c.getString(c.getColumnIndex(TDict.WORD_COUNT));
                String idxFileSize = c.getString(c.getColumnIndex(TDict.IDX_FILE_SIZE));
                String sameTypeSequence = c.getString(c.getColumnIndex(TDict.SAME_TYPE_SEQUENCE));

                int type = c.getInt(c.getColumnIndex(TDict.TYPE));
                boolean loaded = c.getInt(c.getColumnIndex(TDict.LOADED)) == 1;
                boolean internal = c.getInt(c.getColumnIndex(TDict.INTERNAL)) == 1;
                String dictDirName = c.getString(c.getColumnIndex(TDict.DICT_DIR_NAME));
                long date = c.getLong(c.getColumnIndex(TDict.DATE));

                info = new DictInfo(dictName, wordCount, idxFileSize, sameTypeSequence);
                info.type = type;
                info.loaded = loaded;
                info.internal = internal;
                info.dictDirName = dictDirName;
                info.date = date;
                map.put(dictDirName, info);
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return map;
    }

    public static HashMap<String, DictInfo> loadDefault(Context context) {
        HashMap<String, DictInfo> map = new HashMap<String, DictInfo>();
        String dictDirName = "a49_stardict_1_3";
        DictInfo info = loadFromFile(context, true, dictDirName);
        map.put(dictDirName, info);

        dictDirName = "a50_oxford_gb_formated";
        info = loadFromFile(context, true, dictDirName);
        map.put(dictDirName, info);
        return map;
    }

    private static DictInfo loadFromDB(Context context, DictLibrary lib) {
        DictInfo info = null;
        String libDirName = lib.getLibDirName();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(KingWord.TDict.CONTENT_URI, null,
                    KingWord.TDict.DICT_DIR_NAME + " = '" + libDirName + "'", null, null);
            if (c.moveToFirst()) {
                String dictName = c.getString(c.getColumnIndex(TDict.DICT_NAME));
                String wordCount = c.getString(c.getColumnIndex(TDict.WORD_COUNT));
                String idxFileSize = c.getString(c.getColumnIndex(TDict.IDX_FILE_SIZE));
                String sameTypeSequence = c.getString(c.getColumnIndex(TDict.SAME_TYPE_SEQUENCE));

                boolean loaded = c.getInt(c.getColumnIndex(TDict.LOADED)) == 1;
                boolean internal = c.getInt(c.getColumnIndex(TDict.INTERNAL)) == 1;
                String dictDirName = c.getString(c.getColumnIndex(TDict.DICT_DIR_NAME));
                long date = c.getLong(c.getColumnIndex(TDict.DATE));

                info = new DictInfo(dictName, wordCount, idxFileSize, sameTypeSequence);
                info.loaded = loaded;
                info.internal = internal;
                info.dictDirName = dictDirName;
                info.date = date;
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return info;
    }

    public static DictInfo loadFromFile(Context context, boolean internal, String libDirName) {
        InputStream is = null;
        String ifo = "kingword/dicts/" + libDirName + ".ifo";
        try {
            if (internal) {
                is = context.getAssets().open(ifo, AssetManager.ACCESS_RANDOM);
            } else {
                is = new FileInputStream(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + ifo);
            }
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String bookName = null;
            String wordCount = null;
            String idxFileSize = null;
            String sameTypeSequence = null;
            while ((line = br.readLine()) != null) {
                String[] info = line.split("=");
                if (info[0].equals("bookname")) {
                    info[1] = new String(info[1].getBytes("ISO-8859-1"), "UTF-8");
                    bookName = info[1];
                } else if (info[0].equals("wordcount")) {
                    wordCount = info[1];
                } else if (info[0].equals("idxfilesize")) {
                    idxFileSize = info[1];
                } else if (info[0].equals("sametypesequence")) {
                    sameTypeSequence = info[1];
                }
            }
            DictInfo info = new DictInfo(bookName, wordCount, idxFileSize, sameTypeSequence);
            info.type = 0;
            info.loaded = false;
            info.dictDirName = libDirName;
            info.internal = internal;
            info.date = System.currentTimeMillis();
            return info;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
            }
        }
    }

    private int getType() {
        if (type == -1) {
            if (dictDirName == null) {
                type = 0;
            } else if (dictDirName.equals(DictManager.getInstance().getCurLibDirName())
                    && dictDirName.equals(DictManager.getInstance().getMoreLibDirName())) {
                type = 3;
            } else if (dictDirName.equals(DictManager.getInstance().getCurLibDirName())) {
                type = 1;
            } else if (dictDirName.equals(DictManager.getInstance().getMoreLibDirName())) {
                type = 2;
            } else {
                type = 0;
            }
        }
        return type;
    }
}

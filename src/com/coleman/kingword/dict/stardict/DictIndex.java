/*
 * @(#)DictIndex.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.coleman.kingword.dict.DictManager;
import com.coleman.kingword.provider.DictIndexManager;
import com.coleman.kingword.provider.KingWord.TDict.TDictIndex;
import com.coleman.util.ConvertUtils;
import com.coleman.util.Log;

/**
 * Dictionary index entry structure.
 * <p>
 * Each index entry in the word list contains three fields, one after the other:
 * word_str; // a utf-8 string terminated by '\0'. word_data_offset; // word
 * data's offset in .dict file word_data_size; // word data's total size in
 * .dict file
 * </p>
 * More information please refer to doc/StarDictFileFormat file
 * 
 * @author 88250
 * @version 1.1.0.2, Feb 16, 2008
 */
public class DictIndex {

    private static final String TAG = DictIndex.class.getName();

    /**
     * word string
     */
    public String word;

    /**
     * word data offset
     */
    public long offset;

    /**
     * word data size
     */
    public int size;

    private DictIndex() {
    }

    public DictIndex(String word, long offset, int size) {
        this.word = word;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public String toString() {
        return word + "\t" + offset + "\t" + size;
    }

    static void loadDictIndexMap(Context context, DictLibrary lib) {
        HashMap<String, DictIndex> wordmap = new HashMap<String, DictIndex>();
        readIndexFileNative(context, lib, wordmap);
    }

    /**
     * Read the dictionary index file, store the index entries into
     * {@link #dictIndexList}
     * 
     * @throws java.io.FileNotFoundException
     * @see TDictIndex.edu.ynu.sei.dict.kernel.core.fixed.reader.stardict.DictIndex
     */
    private static void readIndexFileNative(Context context, DictLibrary lib,
            HashMap<String, DictIndex> wordmap) {
        long time = System.currentTimeMillis();
        InputStream reader = null;
        int numCount = lib.getNumCount();
        String indexFileName = lib.getIdxFileName();
        try {
            if (lib.isInternal()) {
                reader = context.getAssets().open(indexFileName, AssetManager.ACCESS_RANDOM);
            } else {
                reader = new FileInputStream(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + indexFileName);
            }
            // the maximun length of a word must less 256
            // 256 bytes(word) + 1 byte('\0') + 4 bytes(offset) + 4 bytes(def
            // size)
            byte[] bytes = new byte[16 * 1024];// 256 + 1 + 4 + 4
            int mark = 0;
            String word = null;
            long offset = 0; // offset of a word in data file
            long size = 0; // size of word's definition
            // the num of records inserted to the DB
            final int INSERT_NUMBER = 1000;
            // count the total number insert to the DB.
            int count = 0;
            while ((mark != 0 ? reader.read(bytes, bytes.length - mark, mark) : reader.read(bytes,
                    0, bytes.length)) != -1) {
                mark = 0;
                word = null;
                offset = 0;
                size = 0;
                DictIndex dictIndex;
                for (int i = 0; i < bytes.length && wordmap.size() < numCount; i++) {
                    if (bytes[i] == 0 && i < bytes.length - 9) {
                        word = new String(bytes, mark, i - mark, "UTF-8");
                        offset = ConvertUtils.unsigned4BytesToInt(bytes, i + 1);
                        size = ConvertUtils.unsigned4BytesToInt(bytes, i + 5);
                        dictIndex = new DictIndex();
                        dictIndex.word = word;
                        dictIndex.offset = offset;
                        dictIndex.size = (int) size;
                        wordmap.put(word, dictIndex);
                        if (wordmap.size() % INSERT_NUMBER == 0) {
                            Log.d(TAG,
                                    "============insert "
                                            + count
                                            + " ~ "
                                            + (count + INSERT_NUMBER)
                                            + " records of "
                                            + indexFileName.substring(indexFileName
                                                    .lastIndexOf("/")) + " to the DB ");
                            count += INSERT_NUMBER;
                            doInsert(context, indexFileName, wordmap);
                            wordmap.clear();
                        }
                        mark = i + 9;
                        i += 9;
                    }
                    if (i == bytes.length - 1 && bytes[bytes.length - 1] == 0) {
                        mark = 0;
                    } else if (i >= bytes.length - 9) {
                        System.arraycopy(bytes, mark, bytes, 0, bytes.length - mark);
                        break;
                    }

                }
            }
            if (wordmap.size() > 0) {
                Log.d(TAG, "============insert " + count + " ~ " + (count + wordmap.size())
                        + " records of " + indexFileName.substring(indexFileName.lastIndexOf("/"))
                        + " to the DB ");
                count += wordmap.size();
                doInsert(context, indexFileName, wordmap);
                wordmap.clear();
            }
            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // save the settings preference.
        DictManager.getInstance().setComplete(context, lib.getLibDirName());

        time = System.currentTimeMillis() - time;
        System.out
                .println(indexFileName + " insert dict indexs to the database cost time: " + time);
    }

    private static void doInsert(Context context, String indexFileName,
            HashMap<String, DictIndex> wordmap) {
        Collection<DictIndex> col = wordmap.values();
        int i = 0;
        ContentValues[] values = new ContentValues[col.size()];
        for (DictIndex dictIndex : col) {
            values[i] = new ContentValues();
            values[i].put(TDictIndex.WORD, dictIndex.word);
            values[i].put(TDictIndex.OFFSET, dictIndex.offset);
            values[i].put(TDictIndex.SIZE, dictIndex.size);
            i++;
        }
        String fileName = indexFileName.substring(indexFileName.lastIndexOf("/") + 1,
                indexFileName.lastIndexOf("."));
        TDictIndex index = DictIndexManager.getInstance().getHashMap().get(fileName);
        if (index != null) {
            context.getContentResolver().bulkInsert(index.getContentUri(), values);
        }
    }
}

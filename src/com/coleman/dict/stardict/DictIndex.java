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

package com.coleman.dict.stardict;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;

import com.coleman.dict.util.ByteConverter;

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
    public long size;

    @Override
    public String toString() {
        return word + "\t" + offset + "\t" + size;
    }

    /**
     * Read the dictionary index file, store the index entries into
     * {@link #dictIndexList}
     * 
     * @throws java.io.FileNotFoundException
     * @see cn.edu.ynu.sei.dict.kernel.core.fixed.reader.stardict.DictIndex
     */
    public static ArrayList<DictIndex> readIndexFile(Context context, String indexFileName,
            int count) {
        ArrayList<DictIndex> dictIndexList = new ArrayList<DictIndex>();
        InputStream reader = null;
        try {
            reader = context.getAssets().open(indexFileName, AssetManager.ACCESS_RANDOM);
            // the maximun length of a word must less 256
            // 256 bytes(word) + 1 byte('\0') + 4 bytes(offset) + 4 bytes(def
            // size)
            byte[] bytes = new byte[256 + 1 + 4 + 4];
            int mark = 0;
            while ((mark != 0 ? reader.read(bytes, bytes.length - mark, mark) : reader.read(bytes,
                    0, bytes.length)) != -1) {
                String word = null;
                long offset = 0; // offset of a word in data file
                long size = 0; // size of word's definition
                mark = 0;
                for (int i = 0; i < bytes.length && dictIndexList.size() < count; i++) {
                    if (bytes[i] == 0 && i < bytes.length - 9) {
                        word = new String(bytes, mark, i - mark, "UTF-8");
                        offset = ByteConverter.unsigned4BytesToInt(bytes, i + 1);
                        size = ByteConverter.unsigned4BytesToInt(bytes, i + 5);
                        DictIndex dictIndex = new DictIndex();
                        dictIndex.word = word;
                        dictIndex.offset = offset;
                        dictIndex.size = size;
                        dictIndexList.add(dictIndex);
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
            reader.close();

            sortIndexList(dictIndexList);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return dictIndexList;
    }

    private static void sortIndexList(List<DictIndex> dictIndexList) {
        java.util.Collections.sort(dictIndexList, new Comparator<DictIndex>() {
            public int compare(DictIndex o1, DictIndex o2) {
                return o1.word.compareTo(o2.word);
            }
        });
    }
}

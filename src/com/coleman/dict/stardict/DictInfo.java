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

package com.coleman.dict.stardict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;

import com.coleman.util.FileAccessor;

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
    public String bookName;

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

    public int count;

    /**
     * Constructor with arguments
     * 
     * @param bookName dictionary name
     * @param wordCount dicitonary vocabulary count
     * @param idxFileSize vocabulary index file
     * @param sametypesequence
     */
    public DictInfo(String bookName, String wordCount, String idxFileSize, String sametypesequence) {
        this.bookName = bookName;
        this.wordCount = wordCount;
        this.idxFileSize = idxFileSize;
        this.sameTypeSequence = sametypesequence;
    }

    @Override
    public String toString() {
        return "Book Name: " + bookName + "\nWord Count: " + wordCount + "\nIndex File Size: "
                + idxFileSize + "\nSame Type Sequence:" + this.sameTypeSequence;
    }

    public static DictInfo readDicInfo(Context context, String ifoFileName) {
        InputStream is = null;
        try {
            is = context.getAssets().open(ifoFileName, AssetManager.ACCESS_RANDOM);
            InputStreamReader reader= new InputStreamReader(is);
            BufferedReader br= new BufferedReader(reader);
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
            return new DictInfo(bookName, wordCount, idxFileSize, sameTypeSequence);
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
}

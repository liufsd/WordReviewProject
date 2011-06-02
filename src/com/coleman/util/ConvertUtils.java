
package com.coleman.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class ConvertUtils {
    public static String convert(String text, String srcEncode, String destEncode)
            throws UnsupportedEncodingException {
        ByteBuffer bytebuffer = ByteBuffer.wrap(text.getBytes());

        Charset decode = Charset.forName(srcEncode);
        CharBuffer decodeCharBuffer = decode.decode(bytebuffer);

        Charset encode = Charset.forName(destEncode);
        ByteBuffer encodeCharBuffer = encode.encode(decodeCharBuffer);

        return new String(encodeCharBuffer.array(), destEncode);
    }

    /**
     * convert unsigned one byte into a 32-bit integer
     * 
     * @param b byte
     * @return convert result
     */
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * convert unsigned one byte into a hexadecimal digit
     * 
     * @param b byte
     * @return convert result
     */
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }

    /**
     * convert unsigned 4 bytes into a 32-bit integer
     * 
     * @param buf bytes buffer
     * @param pos beginning <code>byte</code>> for converting
     * @return convert result
     */
    public static long unsigned4BytesToInt(byte[] buf, int pos) {
        int ret = 0;

        ret += unsignedByteToInt(buf[pos++]) << 24;
        ret += unsignedByteToInt(buf[pos++]) << 16;
        ret += unsignedByteToInt(buf[pos++]) << 8;
        ret += unsignedByteToInt(buf[pos++]) << 0;

        return ret;
    }
}

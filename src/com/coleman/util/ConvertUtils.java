
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
}

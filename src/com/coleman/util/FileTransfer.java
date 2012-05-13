
package com.coleman.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class FileTransfer {
    public void downloadFile(String url, String storePath) {
        HttpURLConnection con = null;
        FileOutputStream out = null;
        InputStream in = null;
        try {

            con = (HttpURLConnection) new URL(url).openConnection();

            con.addRequestProperty("charset", "UTF-8");
            con.setRequestMethod("GET");

            con.setDoInput(true);
            con.setDoOutput(true);

            con.connect();
            in = con.getInputStream();

            String gip = con.getHeaderField("Content-Encoding");

            out = new FileOutputStream(storePath);
            byte buf[] = new byte[1024];

            if (gip != null) {
                if (gip.equalsIgnoreCase("gzip")) {
                    GZIPInputStream tempIn = new GZIPInputStream(in);
                    for (int j = tempIn.read(buf); j > 0; j = tempIn.read(buf)) {
                        out.write(buf, 0, j);

                    }
                    out.flush();
                    tempIn.close();
                } else {
                    for (int j = in.read(buf); j > 0; j = in.read(buf)) {
                        out.write(buf, 0, j);

                    }
                    out.flush();
                }
            } else {
                for (int j = in.read(buf); j > 0; j = in.read(buf)) {
                    out.write(buf, 0, j);
                }
                out.flush();
            }
            out.close();
            in.close();
        } catch (Exception e) {
            try {
                out.close();
            } catch (IOException e1) {
            }
            try {
                in.close();
            } catch (IOException e1) {
            }
            File file = new File(storePath);
            file.delete();
        } finally {
            try {
                con.disconnect();
            } catch (Exception e) {
            }
        }
    }

}

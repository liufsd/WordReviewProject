
package com.coleman.tools.email;

import com.coleman.log.Log;
import com.coleman.util.Config;

public class GMailSenderHelper {
    private static Log Log = Config.getLog();

    /**
     * send a mail to the specified account.
     * 
     * @param title mail title
     * @param msg mail content
     * @param attachFilePath null if with no attachment.
     */
    public static void sendMail(String title, String msg, String attachFilePath) {
        try {
            GMailSender sender = new GMailSender("kingword1984@gmail.com", "zk33403140");
            sender.setAttachment(attachFilePath);
            sender.sendMail(title, msg, "kingword1984@gmail.com", "kingword1984@gmail.com");
        } catch (Exception e) {
            Log.e("SendMail", e);
        }
    }

}

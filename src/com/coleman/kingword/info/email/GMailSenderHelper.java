
package com.coleman.kingword.info.email;

import android.util.Log;

public class GMailSenderHelper {
    public static void sendMail(String title, String msg, String attachFilePath) {
        GMailSender sender = new GMailSender("shagua1984@gmail.com", "test33403140");
        sender.setAttachment(attachFilePath);
        try {
            sender.sendMail(title, msg, "shagua1984@gmail.com", "shagua1984@gmail.com");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

    public static void sendMail(String title, String msg) {
        GMailSender sender = new GMailSender("shagua1984@gmail.com", "test33403140");
        sender.setAttachment(null);
        try {
            sender.sendMail(title, msg, "shagua1984@gmail.com", "shagua1984@gmail.com");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

    public static void sendMessage(String msgBody) {
        GMailSender sender = new GMailSender("shagua1984@gmail.com", "test33403140");
        sender.setAttachment(null);
        try {
            sender.sendMail("Send gather info", msgBody, "shagua1984@gmail.com",
                    "shagua1984@gmail.com");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
}

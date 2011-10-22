
package com.coleman.kingword.info.email;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.coleman.kingword.info.InfoGather;

import android.text.TextUtils;
import android.util.Log;

public class GMailSender extends javax.mail.Authenticator {
    private static final String TAG = GMailSender.class.getName();

    private String mailhost = "smtp.gmail.com";

    private String mUser;

    private String mPassword;

    private Session session;

    private String attachFilePath;

    public GMailSender(String user, String password) {
        this.mUser = user;
        this.mPassword = password;

        Properties props = new Properties();
        // props.setProperty("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", mailhost);
        // port 465 is for ssl, 587 is for starttls, need to enable
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        // Enable authentication if emailProperties.getSmtpUser() is not null
        if (this.mUser != null) {
            props.put("mail.smtp.auth", "true");
        }
        props.put("mail.smtp.user", this.mUser);
        props.put("mail.smtp.password", this.mPassword);
        /**
         * seesion class this class use as the total application environment
         * information and collect the Client with the server to build the net
         * connect conversion information
         */
        this.session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mUser, mPassword);
            }
        });
    }

    public void setAttachment(String path) {
        this.attachFilePath = path;
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients)
            throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setText(body);
        // add attachment
        if (!TextUtils.isEmpty(attachFilePath)) {
            try {
                MimeMultipart allMultipart = new MimeMultipart("mixed");
                MimeBodyPart attachPartPicture = createAttachment(attachFilePath);
                allMultipart.addBodyPart(attachPartPicture);
                message.setContent(allMultipart);
                message.saveChanges();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        // judge multi-recipient or one recipient
        if (recipients.indexOf(',') > 0)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        try {
            session.setDebug(false);
            Transport.send(message);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static MimeBodyPart createAttachment(String filename) throws Exception {
        // TODO Auto-generated method stub
        MimeBodyPart attachPart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(filename);
        attachPart.setDataHandler(new DataHandler(fds));
        attachPart.setFileName(fds.getName());
        return attachPart;
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;

        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getname() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }

        @Override
        public String getName() {
            return null;
        }
    }

}

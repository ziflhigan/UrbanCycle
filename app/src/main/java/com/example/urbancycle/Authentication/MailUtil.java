package com.example.urbancycle.Authentication;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtil {
    public static void sendMail(String recipient, String subject, String content) throws Exception {
        Properties properties = new Properties();
        // Configure mail server
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myAccountEmail = "ziflhigan18@gmail.com";
        String password = "lbthxgwqkirlrlsx";
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(myAccountEmail, password);
            }
        });

        try {
            Message message = prepareMessage(session, myAccountEmail, recipient, subject, content);
            assert message != null;
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipient, String subject, String content) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setText(content);
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

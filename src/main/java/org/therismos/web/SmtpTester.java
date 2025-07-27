package org.therismos.web;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
/**
 * Actual mail config is in {datapath}/config/javamail.properties
 * @author cp_liu
 */
@ViewScoped
@jakarta.inject.Named
public class SmtpTester implements Serializable {
    private static final long serialVersionUID = 1L;
    static final Logger LOG = Logger.getLogger(SmtpTester.class.getName());

    /**
     * @return the config_as_string
     */
    public String getConfig_as_string() {
        return config_as_string;
    }

    /**
     * @param config_as_string the config_as_string to set
     */
    public void setConfig_as_string(String config_as_string) {
        this.config_as_string = config_as_string;
    }

    Properties mailConfig, credential;
    private String config_as_string, email_from, email_to, email_subject, email_body;
    
    public SmtpTester() {
        mailConfig = new Properties();
        credential = new Properties();
    }
    
    private void init() throws IOException {
        Properties config = new Properties();
        try (InputStream is= new java.io.ByteArrayInputStream (config_as_string.getBytes())) {
            config.load(is);
        }
        for (String key: config.stringPropertyNames()) {
//            System.out.println("key:"+key);  
            if (key.startsWith("mail.")) {
                mailConfig.put(key, config.get(key));
            }
            else if (key.startsWith("credential.")) {
                credential.put(key, config.get(key));
            }
        }
    }
    
    private Session getSession() {
        return Session.getInstance(mailConfig, new MyAuthenticator(
                        credential.getProperty("credential.user"),
                        credential.getProperty("credential.pwd")
        ));
    }
    
    private void sendEmail(Session mailSession) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(email_from));
        message.addRecipients(Message.RecipientType.TO, email_to);
        message.setSubject(email_subject);
        message.setText(email_body);
        Transport.send(message);  
    }
    
    public void send() {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            init();
            sendEmail(getSession());
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Mail sent", "Mail Sent"));
        } catch (IOException | MessagingException ex) {
            LOG.log(Level.SEVERE, (String) null, ex);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Sent error", "Error"));
        }
        
    }
    
    public static class MyAuthenticator extends jakarta.mail.Authenticator {
        private final String username;
        private final String password;

        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
            return new jakarta.mail.PasswordAuthentication(username, password);
        }
    }

    /**
     * @return the email_from
     */
    public String getEmail_from() {
        return email_from;
    }

    /**
     * @param email_from the email_from to set
     */
    public void setEmail_from(String email_from) {
        this.email_from = email_from;
    }

    /**
     * @return the email_to
     */
    public String getEmail_to() {
        return email_to;
    }

    /**
     * @param email_to the email_to to set
     */
    public void setEmail_to(String email_to) {
        this.email_to = email_to;
    }

    /**
     * @return the email_subject
     */
    public String getEmail_subject() {
        return email_subject;
    }

    /**
     * @param email_subject the email_subject to set
     */
    public void setEmail_subject(String email_subject) {
        this.email_subject = email_subject;
    }

    /**
     * @return the email_body
     */
    public String getEmail_body() {
        return email_body;
    }

    /**
     * @param email_body the email_body to set
     */
    public void setEmail_body(String email_body) {
        this.email_body = email_body;
    }
}
        

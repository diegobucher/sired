package br.gov.caixa.gitecsa.sired.util;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class MailUtils {
    
    public static final String MAIL_SETTING_PORT = "port";

    public static final String MAIL_SETTING_HOST = "host";
    
    public static final String DEFAULT_MAIL_ENCODING = "UTF-8";
    
    public static void send(String from, String to, String title, StringBuffer body, Map<String, String> settings) throws EmailException {
        MailUtils.send(from, to, null, title, body, null, settings);
    }
    
    public static void send(String from, String to, String title, StringBuffer body, File attachment, Map<String, String> settings) throws EmailException {
        MailUtils.send(from, to, null, title, body, attachment, settings);
    }
    
    public static void send(String from, String to, Set<String> cc, String title, StringBuffer body, File attachment, Map<String, String> settings) throws EmailException {
        
        HtmlEmail mail = new HtmlEmail();
        
        if (!ObjectUtils.isNullOrEmpty(settings)) {
            if (settings.containsKey(MAIL_SETTING_HOST)) {
                mail.setHostName(settings.get(MAIL_SETTING_HOST));
            }
            
            if (settings.containsKey(MAIL_SETTING_PORT)) {
                mail.setSmtpPort(Integer.valueOf(settings.get(MAIL_SETTING_PORT)));
            }
        }
        
        mail.setSSLOnConnect(false);
        mail.setStartTLSEnabled(false);
        mail.setCharset(DEFAULT_MAIL_ENCODING);
        
        mail.setFrom(from);
        mail.addTo(to);
        
        if (!ObjectUtils.isNullOrEmpty(cc)) {
            for (String emailAddress : cc) {
                mail.addCc(emailAddress);
            }
        }
        
        mail.setSubject(title);
        mail.setHtmlMsg(body.toString());
        
        if (!ObjectUtils.isNullOrEmpty(attachment)) {
            mail.attach(attachment);
        }
        
        mail.send();
    }
}

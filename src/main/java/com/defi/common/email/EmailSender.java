/**
 * Email utility package for sending emails via SMTP.
 * Provides synchronous and asynchronous email sending capabilities.
 */
package com.defi.common.email;

import com.defi.common.email.config.SmtpConfig;
import com.defi.common.util.file.FileUtil;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.ErrorLogger;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton email sender that manages SMTP connections and email dispatching.
 * Supports both synchronous and asynchronous email sending.
 * 
 * @author System
 * @since 1.0
 */
public class EmailSender {
    @Getter
    private static final EmailSender instance = new EmailSender();
    private Session session;
    private SmtpConfig config;
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(1);
    
    private EmailSender() {

    }
    
    /**
     * Initializes the email sender with configuration from the specified file.
     * Must be called before sending any emails.
     * 
     * @param configFilePath Path to the JSON configuration file containing SMTP settings
     * @throws RuntimeException if configuration file is not found or invalid
     */
    public void init(String configFilePath) {
        loadConfig(configFilePath);
        initializeSession();
    }
    
    private void loadConfig(String configFilePath) {
        String content = FileUtil.readString(configFilePath);
        if (content == null) {
            throw new RuntimeException("Email config file not found: " + configFilePath);
        }
        this.config = JsonUtil.fromJson(content, SmtpConfig.class);
    }
    
    private void initializeSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", config.getPort());
        props.put("mail.smtp.auth", !config.getUsername().isEmpty());
        
        if (config.isUseSSL()) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        
        if (config.isUseTLS()) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        if (!config.getUsername().isEmpty()) {
            this.session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });
        } else {
            this.session = Session.getInstance(props);
        }
    }
    
    /**
     * Sends an email synchronously. Blocks until the email is sent or fails.
     * 
     * @param request Email request containing recipient, subject, and content
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendEmail(EmailRequest request) {
        try {
            Message message = createMessage(request);
            Transport.send(message);
            return true;
        } catch (Exception e) {
            ErrorLogger.create(e).log();
            return false;
        }
    }
    
    /**
     * Sends an email asynchronously. Returns immediately without blocking.
     * Email is sent on a separate thread pool.
     * 
     * @param request Email request containing recipient, subject, and content
     */
    public void sendEmailAsync(EmailRequest request) {
        CompletableFuture.supplyAsync(() -> {
            try {
                Message message = createMessage(request);
                Transport.send(message);
                return true;
            } catch (Exception e) {
                ErrorLogger.create(e).log();
                return false;
            }
        }, emailExecutor);
    }
    
    private Message createMessage(EmailRequest request) throws Exception {
        Message message = new MimeMessage(session);
        
        message.setFrom(new InternetAddress(config.getUsername(), "System"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo()));
        
        if (request.getCc() != null && !request.getCc().trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(request.getCc()));
        }
        
        if (request.getBcc() != null && !request.getBcc().trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(request.getBcc()));
        }
        
        if (request.getReplyTo() != null && !request.getReplyTo().trim().isEmpty()) {
            message.setReplyTo(InternetAddress.parse(request.getReplyTo()));
        }
        
        message.setSubject(request.getSubject());
        message.setContent(request.getContent(), "text/html; charset=utf-8");
        
        return message;
    }
}
package com.defi.common.email.config;

import lombok.Data;

/**
 * SMTP server configuration for email sending.
 * Contains all necessary settings for establishing SMTP connections.
 * 
 * @author System
 * @since 1.0
 */
@Data
public class SmtpConfig {
    /**
     * SMTP server hostname (e.g., "smtp.gmail.com").
     */
    private String host;
    
    /**
     * SMTP server port (e.g., 587 for TLS, 465 for SSL).
     */
    private int port;
    
    /**
     * Username for SMTP authentication.
     * Usually the sender's email address.
     */
    private String username;
    
    /**
     * Password for SMTP authentication.
     * For Gmail, use app-specific password.
     */
    private String password;
    
    /**
     * Enable SSL encryption for SMTP connection.
     * Typically used with port 465.
     */
    private boolean useSSL;
    
    /**
     * Enable TLS encryption for SMTP connection.
     * Typically used with port 587.
     */
    private boolean useTLS;
}
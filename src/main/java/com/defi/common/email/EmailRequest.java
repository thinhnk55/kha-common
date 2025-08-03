package com.defi.common.email;

import lombok.Builder;
import lombok.Data;

/**
 * Email request model containing all necessary information for sending an email.
 * Supports multiple recipients via comma-separated email addresses.
 * 
 * @author System
 * @since 1.0
 */
@Data
@Builder
public class EmailRequest {
    /**
     * Primary recipients' email addresses (required).
     * Multiple addresses can be comma-separated: "email1@example.com, email2@example.com"
     */
    private String to;
    
    /**
     * Carbon copy recipients' email addresses (optional).
     * Multiple addresses can be comma-separated.
     */
    private String cc;
    
    /**
     * Blind carbon copy recipients' email addresses (optional).
     * Multiple addresses can be comma-separated.
     */
    private String bcc;
    
    /**
     * Reply-to email address (optional).
     * Replies to the email will be sent to this address instead of the sender.
     */
    private String replyTo;
    
    /**
     * Email subject line (required).
     */
    private String subject;
    
    /**
     * Email body content in HTML format (required).
     * Supports full HTML with UTF-8 encoding.
     */
    private String content;
}
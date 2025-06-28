package com.defi.common.token.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code Token} represents the full payload of a JWT issued by the
 * authentication system.
 * It includes session, subject, access roles, token type, and expiration
 * metadata.
 * 
 * <p>
 * This class uses Lombok annotations to generate constructors:
 * </p>
 * <ul>
 * <li>{@code @NoArgsConstructor} - Creates a default constructor for frameworks
 * and serialization</li>
 * <li>{@code @AllArgsConstructor} - Creates a constructor with all fields as
 * parameters</li>
 * <li>{@code @Builder} - Provides a fluent builder pattern for object
 * creation</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    /**
     * Unique session ID associated with the token.
     */
    private String sessionId;

    /**
     * ID of the subject the token represents (e.g., UUID).
     */
    private String subjectId;

    /**
     * Display name or username of the subject.
     */
    private String subjectName;

    /**
     * Type of the subject (user, system, agent).
     */
    private SubjectType subjectType;

    /**
     * List of role IDs granted to the subject.
     */
    private List<String> roles;

    /**
     * List of group IDs the subject belongs to.
     */
    private List<String> groups;

    /**
     * Issued-at time in seconds since epoch.
     */
    private Long iat;

    /**
     * Expiration time in seconds since epoch.
     */
    private Long exp;

    /**
     * Type of token (access or refresh).
     */
    private TokenType tokenType;
}

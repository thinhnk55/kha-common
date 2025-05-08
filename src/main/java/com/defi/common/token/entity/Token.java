package com.defi.common.token.entity;

import com.defi.common.util.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * {@code Token} represents the full payload of a JWT issued by the authentication system.
 * It includes session, subject, access roles, token type, and expiration metadata.
 */
@Data
@Builder
@AllArgsConstructor
public class Token {

    /**
     * Unique session ID associated with the token.
     */
    private UUID sessionId;

    /**
     * ID of the subject the token represents (e.g., user UUID).
     */
    private UUID subjectId;

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
    private List<Integer> roles;

    /**
     * List of group IDs the subject belongs to.
     */
    private List<Integer> groups;

    /**
     * Issued-at time in seconds since epoch.
     */
    private long iat;

    /**
     * Expiration time in seconds since epoch.
     */
    private long exp;

    /**
     * Type of token (access or refresh).
     */
    private TokenType tokenType;

    /**
     * Default no-args constructor required for deserialization frameworks like Jackson.
     */
    public Token() {
        // Required for frameworks
    }

    /**
     * Returns a JSON representation of this token.
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}

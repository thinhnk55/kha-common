package com.defi.common.log.entity;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * {@code ActionLog} represents a structured audit or activity log entry in the system.
 * It records the identity and type of the actor, the action performed, the target of the action,
 * its status, timestamp, and any associated metadata in JSON form.
 *
 * <p>This log format is useful for compliance, auditing, debugging, and traceability of user/system behavior.</p>
 */
@Data
@Builder
@AllArgsConstructor
public class ActionLog {

    /**
     * Unique identifier for the log entry.
     */
    private UUID id;

    /**
     * Type of the actor performing the action (e.g., USER, SYSTEM, AGENT).
     */
    private String actorType;

    /**
     * Identifier of the actor (e.g., username, system ID).
     */
    private String actor;

    /**
     * The type of action performed (e.g., CREATE, DELETE, LOGIN).
     */
    private String action;

    /**
     * The type of entity or resource that was the target of the action.
     */
    private String target;

    /**
     * Status of the action (e.g., SUCCESS, FAILURE).
     */
    private String status;

    /**
     * Unix timestamp (in seconds) when the action occurred.
     */
    private long timestamp;

    /**
     * Optional additional metadata related to the action (structured JSON).
     */
    private JsonNode metadata;

    /**
     * Default no-args constructor required for deserialization frameworks like Jackson.
     */
    public ActionLog() {
        // Required for frameworks
    }

    /**
     * Converts this {@code ActionLog} object to its JSON string representation.
     *
     * @return a JSON-formatted string
     */
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}

package com.defi.common.util.log.entity;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code EventLog} represents a structured audit or activity log entry in the
 * system.
 * It records the identity and type of the subject, the type of event, the
 * target of the event,
 * its status, timestamp, and any associated metadata in JSON form.
 */
@Data
@Builder
@AllArgsConstructor
public class EventLog {

    /**
     * Default constructor for EventLog.
     */
    public EventLog() {
        // Default constructor for Lombok compatibility
    }

    private String id;
    private String targetType;
    private String targetId;
    private String subjectType;
    private String subjectId;
    private String type;
    private ObjectNode data;
    private String correlationId;
    private long createdAt;
}

package com.defi.common.workflow.constant;

/**
 * Constants for workflow system configuration and events.
 * 
 * <p>
 * This class defines the standard constants used throughout the workflow
 * system,
 * including configuration file paths, Redis channel names, and event message
 * formats.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public class WorkflowConstant {

    /**
     * Default Redis channel for workflow definition change notifications.
     * 
     * <p>
     * All services should listen on this channel to receive workflow
     * definition update events. The channel name is designed to be descriptive
     * and avoid conflicts with other Redis usage in the application.
     * </p>
     * 
     * <p>
     * Channel naming convention: {@code workflow:event:channel}
     * </p>
     */
    public static final String WORKFLOW_EVENT_CHANNEL = "workflow:event:channel";

    /**
     * Configuration file path for workflow settings.
     */
    public static final String WORKFLOW_CONFIG_FILE = "config/workflow/workflow.json";

    /**
     * Standard publish message prefix that triggers workflow definition reloading.
     * 
     * <p>
     * When this message is received by
     * {@link com.defi.common.workflow.event.WorkflowEventListener},
     * it will trigger a workflow definition reload for the specified code.
     * </p>
     * 
     * <p>
     * Message format: "WORKFLOW_PUBLISHED:{workflow_code}"
     * </p>
     */
    public static final String WORKFLOW_PUBLISHED_PREFIX = "WORKFLOW_PUBLISHED:";

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private WorkflowConstant() {
        // Utility class - no instantiation allowed
    }
}

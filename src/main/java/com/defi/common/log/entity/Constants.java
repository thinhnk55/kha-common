package com.defi.common.log.entity;

/**
 * {@code Constants} contains predefined constant values used throughout the logging system.
 * These constants define the names and identifiers used for the Redis stream,
 * consumer groups, and log fields.
 *
 * <p>These constants help centralize configuration values related to the logging system,
 * making it easier to maintain and modify the system's behavior without hardcoding values
 * in multiple places.</p>
 */
public class Constants {
    /**
     * Private constructor to prevent instantiation.
     */
    private Constants() {
        // Utility class
    }


    /**
     * The name of the Redis stream used for logging.
     * This stream holds all action log entries.
     */
    public static final String STREAM_NAME = "log-stream";

    /**
     * The name of the Redis consumer group for the log stream.
     * Consumer groups allow multiple consumers to read from the same stream.
     */
    public static final String GROUP_NAME = "log-group";

    /**
     * The name of the Redis consumer within the log group.
     * This identifier is used to track which consumer is reading the stream.
     */
    public static final String CONSUMER_NAME = "log-consumer";

    /**
     * The field name used in Redis to store log entries in the stream.
     * Each log entry is stored under this field in the stream.
     */
    public static final String LOG_FIELD = "log";
}

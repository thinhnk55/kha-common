package com.defi.common.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * {@code DebugLogger} is a utility class for accessing named SLF4J loggers.
 * It includes a default "debug" logger and a method to retrieve custom loggers by name.
 */
public class DebugLogger {

    /**
     * Prevent instantiation of utility class.
     */
    private DebugLogger() {
        // Utility class, not meant to be instantiated
    }

    /**
     * A predefined logger with the name {@code "debug"} for general debugging output.
     */
    public static final Logger logger = LoggerFactory.getLogger("debug");

    /**
     * Returns a logger with the specified name.
     *
     * @param loggerName the name of the logger
     * @return a SLF4J {@link Logger}
     */
    public static Logger getLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }
}

package com.defi.common.util.log;

import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.entity.EventLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for logging structured business or system events.
 * Events are logged as a single JSON line to a dedicated "event" logger,
 * which can be configured to write to a separate file (e.g., logs/event.log).
 */
public class EventLogger {

    private static final Logger logger = LoggerFactory.getLogger("event");

    private EventLogger() {
        // Utility class
    }

    /**
     * Logs an {@link EventLog} object by converting it to a JSON string
     * and writing it to the event logger at the INFO level.
     *
     * @param event The {@link EventLog} to be logged. Must not be null.
     */
    public static void log(EventLog event) {
        if (event != null) {
            logger.info(JsonUtil.toJsonString(event));
        }
    }
}

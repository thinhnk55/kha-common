package com.defi.common.log;

import com.defi.common.log.entity.ActionLog;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.redis.Redisson;
import com.defi.common.util.string.RandomStringUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.redisson.api.RStream;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamCreateGroupArgs;

import java.time.Instant;

import static com.defi.common.log.entity.Constants.*;

/**
 * {@code ActionLogger} is a singleton utility class responsible for logging actions
 * in a Redis stream. It supports creating and managing action log entries, including
 * the initialization of the stream and the actual logging of events.
 *
 * <p>Each action log contains important metadata about the action performed, the actor,
 * and the target of the action, along with its status and timestamp.</p>
 */
public class ActionLogger {

    /**
     * Singleton instance of the {@code ActionLogger}.
     */
    @Getter
    private static final ActionLogger instance = new ActionLogger();

    /**
     * Redis stream where action logs are stored.
     */
    private RStream<String, String> logStream;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ActionLogger() {
    }

    /**
     * Initializes the {@code ActionLogger} by setting up the Redis stream.
     * If the stream group does not exist, it creates one.
     */
    public void init() {
        // Get the Redis stream client from Redisson
        logStream = Redisson.getInstance().getClient().getStream(STREAM_NAME);

        try {
            // Check if the group exists in the Redis stream
            logStream.getPendingInfo(GROUP_NAME);
        } catch (Exception e) {
            // If group doesn't exist, create it
            if (e.getMessage().contains("NOGROUP")) {
                ObjectNode metadata = JsonUtil.createJson();
                metadata.put("key_type", "STREAM");
                metadata.put("key", STREAM_NAME);

                // Create a new action log entry for group creation
                ActionLog log = ActionLog.builder()
                        .id(RandomStringUtil.uuidV7())
                        .actorType("SYSTEM")                     // actorType
                        .actor("SYSTEM")                         // actor
                        .action("CREATE")                        // action
                        .target("REDIS")                         // target
                        .timestamp(Instant.now().toEpochMilli()) // timestamp
                        .status("SUCCESS")                       // status
                        .metadata(metadata)                      // metadata
                        .build();

                // Add the log entry to the stream and create the group
                logStream.add(StreamAddArgs.entry(LOG_FIELD, log.toString()));
                logStream.createGroup(StreamCreateGroupArgs.name(GROUP_NAME));
            } else {
                throw e;
            }
        }
    }

    /**
     * Logs an action by adding it to the Redis stream.
     *
     * @param log the {@code ActionLog} to be logged
     */
    public void log(ActionLog log) {
        // Add the action log entry to the stream
        logStream.add(StreamAddArgs.entry(LOG_FIELD, log.toString()));
    }
}

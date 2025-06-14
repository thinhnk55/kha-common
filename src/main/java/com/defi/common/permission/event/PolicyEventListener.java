package com.defi.common.permission.event;

import com.defi.common.permission.PermissionChecker;
import com.defi.common.util.redis.Redisson;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

/**
 * Redis message listener for policy reload events in distributed systems.
 * 
 * <p>
 * This component implements the Redisson {@link MessageListener} interface
 * to receive policy change notifications via Redis pub/sub mechanism. When a
 * policy reload message is received, it triggers an immediate policy refresh.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Listens for standardized {@link PolicyEventConstant#RELOAD_MESSAGE}
 * events</li>
 * <li>Automatically triggers policy reload via {@link PermissionChecker}</li>
 * <li>Fail-safe operation - errors are logged but don't crash the service</li>
 * <li>Supports distributed policy synchronization across service instances</li>
 * </ul>
 * 
 * <p>
 * Message flow:
 * </p>
 * <ol>
 * <li>Permission updated in database</li>
 * <li>Auth service sends reload message to Redis</li>
 * <li>This listener receives the message on configured channel</li>
 * <li>{@link PermissionChecker} reloads policies from source</li>
 * <li>Casbin enforcer is updated with fresh policies</li>
 * </ol>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class PolicyEventListener implements MessageListener<String> {

    private RTopic topic;
    private final String channelName;

    public PolicyEventListener() {
        this.channelName = PolicyEventConstant.DEFAULT_CHANNEL;
    }

    public PolicyEventListener(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Starts listening for policy events on the configured Redis channel.
     * 
     * @throws RuntimeException if Redisson client is not initialized
     */
    public void startListening() {
        try {
            if (Redisson.getInstance().getClient() == null) {
                throw new RuntimeException(
                        "Redisson client is not initialized. Call Redisson.getInstance().init() first.");
            }

            this.topic = Redisson.getInstance().getClient().getTopic(channelName);
            int listenerId = topic.addListener(String.class, this);

            log.info("PolicyEventListener started listening on channel: {} with listener ID: {}",
                    channelName, listenerId);

        } catch (Exception e) {
            log.error("Failed to start PolicyEventListener", e);
            throw new RuntimeException("Failed to start policy event listener", e);
        }
    }

    /**
     * Stops listening for policy events and cleans up resources.
     */
    public void stopListening() {
        if (topic != null) {
            topic.removeAllListeners();
            log.info("PolicyEventListener stopped listening on channel: {}", channelName);
        }
    }

    /**
     * Handles incoming Redis messages and processes policy reload events.
     * 
     * <p>
     * This method is called by Redisson when a message is received
     * on the configured channel. It checks if the message is a policy reload
     * request and triggers the appropriate action.
     * </p>
     * 
     * <p>
     * The method is designed to be resilient:
     * </p>
     * <ul>
     * <li>Unknown messages are ignored with debug logging</li>
     * <li>Exceptions are caught and logged without rethrowing</li>
     * <li>Successful reloads are logged for audit purposes</li>
     * </ul>
     * 
     * @param channel the Redis channel name
     * @param message the message content
     */
    @Override
    public void onMessage(CharSequence channel, String message) {
        try {
            log.debug("Received message on channel: {} with content: {}", channel, message);

            // Check if it's a reload message
            if (message.startsWith(PolicyEventConstant.RELOAD_MESSAGE)) {
                log.info("Processing policy reload event from channel: {}", channel);

                // Check if PermissionChecker is initialized before reloading
                PermissionChecker checker = PermissionChecker.getInstance();
                if (checker.isInitialized()) {
                    checker.reloadPermission();
                    log.info("Policy reload completed successfully");
                } else {
                    log.warn("PermissionChecker is not initialized - ignoring reload request");
                }

            } else {
                log.debug("Ignoring unknown message: {}", message);
            }

        } catch (Exception e) {
            log.error("Failed to process policy reload message: {}", message, e);
        }
    }
}
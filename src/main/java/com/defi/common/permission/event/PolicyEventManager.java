package com.defi.common.permission.event;

import lombok.extern.slf4j.Slf4j;

/**
 * Manager class for policy event system using Redisson Redis pub/sub.
 * 
 * <p>
 * This class provides a centralized way to manage policy event listening
 * using Redisson Redis client. It simplifies the setup and lifecycle
 * management of the event listener.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * // Initialize the event system
 * PolicyEventManager eventManager = new PolicyEventManager();
 * eventManager.startListening();
 * 
 * // Cleanup
 * eventManager.stopListening();
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class PolicyEventManager {

    private PolicyEventListener listener;
    private final String channelName;

    /**
     * Creates a new PolicyEventManager with default channel.
     */
    public PolicyEventManager() {
        this.channelName = PolicyEventConstant.DEFAULT_CHANNEL;
        this.listener = new PolicyEventListener(channelName);
    }

    /**
     * Creates a new PolicyEventManager with custom channel.
     * 
     * @param channelName the Redis channel name to use
     */
    public PolicyEventManager(String channelName) {
        this.channelName = channelName;
        this.listener = new PolicyEventListener(channelName);
    }

    /**
     * Starts listening for policy events.
     * 
     * @throws RuntimeException if listener cannot be started
     */
    public void startListening() {
        try {
            listener.startListening();
            log.info("PolicyEventManager started listening on channel: {}", channelName);
        } catch (Exception e) {
            log.error("Failed to start PolicyEventManager listening", e);
            throw new RuntimeException("Failed to start policy event listening", e);
        }
    }

    /**
     * Stops listening for policy events.
     */
    public void stopListening() {
        try {
            if (listener != null) {
                listener.stopListening();
                log.info("PolicyEventManager stopped listening on channel: {}", channelName);
            }
        } catch (Exception e) {
            log.error("Failed to stop PolicyEventManager listening", e);
        }
    }

    /**
     * Gets the channel name this manager is using.
     * 
     * @return the Redis channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Gets the event listener instance.
     * 
     * @return the PolicyEventListener instance
     */
    public PolicyEventListener getListener() {
        return listener;
    }
}
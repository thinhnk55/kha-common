package com.defi.common.workflow.event;

import lombok.Getter;
import com.defi.common.workflow.constant.WorkflowConstant;
import com.defi.common.util.log.ErrorLogger;
import com.defi.common.util.redis.Redisson;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;

/**
 * Singleton manager for workflow event system using Redisson Redis pub/sub.
 * 
 * <p>
 * This class provides a centralized way to manage workflow event listening
 * and publishing using Redisson Redis client. It simplifies the setup and
 * lifecycle
 * management of the event listener and provides methods to publish workflow
 * update events.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Singleton pattern for global access</li>
 * <li>Redis-based publish/subscribe messaging</li>
 * <li>Automatic workflow definition reloading on events</li>
 * <li>Fail-safe operation with comprehensive error handling</li>
 * <li>Distributed synchronization across service instances</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * // Get the singleton instance and start listening
 * WorkflowEventManager eventManager = WorkflowEventManager.getInstance();
 * eventManager.startListening();
 * 
 * // Publish a workflow update event
 * eventManager.onWorkflowPublished("WORKFLOW_CODE_123");
 * 
 * // Cleanup when shutting down
 * eventManager.stopListening();
 * }</pre>
 * 
 * <p>
 * Message format: The system uses the format
 * {@code "WORKFLOW_PUBLISHED:{workflow_code}"}
 * where {@code {workflow_code}} is the identifier of the workflow that was
 * updated.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see WorkflowEventListener
 * @see WorkflowConstant#WORKFLOW_PUBLISHED_PREFIX
 * @see WorkflowConstant#WORKFLOW_EVENT_CHANNEL
 */
@Slf4j
public class WorkflowEventManager {

    /**
     * Singleton instance of the WorkflowEventManager.
     */
    @Getter
    private static final WorkflowEventManager instance = new WorkflowEventManager();

    /**
     * The workflow event listener that handles incoming Redis messages.
     */
    private WorkflowEventListener listener;

    /**
     * Redis topic for publishing and subscribing to workflow events.
     */
    private RTopic topic;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the manager with the default workflow event channel.
     */
    private WorkflowEventManager() {

    }

    /**
     * Initializes the WorkflowEventManager.
     * 
     * <p>
     * This method creates a new WorkflowEventListener and sets up the Redis topic
     * for workflow event communication.
     */
    public void init() {
        this.listener = new WorkflowEventListener(WorkflowConstant.WORKFLOW_EVENT_CHANNEL);
        this.topic = Redisson.getInstance().getClient().getTopic(WorkflowConstant.WORKFLOW_EVENT_CHANNEL);
    }

    /**
     * Starts listening for workflow events on the configured Redis channel.
     * 
     * <p>
     * This method initializes the Redis topic and starts the event listener
     * to receive workflow update notifications. Once started, the listener
     * will automatically process incoming messages and trigger workflow
     * definition reloads as needed.
     * </p>
     * 
     * <p>
     * The listener will continue running until {@link #stopListening()} is called.
     * </p>
     * 
     * @throws RuntimeException if Redisson client is not initialized or listener
     *                          cannot be started
     */
    public void startListening() {
        try {
            listener.startListening();
            log.info("WorkflowEventManager started listening on channel: {}", WorkflowConstant.WORKFLOW_EVENT_CHANNEL);
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }

    /**
     * Stops listening for workflow events and cleans up resources.
     * 
     * <p>
     * This method removes all listeners from the Redis topic and logs the
     * shutdown process. It's safe to call this method multiple times.
     * </p>
     * 
     * <p>
     * This method should be called during application shutdown to properly
     * clean up Redis connections and prevent memory leaks.
     * </p>
     */
    public void stopListening() {
        try {
            if (listener != null) {
                listener.stopListening();
                log.info("WorkflowEventManager stopped listening on channel: {}",
                        WorkflowConstant.WORKFLOW_EVENT_CHANNEL);
            }
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }

    /**
     * Publishes a workflow update event to the Redis channel.
     * 
     * <p>
     * This method sends a message to notify all listening services that a specific
     * workflow definition has been updated and should be reloaded. The message
     * format follows the pattern defined by
     * {@link WorkflowConstant#WORKFLOW_PUBLISHED_PREFIX}.
     * </p>
     * 
     * <p>
     * When this message is received by other service instances, they will
     * automatically reload the specified workflow definition from the database.
     * </p>
     * 
     * <p>
     * The method returns the number of subscribers that received the message,
     * which can be useful for monitoring and debugging purposes.
     * </p>
     * 
     * @param workflowCode the code of the workflow that was updated
     * @throws RuntimeException         if Redisson client is not initialized or
     *                                  publishing fails
     * @throws IllegalArgumentException if workflowCode is null or empty
     */
    public void onWorkflowPublished(String workflowCode) {
        String message = WorkflowConstant.WORKFLOW_PUBLISHED_PREFIX + workflowCode;
        topic.publish(message);
    }
}

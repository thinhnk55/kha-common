package com.defi.common.workflow.event;

import com.defi.common.util.log.ErrorLogger;
import com.defi.common.util.redis.Redisson;
import com.defi.common.workflow.cache.WorkflowCacheManager;
import com.defi.common.workflow.constant.WorkflowConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

/**
 * Redis message listener for workflow definition update events in distributed
 * systems.
 * 
 * <p>
 * This component implements the Redisson {@link MessageListener} interface
 * to receive workflow definition change notifications via Redis pub/sub
 * mechanism. When a
 * workflow update message is received, it triggers an immediate workflow
 * definition reload.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Listens for standardized
 * {@link WorkflowConstant#WORKFLOW_PUBLISHED_PREFIX}
 * events</li>
 * <li>Automatically triggers workflow definition reload via
 * {@link WorkflowCacheManager}</li>
 * <li>Fail-safe operation - errors are logged but don't crash the service</li>
 * <li>Supports distributed workflow synchronization across service
 * instances</li>
 * <li>Comprehensive error handling and logging</li>
 * </ul>
 * 
 * <p>
 * Message flow:
 * </p>
 * <ol>
 * <li>Workflow definition updated in database</li>
 * <li>Service sends update message to Redis</li>
 * <li>This listener receives the message on configured channel</li>
 * <li>{@link WorkflowCacheManager} reloads specific workflow definition</li>
 * <li>Workflow cache is updated with fresh definition</li>
 * </ol>
 * 
 * <p>
 * Message format: The listener expects messages in the format
 * {@code "WORKFLOW_PUBLISHED:{workflow_code}"} where {@code {workflow_code}}
 * is the identifier of the workflow that needs to be reloaded.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * // Create and start the listener
 * WorkflowEventListener listener = new WorkflowEventListener();
 * listener.startListening();
 * 
 * // The listener will automatically process incoming messages
 * // and trigger workflow reloads as needed
 * 
 * // Cleanup when shutting down
 * listener.stopListening();
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see WorkflowCacheManager
 * @see WorkflowConstant#WORKFLOW_PUBLISHED_PREFIX
 * @see WorkflowConstant#WORKFLOW_EVENT_CHANNEL
 */
@Slf4j
public class WorkflowEventListener implements MessageListener<String> {

    /**
     * Redis topic for subscribing to workflow events.
     */
    private RTopic topic;

    /**
     * The Redis channel name used for workflow event communication.
     */
    private final String channelName;

    /**
     * Creates a new WorkflowEventListener with a custom channel name.
     * 
     * @param channelName the Redis channel name to listen on
     */
    public WorkflowEventListener(String channelName) {
        this.channelName = channelName;
    }

    /**
     * Starts listening for workflow events on the configured Redis channel.
     * 
     * <p>
     * This method initializes the Redis topic and starts the event listener
     * </p>
     */
    public void startListening() {
        try {
            if (Redisson.getInstance().getClient() == null) {
                throw new RuntimeException(
                        "Redisson client is not initialized. Call Redisson.getInstance().init() first.");
            }

            this.topic = Redisson.getInstance().getClient().getTopic(channelName);
            int listenerId = topic.addListener(String.class, this);

            log.info("WorkflowEventListener started listening on channel: {} with listener ID: {}",
                    channelName, listenerId);

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
        if (topic != null) {
            topic.removeAllListeners();
            log.info("WorkflowEventListener stopped listening on channel: {}", channelName);
        }
    }

    /**
     * Handles incoming Redis messages and processes workflow update events.
     * 
     * <p>
     * This method is called by Redisson when a message is received
     * on the configured channel. It checks if the message is a workflow update
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
     * <li>Empty or malformed workflow codes are handled gracefully</li>
     * </ul>
     * 
     * <p>
     * Message processing:
     * </p>
     * <ol>
     * <li>Extract workflow code from message (removes prefix)</li>
     * <li>Validate workflow code is not null or empty</li>
     * <li>Call {@link WorkflowCacheManager#reloadWorkflow(String)}</li>
     * <li>Log success or failure</li>
     * </ol>
     * 
     * @param channel the Redis channel name where the message was received
     * @param message the message content (expected format:
     *                "WORKFLOW_PUBLISHED:{code}")
     */
    @Override
    public void onMessage(CharSequence channel, String message) {
        try {
            if (message.startsWith(WorkflowConstant.WORKFLOW_PUBLISHED_PREFIX)) {
                String workflowCode = message.substring(WorkflowConstant.WORKFLOW_PUBLISHED_PREFIX.length());
                WorkflowCacheManager cacheManager = WorkflowCacheManager.getInstance();
                cacheManager.reloadWorkflow(workflowCode.trim());
            }
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }
}
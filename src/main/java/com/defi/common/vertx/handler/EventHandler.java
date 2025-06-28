package com.defi.common.vertx.handler;

import com.defi.common.api.BaseResponse;
import com.defi.common.token.entity.Token;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.entity.EventLog;
import com.defi.common.util.string.RandomStringUtil;
import com.defi.common.vertx.HttpApi;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for creating and managing event logs in Vert.x routing contexts.
 * <p>
 * This handler is responsible for creating structured event logs that capture
 * information about API operations, including the subject (user), target
 * resource,
 * event type, and associated data. It integrates with the authentication system
 * to extract user information from tokens and creates comprehensive audit
 * trails.
 * </p>
 * <p>
 * The handler generates unique event IDs using UUID v7 format and stores
 * the event information in the routing context for downstream processing
 * or persistence by event listeners.
 * </p>
 * 
 * @see EventLog
 * @see TokenAuthHandler
 */
public class EventHandler {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods are static and should be accessed directly through the class.
     */
    private EventHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Retrieves an {@link EventLog} object from the routing context.
     * <p>
     * This method extracts the event log that was previously stored in the
     * routing context, typically by the event creation handler. This allows
     * downstream handlers to access and process the event information.
     * </p>
     * 
     * @param ctx the routing context from which to retrieve the event
     * @return the {@link EventLog} object stored in the context, or {@code null}
     *         if no event has been stored
     * 
     * @see #putEventToRoutingContext(RoutingContext, EventLog)
     */
    public static EventLog getEventFromRoutingContext(RoutingContext ctx) {
        return ctx.get("event");
    }

    /**
     * Stores an {@link EventLog} object into the routing context for downstream
     * access.
     * <p>
     * This method places the event log into the routing context so that
     * subsequent handlers in the processing chain can access and utilize
     * the event information for logging, persistence, or other operations.
     * </p>
     * 
     * @param ctx   the routing context in which to store the event
     * @param event the {@link EventLog} object to store
     * 
     * @see #getEventFromRoutingContext(RoutingContext)
     */
    private static void putEventToRoutingContext(RoutingContext ctx, EventLog event) {
        ctx.put("event", event);
    }

    /**
     * Creates a new event handler for the specified target type and event type.
     * <p>
     * This method returns a Vert.x handler that will create a structured
     * {@link EventLog} entry capturing details about the current API operation.
     * The event log includes information from the authenticated user's token,
     * the request body data, and generates unique identifiers for tracking.
     * </p>
     * <p>
     * The created event contains:
     * </p>
     * <ul>
     * <li>Unique event ID (UUID v7 format)</li>
     * <li>Subject information from the authentication token</li>
     * <li>Target type and event type as specified</li>
     * <li>Request body data (if present) as JSON</li>
     * <li>Correlation ID for request tracing</li>
     * <li>Creation timestamp</li>
     * </ul>
     * <p>
     * If an error occurs during event creation, an internal server error
     * response is returned to the client.
     * </p>
     * 
     * @param targetType the type of target resource being operated on
     * @param eventType  the type of event being performed (e.g., "CREATE",
     *                   "UPDATE", "DELETE")
     * @return a Vert.x handler that creates event logs and stores them in
     *         the routing context, or returns an error response if creation fails
     * 
     * @see EventLog
     * @see RandomStringUtil#uuidV7()
     * @see TokenAuthHandler#getTokenFromRoutingContext(RoutingContext)
     */
    public static Handler<RoutingContext> create(String targetType,
            String eventType) {
        return ctx -> {
            try {
                Token token = TokenAuthHandler.getTokenFromRoutingContext(ctx);
                String json = ctx.body().asString();
                String eventId = RandomStringUtil.uuidV7().toString();
                EventLog event = EventLog.builder()
                        .id(eventId)
                        .subjectType(token.getSubjectType())
                        .subjectId(token.getSubjectId())
                        .targetType(targetType)
                        .targetId(null)
                        .type(eventType)
                        .data(json != null ? JsonUtil.fromJson(json, ObjectNode.class) : null)
                        .correlationId(eventId)
                        .createdAt(System.currentTimeMillis())
                        .build();
                putEventToRoutingContext(ctx, event);
                ctx.next();
            } catch (Exception e) {
                HttpApi.response(ctx, BaseResponse.INTERNAL_SERVER_ERROR);
            }
        };
    }
}

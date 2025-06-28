package com.defi.common.util.log;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * {@code ErrorLogger} is a specialized utility for logging errors in a
 * structured JSON format.
 * This approach ensures that error logs are consistent, machine-readable, and
 * rich with context,
 * facilitating easier analysis, searching, and alerting in log management
 * systems.
 * <p>
 * It provides a fluent builder API for constructing detailed error logs.
 *
 * <p>
 * <b>Example Usage:</b>
 * </p>
 * 
 * <pre>{@code
 * try {
 *     // ... code that might throw an exception
 * } catch (Exception e) {
 *     ErrorLogger.create("Failed to process user request", e)
 *             .putContext("userId", 123)
 *             .putContext("requestId", "abc-123-xyz")
 *             .context(ctx -> {
 *                 ctx.put("details", "Some complex details here");
 *                 ctx.putObject("payload").put("field", "value");
 *             })
 *             .log();
 * }
 * }</pre>
 */
public class ErrorLogger {

    private static final Logger logger = LoggerFactory.getLogger("error");

    private ErrorLogger() {
        // Utility class
    }

    /**
     * Creates a new error log builder with both message and throwable.
     * 
     * @param message the error message to log
     * @param t       the throwable/exception that caused the error
     * @return a new ErrorLogBuilder instance for chaining context information
     */
    public static ErrorLogBuilder create(String message, Throwable t) {
        return new ErrorLogBuilder(message, t);
    }

    /**
     * Creates a new error log builder with only a throwable (empty message).
     * 
     * @param t the throwable/exception that caused the error
     * @return a new ErrorLogBuilder instance for chaining context information
     */
    public static ErrorLogBuilder create(Throwable t) {
        return new ErrorLogBuilder("", t);
    }

    private static void performLog(String message, Throwable t, ObjectNode context) {
        ObjectNode logJson = JsonUtil.createObjectNode();

        logJson.put("timestamp", Instant.now().toString());
        logJson.put("level", "ERROR");
        logJson.put("logger_name", "error");
        logJson.put("thread_name", Thread.currentThread().getName());
        logJson.put("message", message);

        if (t != null) {
            ObjectNode exceptionJson = JsonUtil.createObjectNode();
            exceptionJson.put("class", t.getClass().getName());
            if (t.getMessage() != null) {
                exceptionJson.put("message", t.getMessage());
            }

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            exceptionJson.put("stack_trace", sw.toString());

            Throwable rootCause = getRootCause(t);
            if (rootCause != t) { // Only add if root cause is different
                ObjectNode rootCauseJson = JsonUtil.createObjectNode();
                rootCauseJson.put("class", rootCause.getClass().getName());
                if (rootCause.getMessage() != null) {
                    rootCauseJson.put("message", rootCause.getMessage());
                }
                exceptionJson.set("root_cause", rootCauseJson);
            }

            logJson.set("exception", exceptionJson);
        }

        if (context != null && context.size() > 0) {
            logJson.set("context", context);
        }

        logger.error(JsonUtil.toJsonString(logJson));
    }

    private static Throwable getRootCause(Throwable throwable) {
        if (throwable == null)
            return null;
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * A fluent builder for creating structured error logs.
     * This allows for chaining multiple context additions in a readable and
     * powerful way.
     */
    public static class ErrorLogBuilder {
        private final String message;
        private final Throwable throwable;
        private final ObjectNode context;

        private ErrorLogBuilder(String message, Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
            this.context = JsonUtil.createObjectNode();
        }

        /**
         * [POWERFUL] Provides a consumer to build complex, nested context structures
         * directly.
         * This is ideal for scenarios where multiple related context values need to be
         * grouped.
         *
         * @param contextBuilder A {@link Consumer} that receives the internal context
         *                       {@link ObjectNode}.
         * @return This builder instance for chaining.
         */
        public ErrorLogBuilder context(Consumer<ObjectNode> contextBuilder) {
            if (contextBuilder != null) {
                contextBuilder.accept(this.context);
            }
            return this;
        }

        /**
         * Adds a String value to the context.
         * 
         * @param key   the context key
         * @param value the string value to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, String value) {
            context.put(key, value);
            return this;
        }

        /**
         * Adds a numeric (int) value to the context.
         * 
         * @param key   the context key
         * @param value the integer value to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, int value) {
            context.put(key, value);
            return this;
        }

        /**
         * Adds a numeric (long) value to the context.
         * 
         * @param key   the context key
         * @param value the long value to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, long value) {
            context.put(key, value);
            return this;
        }

        /**
         * Adds a numeric (double) value to the context.
         * 
         * @param key   the context key
         * @param value the double value to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, double value) {
            context.put(key, value);
            return this;
        }

        /**
         * Adds a boolean value to the context.
         * 
         * @param key   the context key
         * @param value the boolean value to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, boolean value) {
            context.put(key, value);
            return this;
        }

        /**
         * Adds a pre-built JsonNode to the context. Useful for complex, nested
         * structures.
         * 
         * @param key   the context key
         * @param value the JsonNode to add
         * @return this builder instance for chaining
         */
        public ErrorLogBuilder putContext(String key, JsonNode value) {
            context.set(key, value);
            return this;
        }

        /**
         * [POWERFUL] Adds any Java object to the context by serializing it to a
         * JsonNode.
         * This is perfect for logging complex objects like POJOs, Lists, or Maps.
         *
         * @param key   The context key.
         * @param value The object to be serialized and added.
         * @return This builder instance for chaining.
         */
        public ErrorLogBuilder putContext(String key, Object value) {
            context.set(key, JsonUtil.toJsonNode(value));
            return this;
        }

        /**
         * [POWERFUL] Merges all key-value pairs from the given map into the log
         * context.
         *
         * @param map A map containing contextual information to add.
         * @return This builder instance for chaining.
         */
        public ErrorLogBuilder putContext(java.util.Map<String, ?> map) {
            if (map != null) {
                map.forEach((key, value) -> context.set(key, JsonUtil.toJsonNode(value)));
            }
            return this;
        }

        /**
         * [POWERFUL] Conditionally adds a key-value pair to the context.
         * The value is provided by a Supplier and is only evaluated if the condition is
         * true.
         * This is useful for avoiding expensive value calculations when the context is
         * not needed.
         *
         * @param condition     The boolean condition to check.
         * @param key           The context key.
         * @param valueSupplier A {@link java.util.function.Supplier} that provides the
         *                      value if the condition is met.
         * @return This builder instance for chaining.
         */
        public ErrorLogBuilder putContextIf(boolean condition, String key,
                java.util.function.Supplier<Object> valueSupplier) {
            if (condition) {
                context.set(key, JsonUtil.toJsonNode(valueSupplier.get()));
            }
            return this;
        }

        /**
         * Finalizes the build process and writes the structured log to the error
         * output.
         */
        public void log() {
            ErrorLogger.performLog(this.message, this.throwable, this.context);
        }
    }
}

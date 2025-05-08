package com.defi.common.util.json;

import com.defi.common.util.log.DebugLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * {@code JsonUtil} provides utility methods for working with JSON using Jackson.
 * It supports conversion between JSON strings and Java objects, as well as creating and parsing
 * JSON tree nodes ({@link ObjectNode}, {@link ArrayNode}, etc.).
 */
public class JsonUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonUtil() {
        // Utility class
    }

    /**
     * A shared, pre-configured {@link ObjectMapper} that supports Java 8 date/time types and
     * disables timestamps for dates.
     */
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Creates a new empty {@link ObjectNode}.
     *
     * @return a new JSON object node
     */
    public static ObjectNode createJson() {
        return mapper.createObjectNode();
    }

    /**
     * Parses a JSON string into an {@link ObjectNode}. Returns {@code null} if parsing fails
     * or the root element is not an object.
     *
     * @param data the JSON string
     * @return an {@link ObjectNode}, or {@code null} if invalid or not an object
     */
    public static ObjectNode toJsonObject(String data) {
        try {
            JsonNode node = mapper.readTree(data);
            if (node instanceof ObjectNode) {
                return (ObjectNode) node;
            }
        } catch (Exception e) {
            DebugLogger.logger.error("Failed to parse object node", e);
        }
        return null;
    }

    /**
     * Parses a JSON string into an {@link ArrayNode}. Returns {@code null} if parsing fails
     * or the root element is not an array.
     *
     * @param data the JSON string
     * @return an {@link ArrayNode}, or {@code null} if invalid or not an array
     */
    public static ArrayNode toJsonArray(String data) {
        try {
            JsonNode node = mapper.readTree(data);
            if (node instanceof ArrayNode) {
                return (ArrayNode) node;
            }
        } catch (Exception e) {
            DebugLogger.logger.error("Failed to parse array node", e);
        }
        return null;
    }

    /**
     * Converts a Java object to its JSON string representation.
     *
     * @param object the object to serialize
     * @return the JSON string
     * @throws RuntimeException if serialization fails
     */
    public static String toJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace(); // You might want to remove this in production
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    /**
     * Converts a Java object to a {@link JsonNode}.
     *
     * @param object the object to convert
     * @return the resulting {@link JsonNode}
     * @throws RuntimeException if conversion fails
     */
    public static JsonNode toJsonObject(Object object) {
        try {
            return fromJsonString(mapper.writeValueAsString(object), JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    /**
     * Parses a JSON string into a Java object of the specified class.
     *
     * @param data  the JSON string
     * @param clazz the target class
     * @param <T>   the type of the resulting object
     * @return the deserialized object
     * @throws RuntimeException if parsing fails
     */
    public static <T> T fromJsonString(String data, Class<T> clazz) {
        try {
            return mapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }

    /**
     * Parses a JSON string into a generic Java object using a {@link TypeReference}.
     *
     * @param data          the JSON string
     * @param typeReference the type reference describing the target type
     * @param <T>           the type of the resulting object
     * @return the deserialized object
     * @throws RuntimeException if parsing fails
     */
    public static <T> T fromJsonString(String data, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(data, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }
}

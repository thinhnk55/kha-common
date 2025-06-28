package com.defi.common.util.json;

import com.defi.common.util.log.ErrorLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A robust utility class for JSON processing using the Jackson library.
 * Provides a set of safe, common methods for JSON conversion. For advanced
 * or rare use cases, the pre-configured {@link #mapper} is exposed publicly.
 * All utility methods log detailed errors and return {@code null} on failure.
 */
public class JsonUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JsonUtil() {
        // Utility class
    }

    /**
     * The shared, publicly accessible, configured instance of Jackson's
     * ObjectMapper.
     * Use this for any advanced JSON operations not covered by the utility methods.
     * It's configured to handle Java 8 Date/Time types and disables writing dates
     * as timestamps.
     */
    public static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // --- JSON Creation --- //

    /**
     * Creates a new empty JSON object node.
     * 
     * @return a new empty JsonNode (ObjectNode)
     */
    public static JsonNode createJson() {
        return mapper.createObjectNode();
    }

    /**
     * Creates a new empty JSON object node.
     * 
     * @return a new empty ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * Creates a new empty JSON array node.
     * 
     * @return a new empty ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    // --- Parsing & Deserialization (Safe: returns null on failure) ---

    /**
     * Parses a JSON string into a {@link JsonNode}.
     *
     * @param json The JSON string to parse.
     * @return A {@link JsonNode} if parsing is successful, otherwise {@code null}.
     */
    public static JsonNode parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            ErrorLogger.create("Failed to parse JSON string", e)
                    .putContext("jsonString", json)
                    .log();
            return null;
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified class.
     * 
     * @param <T>   the type of object to deserialize to
     * @param json  the JSON string to deserialize
     * @param clazz the target class type
     * @return the deserialized object, or null if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null)
            return null;
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            ErrorLogger.create("Failed to deserialize JSON string", e)
                    .putContext("targetClass", clazz.getName())
                    .putContext("jsonString", json)
                    .log();
            return null;
        }
    }

    /**
     * Deserializes a JSON string to an object using TypeReference for complex
     * types.
     * 
     * @param <T>  the type of object to deserialize to
     * @param json the JSON string to deserialize
     * @param type the TypeReference specifying the target type
     * @return the deserialized object, or null if deserialization fails
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (json == null)
            return null;
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            ErrorLogger.create("Failed to deserialize JSON string with TypeReference", e)
                    .putContext("targetType", type.getType().getTypeName())
                    .putContext("jsonString", json)
                    .log();
            return null;
        }
    }

    /**
     * Converts a JsonNode to an object of the specified class.
     * 
     * @param <T>   the type of object to convert to
     * @param node  the JsonNode to convert
     * @param clazz the target class type
     * @return the converted object, or null if conversion fails
     */
    public static <T> T fromJson(JsonNode node, Class<T> clazz) {
        if (node == null)
            return null;
        try {
            return mapper.treeToValue(node, clazz);
        } catch (Exception e) {
            ErrorLogger.create("Failed to convert JsonNode to object", e)
                    .putContext("targetClass", clazz.getName())
                    .putContext("sourceNode", node.toString())
                    .log();
            return null;
        }
    }

    /**
     * Converts a JsonNode to an object using TypeReference for complex types.
     * 
     * @param <T>  the type of object to convert to
     * @param node the JsonNode to convert
     * @param type the TypeReference specifying the target type
     * @return the converted object, or null if conversion fails
     */
    public static <T> T fromJson(JsonNode node, TypeReference<T> type) {
        if (node == null)
            return null;
        try {
            return mapper.readValue(mapper.treeAsTokens(node), type);
        } catch (Exception e) {
            ErrorLogger.create("Failed to convert JsonNode to object with TypeReference", e)
                    .putContext("targetType", type.getType().getTypeName())
                    .putContext("sourceNode", node.toString())
                    .log();
            return null;
        }
    }

    /**
     * Serializes an object to a JSON string.
     * 
     * @param obj the object to serialize
     * @return the JSON string representation, or null if serialization fails
     */
    public static String toJsonString(Object obj) {
        if (obj == null)
            return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            ErrorLogger.create("Failed to serialize object to JSON string", e)
                    .putContext("objectClass", obj.getClass().getName())
                    .log();
            return null;
        }
    }

    /**
     * Serializes an object to a pretty-printed JSON string.
     * 
     * @param obj the object to serialize
     * @return the pretty-printed JSON string, or null if serialization fails
     */
    public static String toPrettyJsonString(Object obj) {
        if (obj == null)
            return null;
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            ErrorLogger.create("Failed to serialize object to pretty JSON string", e)
                    .putContext("objectClass", obj.getClass().getName())
                    .log();
            return null;
        }
    }

    /**
     * Converts an object to a JsonNode.
     * 
     * @param obj the object to convert
     * @return the JsonNode representation, or null if conversion fails
     */
    public static JsonNode toJsonNode(Object obj) {
        if (obj == null)
            return null;
        try {
            return mapper.valueToTree(obj);
        } catch (Exception e) {
            ErrorLogger.create("Failed to convert object to JsonNode", e)
                    .putContext("objectClass", obj.getClass().getName())
                    .log();
            return null;
        }
    }

    /**
     * Converts a JSON string to an ObjectNode.
     * 
     * @param jsonString the JSON string to parse
     * @return the ObjectNode representation, or null if parsing fails
     */
    public static ObjectNode toJsonObject(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode node = mapper.readTree(jsonString);
            if (node.isObject()) {
                return (ObjectNode) node;
            } else {
                ErrorLogger.create("JSON string is not an object", null)
                        .putContext("jsonString", jsonString)
                        .log();
                return null;
            }
        } catch (Exception e) {
            ErrorLogger.create("Failed to parse JSON string to ObjectNode", e)
                    .putContext("jsonString", jsonString)
                    .log();
            return null;
        }
    }
}

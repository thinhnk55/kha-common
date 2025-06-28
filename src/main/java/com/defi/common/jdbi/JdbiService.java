package com.defi.common.jdbi;

import com.defi.common.jdbi.JdbiProvider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CaseFormat;
import org.jdbi.v3.core.Handle;

import java.util.*;

/**
 * Service class for JDBI database operations with JSON support.
 * 
 * <p>
 * This service provides utilities for performing partial updates on database
 * tables using ObjectNode data. It automatically handles field name conversion
 * between camelCase (Java) and snake_case (database) conventions, and provides
 * special handling for JSON/JSONB columns in PostgreSQL.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * JdbiService service = new JdbiService();
 * ObjectNode data = JsonUtil.createObjectNode();
 * data.put("firstName", "John");
 * data.put("userId", 123);
 * 
 * // Updates only provided fields, uses userId as key
 * service.updatePartialObject("users", data, "userId");
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
public class JdbiService {

    /**
     * Default constructor for JdbiService.
     */
    public JdbiService() {
        // Default constructor
    }

    /**
     * Converts camelCase string to snake_case for database field names.
     * 
     * @param camel the camelCase string to convert
     * @return the snake_case equivalent
     */
    private String toSnake(String camel) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camel);
    }

    /**
     * Performs a partial update on a database table using ObjectNode data.
     * 
     * <p>
     * This method creates a new database transaction and updates only the fields
     * present in the ObjectNode. Key fields are used for the WHERE clause.
     * </p>
     * 
     * @param table    the name of the database table to update
     * @param object   the ObjectNode containing field values to update
     * @param keyField the field names to use as keys in the WHERE clause
     */
    public void updatePartialObject(String table, ObjectNode object, String... keyField) {
        JdbiProvider.getInstance().getJdbi().useHandle(handle -> updatePartialObject(handle, table, object, keyField));
    }

    /**
     * Performs a partial update on a database table using an existing JDBI handle.
     * 
     * <p>
     * This method builds a dynamic UPDATE statement based on the fields present
     * in the ObjectNode. It automatically:
     * </p>
     * <ul>
     * <li>Converts camelCase field names to snake_case for database columns</li>
     * <li>Handles JSON/JSONB columns by casting to ::jsonb</li>
     * <li>Properly binds null, number, boolean, and string values</li>
     * <li>Excludes key fields from the SET clause</li>
     * </ul>
     * 
     * @param handle   the JDBI handle to use for the operation
     * @param table    the name of the database table to update
     * @param object   the ObjectNode containing field values to update
     * @param keyField the field names to use as keys in the WHERE clause
     */
    public void updatePartialObject(Handle handle, String table, ObjectNode object, String... keyField) {
        List<String> sets = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        Set<String> keySet = new HashSet<>();
        for (String k : keyField)
            keySet.add(toSnake(k));

        object.fieldNames().forEachRemaining(field -> {
            String dbField = toSnake(field);
            var value = object.get(field);
            if (!keySet.contains(dbField)) {
                if (value.isObject() || value.isArray()) {
                    sets.add(dbField + " = :" + dbField + "::jsonb");
                    values.put(dbField, value.toString());
                } else {
                    sets.add(dbField + " = :" + dbField);
                    if (value.isNull()) {
                        values.put(dbField, null);
                    } else if (value.isNumber()) {
                        values.put(dbField, value.numberValue());
                    } else if (value.isBoolean()) {
                        values.put(dbField, value.booleanValue());
                    } else {
                        values.put(dbField, value.asText());
                    }
                }
            } else {
                // key field, bind cho where
                if (value.isNull()) {
                    values.put(dbField, null);
                } else if (value.isNumber()) {
                    values.put(dbField, value.numberValue());
                } else if (value.isBoolean()) {
                    values.put(dbField, value.booleanValue());
                } else {
                    values.put(dbField, value.asText());
                }
            }
        });

        // Build WHERE clause với keyField đã snake_case hóa
        String where = String.join(" AND ", Arrays.stream(keyField)
                .map(k -> toSnake(k) + " = :" + toSnake(k)).toList());

        String sql = String.format(
                "UPDATE %s SET %s WHERE %s",
                table,
                String.join(", ", sets),
                where);

        handle.createUpdate(sql).bindMap(values).execute();
    }
}

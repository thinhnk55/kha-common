package com.defi.common.util.filter;

import com.google.common.base.CaseFormat;

/**
 * Utility class for handling data filtering operations.
 * 
 * <p>
 * This class provides utilities for converting field names and handling
 * database query filtering. It automatically converts between Java naming
 * conventions (camelCase) and database naming conventions (snake_case).
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Convert Java field name to database column name
 * String dbColumn = FilterHelper.toDbColumn("firstName"); // Returns "first_name"
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
public class FilterHelper {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FilterHelper() {
        // Utility class
    }

    /**
     * Converts a camelCase field name to snake_case database column name.
     * 
     * @param field the camelCase field name to convert
     * @return the snake_case database column name
     */
    public static String toDbColumn(String field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field);
    }
}

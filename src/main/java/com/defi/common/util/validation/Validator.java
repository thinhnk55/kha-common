package com.defi.common.util.validation;

import com.defi.common.api.BaseResponse;

import java.util.regex.Pattern;

/**
 * A fluent validation builder that provides chainable validation methods for common validation scenarios.
 * 
 * <p>This class allows building validation chains to check various constraints on input values
 * such as required fields, string length, numeric ranges, and pattern matching.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * BaseResponse<?> result = Validator.create()
 *     .required(username)
 *     .minLength(username, 3)
 *     .maxLength(username, 20)
 *     .pattern(email, ValidationPattern.EMAIL_REGEX)
 *     .validate();
 * }</pre>
 * 
 * @see BaseResponse
 * @see ValidationPattern
 */
public class Validator {
    private boolean hasError = false;

    /**
     * Creates a new instance of Validator.
     * 
     * @return a new Validator instance
     */
    public static Validator create() {
        return new Validator();
    }

    /**
     * Validates that the given value is not null or empty.
     * For String values, also checks that the trimmed string is not empty.
     * 
     * @param value the value to validate
     * @return this Validator instance for method chaining
     */
    public Validator required(Object value) {
        if (value == null) {
            hasError = true;
        } else if (value instanceof String && ((String) value).trim().isEmpty()) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given string has a minimum length after trimming.
     * 
     * @param value the string to validate
     * @param minLength the minimum required length
     * @return this Validator instance for method chaining
     */
    public Validator minLength(String value, int minLength) {
        if (value != null && value.trim().length() < minLength) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given string does not exceed the maximum length.
     * 
     * @param value the string to validate
     * @param maxLength the maximum allowed length
     * @return this Validator instance for method chaining
     */
    public Validator maxLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given string's length is within the specified range.
     * 
     * @param value the string to validate
     * @param minLength the minimum required length
     * @param maxLength the maximum allowed length
     * @return this Validator instance for method chaining
     */
    public Validator rangeLength(String value, int minLength, int maxLength) {
        if (value != null) {
            int val = value.length();
            if (val < minLength || val > maxLength) {
                hasError = true;
            }
        }
        return this;
    }

    /**
     * Validates that the given string matches the specified regular expression pattern.
     * 
     * @param value the string to validate
     * @param regex the regular expression pattern to match against
     * @return this Validator instance for method chaining
     */
    public Validator pattern(String value, String regex) {
        if (value != null && !Pattern.matches(regex, value)) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given numeric value is not less than the minimum value.
     * 
     * @param value the numeric value to validate
     * @param minValue the minimum allowed value
     * @return this Validator instance for method chaining
     */
    public Validator minValue(Number value, Number minValue) {
        if (value != null && value.doubleValue() < minValue.doubleValue()) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given numeric value does not exceed the maximum value.
     * 
     * @param value the numeric value to validate
     * @param maxValue the maximum allowed value
     * @return this Validator instance for method chaining
     */
    public Validator maxValue(Number value, Number maxValue) {
        if (value != null && value.doubleValue() > maxValue.doubleValue()) {
            hasError = true;
        }
        return this;
    }

    /**
     * Validates that the given numeric value is within the specified range.
     * 
     * @param value the numeric value to validate
     * @param minValue the minimum allowed value
     * @param maxValue the maximum allowed value
     * @return this Validator instance for method chaining
     */
    public Validator range(Number value, Number minValue, Number maxValue) {
        if (value != null) {
            double val = value.doubleValue();
            if (val < minValue.doubleValue() || val > maxValue.doubleValue()) {
                hasError = true;
            }
        }
        return this;
    }

    /**
     * Executes the validation and returns the result.
     * 
     * @return {@link BaseResponse#INVALID_PARAM} if any validation failed,
     *         {@link BaseResponse#SUCCESS} otherwise
     */
    public BaseResponse<?> validate() {
        return hasError ? BaseResponse.INVALID_PARAM : BaseResponse.SUCCESS;
    }
}
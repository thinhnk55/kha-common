package com.defi.common.util.validation;

/**
 * {@code ValidationUtil} provides static utility methods for validating common input formats,
 * such as emails, passwords, phone numbers, and folder names. It also includes string length checks.
 *
 * <p>This class is stateless and intended to be used as a helper in input validation logic.</p>
 */
public class ValidationPattern {
    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationPattern() {
        // Utility class
    }


    /** Regex pattern for validating email addresses (basic structure). */
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /** Regex pattern for validating phone numbers (international format with optional country code). */
    public static final String PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$";

    /**
     * Regex for validating strong passwords.
     * Requires:
     * - at least one lowercase letter,
     * - at least one uppercase letter,
     * - at least one digit.
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    /** Regex for validating folder names (alphanumeric, underscores, and dashes). */
    public static final String FOLDER_REGEX = "^[a-zA-Z0-9_-]+$";
}

package com.defi.common.util.validation;

import java.util.regex.Pattern;

/**
 * {@code ValidationUtil} provides static utility methods for validating common input formats,
 * such as emails, passwords, phone numbers, and folder names. It also includes string length checks.
 *
 * <p>This class is stateless and intended to be used as a helper in input validation logic.</p>
 */
public class ValidationUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtil() {
        // Utility class
    }


    /** Regex pattern for validating email addresses (basic structure). */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Regex for validating strong passwords.
     * Requires:
     * - at least one lowercase letter,
     * - at least one uppercase letter,
     * - at least one digit.
     */
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /** Regex for validating folder names (alphanumeric, underscores, and dashes). */
    private static final String FOLDER_REGEX = "^[a-zA-Z0-9_-]+$";
    private static final Pattern FOLDER_PATTERN = Pattern.compile(FOLDER_REGEX);

    /**
     * Checks whether a string is within the specified length range.
     *
     * @param input     the string to check
     * @param minLength the minimum allowed length
     * @param maxLength the maximum allowed length
     * @return {@code true} if the string is within the bounds, {@code false} otherwise
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return false;
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates whether the input password meets basic strength criteria.
     * Requires at least one lowercase, one uppercase, and one digit.
     *
     * @param password the password string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates the format of an email address.
     *
     * @param email the email string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates that the phone number contains only digits and is between 1 and 11 digits long.
     *
     * @param phoneNumber the phone number string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return phoneNumber.matches("\\d{1,11}");
    }

    /**
     * Validates that a folder name contains only letters, digits, dashes, or underscores.
     *
     * @param folderName the folder name string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidFolderName(String folderName) {
        if (folderName == null) {
            return false;
        }
        return FOLDER_PATTERN.matcher(folderName).matches();
    }
}

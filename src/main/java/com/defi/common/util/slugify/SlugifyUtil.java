package com.defi.common.util.slugify;

import com.github.slugify.Slugify;

/**
 * Utility class for generating URL-friendly slugs from strings.
 * 
 * <p>
 * This utility provides methods for converting strings into slug format
 * suitable for URLs, file names, and other identifier purposes. It uses
 * the slugify library with custom configuration for transliteration and
 * character replacement.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * String slug = SlugifyUtil.slugify("Hello World! 123");
 * // Result: "Hello-World--123"
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
public class SlugifyUtil {

    /**
     * Configured Slugify instance with transliteration enabled and case
     * preservation.
     */
    private static final Slugify slugify = Slugify.builder()
            .lowerCase(false)
            .transliterator(true)
            .build();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private SlugifyUtil() {
        // Utility class
    }

    /**
     * Converts a string into a URL-friendly slug format.
     * 
     * <p>
     * This method transliterates unicode characters, removes special characters,
     * and replaces non-alphanumeric characters with hyphens to create a
     * clean slug suitable for URLs or file names.
     * </p>
     * 
     * @param fileName the input string to slugify
     * @return the slugified string with only alphanumeric characters and hyphens
     */
    public static String slugify(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        return slugify.slugify(fileName).replaceAll("[^a-zA-Z0-9]", "-");
    }
}

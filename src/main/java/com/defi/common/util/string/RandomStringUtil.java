package com.defi.common.util.string;

import com.github.f4b6a3.uuid.alt.GUID;
import org.apache.commons.codec.binary.Base64;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * {@code RandomStringUtil} provides utility methods for generating random strings and UUIDs.
 * It supports random alphanumeric and numeric strings, as well as UUID version 1, 4, and 7 generation.
 */
public class RandomStringUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private RandomStringUtil() {
        // Utility class
    }

    /**
     * Generates a base64-url-safe random string of the specified length.
     * Note: The actual randomness is derived from raw bytes and encoded with base64, then truncated.
     *
     * @param length the desired length of the output string
     * @return a random base64-encoded string (URL-safe, no padding)
     */
    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.encodeBase64URLSafeString(bytes).substring(0, length);
    }

    /**
     * Generates a numeric-only string of the specified length.
     * The first digit is guaranteed to be non-zero.
     *
     * @param length the desired length of the numeric string
     * @return a random numeric string
     */
    public static String generateRandomNumericString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextInt(9) + 1); // first digit: 1–9
        for (int i = 1; i < length; i++) {
            sb.append(random.nextInt(10)); // digits: 0–9
        }
        return sb.toString();
    }

    /**
     * Generates a UUID version 1 (time-based).
     *
     * @return a version 1 UUID
     */
    public static UUID uuidV1() {
        return GUID.v1().toUUID();
    }

    /**
     * Generates a UUID version 4 (random-based).
     *
     * @return a version 4 UUID
     */
    public static UUID uuidV4() {
        return GUID.v4().toUUID();
    }

    /**
     * Generates a UUID version 7 (time-ordered, suitable for databases).
     *
     * @return a version 7 UUID
     */
    public static UUID uuidV7() {
        return GUID.v7().toUUID();
    }
}

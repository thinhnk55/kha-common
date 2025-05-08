package com.defi.common.util.file;

import com.defi.common.util.log.DebugLogger;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code FileUtil} is a utility class that provides simple methods for reading and writing
 * text files using UTF-8 encoding, as well as for checking file existence and creating directories.
 *
 * <p>All exceptions are logged using {@link DebugLogger}.</p>
 */
public class FileUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private FileUtil() {
        // Utility class
    }

    /**
     * Reads the entire content of a file as a UTF-8 encoded string.
     *
     * @param filePath the full path to the file
     * @return the file content as a {@link String}, or {@code null} if the file does not exist or an error occurs
     */
    public static String readString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!path.toFile().exists()) {
                return null;
            }
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            DebugLogger.logger.error("Failed to read file: {}", filePath, e);
            return null;
        }
    }

    /**
     * Writes a UTF-8 encoded string to a file. If parent directories do not exist, they will be created.
     *
     * @param fileName the full path to the file
     * @param data     the text content to write
     */
    public static void writeStringToFile(String fileName, String data) {
        Path path = Paths.get(fileName);
        try {
            Files.createDirectories(path.getParent());
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            writer.append(data);
            writer.close();
        } catch (Exception e) {
            DebugLogger.logger.error("Failed to write file: {}", fileName, e);
        }
    }

    /**
     * Checks whether a file exists at the given path.
     *
     * @param filePath the full path to the file
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    public static boolean isExist(String filePath) {
        Path path = Paths.get(filePath);
        return path.toFile().exists();
    }

    /**
     * Creates the parent directories of a given file path if they do not already exist.
     *
     * @param fileName the full path of the file
     */
    public static void createParent(String fileName) {
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            DebugLogger.logger.error("Failed to create parent directories for: {}", fileName, e);
        }
    }
}

package com.defi.common.permission.version;

import javax.sql.DataSource;

/**
 * Factory class for creating VersionChecker implementations based on version
 * source type.
 * 
 * <p>
 * This factory provides a centralized way to create appropriate
 * {@link VersionChecker}
 * implementations for different version sources. It supports both database and
 * API-based
 * version checking to enable automatic policy synchronization.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Database version checker
 * VersionChecker dbChecker = VersionCheckerFactory.createVersionChecker(
 *         "database",
 *         "SELECT MAX(updated_at) FROM policies",
 *         dataSource);
 * 
 * // API version checker
 * VersionChecker apiChecker = VersionCheckerFactory.createVersionChecker(
 *         "api",
 *         "http://auth-service/api/version",
 *         null);
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see VersionChecker
 * @see DatabaseVersionChecker
 * @see ApiVersionChecker
 */
public class VersionCheckerFactory {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private VersionCheckerFactory() {
        // Utility class - no instantiation allowed
    }

    /**
     * Creates a VersionChecker implementation based on the specified version source
     * type.
     * 
     * <p>
     * This method creates and configures the appropriate VersionChecker
     * implementation
     * based on the source type. The returned checker can be used for automatic
     * version
     * polling to detect policy changes.
     * </p>
     *
     * @param versionSourceType the type of version source. Supported values:
     *                          <ul>
     *                          <li><code>"database"</code> - Execute SQL query to
     *                          get version</li>
     *                          <li><code>"api"</code> - Call HTTP endpoint to get
     *                          version</li>
     *                          </ul>
     * @param versionSource     the version source configuration:
     *                          <ul>
     *                          <li><strong>Database</strong>: SQL query that
     *                          returns a single version value</li>
     *                          <li><strong>API</strong>: HTTP endpoint URL that
     *                          returns version information</li>
     *                          </ul>
     * @param dataSource        database connection (required only for database
     *                          type, can be null for API type)
     * @return VersionChecker implementation configured for the specified source
     *         type,
     *         or null if the source type is not supported or parameters are invalid
     * @throws RuntimeException if required parameters are missing (e.g., DataSource
     *                          is null for database type)
     */
    public static VersionChecker createVersionChecker(String versionSourceType,
            String versionSource,
            DataSource dataSource) {
        if (versionSourceType == null || versionSource == null) {
            return null;
        }

        return switch (versionSourceType.toLowerCase()) {
            case "database" -> {
                if (dataSource == null) {
                    throw new RuntimeException("DataSource is required for database version checker");
                }
                DatabaseVersionChecker checker = new DatabaseVersionChecker(dataSource);
                checker.setSqlQuery(versionSource);
                yield checker;
            }
            case "api" -> {
                ApiVersionChecker checker = new ApiVersionChecker();
                checker.setApiEndpoint(versionSource);
                yield checker;
            }
            default -> null; // Unsupported type - no version checking
        };
    }
}
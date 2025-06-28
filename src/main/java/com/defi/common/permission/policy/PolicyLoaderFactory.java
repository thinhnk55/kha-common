package com.defi.common.permission.policy;



import javax.sql.DataSource;
import java.util.List;

/**
 * Factory class for creating PolicyLoader implementations based on policy
 * source type.
 * 
 * <p>
 * This factory provides a centralized way to create appropriate
 * {@link PolicyLoader}
 * implementations based on the specified policy source type. It supports
 * database,
 * resource file, and API-based policy sources.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Database source
 * PolicyLoader dbLoader = PolicyLoaderFactory.createPolicyLoader(
 *         "database",
 *         "SELECT * FROM policies",
 *         Arrays.asList("app1"),
 *         dataSource);
 * 
 * // Resource file source
 * PolicyLoader fileLoader = PolicyLoaderFactory.createPolicyLoader(
 *         "resource",
 *         "casbin/policies.csv",
 *         Collections.emptyList(),
 *         null);
 * 
 * // API source
 * PolicyLoader apiLoader = PolicyLoaderFactory.createPolicyLoader(
 *         "api",
 *         "http://auth-service/api/policies",
 *         Arrays.asList("app1"),
 *         null);
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see PolicyLoader
 * @see DatabasePolicyLoaderImpl
 * @see ResourcePolicyLoaderImpl
 * @see ApiPolicyLoaderImpl
 */
public class PolicyLoaderFactory {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private PolicyLoaderFactory() {
        // Utility class - no instantiation allowed
    }

    /**
     * Creates a PolicyLoader implementation based on the specified policy source
     * type.
     *
     * <p>
     * This method creates and configures the appropriate PolicyLoader
     * implementation
     * based on the source type. Each implementation is optimized for its specific
     * data source type.
     * </p>
     *
     * @param policySourceType the type of policy source. Must be one of:
     *                         <ul>
     *                         <li><code>"database"</code> - Load from database
     *                         using SQL queries</li>
     *                         <li><code>"resource"</code> - Load from classpath
     *                         resource files (CSV format)</li>
     *                         <li><code>"api"</code> - Load from REST API
     *                         endpoints</li>
     *                         </ul>
     * @param policySource     the policy source configuration:
     *                         <ul>
     *                         <li><strong>Database</strong>: SQL query to
     *                         execute</li>
     *                         <li><strong>Resource</strong>: Path to CSV file in
     *                         classpath</li>
     *                         <li><strong>API</strong>: HTTP endpoint URL</li>
     *                         </ul>
     * @param resources        list of resource codes to filter by. Pass empty list
     *                         to load all policies.
     *                         Only policies matching these resource codes will be
     *                         loaded.
     * @param dataSource       database connection (required only for database type,
     *                         can be null for others)
     * @return PolicyLoader implementation configured for the specified source type
     * @throws RuntimeException if policy source type is unsupported or if required
     *                          parameters are missing
     *                          (e.g., DataSource is null for database type)
     */
    public static PolicyLoader createPolicyLoader(String policySourceType,
            String policySource,
            List<String> resources,
            DataSource dataSource) {
        return switch (policySourceType) {
            case "database" -> {
                if (dataSource == null) {
                    throw new RuntimeException("DataSource is required for database policy loader");
                }
                yield new DatabasePolicyLoaderImpl(dataSource, policySource, resources);
            }
            case "resource" -> new ResourcePolicyLoaderImpl(policySource, resources);
            case "api" -> new ApiPolicyLoaderImpl(policySource, resources);
            default -> throw new RuntimeException("Unsupported policy source type: " + policySourceType);
        };
    }
}
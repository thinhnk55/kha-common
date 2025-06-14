package com.defi.common.permission.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Configuration properties for Casbin policy loading and management.
 * 
 * <p>
 * This class encapsulates all configuration needed for policy loading,
 * including resource filtering, policy source configuration, and polling
 * settings for automatic policy synchronization.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * CasbinProperties properties = CasbinProperties.builder()
 *         .resources(Arrays.asList("app1", "app2"))
 *         .policySourceType("database")
 *         .policySource("SELECT * FROM policies")
 *         .polling(PollingConfig.builder().enabled(true).build())
 *         .build();
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Data
@Builder
public class CasbinProperties {

    /**
     * List of resources this service manages or needs policies for.
     * When filtering is enabled, only policies for these resources will be loaded.
     * 
     * <p>
     * If empty or null, all policies will be loaded without filtering.
     * This is useful in microservice architectures where each service
     * only needs policies for specific resources.
     * </p>
     *
     * @default empty list (loads all policies)
     */
    @Builder.Default
    private List<String> resources = List.of();

    /**
     * Type of policy source to load from.
     * 
     * <p>
     * Supported values:
     * </p>
     * <ul>
     * <li><code>database</code> - Load from database using SQL queries</li>
     * <li><code>resource</code> - Load from classpath resource files (CSV
     * format)</li>
     * <li><code>api</code> - Load from REST API endpoints</li>
     * </ul>
     */
    private String policySourceType;

    /**
     * Policy source configuration.
     * 
     * <p>
     * The meaning depends on {@link #policySourceType}:
     * </p>
     * <ul>
     * <li><strong>database</strong>: SQL query to execute</li>
     * <li><strong>resource</strong>: Path to CSV file in classpath</li>
     * <li><strong>api</strong>: HTTP endpoint URL</li>
     * </ul>
     * 
     * @example "SELECT role_id, resource_code, action_code FROM policies"
     * @example "casbin/policies.csv"
     * @example "http://auth-service/api/policies"
     */
    private String policySource;

    /**
     * Polling configuration for version checking and policy reloading.
     * 
     * <p>
     * When polling is enabled, the system will periodically check for
     * version changes and automatically reload policies when changes
     * are detected.
     * </p>
     * 
     * @see PollingConfig
     */
    private PollingConfig polling;
}
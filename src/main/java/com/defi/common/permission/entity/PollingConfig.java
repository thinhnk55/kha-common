package com.defi.common.permission.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Configuration for policy version polling and automatic synchronization.
 * 
 * <p>
 * This class defines settings for automatic policy version checking
 * and policy reloading when changes are detected. It supports both
 * database and API-based version sources.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * PollingConfig config = PollingConfig.builder()
 *         .enabled(true)
 *         .duration(Duration.ofMinutes(5))
 *         .versionSourceType("database")
 *         .versionSource("SELECT MAX(updated_at) FROM policies")
 *         .build();
 * }</pre>
 * 
 * <p>
 * Default constructor is provided by Lombok @NoArgsConstructor annotation.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class PollingConfig {

    /**
     * Default constructor for PollingConfig.
     */
    public PollingConfig() {
        // Default constructor
    }

    /**
     * Duration between version checks.
     *
     * <p>
     * Minimum allowed duration is 1 minute (PT1M).
     * If not configured or disabled, polling will not be enabled.
     * </p>
     *
     * <p>
     * Examples:
     * </p>
     * <ul>
     * <li>PT1M - 1 minute (minimum)</li>
     * <li>PT5M - 5 minutes</li>
     * <li>PT1H - 1 hour</li>
     * </ul>
     * 
     * @see #isValidForPolling()
     */
    private Duration duration;

    /**
     * Whether polling is enabled.
     *
     * <p>
     * When enabled, the system will periodically check the version source
     * for changes and automatically reload policies if a version change
     * is detected. This provides automatic synchronization with the
     * authoritative policy source.
     * </p>
     * 
     * @default false
     */
    @Builder.Default
    private boolean enabled = false;

    /**
     * Version source configuration for checking policy changes.
     *
     * <p>
     * The format depends on {@link #versionSourceType}:
     * </p>
     * <ul>
     * <li><strong>Database source</strong>: SQL query that returns a single version
     * value</li>
     * <li><strong>API source</strong>: HTTP endpoint URL that returns version
     * information</li>
     * </ul>
     * 
     * @example "SELECT MAX(updated_at) FROM policies"
     * @example "http://api.example.com/auth/v1/internal/version/policy_version"
     */
    private String versionSource;

    /**
     * Type of version source.
     * 
     * <p>
     * Supported values:
     * </p>
     * <ul>
     * <li><code>database</code> - Execute SQL query to get version</li>
     * <li><code>api</code> - Call HTTP endpoint to get version</li>
     * </ul>
     * 
     * @see #versionSource
     */
    private String versionSourceType;

    /**
     * Checks if polling configuration is valid and can be enabled.
     *
     * <p>
     * For polling to be valid, the duration must be specified and
     * must be at least 1 minute to prevent excessive polling.
     * </p>
     * 
     * @return true if polling can be enabled with this configuration
     */
    public boolean isValidForPolling() {
        return duration != null &&
                duration.toMinutes() >= 1;
    }

    /**
     * Gets the minimum allowed polling duration (1 minute).
     *
     * <p>
     * This is enforced to prevent excessive polling that could
     * impact system performance.
     * </p>
     * 
     * @return minimum duration of 1 minute
     */
    public static Duration getMinimumDuration() {
        return Duration.ofMinutes(1);
    }
}
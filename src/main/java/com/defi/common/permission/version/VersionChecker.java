package com.defi.common.permission.version;

import java.util.Optional;

/**
 * Interface for checking version numbers from different sources.
 * 
 * <p>
 * This interface abstracts version checking logic to support
 * different sources like database direct access or API endpoints.
 * It is used by the version polling system to detect changes in
 * policy data and trigger automatic policy reloads.
 * </p>
 * 
 * <p>
 * Implementations of this interface should be thread-safe as they
 * may be called from background polling threads.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see DatabaseVersionChecker
 * @see ApiVersionChecker
 */
public interface VersionChecker {

    /**
     * Gets the current version for policy data.
     * 
     * <p>
     * This method should return a version number that represents the
     * current state of the policy data. The exact meaning of the version
     * depends on the implementation (e.g., timestamp, sequence number).
     * </p>
     * 
     * @return current version number, or empty if not found or error occurred
     */
    Optional<Long> getCurrentVersion();

    /**
     * Checks if the version checker is available and working.
     * 
     * <p>
     * This method should perform a quick health check to determine if
     * the version source is accessible and operational. It is used to
     * determine whether version polling should be enabled.
     * </p>
     * 
     * @return true if version checking is operational, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets a description of this version checker implementation.
     * 
     * <p>
     * This method returns a human-readable description that can be used
     * for logging and debugging purposes. It should include information
     * about the version source being used.
     * </p>
     * 
     * @return description string
     */
    String getDescription();
}
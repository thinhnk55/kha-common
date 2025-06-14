package com.defi.common.permission;

import com.defi.common.permission.event.PolicyEventManager;
import com.defi.common.permission.policy.PolicyLoader;
import com.defi.common.permission.policy.PolicyLoaderFactory;
import com.defi.common.permission.version.VersionChecker;
import com.defi.common.permission.version.VersionCheckerFactory;
import com.defi.common.permission.version.VersionPollingService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;

/**
 * {@code PermissionChecker} is a singleton class that manages permission
 * checking and
 * policy enforcement using the Casbin library for access control.
 * 
 * <p>
 * This class provides:
 * </p>
 * <ul>
 * <li>Policy loading from various sources (database, resource, API)</li>
 * <li>Version polling for automatic policy synchronization</li>
 * <li>Event listening for real-time policy updates</li>
 * <li>Casbin-based permission checking</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * PermissionChecker checker = PermissionChecker.getInstance();
 * checker.init(modelConfig, "database", sqlQuery, resources, true,
 *         "database", versionQuery, 300, dataSource);
 * 
 * // Check permission
 * boolean allowed = checker.checkPermission("user123", "users", "read");
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class PermissionChecker {

    /**
     * Singleton instance of {@code PermissionChecker}.
     */
    @Getter
    private static final PermissionChecker instance = new PermissionChecker();

    /**
     * Enforcer for Casbin access control model.
     */
    @Getter
    private Enforcer enforcer;

    /**
     * Policy loader for loading policies from configured source.
     */
    private PolicyLoader policyLoader;

    /**
     * Event manager for handling policy events via Redis pub/sub.
     */
    private PolicyEventManager eventManager;

    /**
     * Version polling service for automatic policy synchronization.
     */
    private VersionPollingService versionPollingService;

    /**
     * Flag to track initialization status.
     */
    private volatile boolean initialized = false;

    /**
     * Private constructor to prevent direct instantiation.
     * Use {@code PermissionChecker.getInstance()} to get the singleton instance.
     */
    private PermissionChecker() {
    }

    /**
     * Initializes the PermissionChecker with comprehensive policy management.
     * 
     * <p>
     * This method can only be called once. Subsequent calls will be ignored
     * to prevent resource conflicts and memory leaks.
     * </p>
     * 
     * <p>
     * This method performs the following operations:
     * </p>
     * <ol>
     * <li>Load Casbin model and create enforcer</li>
     * <li>Create and configure policy loader</li>
     * <li>Load initial policies</li>
     * <li>Setup version polling (if enabled and applicable)</li>
     * <li>Start event listening system</li>
     * </ol>
     * 
     * @param modelConfig       Casbin model configuration text
     * @param policySourceType  Type of policy source ("database", "resource",
     *                          "api")
     * @param policySource      Policy source configuration (SQL query, file path,
     *                          or API endpoint)
     * @param resources         List of resource codes to filter (empty for all)
     * @param pollingEnabled    Whether to enable version polling
     * @param versionSourceType Type of version source ("database", "api")
     * @param versionSource     Version source configuration (SQL query or API
     *                          endpoint)
     * @param durationSeconds   Polling interval in seconds
     * @param dataSource        Database connection (required for database sources)
     * @throws RuntimeException if initialization fails or already initialized
     */
    public synchronized void init(String modelConfig,
            String policySourceType,
            String policySource,
            List<String> resources,
            boolean pollingEnabled,
            String versionSourceType,
            String versionSource,
            int durationSeconds,
            DataSource dataSource) {

        if (initialized) {
            log.warn("PermissionChecker is already initialized - ignoring subsequent init call");
            return;
        }

        log.info("Initializing PermissionChecker - policySource: {}, polling: {}",
                policySourceType, pollingEnabled);

        try {
            // 1. Initialize Casbin model and enforcer
            initializeCasbin(modelConfig);

            // 2. Create and configure policy loader
            initializePolicyLoader(policySourceType, policySource, resources, dataSource);

            // 3. Load initial policies
            loadInitialPolicies();

            // 4. Setup version polling (if conditions are met)
            initializeVersionPolling(policySourceType, pollingEnabled, versionSourceType,
                    versionSource, durationSeconds, dataSource);

            // 5. Start event listening system
            initializeEventSystem();

            initialized = true;
            log.info("PermissionChecker initialization completed successfully");

        } catch (Exception e) {
            log.error("Failed to initialize PermissionChecker", e);
            // Cleanup on failure
            cleanup();
            throw new RuntimeException("PermissionChecker initialization failed", e);
        }
    }

    /**
     * Checks if a subject has permission to perform an action on a resource.
     * 
     * @param subject  the subject (user ID, role ID, etc.)
     * @param resource the resource being accessed
     * @param action   the action being performed
     * @return true if permission is granted, false otherwise
     * @throws IllegalStateException if PermissionChecker is not initialized
     */
    public boolean checkPermission(String subject, String resource, String action) {
        if (!initialized || enforcer == null) {
            throw new IllegalStateException("PermissionChecker is not initialized");
        }

        try {
            boolean result = enforcer.enforce(subject, resource, action);
            log.debug("Permission check: {} -> {} on {} = {}", subject, action, resource, result);
            return result;
        } catch (Exception e) {
            log.error("Error during permission check: {} -> {} on {}", subject, action, resource, e);
            return false; // Fail-safe: deny access on error
        }
    }

    /**
     * Checks if a subject has permission to perform an action on a resource.
     * 
     * @param subject  the subject (numeric ID)
     * @param resource the resource being accessed
     * @param action   the action being performed
     * @return true if permission is granted, false otherwise
     */
    public boolean checkPermission(Long subject, String resource, String action) {
        return checkPermission(String.valueOf(subject), resource, action);
    }

    /**
     * Checks if the PermissionChecker has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initializes Casbin model and enforcer.
     */
    private void initializeCasbin(String modelConfig) {
        log.debug("Initializing Casbin model and enforcer");
        Model model = new Model();
        model.loadModelFromText(modelConfig);
        this.enforcer = new Enforcer(model);
        log.debug("Casbin enforcer initialized successfully");
    }

    /**
     * Initializes policy loader.
     */
    private void initializePolicyLoader(String policySourceType, String policySource,
            List<String> resources, DataSource dataSource) {
        log.debug("Creating policy loader - type: {}, source: {}", policySourceType, policySource);
        this.policyLoader = PolicyLoaderFactory.createPolicyLoader(
                policySourceType, policySource, resources, dataSource);

        if (this.policyLoader == null) {
            throw new RuntimeException("Failed to create policy loader for type: " + policySourceType);
        }

        log.debug("Policy loader created successfully");
    }

    /**
     * Loads initial policies into enforcer.
     */
    private void loadInitialPolicies() {
        log.info("Loading initial policies");
        policyLoader.loadPolicies(enforcer);
        log.info("Initial policies loaded successfully");
    }

    /**
     * Initializes version polling service if conditions are met.
     */
    private void initializeVersionPolling(String policySourceType, boolean pollingEnabled,
            String versionSourceType, String versionSource,
            int durationSeconds, DataSource dataSource) {

        // Check if version polling should be enabled
        if (!shouldEnableVersionPolling(policySourceType, pollingEnabled)) {
            log.info("Version polling disabled - policySourceType: {}, pollingEnabled: {}",
                    policySourceType, pollingEnabled);
            return;
        }

        log.info("Initializing version polling - type: {}, interval: {}s",
                versionSourceType, durationSeconds);

        try {
            // Create version checker
            VersionChecker versionChecker = VersionCheckerFactory.createVersionChecker(
                    versionSourceType, versionSource, dataSource);

            if (versionChecker == null) {
                log.warn("Could not create version checker for type: {} - polling disabled",
                        versionSourceType);
                return;
            }

            // Create and start version polling service
            this.versionPollingService = new VersionPollingService(
                    versionChecker,
                    Duration.ofSeconds(durationSeconds),
                    this::reloadPermission);

            versionPollingService.start();
            log.info("Version polling service started successfully");

        } catch (Exception e) {
            log.error("Failed to initialize version polling - continuing without polling", e);
        }
    }

    /**
     * Initializes event system for real-time policy updates.
     */
    private void initializeEventSystem() {
        try {
            log.info("Starting policy event system");
            startEventSystem();
            log.info("Policy event system started successfully");
        } catch (Exception e) {
            log.error("Failed to start policy event system - continuing without events", e);
        }
    }

    /**
     * Determines if version polling should be enabled based on policy source type
     * and settings.
     */
    private boolean shouldEnableVersionPolling(String policySourceType, boolean pollingEnabled) {
        return pollingEnabled && !"resource".equalsIgnoreCase(policySourceType);
    }

    /**
     * Reloads permission policies from the configured source.
     * Clears the current Casbin policy and loads fresh policies.
     */
    public void reloadPermission() {
        log.info("Reloading policies");
        try {
            if (policyLoader != null) {
                policyLoader.loadPolicies(enforcer);
                log.info("Policy reload completed successfully");
            } else {
                log.warn("Policy loader not initialized - cannot reload policies");
            }
        } catch (Exception e) {
            log.error("Failed to reload policies", e);
            throw new RuntimeException("Policy reload failed", e);
        }
    }

    /**
     * Initializes and starts the event system for policy change notifications.
     * 
     * @param channelName optional Redis channel name (uses default if null)
     */
    public void startEventSystem(String channelName) {
        try {
            if (channelName != null) {
                eventManager = new PolicyEventManager(channelName);
            } else {
                eventManager = new PolicyEventManager();
            }
            eventManager.startListening();
            log.debug("Event system started on channel: {}", eventManager.getChannelName());
        } catch (Exception e) {
            log.error("Failed to start policy event system", e);
            throw new RuntimeException("Failed to start policy event system", e);
        }
    }

    /**
     * Starts the event system with default channel.
     */
    public void startEventSystem() {
        startEventSystem(null);
    }

    /**
     * Stops the event system and cleans up resources.
     */
    public void stopEventSystem() {
        if (eventManager != null) {
            eventManager.stopListening();
            log.debug("Event system stopped");
        }
    }

    /**
     * Stops version polling service and cleans up resources.
     */
    public void stopVersionPolling() {
        if (versionPollingService != null) {
            versionPollingService.stop();
            log.debug("Version polling service stopped");
        }
    }

    /**
     * Stops all services and cleans up resources.
     */
    public void shutdown() {
        log.info("Shutting down PermissionChecker");
        stopEventSystem();
        stopVersionPolling();
        log.info("PermissionChecker shutdown completed");
    }

    /**
     * Gets the event manager instance.
     * 
     * @return the PolicyEventManager instance or null if not initialized
     */
    public PolicyEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the version polling service instance.
     * 
     * @return the VersionPollingService instance or null if not initialized
     */
    public VersionPollingService getVersionPollingService() {
        return versionPollingService;
    }

    /**
     * Gets the policy loader instance.
     * 
     * @return the PolicyLoader instance or null if not initialized
     */
    public PolicyLoader getPolicyLoader() {
        return policyLoader;
    }

    /**
     * Cleans up resources on failure.
     */
    private void cleanup() {
        log.debug("Cleaning up resources after initialization failure");

        // Stop version polling if started
        if (versionPollingService != null) {
            try {
                versionPollingService.stop();
                versionPollingService = null;
            } catch (Exception e) {
                log.warn("Error stopping version polling during cleanup", e);
            }
        }

        // Stop event system if started
        if (eventManager != null) {
            try {
                eventManager.stopListening();
                eventManager = null;
            } catch (Exception e) {
                log.warn("Error stopping event system during cleanup", e);
            }
        }

        // Clear policy loader
        policyLoader = null;

        // Clear enforcer
        if (enforcer != null) {
            try {
                enforcer.clearPolicy();
                enforcer = null;
            } catch (Exception e) {
                log.warn("Error clearing enforcer during cleanup", e);
            }
        }

        // Reset initialization flag
        initialized = false;

        log.debug("Resource cleanup completed");
    }
}

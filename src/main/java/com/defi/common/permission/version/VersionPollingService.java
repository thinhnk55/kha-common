package com.defi.common.permission.version;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for polling version changes and triggering policy reloads.
 * 
 * <p>
 * This service runs in background to periodically check version changes
 * from the configured version source. When a version change is detected,
 * it triggers a callback to reload policies.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class VersionPollingService {

    private final VersionChecker versionChecker;
    private final Duration pollingInterval;
    private final Runnable reloadCallback;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollingTask;
    private final AtomicLong cachedVersion = new AtomicLong(-1L);
    private volatile boolean isRunning = false;

    /**
     * Creates a new VersionPollingService.
     * 
     * @param versionChecker  the version checker to use
     * @param pollingInterval the interval between version checks
     * @param reloadCallback  callback to invoke when version changes
     */
    public VersionPollingService(VersionChecker versionChecker,
            Duration pollingInterval,
            Runnable reloadCallback) {
        this.versionChecker = versionChecker;
        this.pollingInterval = pollingInterval;
        this.reloadCallback = reloadCallback;
    }

    /**
     * Starts the version polling service.
     * 
     * @throws RuntimeException if service is already running or cannot be started
     */
    public synchronized void start() {
        if (isRunning) {
            log.warn("Version polling service is already running");
            return;
        }

        if (versionChecker == null) {
            log.warn("No version checker configured - polling disabled");
            return;
        }

        if (!versionChecker.isAvailable()) {
            log.warn("Version checker is not available - polling disabled: {}",
                    versionChecker.getDescription());
            return;
        }

        try {
            // Initialize cached version
            loadInitialVersion();

            // Start scheduler
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "version-polling-thread");
                t.setDaemon(true);
                return t;
            });

            pollingTask = scheduler.scheduleWithFixedDelay(
                    this::checkVersionChange,
                    pollingInterval.toSeconds(),
                    pollingInterval.toSeconds(),
                    TimeUnit.SECONDS);

            isRunning = true;
            log.info("Version polling service started - interval: {} seconds", pollingInterval.toSeconds());

        } catch (Exception e) {
            log.error("Failed to start version polling service", e);
            throw new RuntimeException("Failed to start version polling service", e);
        }
    }

    /**
     * Stops the version polling service.
     */
    public synchronized void stop() {
        if (!isRunning) {
            return;
        }

        try {
            // Set running flag to false first to prevent new operations
            isRunning = false;

            // Cancel the polling task
            if (pollingTask != null) {
                pollingTask.cancel(true);
                pollingTask = null;
            }

            // Shutdown the scheduler
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("Scheduler did not terminate gracefully, forcing shutdown");
                        scheduler.shutdownNow();
                        // Wait a bit more for tasks to respond to being cancelled
                        if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                            log.error("Scheduler did not terminate after forced shutdown");
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted while waiting for scheduler termination");
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    scheduler = null;
                }
            }

            log.info("Version polling service stopped");

        } catch (Exception e) {
            log.error("Error stopping version polling service", e);
            // Ensure cleanup even on error
            isRunning = false;
            pollingTask = null;
            if (scheduler != null) {
                scheduler.shutdownNow();
                scheduler = null;
            }
        }
    }

    /**
     * Loads the initial version and caches it.
     */
    private void loadInitialVersion() {
        try {
            Optional<Long> version = versionChecker.getCurrentVersion();
            if (version.isPresent()) {
                cachedVersion.set(version.get());
                log.info("Initial version loaded and cached: {}", version.get());
            } else {
                log.warn("Failed to load initial version - using default value");
                cachedVersion.set(-1L);
            }
        } catch (Exception e) {
            log.error("Error loading initial version", e);
            cachedVersion.set(-1L);
        }
    }

    /**
     * Checks for version changes and triggers reload if needed.
     */
    private void checkVersionChange() {
        try {
            Optional<Long> currentVersion = versionChecker.getCurrentVersion();

            if (currentVersion.isEmpty()) {
                log.debug("Could not retrieve current version - skipping check");
                return;
            }

            long newVersion = currentVersion.get();
            long oldVersion = cachedVersion.get();

            if (oldVersion != -1L && newVersion != oldVersion) {
                log.info("Version change detected: {} -> {} - triggering policy reload",
                        oldVersion, newVersion);

                // Update cached version first
                cachedVersion.set(newVersion);

                // Trigger reload callback
                try {
                    reloadCallback.run();
                    log.info("Policy reload completed successfully due to version change");
                } catch (Exception e) {
                    log.error("Error during policy reload callback", e);
                }

            } else if (oldVersion == -1L) {
                // First successful check after initialization
                cachedVersion.set(newVersion);
                log.debug("Version initialized: {}", newVersion);
            } else {
                log.debug("Version unchanged: {}", newVersion);
            }

        } catch (Exception e) {
            log.error("Error during version check", e);
        }
    }

    /**
     * Gets the currently cached version.
     * 
     * @return cached version or -1 if not available
     */
    public long getCachedVersion() {
        return cachedVersion.get();
    }

    /**
     * Sets the cached version manually (for external version updates).
     * 
     * @param version the version to cache
     */
    public void setCachedVersion(long version) {
        cachedVersion.set(version);
        log.debug("Cached version updated to: {}", version);
    }

    /**
     * Checks if the polling service is currently running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the version checker being used.
     * 
     * @return the version checker
     */
    public VersionChecker getVersionChecker() {
        return versionChecker;
    }
}
package com.defi.common.util.flyway;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import javax.sql.DataSource;

/**
 * Utility class for executing Flyway database migrations.
 * 
 * <p>
 * This class provides a simple interface for running Flyway database
 * migrations with comprehensive logging of the migration process.
 * It automatically configures Flyway with sensible defaults and
 * provides detailed feedback about the migration results.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * DataSource dataSource = getDataSource();
 * FlywayMigrator.migrate(dataSource);
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
@Slf4j
public class FlywayMigrator {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FlywayMigrator() {
        // Utility class
    }

    /**
     * Executes Flyway database migrations with detailed logging.
     * 
     * <p>
     * This method configures and runs Flyway migrations, providing
     * comprehensive logging of the migration process including:
     * </p>
     * <ul>
     * <li>Number of migrations executed</li>
     * <li>Target schema version reached</li>
     * <li>Database information</li>
     * <li>Details of each migration performed</li>
     * </ul>
     * 
     * @param dataSource the database connection source
     * @param scriptLocations locations of sql scripts
     * @throws RuntimeException if migration fails
     */
    public static void migrate(DataSource dataSource, String... scriptLocations) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(scriptLocations)
                    .load();
            MigrateResult result = flyway.migrate();

            log.info(
                    "Flyway migrate finished. Success: true, Migrations executed: {}, Target version: {}, Database: {}",
                    result.migrations.size(), result.targetSchemaVersion, result.database);

            if (!result.migrations.isEmpty()) {
                result.migrations.forEach(migration -> log.info(
                        "-> Version: {}, Description: {}, Type: {}",
                        migration.version, migration.description, migration.type));
            } else {
                log.info("No migrations executed (already up to date).");
            }
        } catch (Exception e) {
            log.error("Flyway migration FAILED: {}", e.getMessage(), e);
            throw new RuntimeException("Flyway migration failed", e);
        }
    }
}

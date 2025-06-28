package com.defi.common.permission.version;

import com.defi.common.util.log.ErrorLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * Database-based version checker implementation using DataSource.
 * 
 * <p>
 * This version uses plain JDBC via DataSource for raw SQL execution.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */

@RequiredArgsConstructor
@Slf4j
public class DatabaseVersionChecker implements VersionChecker {

    /**
     * Default constructor for DatabaseVersionChecker.
     */
    public DatabaseVersionChecker() {
        // This constructor is not used due to @RequiredArgsConstructor
        this.dataSource = null;
    }

    private final DataSource dataSource;
    private String sqlQuery;

    /**
     * Sets the SQL query for version checking.
     * 
     * @param sqlQuery the SQL query to execute for retrieving version information
     */
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public Optional<Long> getCurrentVersion() {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlQuery);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                long version = rs.getLong(1);
                return Optional.of(version);
            }

        } catch (Exception e) {
            ErrorLogger.create("Failed to get version from database", e)
                    .putContext("sqlQuery", sqlQuery)
                    .log();
        }
        return Optional.empty();
    }

    @Override
    public boolean isAvailable() {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlQuery);
                ResultSet rs = ps.executeQuery()) {
            return rs.next(); // If query runs and returns something, it's available
        } catch (Exception e) {
            ErrorLogger.create("Database version checker is unavailable", e)
                    .putContext("sqlQuery", sqlQuery)
                    .log();
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Database-based version checker using: " + sqlQuery;
    }
}

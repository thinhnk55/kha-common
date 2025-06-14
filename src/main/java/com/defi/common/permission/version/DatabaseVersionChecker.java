package com.defi.common.permission.version;

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

    private final DataSource dataSource;
    private String sqlQuery;

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
            log.debug("Failed to get version from database using default query", e);
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
            log.debug("Version check failed (unavailable): {}", sqlQuery, e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Database-based version checker using: " + sqlQuery;
    }
}

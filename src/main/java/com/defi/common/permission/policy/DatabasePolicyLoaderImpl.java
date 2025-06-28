package com.defi.common.permission.policy;

import com.defi.common.permission.entity.PolicyRule;
import com.defi.common.util.log.ErrorLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link PolicyLoader} for loading policies from database
 * sources.
 * 
 * <p>
 * This implementation loads policy rules from a database using custom SQL
 * queries.
 * It supports resource filtering by dynamically modifying SQL queries to
 * include
 * WHERE clauses for specific resource codes.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Custom SQL query execution for flexible policy retrieval</li>
 * <li>Automatic resource filtering with parameterized queries</li>
 * <li>Batch loading into Casbin enforcer for optimal performance</li>
 * <li>Transaction safety with proper resource management</li>
 * </ul>
 * 
 * <p>
 * Expected database schema:
 * </p>
 * 
 * <pre>
 * CREATE TABLE policies (
 *     id BIGINT PRIMARY KEY,
 *     role_id BIGINT NOT NULL,
 *     resource_code VARCHAR(255) NOT NULL,
 *     action_code VARCHAR(255) NOT NULL
 * );
 * </pre>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * DatabasePolicyLoaderImpl loader = new DatabasePolicyLoaderImpl(
 *         dataSource,
 *         "SELECT id, role_id, resource_code, action_code FROM policies",
 *         Arrays.asList("app1", "app2"));
 * loader.loadPolicies(enforcer);
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see PolicyLoader
 * @see PolicyRule
 */
@RequiredArgsConstructor
@Slf4j
public class DatabasePolicyLoaderImpl implements PolicyLoader {

    /**
     * Default constructor for DatabasePolicyLoaderImpl.
     */
    public DatabasePolicyLoaderImpl() {
        // This constructor is not used due to @RequiredArgsConstructor
        this.dataSource = null;
        this.sqlQuery = null;
        this.resources = null;
    }

    /**
     * Database connection source for executing SQL queries.
     */
    private final DataSource dataSource;

    /**
     * SQL query to execute for loading policies.
     * Should select columns: id, role_id, resource_code, action_code.
     */
    private final String sqlQuery;

    /**
     * List of resource codes to filter by.
     * If empty, all policies will be loaded.
     */
    private final List<String> resources;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This implementation:
     * </p>
     * <ol>
     * <li>Clears existing policies from the enforcer</li>
     * <li>Executes the configured SQL query to load policy rules</li>
     * <li>Applies resource filtering if specified</li>
     * <li>Batch loads all policies into the Casbin enforcer</li>
     * </ol>
     * 
     * @param enforcer the Casbin enforcer to load policies into
     * @throws RuntimeException if database operations fail or SQL query is invalid
     */
    @Override
    public void loadPolicies(Enforcer enforcer) {
        log.info("Loading policy rules from database with query: {}", sqlQuery);

        try {
            // Clear existing policies first
            enforcer.clearPolicy();

            List<PolicyRule> policies = loadPolicyRulesFromDatabase(sqlQuery, resources);

            // Load new policies into enforcer
            loadPoliciesIntoEnforcer(enforcer, policies);

            log.info("Policy loading completed successfully - {} policies loaded from database", policies.size());

        } catch (Exception e) {
            ErrorLogger.create("Failed to load policies from database", e)
                    .putContext("sqlQuery", sqlQuery)
                    .log();
            throw new RuntimeException("Database policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads policy rules from database using a custom SQL query.
     * 
     * <p>
     * This method executes the configured SQL query and maps the results to
     * {@link PolicyRule} objects. If resource filtering is enabled, the query
     * is automatically modified to include appropriate WHERE clauses.
     * </p>
     *
     * @param sqlQuery  the SQL query to execute
     * @param resources list of resource codes to filter by (empty list loads all)
     * @return list of policy rules loaded from database
     * @throws RuntimeException if database query fails
     */
    private List<PolicyRule> loadPolicyRulesFromDatabase(String sqlQuery, List<String> resources) {
        List<PolicyRule> policies = new ArrayList<>();

        String finalQuery = sqlQuery;
        if (!resources.isEmpty()) {
            finalQuery = addResourceFilterToQuery(sqlQuery, resources);
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(finalQuery)) {

            if (!resources.isEmpty()) {
                for (int i = 0; i < resources.size(); i++) {
                    ps.setString(i + 1, resources.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    policies.add(PolicyRule.builder()
                            .id(rs.getLong("id"))
                            .roleId(rs.getLong("role_id"))
                            .resourceCode(rs.getString("resource_code"))
                            .actionCode(rs.getString("action_code"))
                            .build());
                }
            }

            log.info("Successfully loaded {} policy rules from database", policies.size());
            return policies;

        } catch (Exception e) {
            ErrorLogger.create("Failed to load policy rules from database", e)
                    .putContext("finalQuery", finalQuery)
                    .putContext("filteredResources", resources)
                    .log();
            throw new RuntimeException("Database policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads policies into Casbin enforcer using batch operations for optimal
     * performance.
     * 
     * <p>
     * This method converts all policy rules to Casbin format and performs a single
     * batch insert operation, which is significantly faster than individual inserts
     * when dealing with large numbers of policies.
     * </p>
     *
     * @param enforcer the Casbin enforcer to load policies into
     * @param policies list of policy rules to load
     * @throws RuntimeException if batch loading fails
     */
    private void loadPoliciesIntoEnforcer(Enforcer enforcer, List<PolicyRule> policies) {
        log.debug("Loading {} policies into enforcer", policies.size());

        if (policies.isEmpty()) {
            log.info("No policies to load");
            return;
        }

        try {
            // Convert all policies to Casbin format
            String[][] casbinPolicies = policies.stream()
                    .map(PolicyRule::toCasbinPolicy)
                    .toArray(String[][]::new);

            // Add all policies in batch
            boolean success = enforcer.addPolicies(casbinPolicies);

            if (success) {
                log.info("Successfully loaded {} policies into enforcer", policies.size());
            } else {
                log.warn("Some policies may have failed to load or already existed");
            }

        } catch (Exception e) {
            ErrorLogger.create("Failed to load policies into enforcer", e)
                    .putContext("policyCount", policies.size())
                    .log();
            throw new RuntimeException("Policy loading into enforcer failed", e);
        }
    }

    /**
     * Adds resource filtering to a SQL query by appending WHERE clause.
     * 
     * <p>
     * This method intelligently modifies the SQL query to include resource
     * filtering.
     * It detects whether the query already has a WHERE clause and adds the filter
     * accordingly using parameterized queries to prevent SQL injection.
     * </p>
     *
     * @param originalQuery the original SQL query
     * @param resources     list of resource codes to filter by
     * @return modified SQL query with resource filtering
     */
    private String addResourceFilterToQuery(String originalQuery, List<String> resources) {
        String upperQuery = originalQuery.toUpperCase().trim();
        String placeholders = String.join(",", resources.stream().map(r -> "?").toList());
        String resourceFilter = "resource_code IN (" + placeholders + ")";

        if (upperQuery.contains("WHERE")) {
            return originalQuery + " AND " + resourceFilter;
        } else {
            return originalQuery + " WHERE " + resourceFilter;
        }
    }
}
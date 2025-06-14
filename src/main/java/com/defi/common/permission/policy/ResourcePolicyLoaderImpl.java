package com.defi.common.permission.policy;

import com.defi.common.mode.ModeManager;
import com.defi.common.permission.entity.PolicyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PolicyLoader for loading policies from resource files (CSV
 * format).
 */
@RequiredArgsConstructor
@Slf4j
public class ResourcePolicyLoaderImpl implements PolicyLoader {

    private final String resourcePath;
    private final List<String> resources;

    @Override
    public void loadPolicies(Enforcer enforcer) {
        log.info("Loading policy rules from resource: {}", resourcePath);

        try {
            // Clear existing policies first
            enforcer.clearPolicy();

            List<PolicyRule> policies = loadPolicyRulesFromCsv(resourcePath, resources);

            // Load new policies into enforcer
            loadPoliciesIntoEnforcer(enforcer, policies);

            log.info("Policy loading completed successfully - {} policies loaded from resource", policies.size());

        } catch (Exception e) {
            log.error("Failed to load policies from resource: {}", resourcePath, e);
            throw new RuntimeException("Resource policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads policy rules from a CSV resource file.
     *
     * @param resourcePath the path to the CSV resource file (e.g.,
     *                     "casbin/policy.csv")
     * @param resources    list of resource codes to filter by (empty list loads
     *                     all)
     * @return list of policy rules loaded from the CSV file
     * @throws RuntimeException if the file cannot be read or parsed
     */
    private List<PolicyRule> loadPolicyRulesFromCsv(String resourcePath, List<String> resources) {
        try {
            String csvContent = ModeManager.getInstance().getConfigContent(resourcePath);
            List<PolicyRule> policies = new ArrayList<>();
            String[] lines = csvContent.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse Casbin CSV format: p,roleId,resourceCode,actionCode
                String[] parts = line.split(",");
                if (parts.length >= 4 && "p".equals(parts[0].trim())) {
                    try {
                        Long roleId = Long.parseLong(parts[1].trim());
                        String resourceCode = parts[2].trim();
                        String actionCode = parts[3].trim();

                        // Apply resource filtering if specified
                        if (resources.isEmpty() || resources.contains(resourceCode)) {
                            PolicyRule policyRule = PolicyRule.builder()
                                    .id((long) (i + 1)) // Use line number as ID
                                    .roleId(roleId)
                                    .resourceCode(resourceCode)
                                    .actionCode(actionCode)
                                    .build();
                            policies.add(policyRule);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid role ID format at line {}: {}", i + 1, line);
                    }
                }
            }

            log.info("Successfully loaded {} policy rules from resource: {}", policies.size(), resourcePath);
            return policies;

        } catch (Exception e) {
            log.error("Failed to load policy rules from resource: {}", resourcePath, e);
            throw new RuntimeException("Resource policy loading failed: " + resourcePath, e);
        }
    }

    /**
     * Loads policies into Casbin enforcer using batch operations for optimal
     * performance.
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
            log.error("Failed to load policies into enforcer", e);
            throw new RuntimeException("Policy loading into enforcer failed", e);
        }
    }
}
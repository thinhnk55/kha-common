package com.defi.common.permission.policy;

import com.defi.common.api.BaseResponse;
import com.defi.common.permission.entity.PolicyRule;
import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PolicyLoader} for loading policies from REST API
 * endpoints.
 * 
 * <p>
 * This implementation loads policy rules from HTTP API endpoints using
 * synchronous
 * HTTP requests. It supports resource filtering by adding query parameters to
 * the
 * API request URL.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>HTTP client with configurable timeouts</li>
 * <li>Automatic resource filtering via query parameters</li>
 * <li>JSON response parsing with validation</li>
 * <li>Batch loading into Casbin enforcer for optimal performance</li>
 * <li>Comprehensive error handling and logging</li>
 * </ul>
 * 
 * <p>
 * Expected API response format:
 * </p>
 * 
 * <pre>{@code
 * {
 *   "data": [
 *     {
 *       "id": 1,
 *       "roleId": 100,
 *       "resourceCode": "users",
 *       "actionCode": "read"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * ApiPolicyLoaderImpl loader = new ApiPolicyLoaderImpl(
 *         "http://auth-service/api/policies",
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
public class ApiPolicyLoaderImpl implements PolicyLoader {

    /**
     * HTTP API endpoint URL for loading policies.
     */
    private final String apiEndpoint;

    /**
     * List of resource codes to filter by.
     * If empty, all policies will be loaded.
     */
    private final List<String> resources;

    /**
     * HTTP client for making API requests.
     */
    private final HttpClient httpClient;

    /**
     * Constructs an ApiPolicyLoaderImpl with the specified endpoint and resource
     * filter.
     * 
     * <p>
     * This constructor creates a new HTTP client with a 5-second connection
     * timeout.
     * The client is configured for synchronous operations and will be reused for
     * all API requests.
     * </p>
     *
     * @param apiEndpoint the HTTP API endpoint URL to load policies from
     * @param resources   list of resource codes to filter by (pass empty list to
     *                    load all policies)
     */
    public ApiPolicyLoaderImpl(String apiEndpoint, List<String> resources) {
        this.apiEndpoint = apiEndpoint;
        this.resources = resources;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public void loadPolicies(Enforcer enforcer) {
        String requestUrl = buildApiUrl(apiEndpoint, resources);
        log.info("Loading policy rules from API: {}", requestUrl);

        try {
            // Clear existing policies first
            enforcer.clearPolicy();

            List<PolicyRule> policies = loadPolicyRulesFromApi(requestUrl, resources);

            // Load new policies into enforcer
            loadPoliciesIntoEnforcer(enforcer, policies);

            log.info("Policy loading completed successfully - {} policies loaded from API", policies.size());

        } catch (Exception e) {
            log.error("Failed to load policies from API: {}", requestUrl, e);
            throw new RuntimeException("API policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads policy rules from an HTTP API endpoint (synchronously).
     *
     * @param apiEndpoint the HTTP API endpoint
     * @param resources   list of resource codes to filter by (optional)
     * @return list of policy rules
     */
    private List<PolicyRule> loadPolicyRulesFromApi(String apiEndpoint, List<String> resources) {
        log.debug("Resource filter: {}", resources);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String jsonResponse = response.body();

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.warn("Received empty response from API: {}", apiEndpoint);
                return new ArrayList<>();
            }

            log.debug("Raw API response: {}", jsonResponse);

            List<PolicyRule> policies = parseApiResponse(jsonResponse);
            log.info("Successfully loaded {} policy rules from API", policies.size());
            log.debug("Loaded policies: {}", policies.stream()
                    .map(PolicyRule::toString)
                    .collect(Collectors.joining(", ")));

            return policies;

        } catch (Exception e) {
            log.error("Failed to load policy rules from API: {}", apiEndpoint, e);
            throw new RuntimeException("API policy loading failed: " + e.getMessage(), e);
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

    private String buildApiUrl(String baseUrl, List<String> resources) {
        if (resources == null || resources.isEmpty())
            return baseUrl;

        String resourceParam = String.join(",", resources);
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "resourceCode=" + resourceParam;
    }

    private List<PolicyRule> parseApiResponse(String jsonResponse) {
        try {
            TypeReference<BaseResponse<List<PolicyRule>>> typeRef = new TypeReference<>() {
            };
            BaseResponse<List<PolicyRule>> response = JsonUtil.fromJsonString(jsonResponse, typeRef);

            if (response.data() == null) {
                log.warn("API response 'data' field is null");
                return new ArrayList<>();
            }

            List<PolicyRule> validPolicies = new ArrayList<>();
            for (PolicyRule policy : response.data()) {
                if (isValidPolicyRule(policy)) {
                    validPolicies.add(policy);
                } else {
                    log.warn("Skipping invalid policy rule: {}", policy);
                }
            }

            return validPolicies;

        } catch (Exception e) {
            log.error("Failed to parse API response", e);
            throw new RuntimeException("Invalid JSON format in API response", e);
        }
    }

    private boolean isValidPolicyRule(PolicyRule policy) {
        return policy != null
                && policy.getRoleId() != null
                && policy.getResourceCode() != null && !policy.getResourceCode().trim().isEmpty()
                && policy.getActionCode() != null && !policy.getActionCode().trim().isEmpty();
    }
}
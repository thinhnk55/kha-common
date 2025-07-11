package com.defi.common.permission.version;

import com.defi.common.api.BaseResponse;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.log.ErrorLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * API-based version checker implementation using Java Core HTTP client.
 * 
 * <p>
 * No RestTemplate, no ObjectMapper — fully Java Core and custom JsonUtil.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */

@Slf4j
public class ApiVersionChecker implements VersionChecker {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private String apiEndpoint;

    /**
     * Creates a new ApiVersionChecker with default HTTP client configuration.
     */
    public ApiVersionChecker() {
        // Default constructor for framework usage
    }

    /**
     * Sets the API endpoint for version checking.
     * 
     * @param endpoint the HTTP endpoint URL to check for version information
     */
    public void setApiEndpoint(String endpoint) {
        this.apiEndpoint = endpoint;
    }

    @Override
    public Optional<Long> getCurrentVersion() {
        if (apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            log.debug("Version API endpoint not configured");
            return Optional.empty();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            if (json == null || json.trim().isEmpty()) {
                log.debug("Empty response from version API for apiEndpoint: {}", apiEndpoint);
                return Optional.empty();
            }

            TypeReference<BaseResponse<Long>> typeRef = new TypeReference<>() {
            };
            BaseResponse<Long> parsed = JsonUtil.fromJson(json, typeRef);

            if (parsed.data() != null) {
                return Optional.of(parsed.data());
            } else {
                log.debug("Null data in version API response for apiEndpoint: {}", apiEndpoint);
                return Optional.empty();
            }

        } catch (Exception e) {
            ErrorLogger.create("Failed to get version from API", e)
                    .putContext("apiEndpoint", apiEndpoint)
                    .log();
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        if (apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            log.debug("Version API endpoint not configured or empty");
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            return json != null && !json.trim().isEmpty();

        } catch (Exception e) {
            ErrorLogger.create("API version checker is unavailable", e)
                    .putContext("apiEndpoint", apiEndpoint)
                    .log();
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "API-based version checker using HTTP calls to: " +
                (apiEndpoint != null ? apiEndpoint : "not configured");
    }
}

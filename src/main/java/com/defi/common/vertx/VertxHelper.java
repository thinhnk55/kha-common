package com.defi.common.vertx;

import com.defi.common.token.entity.Token;
import io.vertx.ext.web.RoutingContext;

/**
 * Utility class for commonly used Vert.x operations within the routing context.
 */
public class VertxHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private VertxHelper() {
        // Utility class
    }

    /**
     * Retrieves the client's IP address from the routing context.
     * <p>
     * If the {@code X-Forwarded-For} header is present (common in reverse proxy
     * setups),
     * it is used. Otherwise, it falls back to the remote address of the request.
     * </p>
     *
     * @param rc the routing context
     * @return the client's IP address as a string
     */
    public static String getIpAddress(RoutingContext rc) {
        // Prefer CF-Connecting-IP (most reliable with Cloudflare)
        String ipAddress = rc.request().getHeader("CF-Connecting-IP");

        // Fallback to X-Forwarded-For
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = rc.request().getHeader("X-Forwarded-For");
            // X-Forwarded-For can have multiple IPs: "client, proxy1, proxy2"
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
        }

        // Fallback to X-Real-IP
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = rc.request().getHeader("X-Real-IP");
        }

        // Finally use remoteAddress
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = rc.request().remoteAddress().host();
        }

        return ipAddress;
    }

    /**
     * Stores a {@link Token} object into the routing context for downstream access.
     *
     * @param rc    the routing context
     * @param token the token to store
     */
    public static void setTokenToRoutingContext(RoutingContext rc, Token token) {
        rc.put("token", token);
    }

    /**
     * Retrieves a {@link Token} object from the routing context.
     *
     * @param rc the routing context
     * @return the token object, or null if not present
     */
    public static Token getTokenFromRoutingContext(RoutingContext rc) {
        return rc.get("token");
    }
}

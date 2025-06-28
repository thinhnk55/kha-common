package com.defi.common.vertx.handler;

import com.defi.common.api.BaseResponse;
import com.defi.common.token.TokenManager;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import io.vertx.ext.web.RoutingContext;

/**
 * Middleware handler for authenticating incoming requests using Bearer tokens.
 * <p>
 * This handler extracts the JWT from the {@code Authorization} header,
 * parses it, and validates that it is an access token. If valid, it attaches
 * the token to the routing context and allows the request to proceed.
 * Otherwise, it returns an {@code UNAUTHORIZED} response.
 * </p>
 */
public class TokenAuthHandler {
    /**
     * Default constructor for use in Vert.x route registration.
     */
    public TokenAuthHandler() {
    }

    /**
     * Stores a {@link Token} object into the routing context for downstream access.
     *
     * @param rc    the routing context
     * @param token the token to store
     */
    public static void putTokenToRoutingContext(RoutingContext rc, Token token) {
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

    /**
     * Processes the incoming request and authenticates it using the Bearer token.
     *
     * @param rc the routing context representing the HTTP request and response
     */
    public static void handle(RoutingContext rc) {
        String authHeader = rc.request().getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            Token token = TokenManager.getInstance().parseToken(jwt);

            if (token != null && token.getTokenType() == TokenType.ACCESS_TOKEN) {
                putTokenToRoutingContext(rc, token);
                rc.next();
                return; // Prevents falling through to unauthorized response
            }
        }

        rc.response().end(BaseResponse.UNAUTHORIZED.toString());
    }
}

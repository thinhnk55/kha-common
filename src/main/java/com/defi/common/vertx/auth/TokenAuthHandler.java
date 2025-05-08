package com.defi.common.vertx.auth;

import com.defi.common.api.BaseResponse;
import com.defi.common.token.TokenManager;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.vertx.VertxHelper;
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
    public TokenAuthHandler() {}

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
                VertxHelper.setTokenToRoutingContext(rc, token);
                rc.next();
                return; // Prevents falling through to unauthorized response
            }
        }

        rc.response().end(BaseResponse.UNAUTHORIZED.toString());
    }
}

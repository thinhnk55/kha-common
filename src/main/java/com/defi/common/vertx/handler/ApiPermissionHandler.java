package com.defi.common.vertx.handler;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.permission.PermissionChecker;
import com.defi.common.token.entity.Token;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for API permission validation in Vert.x routing contexts.
 * <p>
 * This handler validates whether the authenticated user has the required
 * permissions to access a specific resource and perform a given action.
 * It integrates with the permission checking system using Casbin enforcer
 * to evaluate role-based access control (RBAC) rules.
 * </p>
 * <p>
 * The handler expects a valid {@link Token} to be present in the routing
 * context (typically set by {@link TokenAuthHandler}) and validates
 * permissions based on the user's roles.
 * </p>
 * 
 * @see TokenAuthHandler
 * @see PermissionChecker
 */
public class ApiPermissionHandler {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods are static and should be accessed directly through the class.
     */
    private ApiPermissionHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Creates a new permission handler for the specified resource and action.
     * <p>
     * This method returns a Vert.x handler that will validate whether the
     * authenticated user (from the routing context token) has permission
     * to perform the specified action on the given resource.
     * </p>
     * <p>
     * The permission check is performed by evaluating the user's roles
     * against the configured RBAC policies using the Casbin enforcer.
     * If any of the user's roles grants the required permission, access
     * is allowed. Otherwise, a FORBIDDEN response is returned.
     * </p>
     * 
     * @param resource the resource identifier to check permissions for
     * @param action   the action to be performed on the resource
     * @return a Vert.x handler that validates permissions and either
     *         allows the request to continue or returns a FORBIDDEN response
     * @throws IllegalStateException if no token is found in the routing context
     * 
     * @see PermissionChecker
     */
    public static Handler<RoutingContext> create(String resource, String action) {
        return ctx -> {
            Token token = TokenAuthHandler.getTokenFromRoutingContext(ctx);
            boolean isPermission = token.getRoles().stream().anyMatch(
                    role -> PermissionChecker.getInstance().getEnforcer()
                            .enforce(role, resource, action));
            if (!isPermission) {
                ctx.response().setStatusCode(CommonError.FORBIDDEN.getCode()).end(BaseResponse.FORBIDDEN.toString());
                return;
            }
            ctx.next();
        };
    }
}

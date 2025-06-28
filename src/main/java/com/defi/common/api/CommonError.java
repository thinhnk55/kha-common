package com.defi.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code CommonError} defines a standard set of API error codes and messages
 * used throughout the application. This enum implements {@link IError}, allowing it
 * to be used in consistent response construction via {@link BaseResponse}.
 *
 * <p>Each constant includes an HTTP-style status code and a message.</p>
 */
@Getter
@AllArgsConstructor
public enum CommonError implements IError {

    /** Request was successful. */
    SUCCESS(200, "success"),

    /** The request was malformed or invalid. */
    BAD_REQUEST(400, "bad_request"),

    /** One or more parameters are invalid. */
    INVALID_PARAM(400, "invalid_param"),

    /** Authentication is required or has failed. */
    UNAUTHORIZED(401, "unauthorized"),

    /** The user does not have permission to access the resource. */
    FORBIDDEN(403, "forbidden"),

    /** The requested resource was not found. */
    NOT_FOUND(404, "not_found"),

    /** The HTTP method is not supported for the requested resource. */
    METHOD_NOT_ALLOWED(405, "method_not_allowed"),

    /** A conflict occurred (e.g., duplicate resource). */
    CONFLICT(409, "conflict"),

    /** The client has sent too many requests in a given amount of time. */
    TOO_MANY_REQUESTS(429, "too_many_requests"),

    /** An unexpected server-side error occurred. */
    INTERNAL_SERVER(500, "internal_server"),

    /** The server is currently unable to handle the request. */
    SERVICE_UNAVAILABLE(503, "service_unavailable");

    /** The numeric status code (usually aligned with HTTP status codes). */
    private final int code;

    /** The human-readable or machine-friendly message. */
    private final String message;
}

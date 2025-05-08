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

    /** Authentication is required or has failed. */
    UNAUTHORIZED(401, "unauthorized"),

    /** The user does not have permission to access the resource. */
    FORBIDDEN(403, "forbidden"),

    /** The requested resource was not found. */
    NOT_FOUND(404, "not_found"),

    /** A conflict occurred (e.g., duplicate resource). */
    CONFLICT(409, "conflict"),

    /** An unexpected server-side error occurred. */
    INTERNAL_SERVER(500, "internal_server");

    /** The numeric status code (usually aligned with HTTP status codes). */
    private final int code;

    /** The human-readable or machine-friendly message. */
    private final String message;
}

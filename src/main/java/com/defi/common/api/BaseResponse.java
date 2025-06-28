package com.defi.common.api;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A generic API response wrapper that standardizes HTTP responses across the application.
 *
 * <p>This class is a {@link java.lang.Record}, which makes it immutable and ideal
 * for use as a DTO (Data Transfer Object) in REST APIs.</p>
 *
 * @param <T> the type of the response body
 * @param code       the status code (e.g., 200 for success)
 * @param message    a human-readable message
 * @param data       the actual response body (can be null)
 * @param pagination optional pagination metadata
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
        int code,
        String message,
        T data,
        Pagination pagination
) {

    /**
     * Creates a {@code BaseResponse} with the given status code and message.
     * Typically used when no data is returned (e.g., for errors or empty results).
     *
     * @param code    the status code
     * @param message the associated message
     * @param <T>     the response type
     * @return a {@code BaseResponse} instance
     */
    public static <T> BaseResponse<T> of(int code, String message) {
        return new BaseResponse<>(code, message, null, null);
    }

    /**
     * Creates a {@code BaseResponse} from an error and includes data and pagination info.
     *
     * @param error      an error definition implementing {@link IError}
     * @param data       the response data
     * @param pagination the pagination information
     * @param <T>        the response type
     * @return a {@code BaseResponse} instance
     */
    public static <T> BaseResponse<T> of(IError error, T data, Pagination pagination) {
        return new BaseResponse<>(error.getCode(), error.getMessage(), data, pagination);
    }

    /**
     * Creates a {@code BaseResponse} from an error and includes only data.
     *
     * @param error an error definition implementing {@link IError}
     * @param data  the response data
     * @param <T>   the response type
     * @return a {@code BaseResponse} instance
     */
    public static <T> BaseResponse<T> of(IError error, T data) {
        return new BaseResponse<>(error.getCode(), error.getMessage(), data, null);
    }

    /**
     * Creates a {@code BaseResponse} from an error with no data or pagination.
     *
     * @param error an error definition implementing {@link IError}
     * @param <T>   the response type
     * @return a {@code BaseResponse} instance
     */
    public static <T> BaseResponse<T> of(IError error) {
        return new BaseResponse<>(error.getCode(), error.getMessage(), null, null);
    }

    /** A success response with no data. */
    public static final BaseResponse<Void> SUCCESS = of(CommonError.SUCCESS);

    /** A bad request response. */
    public static final BaseResponse<Void> BAD_REQUEST = of(CommonError.BAD_REQUEST);

    /** An unauthorized access response. */
    public static final BaseResponse<Void> UNAUTHORIZED = of(CommonError.UNAUTHORIZED);

    /** A forbidden request response. */
    public static final BaseResponse<Void> FORBIDDEN = of(CommonError.FORBIDDEN);

    /** A resource not found response. */
    public static final BaseResponse<Void> NOT_FOUND = of(CommonError.NOT_FOUND);

    /** An internal server error response. */
    public static final BaseResponse<Void> INTERNAL_SERVER_ERROR = of(CommonError.INTERNAL_SERVER);

    /** A conflict response. */
    public static final BaseResponse<Void> CONFLICT = of(CommonError.CONFLICT);

    /** An invalid parameter response. */
    public static final BaseResponse<Void> INVALID_PARAM = of(CommonError.INVALID_PARAM);

    /** A method not allowed response. */
    public static final BaseResponse<Void> METHOD_NOT_ALLOWED = of(CommonError.METHOD_NOT_ALLOWED);

    /** A too many requests response. */
    public static final BaseResponse<Void> TOO_MANY_REQUESTS = of(CommonError.TOO_MANY_REQUESTS);

    /** A service unavailable response. */
    public static final BaseResponse<Void> SERVICE_UNAVAILABLE = of(CommonError.SERVICE_UNAVAILABLE);

    /**
     * Converts the {@code BaseResponse} to a JSON string using {@link JsonUtil}.
     *
     * @return the JSON representation of the response
     */
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}

package com.defi.common.api;

/**
 * {@code IError} defines a standard contract for error representations used in API responses.
 * It allows consistent access to an error's code and message, enabling reuse across response wrappers,
 * enums, and error handling mechanisms.
 *
 * <p>This interface is typically implemented by enums such as {@link CommonError}.</p>
 */
public interface IError {

    /**
     * Returns the numeric code representing the error.
     *
     * @return the error code
     */
    int getCode();

    /**
     * Returns the human-readable or machine-readable message for the error.
     *
     * @return the error message
     */
    String getMessage();
}

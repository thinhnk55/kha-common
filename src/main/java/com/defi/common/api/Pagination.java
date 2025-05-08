package com.defi.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * {@code Pagination} is a DTO used to encapsulate pagination metadata in API responses.
 * It includes the current page, page size, and total number of records.
 */
@Data
@Builder
@AllArgsConstructor
public class Pagination {

    /**
     * The current page number (starting from 0 or 1 depending on convention).
     */
    private long page;

    /**
     * The number of records per page.
     */
    private long size;

    /**
     * The total number of records across all pages.
     */
    private long total;

    /**
     * Default no-args constructor required for deserialization frameworks like Jackson.
     */
    public Pagination() {
        // No-op constructor
    }
}

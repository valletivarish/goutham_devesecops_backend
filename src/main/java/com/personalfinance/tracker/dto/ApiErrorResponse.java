package com.personalfinance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response following the RFC 7807 "Problem Details" pattern.
 *
 * <p>Returned by the global exception handler whenever an API request fails due
 * to validation errors, authentication failures, or unexpected server errors.
 * If the failure is caused by field-level validation, the {@code fieldErrors}
 * list will contain one entry per invalid field.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    /** HTTP status code (e.g., 400, 401, 404, 500). */
    private int status;

    /** Short error label corresponding to the HTTP status (e.g., "Bad Request"). */
    private String error;

    /** Human-readable description of the problem. */
    private String message;

    /** Server timestamp when the error occurred. */
    private LocalDateTime timestamp;

    /** The request URI path that triggered the error. */
    private String path;

    /**
     * List of individual field-level validation errors.
     * Null or empty when the error is not related to input validation.
     */
    private List<FieldError> fieldErrors;

    /**
     * Describes a single field-level validation failure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {

        /** Name of the field that failed validation (e.g., "amount"). */
        private String field;

        /** Validation constraint code (e.g., "NotNull", "Size"). */
        private String code;

        /** Human-readable validation error message. */
        private String message;
    }
}

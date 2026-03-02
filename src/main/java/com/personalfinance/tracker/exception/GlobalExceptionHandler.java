package com.personalfinance.tracker.exception;

import com.personalfinance.tracker.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized exception handler for the entire REST API surface.
 *
 * <p>Intercepts exceptions thrown by controllers and services and converts them
 * into consistent {@link ApiErrorResponse} payloads. This ensures every error
 * response follows the same JSON structure regardless of the error type.</p>
 *
 * <p>Exception-to-status mapping:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} &rarr; 400 with field-level errors</li>
 *   <li>{@link ValidationException} &rarr; 400</li>
 *   <li>{@link AccessDeniedException} &rarr; 403</li>
 *   <li>{@link ResourceNotFoundException} &rarr; 404</li>
 *   <li>All other exceptions &rarr; 500</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles bean-validation failures triggered by {@code @Valid} on request bodies.
     * Extracts each field error and returns them as a structured list so the client
     * can display inline validation messages.
     *
     * @param ex      the validation exception containing field errors
     * @param request the current HTTP request (used to capture the request path)
     * @return a 400 Bad Request response with per-field error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ApiErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .code(error.getCode())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed for one or more fields")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles domain-level resource-not-found scenarios such as looking up
     * a transaction or category by an ID that does not exist.
     *
     * @param ex      the not-found exception
     * @param request the current HTTP request
     * @return a 404 Not Found response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles custom business-validation failures such as duplicate usernames
     * or category names.
     *
     * @param ex      the validation exception
     * @param request the current HTTP request
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles Spring Security access-denied scenarios where an authenticated user
     * attempts to access a resource they are not authorized for.
     *
     * @param ex      the access-denied exception
     * @param request the current HTTP request
     * @return a 403 Forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("You do not have permission to access this resource")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Catch-all handler for any unhandled exceptions. Logs the full stack trace
     * at ERROR level for debugging while returning a generic 500 response to
     * avoid leaking internal details to the client.
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return a 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Unhandled exception on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

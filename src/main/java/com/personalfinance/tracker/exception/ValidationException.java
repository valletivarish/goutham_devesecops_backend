package com.personalfinance.tracker.exception;

/**
 * Thrown when a business-level validation rule is violated.
 *
 * <p>Unlike bean-validation errors (which are caught from
 * {@link org.springframework.web.bind.MethodArgumentNotValidException}), this
 * exception represents custom domain constraints such as "category name already
 * exists" or "username is already taken".</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to produce a 400 Bad Request response.</p>
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message a human-readable description of the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }
}

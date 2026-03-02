package com.personalfinance.tracker.exception;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * <p>This exception carries the resource type name and the identifier that was
 * searched for, enabling the global exception handler to construct a meaningful
 * 404 response message such as "Transaction not found with id: 42".</p>
 *
 * <p>Handled by {@link GlobalExceptionHandler} to produce a 404 Not Found response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Object resourceId;

    /**
     * Constructs a new ResourceNotFoundException with a descriptive message.
     *
     * @param resourceName the type of resource that was not found (e.g., "Transaction")
     * @param resourceId   the identifier that was searched for
     */
    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    /**
     * Returns the type name of the resource that could not be found.
     *
     * @return the resource type name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the identifier that was used in the failed lookup.
     *
     * @return the resource identifier
     */
    public Object getResourceId() {
        return resourceId;
    }
}

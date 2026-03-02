package com.personalfinance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned after successful authentication.
 *
 * <p>Contains the JWT bearer token that the client must include in the
 * {@code Authorization} header of subsequent requests, along with the
 * authenticated username and an optional human-readable message.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** JWT bearer token to be used for authenticated API calls. */
    private String token;

    /** The authenticated user's username. */
    private String username;

    /** Optional informational message (e.g., "Login successful"). */
    private String message;
}

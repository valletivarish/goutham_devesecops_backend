package com.personalfinance.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for user authentication (login).
 *
 * <p>Contains the credentials required to obtain a JWT token via the
 * authentication endpoint.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    /** The user's unique login name. */
    @NotBlank(message = "Username is required")
    private String username;

    /** The user's password. Must be at least 6 characters. */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}

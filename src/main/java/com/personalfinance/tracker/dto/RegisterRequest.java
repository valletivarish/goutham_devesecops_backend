package com.personalfinance.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for new user registration.
 *
 * <p>All fields are mandatory. The {@code username} must be unique across
 * the system, and the {@code email} must be a well-formed address.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** Desired username. Must be between 3 and 50 characters and globally unique. */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** User's email address. Must be a valid email format and globally unique. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /** Desired password. Must be at least 6 characters. */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}

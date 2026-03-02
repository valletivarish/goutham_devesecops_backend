package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.AuthRequest;
import com.personalfinance.tracker.dto.AuthResponse;
import com.personalfinance.tracker.dto.RegisterRequest;
import com.personalfinance.tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Exposes public endpoints for user registration and login. Both endpoints
 * return an {@link AuthResponse} containing a JWT token that the client should
 * include in the {@code Authorization} header for all subsequent API calls.</p>
 *
 * <p>These endpoints are explicitly permitted in {@link com.personalfinance.tracker.config.SecurityConfig}
 * and do not require authentication.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs the controller with the authentication service.
     *
     * @param authService service handling registration and login logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * <p>Validates the request body, creates the user, and returns a JWT token
     * so the user is immediately authenticated after registration.</p>
     *
     * @param request the registration payload with username, email, and password
     * @return 201 Created with the {@link AuthResponse} containing the JWT
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user with their credentials.
     *
     * <p>Validates the request body, verifies credentials, and returns a JWT token
     * for subsequent authenticated requests.</p>
     *
     * @param request the login payload with username and password
     * @return 200 OK with the {@link AuthResponse} containing the JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

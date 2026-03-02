package com.personalfinance.tracker.service;

import com.personalfinance.tracker.config.JwtTokenProvider;
import com.personalfinance.tracker.dto.AuthRequest;
import com.personalfinance.tracker.dto.AuthResponse;
import com.personalfinance.tracker.dto.RegisterRequest;
import com.personalfinance.tracker.exception.ValidationException;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication and user management service.
 *
 * <p>Handles user registration (with uniqueness validation) and login
 * (with credential verification). User-details loading for Spring Security
 * is handled by {@link CustomUserDetailsService}.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        String token = jwtTokenProvider.generateToken(request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .message("Login successful")
                .build();
    }
}

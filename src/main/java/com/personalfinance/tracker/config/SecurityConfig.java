package com.personalfinance.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the REST API.
 *
 * <p>Configures a stateless, JWT-based security model suitable for a single-page
 * application front-end:
 * <ul>
 *   <li>CSRF is disabled because the API relies on Bearer tokens, not cookies.</li>
 *   <li>CORS is enabled and delegated to {@link CorsConfig}.</li>
 *   <li>Session management is set to {@code STATELESS} so that no HTTP session
 *       is ever created or used.</li>
 *   <li>Public endpoints (auth, Swagger UI, OpenAPI docs, Actuator) are explicitly
 *       permitted without authentication.</li>
 *   <li>All other endpoints require a valid JWT.</li>
 *   <li>The {@link JwtAuthenticationFilter} is inserted before Spring's default
 *       {@link UsernamePasswordAuthenticationFilter} so that JWT-based
 *       authentication takes precedence.</li>
 * </ul>
 * </p>
 *
 * <p>Uses the modern {@link SecurityFilterChain} bean approach instead of the
 * deprecated {@code WebSecurityConfigurerAdapter}.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the security configuration with the JWT filter.
     *
     * @param jwtAuthenticationFilter the filter that validates JWT tokens on each request
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Defines the HTTP security filter chain.
     *
     * @param http the {@link HttpSecurity} builder provided by Spring
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so it can be injected
     * into services that need to perform programmatic authentication (e.g., login).
     *
     * @param authenticationConfiguration Spring's auto-configured authentication setup
     * @return the authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides the BCrypt password encoder used for hashing user passwords
     * during registration and verifying them during login.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

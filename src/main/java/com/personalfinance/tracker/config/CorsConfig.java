package com.personalfinance.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) configuration for the REST API.
 *
 * <p>Allows requests from common local development servers (React on port 3000,
 * Vite on port 5173) as well as any deployed front-end via a wildcard origin.
 * All standard HTTP methods and headers are permitted to support typical
 * SPA interactions including preflight OPTIONS requests.</p>
 *
 * <p>The {@code allowCredentials} flag is intentionally set to {@code false}
 * because the API uses stateless JWT authentication via the Authorization header
 * rather than cookies.</p>
 */
@Configuration
public class CorsConfig {

    /**
     * Registers a global CORS configuration source that applies to all endpoints.
     *
     * @return the {@link CorsConfigurationSource} bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow local dev servers and any deployed frontend
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allow all standard HTTP methods for full CRUD support
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers to support Authorization bearer tokens and content types
        configuration.setAllowedHeaders(List.of("*"));

        // Expose the Authorization header so the client can read it from responses
        configuration.setExposedHeaders(List.of("Authorization"));

        // Credentials are not needed since we use stateless JWT via headers
        configuration.setAllowCredentials(false);

        // Cache preflight response for 1 hour to reduce OPTIONS requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

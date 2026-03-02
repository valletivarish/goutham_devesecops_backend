package com.personalfinance.tracker.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for creating and validating JSON Web Tokens (JWT).
 *
 * <p>Uses the JJWT library with the HS256 (HMAC-SHA256) signing algorithm.
 * The secret key and token expiration duration are externalized to
 * {@code application.properties} so they can be overridden per environment
 * without code changes.</p>
 *
 * <p>Property keys:
 * <ul>
 *   <li>{@code app.jwt.secret} -- base secret string used to derive the HMAC key</li>
 *   <li>{@code app.jwt.expiration} -- token lifetime in milliseconds</li>
 * </ul>
 * </p>
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long jwtExpiration;

    /**
     * Constructs the provider and derives the HMAC signing key from the
     * configured secret string.
     *
     * @param jwtSecret     the raw secret string from properties
     * @param jwtExpiration the token lifetime in milliseconds from properties
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Generates a signed JWT for the given username.
     *
     * <p>The token's subject is set to the username, and its expiration is
     * calculated by adding the configured duration to the current time.</p>
     *
     * @param username the authenticated user's username to embed as the subject
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a valid JWT.
     *
     * @param token the JWT string to parse
     * @return the username embedded in the token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates the structural integrity and expiration of a JWT.
     *
     * <p>Catches and logs specific failure reasons (malformed, expired,
     * unsupported, or empty tokens) to aid debugging without exposing
     * details to the client.</p>
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Malformed JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}

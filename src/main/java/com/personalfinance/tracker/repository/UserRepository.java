package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Provides standard CRUD operations plus custom finders needed by the
 * authentication layer to locate users by their unique login credentials.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the login name to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return {@code true} if a user with that username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);
}

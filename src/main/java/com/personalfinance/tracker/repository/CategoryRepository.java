package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 *
 * <p>Provides user-scoped category lookups and a duplicate-name check
 * to enforce uniqueness within a single user's category set.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Returns all categories belonging to a specific user.
     *
     * @param userId the owning user's ID
     * @return list of categories owned by the user
     */
    List<Category> findByUserId(Long userId);

    /**
     * Finds a single category by its ID and owning user's ID.
     * Ensures users can only access their own categories.
     *
     * @param id     the category ID
     * @param userId the owning user's ID
     * @return an {@link Optional} containing the category if found
     */
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks whether a category with the given name already exists for the user.
     * Used to prevent duplicate category names within a single user's account.
     *
     * @param name   the category name to check
     * @param userId the owning user's ID
     * @return {@code true} if a category with that name exists for the user
     */
    boolean existsByNameAndUserId(String name, Long userId);
}

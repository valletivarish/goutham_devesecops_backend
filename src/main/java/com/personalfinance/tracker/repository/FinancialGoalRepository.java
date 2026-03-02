package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FinancialGoal} entities.
 *
 * <p>Provides user-scoped goal lookups and a count query
 * used by the dashboard summary.</p>
 */
@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    /**
     * Returns all financial goals belonging to a specific user.
     *
     * @param userId the owning user's ID
     * @return list of goals owned by the user
     */
    List<FinancialGoal> findByUserId(Long userId);

    /**
     * Finds a single goal by its ID and owning user's ID.
     * Ensures users can only access their own goals.
     *
     * @param id     the goal ID
     * @param userId the owning user's ID
     * @return an {@link Optional} containing the goal if found
     */
    Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);

    /**
     * Counts the total number of financial goals for a user.
     * Used by the dashboard summary to display goal statistics.
     *
     * @param userId the owning user's ID
     * @return the number of goals owned by the user
     */
    long countByUserId(Long userId);
}

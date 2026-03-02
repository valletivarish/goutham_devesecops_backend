package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Budget} entities.
 *
 * <p>Provides user-scoped budget lookups and a count query
 * used by the dashboard summary.</p>
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Returns all budgets belonging to a specific user.
     *
     * @param userId the owning user's ID
     * @return list of budgets owned by the user
     */
    List<Budget> findByUserId(Long userId);

    /**
     * Finds a single budget by its ID and owning user's ID.
     * Ensures users can only access their own budgets.
     *
     * @param id     the budget ID
     * @param userId the owning user's ID
     * @return an {@link Optional} containing the budget if found
     */
    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    /**
     * Counts the total number of budgets for a user.
     * Used by the dashboard summary to display budget statistics.
     *
     * @param userId the owning user's ID
     * @return the number of budgets owned by the user
     */
    long countByUserId(Long userId);
}

package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.FinancialGoal;
import com.personalfinance.tracker.model.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link FinancialGoal} entities.
 *
 * <p>Provides retrieval of financial goals by user, with optional filtering
 * by goal status (ACTIVE, COMPLETED, or CANCELLED).</p>
 */
@Repository
public interface GoalRepository extends JpaRepository<FinancialGoal, Long> {

    /**
     * Returns all financial goals belonging to a user.
     *
     * @param userId the owning user's ID
     * @return list of the user's goals
     */
    List<FinancialGoal> findByUserId(Long userId);

    /**
     * Returns financial goals belonging to a user, filtered by status.
     *
     * @param userId the owning user's ID
     * @param status the goal status to filter by (ACTIVE, COMPLETED, or CANCELLED)
     * @return list of matching goals
     */
    List<FinancialGoal> findByUserIdAndStatus(Long userId, GoalStatus status);
}

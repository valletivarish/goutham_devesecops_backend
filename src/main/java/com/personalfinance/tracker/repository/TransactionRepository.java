package com.personalfinance.tracker.repository;

import com.personalfinance.tracker.model.Transaction;
import com.personalfinance.tracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Transaction} entities.
 *
 * <p>Extends standard CRUD with user-scoped queries, date-range aggregations
 * for budget tracking, and monthly totals used by the forecasting engine.</p>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Returns all transactions belonging to a specific user, ordered newest first.
     *
     * @param userId the owning user's ID
     * @return list of transactions ordered by transaction date descending
     */
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    /**
     * Finds a single transaction by its ID and owning user's ID.
     * Ensures users can only access their own transactions.
     *
     * @param id     the transaction ID
     * @param userId the owning user's ID
     * @return an {@link Optional} containing the transaction if found
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Returns the 5 most recent transactions for a user.
     * Used by the dashboard to display a quick transaction summary.
     *
     * @param userId the owning user's ID
     * @return list of the 5 most recent transactions ordered by date descending
     */
    List<Transaction> findTop5ByUserIdOrderByTransactionDateDesc(Long userId);

    /**
     * Sums the transaction amounts for a given user, category, type, and date range.
     * Used by the budget service to calculate how much has been spent within a budget period.
     *
     * @param userId     the owning user's ID
     * @param categoryId the category to filter by
     * @param type       INCOME or EXPENSE
     * @param startDate  inclusive start of the period
     * @param endDate    inclusive end of the period
     * @return the total amount, or zero if no matching transactions exist
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.id = :categoryId " +
           "AND t.type = :type " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByUserAndCategoryAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sums all transaction amounts of a given type for a user.
     * Used by the dashboard to compute total income or total expenses.
     *
     * @param userId the owning user's ID
     * @param type   INCOME or EXPENSE
     * @return the total amount, or zero if no matching transactions exist
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId,
                                        @Param("type") TransactionType type);

    /**
     * Retrieves monthly spending totals for a user, grouped by year-month.
     * Each result row contains [year, month, totalAmount].
     * Used by the forecast service for linear regression analysis.
     *
     * @param userId the owning user's ID
     * @param type   the transaction type to aggregate (typically EXPENSE)
     * @return list of Object arrays: [Integer year, Integer month, BigDecimal total]
     */
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlyTotalsByUserIdAndType(@Param("userId") Long userId,
                                                     @Param("type") TransactionType type);
}

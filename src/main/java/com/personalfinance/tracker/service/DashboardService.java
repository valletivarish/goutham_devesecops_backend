package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.DashboardDTO;
import com.personalfinance.tracker.dto.TransactionDTO;
import com.personalfinance.tracker.model.Transaction;
import com.personalfinance.tracker.model.TransactionType;
import com.personalfinance.tracker.repository.BudgetRepository;
import com.personalfinance.tracker.repository.FinancialGoalRepository;
import com.personalfinance.tracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for the dashboard summary endpoint.
 *
 * <p>Aggregates high-level financial data for the authenticated user into a
 * single {@link DashboardDTO} that the front-end can render as an at-a-glance
 * overview. The summary includes income and expense totals, net balance,
 * budget and goal counts, and the five most recent transactions.</p>
 *
 * <p>All queries are read-only and scoped to the authenticated user data.</p>
 */
@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final FinancialGoalRepository goalRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param transactionRepository repository for transaction aggregation queries
     * @param budgetRepository      repository for budget count queries
     * @param goalRepository        repository for goal count queries
     */
    public DashboardService(TransactionRepository transactionRepository,
                            BudgetRepository budgetRepository,
                            FinancialGoalRepository goalRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Builds a consolidated dashboard summary for the given user.
     *
     * <p>The summary includes:
     * <ul>
     *   <li><b>totalIncome</b> -- sum of all INCOME transactions</li>
     *   <li><b>totalExpenses</b> -- sum of all EXPENSE transactions</li>
     *   <li><b>balance</b> -- totalIncome minus totalExpenses</li>
     *   <li><b>totalBudgets</b> -- count of all budgets</li>
     *   <li><b>activeBudgets</b> -- same as totalBudgets (all are considered active)</li>
     *   <li><b>totalGoals</b> -- count of all financial goals</li>
     *   <li><b>recentTransactions</b> -- the 5 most recent transactions</li>
     * </ul>
     * </p>
     *
     * @param userId the authenticated user ID
     * @return a fully populated {@link DashboardDTO}
     */
    @Transactional(readOnly = true)
    public DashboardDTO getSummary(Long userId) {
        // Aggregate income and expense totals using repository sum queries
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(
                userId, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndType(
                userId, TransactionType.EXPENSE);

        // Ensure non-null values for arithmetic
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Count budgets and goals
        long budgetCount = budgetRepository.countByUserId(userId);
        long goalCount = goalRepository.countByUserId(userId);

        // Fetch the 5 most recent transactions and map them to DTOs
        List<TransactionDTO> recentTransactions = transactionRepository
                .findTop5ByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::mapTransactionToDTO)
                .toList();

        return DashboardDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .totalBudgets((int) budgetCount)
                .activeBudgets((int) budgetCount)
                .totalGoals((int) goalCount)
                .recentTransactions(recentTransactions)
                .build();
    }

    /**
     * Maps a {@link Transaction} entity to a {@link TransactionDTO}.
     *
     * <p>Duplicated from TransactionService intentionally to keep the dashboard
     * service self-contained and avoid a circular dependency.</p>
     *
     * @param transaction the entity to map
     * @return the corresponding DTO
     */
    private TransactionDTO mapTransactionToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

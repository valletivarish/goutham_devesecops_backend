package com.personalfinance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated dashboard summary returned to the client.
 *
 * <p>Provides a high-level financial overview including income/expense totals,
 * net balance, budget and goal counts, and a list of the most recent
 * transactions. This DTO is read-only and assembled entirely on the server.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    /** Sum of all INCOME transactions for the current period. */
    private BigDecimal totalIncome;

    /** Sum of all EXPENSE transactions for the current period. */
    private BigDecimal totalExpenses;

    /** Net balance calculated as {@code totalIncome - totalExpenses}. */
    private BigDecimal balance;

    /** Total number of budgets defined by the user. */
    private int totalBudgets;

    /** Number of budgets that are currently active (within their date range). */
    private int activeBudgets;

    /** Total number of financial goals defined by the user. */
    private int totalGoals;

    /** A short list of the user's most recent transactions, ordered newest first. */
    private List<TransactionDTO> recentTransactions;
}

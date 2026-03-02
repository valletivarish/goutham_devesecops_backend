package com.personalfinance.tracker.model;

/**
 * Enum representing the recurrence period of a {@link Budget}.
 *
 * <p>Determines how often a budget cycle resets and spending limits are re-evaluated.</p>
 */
public enum BudgetPeriod {

    /** Budget resets every seven days. */
    WEEKLY,

    /** Budget resets every calendar month. */
    MONTHLY,

    /** Budget resets every calendar year. */
    YEARLY
}

package com.personalfinance.tracker.model;

/**
 * Enum representing the type of a financial transaction.
 *
 * <p>Used by {@link Transaction} and {@link Category} to classify
 * monetary movements as either money coming in (INCOME) or money going out (EXPENSE).</p>
 */
public enum TransactionType {

    /** Revenue, salary, dividends, or any other form of incoming funds. */
    INCOME,

    /** Purchases, bills, subscriptions, or any other form of outgoing funds. */
    EXPENSE
}

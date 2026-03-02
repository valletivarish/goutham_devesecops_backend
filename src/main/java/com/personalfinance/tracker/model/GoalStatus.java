package com.personalfinance.tracker.model;

/**
 * Enum representing the lifecycle status of a {@link FinancialGoal}.
 *
 * <p>Tracks whether a savings or investment goal is still being pursued,
 * has been reached, or was abandoned by the user.</p>
 */
public enum GoalStatus {

    /** The goal is currently being tracked and has not yet been met. */
    ACTIVE,

    /** The goal's target amount has been reached or the user marked it as done. */
    COMPLETED,

    /** The user chose to abandon this goal before completion. */
    CANCELLED
}

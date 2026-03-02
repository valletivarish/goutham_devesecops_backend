package com.personalfinance.tracker.dto;

import com.personalfinance.tracker.model.BudgetPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for budget definitions.
 *
 * <p>Represents a spending limit for a specific category over a given period.
 * The {@code spent} and {@code categoryName} fields are calculated/resolved on
 * the server side and included in responses only.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    /** Unique identifier of the budget. Populated in responses only. */
    private Long id;

    /** Maximum spending limit for this budget. Must be at least 0.01. */
    @NotNull(message = "Amount limit is required")
    @DecimalMin(value = "0.01", message = "Amount limit must be at least 0.01")
    private BigDecimal amountLimit;

    /** Recurrence period of the budget (WEEKLY, MONTHLY, or YEARLY). */
    @NotNull(message = "Budget period is required")
    private BudgetPeriod period;

    /** Start date of the budget period. */
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /** Optional end date of the budget period. Null means the budget repeats indefinitely. */
    private LocalDate endDate;

    /** Foreign key to the category this budget applies to. */
    @NotNull(message = "Category is required")
    private Long categoryId;

    /** Human-readable category name. Populated in responses only. */
    private String categoryName;

    /**
     * Total amount already spent within the current budget period.
     * Calculated on the server by aggregating matching transactions.
     * Populated in responses only.
     */
    private BigDecimal spent;

    /** Timestamp when the budget was persisted. Populated in responses only. */
    private LocalDateTime createdAt;
}

package com.personalfinance.tracker.dto;

import com.personalfinance.tracker.model.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for financial goals.
 *
 * <p>Represents a savings or investment target that users work toward over time.
 * The {@code progressPercentage} field is computed on the server as
 * {@code (currentAmount / targetAmount) * 100} and is included in responses only.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {

    /** Unique identifier of the goal. Populated in responses only. */
    private Long id;

    /** Descriptive name of the financial goal. Must be between 1 and 200 characters. */
    @NotBlank(message = "Goal name is required")
    @Size(min = 1, max = 200, message = "Goal name must be between 1 and 200 characters")
    private String name;

    /** Target monetary amount to reach. Must be at least 0.01. */
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be at least 0.01")
    private BigDecimal targetAmount;

    /** Amount saved so far toward this goal. */
    private BigDecimal currentAmount;

    /** Optional target date by which the goal should be achieved. */
    private LocalDate deadline;

    /** Current lifecycle status of the goal (ACTIVE, COMPLETED, or CANCELLED). */
    private GoalStatus status;

    /**
     * Percentage of progress toward the target amount.
     * Calculated as {@code (currentAmount / targetAmount) * 100}.
     * Populated in responses only.
     */
    private Double progressPercentage;

    /** Timestamp when the goal was persisted. Populated in responses only. */
    private LocalDateTime createdAt;
}

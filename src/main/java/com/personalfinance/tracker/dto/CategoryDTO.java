package com.personalfinance.tracker.dto;

import com.personalfinance.tracker.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for transaction categories.
 *
 * <p>Categories group transactions (and budgets) under meaningful labels such as
 * "Groceries", "Salary", or "Utilities". Each category is associated with a
 * {@link TransactionType} so that INCOME and EXPENSE categories remain distinct.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    /** Unique identifier of the category. Populated in responses only. */
    private Long id;

    /** Display name of the category. Must be between 1 and 100 characters. */
    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    /** Whether this category classifies INCOME or EXPENSE transactions. */
    @NotNull(message = "Category type is required")
    private TransactionType type;

    /** Optional icon identifier (e.g., an emoji or icon class name) for UI display. */
    private String icon;
}

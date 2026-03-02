package com.personalfinance.tracker.dto;

import com.personalfinance.tracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for financial transactions.
 *
 * <p>Serves as both a request payload (when creating/updating transactions) and
 * a response payload. Fields marked "response only" (such as {@code id},
 * {@code categoryName}, and {@code createdAt}) are populated by the server and
 * ignored during deserialization of incoming requests.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    /** Unique identifier of the transaction. Populated in responses only. */
    private Long id;

    /** Monetary amount of the transaction. Must be between 0.01 and 999,999,999.99. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999999.99", message = "Amount must not exceed 999,999,999.99")
    private BigDecimal amount;

    /** Whether this transaction is an INCOME or EXPENSE. */
    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    /** Optional free-text description of the transaction. */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** Date when the transaction occurred. Cannot be in the future. */
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    /** Foreign key to the category this transaction belongs to. */
    @NotNull(message = "Category is required")
    private Long categoryId;

    /** Human-readable category name. Populated in responses only. */
    private String categoryName;

    /** Timestamp when the transaction was persisted. Populated in responses only. */
    private LocalDateTime createdAt;
}

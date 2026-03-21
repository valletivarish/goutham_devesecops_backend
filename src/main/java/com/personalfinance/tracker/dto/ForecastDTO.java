package com.personalfinance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for spending/income forecast projections.
 *
 * <p>Contains month-by-month predictions generated from historical transaction
 * data using linear regression, along with a trend indicator and a confidence
 * score (R-squared value) that conveys how well the model fits past data.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDTO {

    /** Ordered list of historical monthly spending totals used for the regression. */
    private List<MonthlyAmount> historicalData;

    /** Ordered list of monthly predictions for upcoming periods. */
    private List<MonthlyPrediction> predictions;

    /**
     * Overall spending/income trend derived from the regression slope.
     * Possible values: {@code "INCREASING"}, {@code "DECREASING"}, or {@code "STABLE"}.
     */
    private String trend;

    /**
     * R-squared (coefficient of determination) value indicating how well the
     * linear model fits historical data. Ranges from 0.0 (no fit) to 1.0 (perfect fit).
     */
    private Double confidence;

    /**
     * Represents a single month's actual historical spending amount.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyAmount {

        /** Month identifier in {@code "YYYY-MM"} format (e.g., "2026-01"). */
        private String month;

        /** Actual spending amount for the given month. */
        private BigDecimal amount;
    }

    /**
     * Represents a single month's predicted financial amount.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPrediction {

        /** Month identifier in {@code "YYYY-MM"} format (e.g., "2026-04"). */
        private String month;

        /** Predicted monetary amount for the given month. */
        private BigDecimal predictedAmount;
    }
}

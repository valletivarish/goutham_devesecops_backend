package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.ForecastDTO;
import com.personalfinance.tracker.model.TransactionType;
import com.personalfinance.tracker.repository.TransactionRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service layer for spending forecast projections.
 *
 * <p>Uses Apache Commons Math3 {@link SimpleRegression} to perform simple linear
 * regression on a user historical monthly EXPENSE totals. The model treats each
 * month as an integer index (0, 1, 2, ...) on the X axis and the monthly total
 * spending as the Y value. From the fitted line, the service predicts spending for
 * the next 3 months and reports the trend direction and confidence (R-squared).</p>
 *
 * <p>A minimum of 3 months of data is required for meaningful predictions. If
 * fewer months are available, an empty prediction list is returned with a
 * descriptive message encoded in the trend field.</p>
 */
@Service
public class ForecastService {

    private static final int MIN_MONTHS_FOR_FORECAST = 3;
    private static final int FORECAST_MONTHS_AHEAD = 3;
    private static final double SLOPE_THRESHOLD = 0.01;

    private final TransactionRepository transactionRepository;

    /**
     * Constructs the service with the transaction repository.
     *
     * @param transactionRepository repository for querying monthly spending totals
     */
    public ForecastService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generates a spending forecast for the authenticated user.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Query monthly EXPENSE totals grouped by year-month.</li>
     *   <li>If fewer than 3 months are available, return an empty forecast
     *       with a NOT_ENOUGH_DATA trend.</li>
     *   <li>Feed each month index (0-based) and total as (X, Y) data points
     *       into a simple linear regression model.</li>
     *   <li>Use the fitted model to predict the next 3 months.</li>
     *   <li>Determine the trend direction from the regression slope:
     *       positive = INCREASING, negative = DECREASING, near zero = STABLE.</li>
     *   <li>Report the R-squared value as a confidence metric.</li>
     * </ol>
     * </p>
     *
     * @param userId the authenticated user ID
     * @return a {@link ForecastDTO} containing predictions, trend, and confidence
     */
    @Transactional(readOnly = true)
    public ForecastDTO getSpendingForecast(Long userId) {
        // Fetch monthly spending totals: each row is [year, month, sum]
        List<Object[]> monthlyTotals = transactionRepository
                .findMonthlyTotalsByUserIdAndType(userId, TransactionType.EXPENSE);

        // Guard: not enough historical data for a meaningful forecast
        if (monthlyTotals.size() < MIN_MONTHS_FOR_FORECAST) {
            return ForecastDTO.builder()
                    .predictions(Collections.emptyList())
                    .trend("NOT_ENOUGH_DATA")
                    .confidence(0.0)
                    .build();
        }

        // Build the regression model and collect historical data
        SimpleRegression regression = new SimpleRegression();
        List<ForecastDTO.MonthlyAmount> historicalData = new ArrayList<>();
        YearMonth lastMonth = null;

        for (int i = 0; i < monthlyTotals.size(); i++) {
            Object[] row = monthlyTotals.get(i);
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            double total = ((Number) row[2]).doubleValue();

            regression.addData(i, total);
            lastMonth = YearMonth.of(year, month);

            historicalData.add(ForecastDTO.MonthlyAmount.builder()
                    .month(lastMonth.toString())
                    .amount(BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        // Generate predictions for the next N months
        int nextIndex = monthlyTotals.size();
        List<ForecastDTO.MonthlyPrediction> predictions = new ArrayList<>();

        for (int i = 0; i < FORECAST_MONTHS_AHEAD; i++) {
            YearMonth predictionMonth = lastMonth.plusMonths(i + 1);
            double predictedValue = regression.predict(nextIndex + i);

            // Floor negative predictions at zero since spending cannot be negative
            BigDecimal predictedAmount = BigDecimal.valueOf(Math.max(predictedValue, 0))
                    .setScale(2, RoundingMode.HALF_UP);

            predictions.add(ForecastDTO.MonthlyPrediction.builder()
                    .month(predictionMonth.toString())
                    .predictedAmount(predictedAmount)
                    .build());
        }

        // Determine trend direction from the regression slope
        double slope = regression.getSlope();
        String trend;
        if (slope > SLOPE_THRESHOLD) {
            trend = "INCREASING";
        } else if (slope < -SLOPE_THRESHOLD) {
            trend = "DECREASING";
        } else {
            trend = "STABLE";
        }

        // R-squared indicates how well the linear model fits historical data
        double rSquared = regression.getRSquare();
        // Handle NaN case when all Y values are identical (perfect horizontal line)
        if (Double.isNaN(rSquared)) {
            rSquared = 1.0;
        }

        return ForecastDTO.builder()
                .historicalData(historicalData)
                .predictions(predictions)
                .trend(trend)
                .confidence(Math.round(rSquared * 10000.0) / 10000.0)
                .build();
    }
}

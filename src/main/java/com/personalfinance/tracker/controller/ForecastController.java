package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.ForecastDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for spending forecast endpoints.
 *
 * <p>Provides linear-regression-based spending predictions derived from
 * the authenticated user historical transaction data. The forecast includes
 * predicted amounts for the next 3 months, a trend direction indicator,
 * and an R-squared confidence score.</p>
 */
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param forecastService service for generating spending forecasts
     * @param userRepository  repository for resolving the authenticated user ID
     */
    public ForecastController(ForecastService forecastService,
                              UserRepository userRepository) {
        this.forecastService = forecastService;
        this.userRepository = userRepository;
    }

    /**
     * Returns a spending forecast for the authenticated user.
     *
     * <p>Uses simple linear regression on monthly EXPENSE totals to predict
     * the next 3 months of spending. If fewer than 3 months of historical
     * data exist, returns an empty prediction list with a NOT_ENOUGH_DATA trend.</p>
     *
     * @param authentication the current security context
     * @return 200 OK with the {@link ForecastDTO}
     */
    @GetMapping("/spending")
    public ResponseEntity<ForecastDTO> getSpendingForecast(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(forecastService.getSpendingForecast(userId));
    }

    /**
     * Resolves the current authenticated user ID from the security context.
     *
     * @param authentication the current security context
     * @return the authenticated user database ID
     * @throws UsernameNotFoundException if the user cannot be found
     */
    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
        return user.getId();
    }
}

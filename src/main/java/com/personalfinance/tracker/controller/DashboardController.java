package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.DashboardDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the dashboard summary endpoint.
 *
 * <p>Provides a single aggregated view of the authenticated user financial
 * data including income/expense totals, net balance, budget and goal counts,
 * and recent transactions.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param dashboardService service for aggregating dashboard data
     * @param userRepository   repository for resolving the authenticated user ID
     */
    public DashboardController(DashboardService dashboardService,
                               UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    /**
     * Returns the dashboard summary for the authenticated user.
     *
     * <p>Aggregates total income, total expenses, balance, budget count,
     * goal count, and the 5 most recent transactions.</p>
     *
     * @param authentication the current security context
     * @return 200 OK with the {@link DashboardDTO}
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardDTO> getSummary(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(dashboardService.getSummary(userId));
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

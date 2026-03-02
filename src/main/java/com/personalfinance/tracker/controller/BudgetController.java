package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.BudgetDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for budget CRUD operations.
 *
 * <p>All endpoints require authentication. Budget responses include a calculated
 * {@code spent} field showing how much of the budget has been consumed by
 * matching expense transactions.</p>
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param budgetService  service for budget business logic
     * @param userRepository repository for resolving the authenticated user ID
     */
    public BudgetController(BudgetService budgetService,
                            UserRepository userRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    /**
     * Lists all budgets for the authenticated user.
     *
     * @param authentication the current security context
     * @return 200 OK with a list of budget DTOs including spent amounts
     */
    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getAllBudgets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.getAllBudgets(userId));
    }

    /**
     * Retrieves a single budget by its ID.
     *
     * @param id             the budget ID
     * @param authentication the current security context
     * @return 200 OK with the budget DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudgetById(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.getBudgetById(id, userId));
    }

    /**
     * Creates a new budget for the authenticated user.
     *
     * @param dto            the validated budget data
     * @param authentication the current security context
     * @return 201 Created with the persisted budget DTO
     */
    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(
            @Valid @RequestBody BudgetDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        BudgetDTO created = budgetService.createBudget(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing budget.
     *
     * @param id             the budget ID to update
     * @param dto            the validated updated budget data
     * @param authentication the current security context
     * @return 200 OK with the updated budget DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(budgetService.updateBudget(id, dto, userId));
    }

    /**
     * Deletes a budget by its ID.
     *
     * @param id             the budget ID to delete
     * @param authentication the current security context
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.noContent().build();
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

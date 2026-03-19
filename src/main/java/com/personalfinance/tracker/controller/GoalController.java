package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.GoalDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.GoalService;
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
 * REST controller for financial goal CRUD operations.
 *
 * <p>All endpoints require authentication. Goal responses include a calculated
 * {@code progressPercentage} field showing the ratio of current savings to the
 * target amount.</p>
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param goalService    service for goal business logic
     * @param userRepository repository for resolving the authenticated user ID
     */
    public GoalController(GoalService goalService,
                          UserRepository userRepository) {
        this.goalService = goalService;
        this.userRepository = userRepository;
    }

    /**
     * Lists all financial goals for the authenticated user.
     *
     * @param authentication the current security context
     * @return 200 OK with a list of goal DTOs including progress percentages
     */
    @GetMapping
    public ResponseEntity<List<GoalDTO>> getAllGoals(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(goalService.getAllGoals(userId));
    }

    /**
     * Retrieves a single financial goal by its ID.
     *
     * @param id             the goal ID
     * @param authentication the current security context
     * @return 200 OK with the goal DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalDTO> getGoalById(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(goalService.getGoalById(id, userId));
    }

    /**
     * Creates a new financial goal for the authenticated user.
     *
     * @param dto            the validated goal data
     * @param authentication the current security context
     * @return 201 Created with the persisted goal DTO
     */
    @PostMapping
    public ResponseEntity<GoalDTO> createGoal(
            @Valid @RequestBody GoalDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        GoalDTO created = goalService.createGoal(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing financial goal.
     *
     * <p>If the updated current amount reaches or exceeds the target amount,
     * the goal status is automatically set to COMPLETED.</p>
     *
     * @param id             the goal ID to update
     * @param dto            the validated updated goal data
     * @param authentication the current security context
     * @return 200 OK with the updated goal DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalDTO> updateGoal(
            @PathVariable("id") Long id,
            @Valid @RequestBody GoalDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(goalService.updateGoal(id, dto, userId));
    }

    /**
     * Deletes a financial goal by its ID.
     *
     * @param id             the goal ID to delete
     * @param authentication the current security context
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        goalService.deleteGoal(id, userId);
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

package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.GoalDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.model.FinancialGoal;
import com.personalfinance.tracker.model.GoalStatus;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.FinancialGoalRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service layer for managing financial goals.
 *
 * <p>Provides full CRUD operations on {@link FinancialGoal} entities, scoped to
 * the authenticated user. Each goal DTO returned to the client includes a
 * calculated {@code progressPercentage} representing how close the user is
 * to reaching their target amount.</p>
 *
 * <p>The update operation includes automatic status transition logic: when the
 * {@code currentAmount} reaches or exceeds the {@code targetAmount}, the goal
 * status is automatically set to {@link GoalStatus#COMPLETED}.</p>
 */
@Service
@Transactional
public class GoalService {

    private final FinancialGoalRepository goalRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param goalRepository repository for financial goal persistence
     * @param userRepository repository for user lookups
     */
    public GoalService(FinancialGoalRepository goalRepository,
                       UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all financial goals belonging to the authenticated user.
     *
     * @param userId the authenticated user ID
     * @return list of goal DTOs with calculated progress percentages
     */
    @Transactional(readOnly = true)
    public List<GoalDTO> getAllGoals(Long userId) {
        return goalRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Retrieves a single financial goal by its ID, verifying ownership.
     *
     * @param id     the goal ID
     * @param userId the authenticated user ID
     * @return the goal DTO with calculated progress percentage
     * @throws ResourceNotFoundException if the goal does not exist or belongs to another user
     */
    @Transactional(readOnly = true)
    public GoalDTO getGoalById(Long id, Long userId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        return mapToDTO(goal);
    }

    /**
     * Creates a new financial goal for the authenticated user.
     *
     * <p>The goal is initialized with {@link GoalStatus#ACTIVE} status and
     * a current amount of zero if not provided.</p>
     *
     * @param dto    the goal data from the request body
     * @param userId the authenticated user ID
     * @return the persisted goal as a DTO
     * @throws ResourceNotFoundException if the user does not exist
     */
    public GoalDTO createGoal(GoalDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FinancialGoal goal = FinancialGoal.builder()
                .name(dto.getName())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO)
                .deadline(dto.getDeadline())
                .status(dto.getStatus() != null ? dto.getStatus() : GoalStatus.ACTIVE)
                .user(user)
                .build();

        FinancialGoal saved = goalRepository.save(goal);
        return mapToDTO(saved);
    }

    /**
     * Updates an existing financial goal, verifying ownership.
     *
     * <p>Includes automatic status transition: if the updated {@code currentAmount}
     * is greater than or equal to the {@code targetAmount}, the status is
     * automatically set to {@link GoalStatus#COMPLETED}.</p>
     *
     * @param id     the goal ID to update
     * @param dto    the updated goal data
     * @param userId the authenticated user ID
     * @return the updated goal as a DTO
     * @throws ResourceNotFoundException if the goal does not exist
     */
    public GoalDTO updateGoal(Long id, GoalDTO dto, Long userId) {
        FinancialGoal existing = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));

        existing.setName(dto.getName());
        existing.setTargetAmount(dto.getTargetAmount());
        existing.setCurrentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : existing.getCurrentAmount());
        existing.setDeadline(dto.getDeadline());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());

        // Auto-complete goal when currentAmount reaches or exceeds targetAmount
        if (existing.getCurrentAmount().compareTo(existing.getTargetAmount()) >= 0) {
            existing.setStatus(GoalStatus.COMPLETED);
        }

        FinancialGoal updated = goalRepository.save(existing);
        return mapToDTO(updated);
    }

    /**
     * Deletes a financial goal by its ID, verifying ownership.
     *
     * @param id     the goal ID to delete
     * @param userId the authenticated user ID
     * @throws ResourceNotFoundException if the goal does not exist or belongs to another user
     */
    public void deleteGoal(Long id, Long userId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        goalRepository.delete(goal);
    }

    /**
     * Maps a {@link FinancialGoal} entity to a {@link GoalDTO}, including the
     * calculated progress percentage.
     *
     * <p>Progress is calculated as {@code (currentAmount / targetAmount) * 100},
     * capped at 100% to handle overfunding scenarios. If the target amount is
     * zero or null, progress defaults to 0%.</p>
     *
     * @param goal the entity to map
     * @return the corresponding DTO with progress percentage
     */
    private GoalDTO mapToDTO(FinancialGoal goal) {
        double progressPercentage = 0.0;

        if (goal.getTargetAmount() != null
                && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                && goal.getCurrentAmount() != null) {
            progressPercentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            // Cap at 100% to avoid misleading progress indicators
            progressPercentage = Math.min(progressPercentage, 100.0);
        }

        return GoalDTO.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .deadline(goal.getDeadline())
                .status(goal.getStatus())
                .progressPercentage(progressPercentage)
                .createdAt(goal.getCreatedAt())
                .build();
    }
}

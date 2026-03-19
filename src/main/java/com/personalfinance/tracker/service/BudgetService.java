package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.BudgetDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.model.Budget;
import com.personalfinance.tracker.model.BudgetPeriod;
import com.personalfinance.tracker.model.Category;
import com.personalfinance.tracker.model.TransactionType;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.BudgetRepository;
import com.personalfinance.tracker.repository.CategoryRepository;
import com.personalfinance.tracker.repository.TransactionRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for managing spending budgets.
 *
 * <p>Provides full CRUD operations on {@link Budget} entities, scoped to the
 * authenticated user. Each budget DTO returned to the client includes a
 * calculated {@code spent} field representing how much of the budget has been
 * consumed by matching expense transactions within the budget's date range.</p>
 *
 * <p>The spent amount is computed by querying the {@link TransactionRepository}
 * for the sum of EXPENSE transactions that fall within the budget's category
 * and date window (startDate to endDate inclusive).</p>
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param budgetRepository      repository for budget persistence
     * @param categoryRepository    repository for category lookups
     * @param transactionRepository repository for calculating spent amounts
     * @param userRepository        repository for user lookups
     */
    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all budgets belonging to the user, each enriched with its
     * calculated spent amount.
     *
     * @param userId the authenticated user's ID
     * @return list of budget DTOs with spent amounts
     */
    @Transactional(readOnly = true)
    public List<BudgetDTO> getAllBudgets(Long userId) {
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(budget -> mapToDTO(budget, userId))
                .toList();
    }

    /**
     * Retrieves a single budget by its ID, verifying ownership.
     *
     * @param id     the budget ID
     * @param userId the authenticated user's ID
     * @return the budget DTO with calculated spent amount
     * @throws ResourceNotFoundException if the budget does not exist or belongs to another user
     */
    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return mapToDTO(budget, userId);
    }

    /**
     * Creates a new budget for the authenticated user.
     *
     * @param dto    the budget data from the request body
     * @param userId the authenticated user's ID
     * @return the persisted budget as a DTO
     * @throws ResourceNotFoundException if the referenced category does not exist
     */
    public BudgetDTO createBudget(BudgetDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        LocalDate endDate = dto.getEndDate() != null
                ? dto.getEndDate()
                : resolveEndDate(dto.getStartDate(), dto.getPeriod());

        Budget budget = Budget.builder()
                .amountLimit(dto.getAmountLimit())
                .period(dto.getPeriod())
                .startDate(dto.getStartDate())
                .endDate(endDate)
                .user(user)
                .category(category)
                .build();

        Budget saved = budgetRepository.save(budget);
        return mapToDTO(saved, userId);
    }

    /**
     * Updates an existing budget, verifying ownership.
     *
     * @param id     the budget ID to update
     * @param dto    the updated budget data
     * @param userId the authenticated user's ID
     * @return the updated budget as a DTO
     * @throws ResourceNotFoundException if the budget or category does not exist
     */
    public BudgetDTO updateBudget(Long id, BudgetDTO dto, Long userId) {
        Budget existing = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        LocalDate endDate = dto.getEndDate() != null
                ? dto.getEndDate()
                : resolveEndDate(dto.getStartDate(), dto.getPeriod());

        existing.setAmountLimit(dto.getAmountLimit());
        existing.setPeriod(dto.getPeriod());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(endDate);
        existing.setCategory(category);

        Budget updated = budgetRepository.save(existing);
        return mapToDTO(updated, userId);
    }

    /**
     * Deletes a budget by its ID, verifying ownership.
     *
     * @param id     the budget ID to delete
     * @param userId the authenticated user's ID
     * @throws ResourceNotFoundException if the budget does not exist or belongs to another user
     */
    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(budget);
    }

    /**
     * Maps a {@link Budget} entity to a {@link BudgetDTO}, including the calculated
     * spent amount for the budget's category and date range.
     *
     * <p>The spent amount is the sum of all EXPENSE transactions belonging to the
     * same user, within the same category, and falling between the budget's
     * start and end dates (inclusive).</p>
     *
     * @param budget the entity to map
     * @param userId the authenticated user's ID (needed for the aggregation query)
     * @return the corresponding DTO with spent amount calculated
     */
    private BudgetDTO mapToDTO(Budget budget, Long userId) {
        BigDecimal spent = transactionRepository.sumAmountByUserAndCategoryAndTypeAndDateBetween(
                userId,
                budget.getCategory().getId(),
                TransactionType.EXPENSE,
                budget.getStartDate(),
                budget.getEndDate());

        return BudgetDTO.builder()
                .id(budget.getId())
                .amountLimit(budget.getAmountLimit())
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .spent(spent != null ? spent : BigDecimal.ZERO)
                .createdAt(budget.getCreatedAt())
                .build();
    }

    private LocalDate resolveEndDate(LocalDate startDate, BudgetPeriod period) {
        if (startDate == null || period == null) {
            return startDate;
        }
        return switch (period) {
            case WEEKLY -> startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case YEARLY -> startDate.plusYears(1).minusDays(1);
            default -> startDate.plusMonths(1).minusDays(1);
        };
    }
}

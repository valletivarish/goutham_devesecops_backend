package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.TransactionDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.model.Category;
import com.personalfinance.tracker.model.Transaction;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.CategoryRepository;
import com.personalfinance.tracker.repository.TransactionRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing financial transactions.
 *
 * <p>Provides full CRUD operations on {@link Transaction} entities, scoped to
 * the authenticated user. Every public method validates that the referenced
 * user and category exist before persisting changes, and maps between the
 * JPA entity and {@link TransactionDTO} to keep the API boundary clean.</p>
 *
 * <p>The class-level {@code @Transactional} annotation ensures that each
 * public method runs within a database transaction. Read-only methods benefit
 * from {@code readOnly = true} optimizations at the method level.</p>
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param transactionRepository repository for transaction persistence
     * @param categoryRepository    repository for category lookups
     * @param userRepository        repository for user lookups
     */
    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all transactions belonging to the given user, ordered by date descending.
     *
     * @param userId the authenticated user's ID
     * @return list of transaction DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Retrieves a single transaction by its ID, verifying ownership.
     *
     * @param id     the transaction ID
     * @param userId the authenticated user's ID
     * @return the transaction DTO
     * @throws ResourceNotFoundException if the transaction does not exist or belongs to another user
     */
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return mapToDTO(transaction);
    }

    /**
     * Creates a new transaction for the authenticated user.
     *
     * <p>Validates that the referenced category exists and belongs to the user
     * before persisting the transaction.</p>
     *
     * @param dto    the transaction data from the request body
     * @param userId the authenticated user's ID
     * @return the persisted transaction as a DTO (with generated ID and timestamps)
     * @throws ResourceNotFoundException if the referenced category does not exist
     */
    public TransactionDTO createTransaction(TransactionDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        Transaction transaction = Transaction.builder()
                .amount(dto.getAmount())
                .type(dto.getType())
                .description(dto.getDescription())
                .transactionDate(dto.getTransactionDate())
                .user(user)
                .category(category)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToDTO(saved);
    }

    /**
     * Updates an existing transaction, verifying ownership.
     *
     * <p>Finds the existing transaction, applies the updated field values from
     * the DTO, resolves the new category reference, and saves the changes.</p>
     *
     * @param id     the transaction ID to update
     * @param dto    the updated transaction data
     * @param userId the authenticated user's ID
     * @return the updated transaction as a DTO
     * @throws ResourceNotFoundException if the transaction or category does not exist
     */
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto, Long userId) {
        Transaction existing = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", dto.getCategoryId()));

        existing.setAmount(dto.getAmount());
        existing.setType(dto.getType());
        existing.setDescription(dto.getDescription());
        existing.setTransactionDate(dto.getTransactionDate());
        existing.setCategory(category);

        Transaction updated = transactionRepository.save(existing);
        return mapToDTO(updated);
    }

    /**
     * Deletes a transaction by its ID, verifying ownership.
     *
     * @param id     the transaction ID to delete
     * @param userId the authenticated user's ID
     * @throws ResourceNotFoundException if the transaction does not exist or belongs to another user
     */
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        transactionRepository.delete(transaction);
    }

    /**
     * Maps a {@link Transaction} entity to a {@link TransactionDTO}.
     *
     * <p>Resolves the category name from the associated category entity
     * and flattens the relationship into a simple categoryId + categoryName pair.</p>
     *
     * @param transaction the entity to map
     * @return the corresponding DTO
     */
    private TransactionDTO mapToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.TransactionDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.TransactionService;
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
 * REST controller for financial transaction CRUD operations.
 *
 * <p>All endpoints require authentication. The current user is resolved from
 * the JWT-backed {@link Authentication} principal, and all operations are
 * scoped to that user to enforce data isolation.</p>
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param transactionService service for transaction business logic
     * @param userRepository     repository for resolving the authenticated user ID
     */
    public TransactionController(TransactionService transactionService,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    /**
     * Lists all transactions for the authenticated user.
     *
     * @param authentication the current security context
     * @return 200 OK with a list of transaction DTOs
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.getAllTransactions(userId));
    }

    /**
     * Retrieves a single transaction by its ID.
     *
     * @param id             the transaction ID
     * @param authentication the current security context
     * @return 200 OK with the transaction DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.getTransactionById(id, userId));
    }

    /**
     * Creates a new transaction for the authenticated user.
     *
     * @param dto            the validated transaction data
     * @param authentication the current security context
     * @return 201 Created with the persisted transaction DTO
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @Valid @RequestBody TransactionDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        TransactionDTO created = transactionService.createTransaction(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing transaction.
     *
     * @param id             the transaction ID to update
     * @param dto            the validated updated transaction data
     * @param authentication the current security context
     * @return 200 OK with the updated transaction DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(transactionService.updateTransaction(id, dto, userId));
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id             the transaction ID to delete
     * @param authentication the current security context
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resolves the current authenticated user ID from the security context.
     *
     * <p>Extracts the username from the JWT-backed authentication principal
     * and looks up the corresponding User entity to obtain the database ID.</p>
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

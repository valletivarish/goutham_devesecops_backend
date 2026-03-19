package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.TransactionDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.model.Category;
import com.personalfinance.tracker.model.Transaction;
import com.personalfinance.tracker.model.TransactionType;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.CategoryRepository;
import com.personalfinance.tracker.repository.TransactionRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionService}.
 *
 * <p>All dependencies (repositories) are replaced with Mockito mocks so that
 * the service logic can be exercised in complete isolation — no Spring context
 * or database is required.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService unit tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    // ---------------------------------------------------------------
    // Shared test fixtures
    // ---------------------------------------------------------------

    private static final Long USER_ID      = 1L;
    private static final Long CATEGORY_ID  = 10L;
    private static final Long TRANSACTION_ID = 100L;

    private User testUser;
    private Category testCategory;
    private Transaction testTransaction;
    private TransactionDTO testTransactionDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .username("testuser")
                .email("testuser@example.com")
                .password("hashed-password")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(CATEGORY_ID)
                .name("Groceries")
                .type(TransactionType.EXPENSE)
                .icon("shopping-cart")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        testTransaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .amount(new BigDecimal("49.99"))
                .type(TransactionType.EXPENSE)
                .description("Weekly grocery run")
                .transactionDate(LocalDate.of(2026, 3, 15))
                .user(testUser)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTransactionDTO = TransactionDTO.builder()
                .amount(new BigDecimal("49.99"))
                .type(TransactionType.EXPENSE)
                .description("Weekly grocery run")
                .transactionDate(LocalDate.of(2026, 3, 15))
                .categoryId(CATEGORY_ID)
                .build();
    }

    // ---------------------------------------------------------------
    // getAllTransactions
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllTransactions - returns mapped DTOs for all user transactions")
    void getAllTransactions_returnsMappedDTOsForUser() {
        Transaction secondTransaction = Transaction.builder()
                .id(101L)
                .amount(new BigDecimal("2500.00"))
                .type(TransactionType.INCOME)
                .description("Monthly salary")
                .transactionDate(LocalDate.of(2026, 3, 1))
                .user(testUser)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdOrderByTransactionDateDesc(USER_ID))
                .thenReturn(List.of(testTransaction, secondTransaction));

        List<TransactionDTO> result = transactionService.getAllTransactions(USER_ID);

        assertThat(result).hasSize(2);

        TransactionDTO first = result.get(0);
        assertThat(first.getId()).isEqualTo(TRANSACTION_ID);
        assertThat(first.getAmount()).isEqualByComparingTo("49.99");
        assertThat(first.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(first.getDescription()).isEqualTo("Weekly grocery run");
        assertThat(first.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(first.getCategoryName()).isEqualTo("Groceries");

        TransactionDTO second = result.get(1);
        assertThat(second.getId()).isEqualTo(101L);
        assertThat(second.getAmount()).isEqualByComparingTo("2500.00");
        assertThat(second.getType()).isEqualTo(TransactionType.INCOME);

        verify(transactionRepository).findByUserIdOrderByTransactionDateDesc(USER_ID);
    }

    @Test
    @DisplayName("getAllTransactions - returns empty list when user has no transactions")
    void getAllTransactions_returnsEmptyListWhenNoTransactionsExist() {
        when(transactionRepository.findByUserIdOrderByTransactionDateDesc(USER_ID))
                .thenReturn(List.of());

        List<TransactionDTO> result = transactionService.getAllTransactions(USER_ID);

        assertThat(result).isEmpty();
        verify(transactionRepository).findByUserIdOrderByTransactionDateDesc(USER_ID);
    }

    // ---------------------------------------------------------------
    // getTransactionById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getTransactionById - returns DTO when transaction exists and belongs to user")
    void getTransactionById_returnsTransactionDTO() {
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(testTransaction));

        TransactionDTO result = transactionService.getTransactionById(TRANSACTION_ID, USER_ID);

        assertThat(result.getId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getAmount()).isEqualByComparingTo("49.99");
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getDescription()).isEqualTo("Weekly grocery run");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getCategoryName()).isEqualTo("Groceries");
        assertThat(result.getCreatedAt()).isNotNull();

        verify(transactionRepository).findByIdAndUserId(TRANSACTION_ID, USER_ID);
    }

    @Test
    @DisplayName("getTransactionById - throws ResourceNotFoundException when transaction not found")
    void getTransactionById_throwsWhenTransactionNotFound() {
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(TRANSACTION_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction");

        verify(transactionRepository).findByIdAndUserId(TRANSACTION_ID, USER_ID);
    }

    // ---------------------------------------------------------------
    // createTransaction
    // ---------------------------------------------------------------

    @Test
    @DisplayName("createTransaction - persists and returns DTO when user and category exist")
    void createTransaction_savesAndReturnsDTO() {
        Transaction savedTransaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .amount(testTransactionDTO.getAmount())
                .type(testTransactionDTO.getType())
                .description(testTransactionDTO.getDescription())
                .transactionDate(testTransactionDTO.getTransactionDate())
                .user(testUser)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionDTO result = transactionService.createTransaction(testTransactionDTO, USER_ID);

        assertThat(result.getId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getAmount()).isEqualByComparingTo("49.99");
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getDescription()).isEqualTo("Weekly grocery run");
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getCategoryName()).isEqualTo("Groceries");

        verify(userRepository).findById(USER_ID);
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction - throws ResourceNotFoundException when user does not exist")
    void createTransaction_throwsWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(testTransactionDTO, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(categoryRepository, never()).findByIdAndUserId(anyLong(), anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction - throws ResourceNotFoundException when category does not exist")
    void createTransaction_throwsWhenCategoryNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(testTransactionDTO, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ---------------------------------------------------------------
    // updateTransaction
    // ---------------------------------------------------------------

    @Test
    @DisplayName("updateTransaction - updates fields and returns DTO when transaction exists")
    void updateTransaction_savesAndReturnsUpdatedDTO() {
        TransactionDTO updateDTO = TransactionDTO.builder()
                .amount(new BigDecimal("75.50"))
                .type(TransactionType.EXPENSE)
                .description("Big grocery shop")
                .transactionDate(LocalDate.of(2026, 3, 18))
                .categoryId(CATEGORY_ID)
                .build();

        Transaction updatedTransaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .amount(updateDTO.getAmount())
                .type(updateDTO.getType())
                .description(updateDTO.getDescription())
                .transactionDate(updateDTO.getTransactionDate())
                .user(testUser)
                .category(testCategory)
                .createdAt(testTransaction.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(testTransaction));
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

        TransactionDTO result = transactionService.updateTransaction(TRANSACTION_ID, updateDTO, USER_ID);

        assertThat(result.getAmount()).isEqualByComparingTo("75.50");
        assertThat(result.getDescription()).isEqualTo("Big grocery shop");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDate.of(2026, 3, 18));

        verify(transactionRepository).findByIdAndUserId(TRANSACTION_ID, USER_ID);
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("updateTransaction - throws ResourceNotFoundException when transaction not found")
    void updateTransaction_throwsWhenTransactionNotFound() {
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(TRANSACTION_ID, testTransactionDTO, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ---------------------------------------------------------------
    // deleteTransaction
    // ---------------------------------------------------------------

    @Test
    @DisplayName("deleteTransaction - deletes transaction when it exists and belongs to user")
    void deleteTransaction_deletesSuccessfully() {
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(testTransaction));

        transactionService.deleteTransaction(TRANSACTION_ID, USER_ID);

        verify(transactionRepository).findByIdAndUserId(TRANSACTION_ID, USER_ID);
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    @DisplayName("deleteTransaction - throws ResourceNotFoundException when transaction not found")
    void deleteTransaction_throwsWhenTransactionNotFound() {
        when(transactionRepository.findByIdAndUserId(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(TRANSACTION_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction");

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    // ---------------------------------------------------------------
    // DTO mapping integrity
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllTransactions - maps null category to null categoryId and categoryName")
    void getAllTransactions_mapsNullCategoryCorrectly() {
        Transaction noCategoryTransaction = Transaction.builder()
                .id(102L)
                .amount(new BigDecimal("10.00"))
                .type(TransactionType.EXPENSE)
                .description("Cash purchase")
                .transactionDate(LocalDate.of(2026, 3, 10))
                .user(testUser)
                .category(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdOrderByTransactionDateDesc(USER_ID))
                .thenReturn(List.of(noCategoryTransaction));

        List<TransactionDTO> result = transactionService.getAllTransactions(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isNull();
        assertThat(result.get(0).getCategoryName()).isNull();
    }
}

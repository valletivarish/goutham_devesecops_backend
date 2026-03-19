package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.CategoryDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.exception.ValidationException;
import com.personalfinance.tracker.model.Category;
import com.personalfinance.tracker.model.TransactionType;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.CategoryRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Unit tests for {@link CategoryService}.
 *
 * <p>All dependencies (repositories) are replaced with Mockito mocks so that
 * the service logic can be exercised in complete isolation — no Spring context
 * or database is required.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService unit tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    // ---------------------------------------------------------------
    // Shared test fixtures
    // ---------------------------------------------------------------

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 10L;

    private User testUser;
    private Category testCategory;
    private CategoryDTO testCategoryDTO;

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

        testCategoryDTO = CategoryDTO.builder()
                .name("Groceries")
                .type(TransactionType.EXPENSE)
                .icon("shopping-cart")
                .build();
    }

    // ---------------------------------------------------------------
    // getAllCategories
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllCategories - returns mapped DTOs for all user categories")
    void getAllCategories_returnsAllCategoriesForUser() {
        Category secondCategory = Category.builder()
                .id(11L)
                .name("Salary")
                .type(TransactionType.INCOME)
                .icon("money")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryRepository.findByUserId(USER_ID))
                .thenReturn(List.of(testCategory, secondCategory));

        List<CategoryDTO> result = categoryService.getAllCategories(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Groceries");
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.get(0).getIcon()).isEqualTo("shopping-cart");
        assertThat(result.get(1).getName()).isEqualTo("Salary");
        assertThat(result.get(1).getType()).isEqualTo(TransactionType.INCOME);
        verify(categoryRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("getAllCategories - returns empty list when user has no categories")
    void getAllCategories_returnsEmptyListWhenNoCategoriesExist() {
        when(categoryRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<CategoryDTO> result = categoryService.getAllCategories(USER_ID);

        assertThat(result).isEmpty();
        verify(categoryRepository).findByUserId(USER_ID);
    }

    // ---------------------------------------------------------------
    // getCategoryById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getCategoryById - returns DTO when category exists and belongs to user")
    void getCategoryById_returnsCategoryDTO() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(testCategory));

        CategoryDTO result = categoryService.getCategoryById(CATEGORY_ID, USER_ID);

        assertThat(result.getId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getName()).isEqualTo("Groceries");
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getIcon()).isEqualTo("shopping-cart");
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
    }

    @Test
    @DisplayName("getCategoryById - throws ResourceNotFoundException when category not found")
    void getCategoryById_throwsWhenCategoryNotFound() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(CATEGORY_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
    }

    // ---------------------------------------------------------------
    // createCategory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("createCategory - persists and returns DTO when name is unique")
    void createCategory_savesAndReturnsDTO() {
        Category savedCategory = Category.builder()
                .id(CATEGORY_ID)
                .name(testCategoryDTO.getName())
                .type(testCategoryDTO.getType())
                .icon(testCategoryDTO.getIcon())
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryRepository.existsByNameAndUserId(testCategoryDTO.getName(), USER_ID))
                .thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryDTO result = categoryService.createCategory(testCategoryDTO, USER_ID);

        assertThat(result.getId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getName()).isEqualTo("Groceries");
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getIcon()).isEqualTo("shopping-cart");
        verify(categoryRepository).existsByNameAndUserId("Groceries", USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - throws ValidationException when category name already exists")
    void createCategory_throwsWhenDuplicateName() {
        when(categoryRepository.existsByNameAndUserId(testCategoryDTO.getName(), USER_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDTO, USER_ID))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Groceries");

        verify(categoryRepository).existsByNameAndUserId("Groceries", USER_ID);
        verify(userRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - throws ResourceNotFoundException when user does not exist")
    void createCategory_throwsWhenUserNotFound() {
        when(categoryRepository.existsByNameAndUserId(testCategoryDTO.getName(), USER_ID))
                .thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDTO, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ---------------------------------------------------------------
    // updateCategory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("updateCategory - updates and returns DTO when category exists")
    void updateCategory_savesAndReturnsUpdatedDTO() {
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Food & Dining")
                .type(TransactionType.EXPENSE)
                .icon("fork-knife")
                .build();

        Category updatedCategory = Category.builder()
                .id(CATEGORY_ID)
                .name("Food & Dining")
                .type(TransactionType.EXPENSE)
                .icon("fork-knife")
                .user(testUser)
                .createdAt(testCategory.getCreatedAt())
                .build();

        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryDTO result = categoryService.updateCategory(CATEGORY_ID, updateDTO, USER_ID);

        assertThat(result.getName()).isEqualTo("Food & Dining");
        assertThat(result.getIcon()).isEqualTo("fork-knife");
        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - throws ResourceNotFoundException when category not found")
    void updateCategory_throwsWhenCategoryNotFound() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, testCategoryDTO, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ---------------------------------------------------------------
    // deleteCategory
    // ---------------------------------------------------------------

    @Test
    @DisplayName("deleteCategory - deletes category when it exists and belongs to user")
    void deleteCategory_deletesSuccessfully() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(testCategory));

        categoryService.deleteCategory(CATEGORY_ID, USER_ID);

        verify(categoryRepository).findByIdAndUserId(CATEGORY_ID, USER_ID);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("deleteCategory - throws ResourceNotFoundException when category not found")
    void deleteCategory_throwsWhenCategoryNotFound() {
        when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID, USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ---------------------------------------------------------------
    // DTO mapping integrity
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getAllCategories - correctly maps icon field including null")
    void getAllCategories_mapsIconFieldCorrectly() {
        Category noIconCategory = Category.builder()
                .id(20L)
                .name("Miscellaneous")
                .type(TransactionType.EXPENSE)
                .icon(null)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryRepository.findByUserId(USER_ID)).thenReturn(List.of(noIconCategory));

        List<CategoryDTO> result = categoryService.getAllCategories(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIcon()).isNull();
        assertThat(result.get(0).getName()).isEqualTo("Miscellaneous");
    }
}

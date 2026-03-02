package com.personalfinance.tracker.service;

import com.personalfinance.tracker.dto.CategoryDTO;
import com.personalfinance.tracker.exception.ResourceNotFoundException;
import com.personalfinance.tracker.exception.ValidationException;
import com.personalfinance.tracker.model.Category;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.CategoryRepository;
import com.personalfinance.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing transaction categories.
 *
 * <p>Provides full CRUD operations on {@link Category} entities, scoped to the
 * authenticated user. Enforces a business rule that category names must be
 * unique within each user account to prevent ambiguity when classifying
 * transactions and budgets.</p>
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param categoryRepository repository for category persistence
     * @param userRepository     repository for user lookups
     */
    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all categories belonging to the authenticated user.
     *
     * @param userId the authenticated user ID
     * @return list of category DTOs
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories(Long userId) {
        return categoryRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Retrieves a single category by its ID, verifying ownership.
     *
     * @param id     the category ID
     * @param userId the authenticated user ID
     * @return the category DTO
     * @throws ResourceNotFoundException if the category does not exist or belongs to another user
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return mapToDTO(category);
    }

    /**
     * Creates a new category for the authenticated user.
     *
     * <p>Validates that no other category with the same name exists for the user
     * to prevent duplicate classifications.</p>
     *
     * @param dto    the category data from the request body
     * @param userId the authenticated user ID
     * @return the persisted category as a DTO
     * @throws ValidationException       if a category with the same name already exists
     * @throws ResourceNotFoundException if the user does not exist
     */
    public CategoryDTO createCategory(CategoryDTO dto, Long userId) {
        // Enforce unique category name per user
        if (categoryRepository.existsByNameAndUserId(dto.getName(), userId)) {
            throw new ValidationException(
                    "Category with name '" + dto.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = Category.builder()
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToDTO(saved);
    }

    /**
     * Updates an existing category, verifying ownership.
     *
     * @param id     the category ID to update
     * @param dto    the updated category data
     * @param userId the authenticated user ID
     * @return the updated category as a DTO
     * @throws ResourceNotFoundException if the category does not exist
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO dto, Long userId) {
        Category existing = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        existing.setName(dto.getName());
        existing.setType(dto.getType());
        existing.setIcon(dto.getIcon());

        Category updated = categoryRepository.save(existing);
        return mapToDTO(updated);
    }

    /**
     * Deletes a category by its ID, verifying ownership.
     *
     * @param id     the category ID to delete
     * @param userId the authenticated user ID
     * @throws ResourceNotFoundException if the category does not exist or belongs to another user
     */
    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        categoryRepository.delete(category);
    }

    /**
     * Maps a {@link Category} entity to a {@link CategoryDTO}.
     *
     * @param category the entity to map
     * @return the corresponding DTO
     */
    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .build();
    }
}

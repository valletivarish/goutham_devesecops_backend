package com.personalfinance.tracker.controller;

import com.personalfinance.tracker.dto.CategoryDTO;
import com.personalfinance.tracker.model.User;
import com.personalfinance.tracker.repository.UserRepository;
import com.personalfinance.tracker.service.CategoryService;
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
 * REST controller for category CRUD operations.
 *
 * <p>All endpoints require authentication. Categories are scoped per user
 * so that each user maintains their own set of transaction classifications.</p>
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its dependencies.
     *
     * @param categoryService service for category business logic
     * @param userRepository  repository for resolving the authenticated user ID
     */
    public CategoryController(CategoryService categoryService,
                              UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    /**
     * Lists all categories for the authenticated user.
     *
     * @param authentication the current security context
     * @return 200 OK with a list of category DTOs
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryService.getAllCategories(userId));
    }

    /**
     * Retrieves a single category by its ID.
     *
     * @param id             the category ID
     * @param authentication the current security context
     * @return 200 OK with the category DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryService.getCategoryById(id, userId));
    }

    /**
     * Creates a new category for the authenticated user.
     *
     * @param dto            the validated category data
     * @param authentication the current security context
     * @return 201 Created with the persisted category DTO
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        CategoryDTO created = categoryService.createCategory(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing category.
     *
     * @param id             the category ID to update
     * @param dto            the validated updated category data
     * @param authentication the current security context
     * @return 200 OK with the updated category DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryService.updateCategory(id, dto, userId));
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id             the category ID to delete
     * @param authentication the current security context
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        categoryService.deleteCategory(id, userId);
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

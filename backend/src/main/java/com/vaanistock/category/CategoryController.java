package com.vaanistock.category;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List all categories for the authenticated business")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CategoryDto> categories = categoryService.getCategories(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category details by ID")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        CategoryDto category = categoryService.getCategory(principal.getBusinessId(), id);
        return ResponseEntity.ok(ApiResponse.ok(category));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.createCategory(principal.getBusinessId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Category created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        CategoryDto updated = categoryService.updateCategory(principal.getBusinessId(), id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        categoryService.deleteCategory(principal.getBusinessId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully", null));
    }
}

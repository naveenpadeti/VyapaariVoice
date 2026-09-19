package com.vaanistock.product;

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
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog management endpoints")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List products with optional category and search filters")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        List<ProductResponse> products = productService.getProducts(principal.getBusinessId(), categoryId, search);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        ProductResponse product = productService.getProduct(principal.getBusinessId(), id);
        return ResponseEntity.ok(ApiResponse.ok(product));
    }

    @PostMapping
    @Operation(summary = "Create a new product with initial inventory")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(principal.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created successfully", product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product details")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse updated = productService.updateProduct(principal.getBusinessId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product and its inventory")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        productService.deleteProduct(principal.getBusinessId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
    }
}

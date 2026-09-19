package com.vaanistock.inventory;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import com.vaanistock.transaction.TransactionSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Stock management and movement endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "Get current stock levels for all products with optional filtering")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getInventory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        List<InventoryDto> items = inventoryService.getInventory(principal.getBusinessId(), categoryId, status);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get current stock level for a specific product")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventoryByProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        InventoryDto dto = inventoryService.getInventoryByProductId(principal.getBusinessId(), productId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "Add stock to inventory (+ Add Stock)")
    public ResponseEntity<ApiResponse<InventoryDto>> addStock(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StockAdjustmentRequest request) {
        InventoryDto updated = inventoryService.addStock(
                principal.getBusinessId(),
                request.getProductId(),
                request.getQuantity(),
                request.getUnit(),
                TransactionSource.MANUAL,
                request.getReason()
        );
        return ResponseEntity.ok(ApiResponse.ok("Stock added successfully", updated));
    }

    @PostMapping("/remove")
    @Operation(summary = "Remove stock from inventory (- Remove Stock / Sale)")
    public ResponseEntity<ApiResponse<InventoryDto>> removeStock(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StockAdjustmentRequest request) {
        InventoryDto updated = inventoryService.removeStock(
                principal.getBusinessId(),
                request.getProductId(),
                request.getQuantity(),
                request.getUnit(),
                TransactionSource.MANUAL,
                request.getReason(),
                request.isSale()
        );
        return ResponseEntity.ok(ApiResponse.ok("Stock removed successfully", updated));
    }
}

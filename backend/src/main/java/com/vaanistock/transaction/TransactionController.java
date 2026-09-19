package com.vaanistock.transaction;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Stock movement and audit transaction ledger")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get transaction history for the business")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "50") int limit) {
        List<TransactionDto> list = transactionService.getRecentTransactions(principal.getBusinessId(), productId, limit);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get transaction history for a specific product")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsByProduct(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "50") int limit) {
        List<TransactionDto> list = transactionService.getRecentTransactions(principal.getBusinessId(), productId, limit);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}

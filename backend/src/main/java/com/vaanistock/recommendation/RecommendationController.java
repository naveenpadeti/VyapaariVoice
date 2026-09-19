package com.vaanistock.recommendation;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Stock intelligence and deterministic reorder recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/reorder")
    @Operation(summary = "Get list of products recommended for reorder")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getReorderRecommendations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = recommendationService.getReorderRecommendations(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/reorder/{productId}")
    @Operation(summary = "Get reorder recommendation and intelligence for a specific product")
    public ResponseEntity<ApiResponse<StockIntelligenceDto>> getProductRecommendation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        StockIntelligenceDto dto = recommendationService.getStockIntelligence(principal.getBusinessId(), productId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/intelligence")
    @Operation(summary = "Get full stock intelligence for all products")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getAllIntelligence(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = recommendationService.getAllStockIntelligence(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/intelligence/{productId}")
    @Operation(summary = "Get stock intelligence for a specific product")
    public ResponseEntity<ApiResponse<StockIntelligenceDto>> getProductIntelligence(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        StockIntelligenceDto dto = recommendationService.getStockIntelligence(principal.getBusinessId(), productId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}

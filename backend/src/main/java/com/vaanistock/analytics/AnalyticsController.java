package com.vaanistock.analytics;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.recommendation.StockIntelligenceDto;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Stock intelligence and sales analytics endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overall business inventory and sales summary")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/sales-trend")
    @Operation(summary = "Get sales trend data points (range: today, 7days, 30days)")
    public ResponseEntity<ApiResponse<List<SalesTrendPoint>>> getSalesTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "7days") String range) {
        List<SalesTrendPoint> trend = analyticsService.getSalesTrend(principal.getBusinessId(), range);
        return ResponseEntity.ok(ApiResponse.ok(trend));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Get top selling products by quantity")
    public ResponseEntity<ApiResponse<List<TopSellingProductDto>>> getTopSelling(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {
        List<TopSellingProductDto> list = analyticsService.getTopSelling(principal.getBusinessId(), limit);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/fast-moving")
    @Operation(summary = "Get fast-moving products based on daily sales velocity")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getFastMoving(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = analyticsService.getFastMoving(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/slow-moving")
    @Operation(summary = "Get slow-moving products with low sales velocity")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getSlowMoving(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = analyticsService.getSlowMoving(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/no-recent-sales")
    @Operation(summary = "Get products with zero sales in the last 30 days")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getNoRecentSales(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = analyticsService.getNoRecentSales(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products approaching or below reorder level")
    public ResponseEntity<ApiResponse<List<StockIntelligenceDto>>> getLowStock(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<StockIntelligenceDto> list = analyticsService.getLowStock(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}

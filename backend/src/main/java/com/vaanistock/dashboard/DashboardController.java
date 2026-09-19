package com.vaanistock.dashboard;

import com.vaanistock.analytics.AnalyticsService;
import com.vaanistock.analytics.AnalyticsSummaryDto;
import com.vaanistock.analytics.SalesTrendPoint;
import com.vaanistock.analytics.TopSellingProductDto;
import com.vaanistock.common.ApiResponse;
import com.vaanistock.recommendation.StockIntelligenceDto;
import com.vaanistock.security.UserPrincipal;
import com.vaanistock.transaction.TransactionDto;
import com.vaanistock.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Unified dashboard overview endpoint")
public class DashboardController {

    private final AnalyticsService analyticsService;
    private final TransactionService transactionService;

    public DashboardController(AnalyticsService analyticsService, TransactionService transactionService) {
        this.analyticsService = analyticsService;
        this.transactionService = transactionService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get aggregated dashboard data for quick rendering")
    public ResponseEntity<ApiResponse<DashboardOverview>> getOverview(@AuthenticationPrincipal UserPrincipal principal) {
        Long bId = principal.getBusinessId();

        AnalyticsSummaryDto summary = analyticsService.getSummary(bId);
        List<SalesTrendPoint> trend = analyticsService.getSalesTrend(bId, "7days");
        List<TopSellingProductDto> topSelling = analyticsService.getTopSelling(bId, 5);
        List<StockIntelligenceDto> lowStock = analyticsService.getLowStock(bId);
        List<TransactionDto> recentTx = transactionService.getRecentTransactions(bId, null, 10);

        DashboardOverview overview = new DashboardOverview(summary, trend, topSelling, lowStock, recentTx);
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    public static class DashboardOverview {
        private AnalyticsSummaryDto summary;
        private List<SalesTrendPoint> salesTrend;
        private List<TopSellingProductDto> topSelling;
        private List<StockIntelligenceDto> lowStock;
        private List<TransactionDto> recentTransactions;

        public DashboardOverview() {}

        public DashboardOverview(AnalyticsSummaryDto summary,
                                 List<SalesTrendPoint> salesTrend,
                                 List<TopSellingProductDto> topSelling,
                                 List<StockIntelligenceDto> lowStock,
                                 List<TransactionDto> recentTransactions) {
            this.summary = summary;
            this.salesTrend = salesTrend;
            this.topSelling = topSelling;
            this.lowStock = lowStock;
            this.recentTransactions = recentTransactions;
        }

        public AnalyticsSummaryDto getSummary() {
            return summary;
        }

        public void setSummary(AnalyticsSummaryDto summary) {
            this.summary = summary;
        }

        public List<SalesTrendPoint> getSalesTrend() {
            return salesTrend;
        }

        public void setSalesTrend(List<SalesTrendPoint> salesTrend) {
            this.salesTrend = salesTrend;
        }

        public List<TopSellingProductDto> getTopSelling() {
            return topSelling;
        }

        public void setTopSelling(List<TopSellingProductDto> topSelling) {
            this.topSelling = topSelling;
        }

        public List<StockIntelligenceDto> getLowStock() {
            return lowStock;
        }

        public void setLowStock(List<StockIntelligenceDto> lowStock) {
            this.lowStock = lowStock;
        }

        public List<TransactionDto> getRecentTransactions() {
            return recentTransactions;
        }

        public void setRecentTransactions(List<TransactionDto> recentTransactions) {
            this.recentTransactions = recentTransactions;
        }
    }
}

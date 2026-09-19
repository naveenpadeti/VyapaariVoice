package com.vaanistock.analytics;

import com.vaanistock.category.CategoryRepository;
import com.vaanistock.inventory.Inventory;
import com.vaanistock.inventory.InventoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.recommendation.RecommendationService;
import com.vaanistock.recommendation.StockIntelligenceDto;
import com.vaanistock.transaction.Transaction;
import com.vaanistock.transaction.TransactionRepository;
import com.vaanistock.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final RecommendationService recommendationService;

    public AnalyticsService(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            InventoryRepository inventoryRepository,
                            TransactionRepository transactionRepository,
                            RecommendationService recommendationService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.recommendationService = recommendationService;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummary(Long businessId) {
        long totalProducts = productRepository.countByBusinessId(businessId);
        long totalCategories = categoryRepository.countByBusinessId(businessId);

        List<StockIntelligenceDto> allIntelligence = recommendationService.getAllStockIntelligence(businessId);

        BigDecimal totalStockUnits = BigDecimal.ZERO;
        long lowStockCount = 0;
        long outOfStockCount = 0;
        long fastMovingCount = 0;
        long slowMovingCount = 0;
        long noRecentSalesCount = 0;

        for (StockIntelligenceDto dto : allIntelligence) {
            totalStockUnits = totalStockUnits.add(dto.getCurrentStock());
            if ("OUT_OF_STOCK".equals(dto.getReorderStatus())) {
                outOfStockCount++;
            } else if ("LOW_STOCK".equals(dto.getReorderStatus())) {
                lowStockCount++;
            }

            if ("FAST_MOVING".equals(dto.getSalesVelocityCategory())) {
                fastMovingCount++;
            } else if ("SLOW_MOVING".equals(dto.getSalesVelocityCategory())) {
                slowMovingCount++;
            } else if ("NO_SALES".equals(dto.getSalesVelocityCategory())) {
                noRecentSalesCount++;
            }
        }

        Instant now = Instant.now();
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant sevenDaysAgo = now.minus(Duration.ofDays(7));
        Instant thirtyDaysAgo = now.minus(Duration.ofDays(30));

        BigDecimal salesToday = sumSalesInPeriod(businessId, startOfToday);
        BigDecimal sales7Days = sumSalesInPeriod(businessId, sevenDaysAgo);
        BigDecimal sales30Days = sumSalesInPeriod(businessId, thirtyDaysAgo);

        BigDecimal added30Days = sumQuantitiesByType(businessId, TransactionType.ADD, thirtyDaysAgo);
        BigDecimal removed30Days = sumQuantitiesByType(businessId, TransactionType.SALE, thirtyDaysAgo)
                .add(sumQuantitiesByType(businessId, TransactionType.REMOVE, thirtyDaysAgo));

        AnalyticsSummaryDto summary = new AnalyticsSummaryDto();
        summary.setTotalProducts(totalProducts);
        summary.setTotalCategories(totalCategories);
        summary.setTotalStockUnits(totalStockUnits);
        summary.setLowStockCount(lowStockCount);
        summary.setOutOfStockCount(outOfStockCount);
        summary.setFastMovingCount(fastMovingCount);
        summary.setSlowMovingCount(slowMovingCount);
        summary.setNoRecentSalesCount(noRecentSalesCount);
        summary.setSalesToday(salesToday);
        summary.setSalesLast7Days(sales7Days);
        summary.setSalesLast30Days(sales30Days);
        summary.setStockAddedLast30Days(added30Days);
        summary.setStockRemovedLast30Days(removed30Days);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<SalesTrendPoint> getSalesTrend(Long businessId, String range) {
        int days = "today".equalsIgnoreCase(range) ? 1 :
                   "30days".equalsIgnoreCase(range) ? 30 : 7;

        Instant since = Instant.now().minus(Duration.ofDays(days));
        List<Transaction> sales = transactionRepository.findByBusinessIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                businessId, TransactionType.SALE, since);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern(days <= 7 ? "EEE (dd)" : "dd MMM");

        Map<String, BigDecimal> dayQuantities = new LinkedHashMap<>();
        Map<String, Long> dayCounts = new LinkedHashMap<>();
        Map<String, String> dayLabels = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = date.format(formatter);
            String label = date.format(labelFormatter);
            dayQuantities.put(dateKey, BigDecimal.ZERO);
            dayCounts.put(dateKey, 0L);
            dayLabels.put(dateKey, label);
        }

        for (Transaction tx : sales) {
            LocalDate txDate = tx.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            String key = txDate.format(formatter);
            if (dayQuantities.containsKey(key)) {
                dayQuantities.put(key, dayQuantities.get(key).add(tx.getQuantity()));
                dayCounts.put(key, dayCounts.get(key) + 1);
            }
        }

        List<SalesTrendPoint> points = new ArrayList<>();
        for (String key : dayQuantities.keySet()) {
            points.add(new SalesTrendPoint(dayLabels.get(key), key, dayQuantities.get(key), dayCounts.get(key)));
        }

        return points;
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductDto> getTopSelling(Long businessId, int limit) {
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        List<Object[]> results = transactionRepository.sumSalesByProductSince(businessId, thirtyDaysAgo);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<TopSellingProductDto> list = new ArrayList<>();
        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;
            Long productId = (Long) row[0];
            BigDecimal totalSold = (BigDecimal) row[1];

            productRepository.findById(productId).ifPresent(product -> {
                BigDecimal currentStock = inventoryRepository.findByProductId(productId)
                        .map(Inventory::getCurrentQuantity)
                        .orElse(BigDecimal.ZERO);
                BigDecimal velocity = totalSold.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

                TopSellingProductDto dto = new TopSellingProductDto();
                dto.setProductId(product.getId());
                dto.setProductName(product.getName());
                dto.setCategoryName(categoryRepository.findById(product.getCategoryId()).map(c -> c.getName()).orElse(""));
                dto.setUnit(product.getUnit());
                dto.setTotalSold(totalSold);
                dto.setCurrentStock(currentStock);
                dto.setDailyVelocity(velocity);
                list.add(dto);
            });
            count++;
        }

        return list;
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getFastMoving(Long businessId) {
        return recommendationService.getAllStockIntelligence(businessId).stream()
                .filter(dto -> "FAST_MOVING".equals(dto.getSalesVelocityCategory()))
                .sorted(Comparator.comparing(StockIntelligenceDto::getAverageDailySales).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getSlowMoving(Long businessId) {
        return recommendationService.getAllStockIntelligence(businessId).stream()
                .filter(dto -> "SLOW_MOVING".equals(dto.getSalesVelocityCategory()))
                .sorted(Comparator.comparing(StockIntelligenceDto::getAverageDailySales))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getNoRecentSales(Long businessId) {
        return recommendationService.getAllStockIntelligence(businessId).stream()
                .filter(dto -> "NO_SALES".equals(dto.getSalesVelocityCategory()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getLowStock(Long businessId) {
        return recommendationService.getAllStockIntelligence(businessId).stream()
                .filter(dto -> "LOW_STOCK".equals(dto.getReorderStatus()) || "OUT_OF_STOCK".equals(dto.getReorderStatus()))
                .sorted(Comparator.comparing(StockIntelligenceDto::getCurrentStock))
                .toList();
    }

    private BigDecimal sumSalesInPeriod(Long businessId, Instant since) {
        List<Transaction> sales = transactionRepository.findByBusinessIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                businessId, TransactionType.SALE, since);
        return sales.stream()
                .map(Transaction::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumQuantitiesByType(Long businessId, TransactionType type, Instant since) {
        List<Transaction> list = transactionRepository.findByBusinessIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                businessId, type, since);
        return list.stream()
                .map(Transaction::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

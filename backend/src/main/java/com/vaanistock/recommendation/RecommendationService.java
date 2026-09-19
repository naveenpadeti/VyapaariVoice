package com.vaanistock.recommendation;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.common.ResourceNotFoundException;
import com.vaanistock.inventory.Inventory;
import com.vaanistock.inventory.InventoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.product.ProductService;
import com.vaanistock.transaction.Transaction;
import com.vaanistock.transaction.TransactionRepository;
import com.vaanistock.transaction.TransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final int defaultCoverageDays;

    public RecommendationService(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 InventoryRepository inventoryRepository,
                                 TransactionRepository transactionRepository,
                                 @Value("${vaanistock.inventory.default-coverage-days:15}") int defaultCoverageDays) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.defaultCoverageDays = defaultCoverageDays;
    }

    @Transactional(readOnly = true)
    public StockIntelligenceDto getStockIntelligence(Long businessId, Long productId) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getBusinessId().equals(businessId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Uncategorized");

        BigDecimal currentStock = inventoryRepository.findByProductId(productId)
                .map(Inventory::getCurrentQuantity)
                .orElse(BigDecimal.ZERO);

        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        BigDecimal totalSales30Days = transactionRepository.sumSalesSince(businessId, productId, thirtyDaysAgo);

        Optional<Transaction> lastSale = transactionRepository.findFirstByBusinessIdAndProductIdAndTypeOrderByCreatedAtDesc(
                businessId, productId, TransactionType.SALE);

        return calculateIntelligence(product, categoryName, currentStock, totalSales30Days, lastSale.map(Transaction::getCreatedAt).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getAllStockIntelligence(Long businessId) {
        List<Product> products = productRepository.findByBusinessIdOrderByNameAsc(businessId);
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryMap = categoryRepository.findByBusinessIdOrderByNameAsc(businessId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, BigDecimal> stockMap = inventoryRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Inventory::getProductId, Inventory::getCurrentQuantity));

        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));

        // Group 30-day sales
        Map<Long, BigDecimal> salesMap = new HashMap<>();
        List<Object[]> salesResults = transactionRepository.sumSalesByProductSince(businessId, thirtyDaysAgo);
        for (Object[] row : salesResults) {
            Long pid = (Long) row[0];
            BigDecimal qty = (BigDecimal) row[1];
            salesMap.put(pid, qty);
        }

        return products.stream().map(product -> {
            String catName = categoryMap.getOrDefault(product.getCategoryId(), "Uncategorized");
            BigDecimal stock = stockMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            BigDecimal sales = salesMap.getOrDefault(product.getId(), BigDecimal.ZERO);
            return calculateIntelligence(product, catName, stock, sales, null);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<StockIntelligenceDto> getReorderRecommendations(Long businessId) {
        return getAllStockIntelligence(businessId).stream()
                .filter(dto -> dto.getRecommendedRefillQuantity().compareTo(BigDecimal.ZERO) > 0 ||
                               "LOW_STOCK".equals(dto.getReorderStatus()) ||
                               "OUT_OF_STOCK".equals(dto.getReorderStatus()))
                .sorted(Comparator.comparing(StockIntelligenceDto::getRecommendedRefillQuantity).reversed())
                .toList();
    }

    private StockIntelligenceDto calculateIntelligence(Product product, String categoryName,
                                                       BigDecimal currentStock, BigDecimal totalSales30Days,
                                                       Instant lastSaleDate) {
        StockIntelligenceDto dto = new StockIntelligenceDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setUnit(product.getUnit());
        dto.setCurrentStock(currentStock);
        dto.setReorderLevel(product.getReorderLevel());
        dto.setLastSaleDate(lastSaleDate);

        // 1. Average Daily Sales = total sales in 30 days / 30
        BigDecimal avgDailySales = BigDecimal.ZERO;
        if (totalSales30Days != null && totalSales30Days.compareTo(BigDecimal.ZERO) > 0) {
            avgDailySales = totalSales30Days.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        }
        dto.setAverageDailySales(avgDailySales);

        // 2. Stock Coverage Days = currentStock / avgDailySales
        BigDecimal coverageDays = null;
        if (avgDailySales.compareTo(BigDecimal.ZERO) > 0) {
            coverageDays = currentStock.divide(avgDailySales, 1, RoundingMode.HALF_UP);
        }
        dto.setStockCoverageDays(coverageDays);

        // 3. Sales Velocity Category
        if (avgDailySales.compareTo(BigDecimal.ZERO) == 0) {
            dto.setSalesVelocityCategory("NO_SALES");
        } else if (avgDailySales.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
            dto.setSalesVelocityCategory("FAST_MOVING");
        } else if (avgDailySales.compareTo(BigDecimal.valueOf(0.5)) >= 0) {
            dto.setSalesVelocityCategory("NORMAL");
        } else {
            dto.setSalesVelocityCategory("SLOW_MOVING");
        }

        // 4. Target Stock & Recommended Refill
        BigDecimal targetStock = product.getTargetStock();
        if (targetStock == null || targetStock.compareTo(BigDecimal.ZERO) <= 0) {
            if (avgDailySales.compareTo(BigDecimal.ZERO) > 0) {
                targetStock = avgDailySales.multiply(BigDecimal.valueOf(defaultCoverageDays))
                        .setScale(0, RoundingMode.CEILING);
            } else if (product.getReorderLevel() != null && product.getReorderLevel().compareTo(BigDecimal.ZERO) > 0) {
                targetStock = product.getReorderLevel().multiply(BigDecimal.valueOf(2))
                        .setScale(0, RoundingMode.CEILING);
            } else {
                targetStock = BigDecimal.valueOf(20);
            }
        }
        dto.setTargetStock(targetStock);

        BigDecimal recommendedRefill = BigDecimal.ZERO;
        if (currentStock.compareTo(targetStock) < 0) {
            recommendedRefill = targetStock.subtract(currentStock).setScale(0, RoundingMode.CEILING);
        }
        dto.setRecommendedRefillQuantity(recommendedRefill);

        // 5. Reorder Status
        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setReorderStatus("OUT_OF_STOCK");
        } else if (product.getReorderLevel() != null && currentStock.compareTo(product.getReorderLevel()) <= 0) {
            dto.setReorderStatus("LOW_STOCK");
        } else if (recommendedRefill.compareTo(BigDecimal.ZERO) > 0 && coverageDays != null && coverageDays.compareTo(BigDecimal.valueOf(defaultCoverageDays)) < 0) {
            dto.setReorderStatus("REFILL_RECOMMENDED");
        } else {
            dto.setReorderStatus("IN_STOCK");
        }

        // 6. Rationale / Explanation
        dto.setReason(buildReasonText(product.getName(), currentStock, product.getUnit(), avgDailySales, coverageDays, recommendedRefill, defaultCoverageDays));

        return dto;
    }

    private String buildReasonText(String productName, BigDecimal currentStock, String unit,
                                   BigDecimal avgDailySales, BigDecimal coverageDays,
                                   BigDecimal recommendedRefill, int coverageTarget) {
        String stockStr = currentStock.stripTrailingZeros().toPlainString() + " " + unit;
        if (avgDailySales.compareTo(BigDecimal.ZERO) <= 0) {
            if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
                return productName + " is out of stock. Consider restocking to meet potential demand.";
            }
            return productName + " currently has " + stockStr + ". Not enough recent sales data to provide a reliable refill estimate.";
        }

        String salesStr = avgDailySales.stripTrailingZeros().toPlainString() + " " + unit + "/day";
        String covStr = coverageDays != null ? coverageDays.stripTrailingZeros().toPlainString() + " days" : "unknown";

        if (recommendedRefill.compareTo(BigDecimal.ZERO) > 0) {
            String refillStr = recommendedRefill.stripTrailingZeros().toPlainString() + " " + unit;
            return productName + " currently has " + stockStr + ". Based on recent sales of ~" + salesStr +
                    ", current stock may last ~" + covStr + ". Refill ~" + refillStr +
                    " to maintain around " + coverageTarget + " days of stock.";
        } else {
            return productName + " currently has " + stockStr + " (coverage ~" + covStr + "). Stock is currently sufficient for target coverage.";
        }
    }
}

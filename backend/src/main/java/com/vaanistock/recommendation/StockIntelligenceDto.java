package com.vaanistock.recommendation;

import java.math.BigDecimal;
import java.time.Instant;

public class StockIntelligenceDto {
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal averageDailySales;
    private BigDecimal stockCoverageDays; // null if zero sales
    private BigDecimal reorderLevel;
    private BigDecimal targetStock;
    private BigDecimal recommendedRefillQuantity;
    private String reorderStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK, REFILL_RECOMMENDED
    private String salesVelocityCategory; // FAST_MOVING, NORMAL, SLOW_MOVING, NO_SALES
    private Instant lastSaleDate;
    private String reason;

    public StockIntelligenceDto() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getAverageDailySales() {
        return averageDailySales;
    }

    public void setAverageDailySales(BigDecimal averageDailySales) {
        this.averageDailySales = averageDailySales;
    }

    public BigDecimal getStockCoverageDays() {
        return stockCoverageDays;
    }

    public void setStockCoverageDays(BigDecimal stockCoverageDays) {
        this.stockCoverageDays = stockCoverageDays;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getTargetStock() {
        return targetStock;
    }

    public void setTargetStock(BigDecimal targetStock) {
        this.targetStock = targetStock;
    }

    public BigDecimal getRecommendedRefillQuantity() {
        return recommendedRefillQuantity;
    }

    public void setRecommendedRefillQuantity(BigDecimal recommendedRefillQuantity) {
        this.recommendedRefillQuantity = recommendedRefillQuantity;
    }

    public String getReorderStatus() {
        return reorderStatus;
    }

    public void setReorderStatus(String reorderStatus) {
        this.reorderStatus = reorderStatus;
    }

    public String getSalesVelocityCategory() {
        return salesVelocityCategory;
    }

    public void setSalesVelocityCategory(String salesVelocityCategory) {
        this.salesVelocityCategory = salesVelocityCategory;
    }

    public Instant getLastSaleDate() {
        return lastSaleDate;
    }

    public void setLastSaleDate(Instant lastSaleDate) {
        this.lastSaleDate = lastSaleDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

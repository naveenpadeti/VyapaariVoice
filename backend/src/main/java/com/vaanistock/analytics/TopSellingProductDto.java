package com.vaanistock.analytics;

import java.math.BigDecimal;

public class TopSellingProductDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private String unit;
    private BigDecimal totalSold;
    private BigDecimal currentStock;
    private BigDecimal dailyVelocity;

    public TopSellingProductDto() {}

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

    public BigDecimal getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(BigDecimal totalSold) {
        this.totalSold = totalSold;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getDailyVelocity() {
        return dailyVelocity;
    }

    public void setDailyVelocity(BigDecimal dailyVelocity) {
        this.dailyVelocity = dailyVelocity;
    }
}

package com.vaanistock.analytics;

import java.math.BigDecimal;

public class AnalyticsSummaryDto {
    private long totalProducts;
    private long totalCategories;
    private BigDecimal totalStockUnits;
    private long lowStockCount;
    private long outOfStockCount;
    private long fastMovingCount;
    private long slowMovingCount;
    private long noRecentSalesCount;
    private BigDecimal salesToday;
    private BigDecimal salesLast7Days;
    private BigDecimal salesLast30Days;
    private BigDecimal stockAddedLast30Days;
    private BigDecimal stockRemovedLast30Days;

    public AnalyticsSummaryDto() {}

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public BigDecimal getTotalStockUnits() {
        return totalStockUnits;
    }

    public void setTotalStockUnits(BigDecimal totalStockUnits) {
        this.totalStockUnits = totalStockUnits;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(long outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public long getFastMovingCount() {
        return fastMovingCount;
    }

    public void setFastMovingCount(long fastMovingCount) {
        this.fastMovingCount = fastMovingCount;
    }

    public long getSlowMovingCount() {
        return slowMovingCount;
    }

    public void setSlowMovingCount(long slowMovingCount) {
        this.slowMovingCount = slowMovingCount;
    }

    public long getNoRecentSalesCount() {
        return noRecentSalesCount;
    }

    public void setNoRecentSalesCount(long noRecentSalesCount) {
        this.noRecentSalesCount = noRecentSalesCount;
    }

    public BigDecimal getSalesToday() {
        return salesToday;
    }

    public void setSalesToday(BigDecimal salesToday) {
        this.salesToday = salesToday;
    }

    public BigDecimal getSalesLast7Days() {
        return salesLast7Days;
    }

    public void setSalesLast7Days(BigDecimal salesLast7Days) {
        this.salesLast7Days = salesLast7Days;
    }

    public BigDecimal getSalesLast30Days() {
        return salesLast30Days;
    }

    public void setSalesLast30Days(BigDecimal salesLast30Days) {
        this.salesLast30Days = salesLast30Days;
    }

    public BigDecimal getStockAddedLast30Days() {
        return stockAddedLast30Days;
    }

    public void setStockAddedLast30Days(BigDecimal stockAddedLast30Days) {
        this.stockAddedLast30Days = stockAddedLast30Days;
    }

    public BigDecimal getStockRemovedLast30Days() {
        return stockRemovedLast30Days;
    }

    public void setStockRemovedLast30Days(BigDecimal stockRemovedLast30Days) {
        this.stockRemovedLast30Days = stockRemovedLast30Days;
    }
}

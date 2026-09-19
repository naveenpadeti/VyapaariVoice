package com.vaanistock.alert;

import java.math.BigDecimal;

public class LowStockAlertDto {
    private Long productId;
    private String productName;
    private String categoryName;
    private BigDecimal currentStock;
    private String unit;
    private BigDecimal reorderLevel;
    private BigDecimal targetStock;
    private BigDecimal suggestedRefill;
    private String severity; // CRITICAL, HIGH, MEDIUM
    private BigDecimal coverageDays;
    private String reason;
    private String status; // ACTIVE, DISMISSED, READ

    public LowStockAlertDto() {}

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

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public BigDecimal getSuggestedRefill() {
        return suggestedRefill;
    }

    public void setSuggestedRefill(BigDecimal suggestedRefill) {
        this.suggestedRefill = suggestedRefill;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public BigDecimal getCoverageDays() {
        return coverageDays;
    }

    public void setCoverageDays(BigDecimal coverageDays) {
        this.coverageDays = coverageDays;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

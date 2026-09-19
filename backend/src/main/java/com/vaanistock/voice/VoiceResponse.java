package com.vaanistock.voice;

import java.math.BigDecimal;
import java.util.Map;

public class VoiceResponse {
    private VoiceIntent intent;
    private String product;
    private BigDecimal currentStock;
    private String unit;
    private BigDecimal averageDailySales;
    private BigDecimal stockCoverageDays;
    private BigDecimal recommendedRefill;
    private String response;
    private String language;
    private String status; // "SUCCESS", "CONFIRMATION_REQUIRED", "ERROR"
    private Map<String, Object> details;

    public VoiceResponse() {}

    public VoiceIntent getIntent() {
        return intent;
    }

    public void setIntent(VoiceIntent intent) {
        this.intent = intent;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
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

    public BigDecimal getRecommendedRefill() {
        return recommendedRefill;
    }

    public void setRecommendedRefill(BigDecimal recommendedRefill) {
        this.recommendedRefill = recommendedRefill;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}

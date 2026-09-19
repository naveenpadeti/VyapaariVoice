package com.vaanistock.common;

import java.math.BigDecimal;

public class InsufficientStockException extends BusinessException {
    private final BigDecimal availableQuantity;
    private final BigDecimal requestedQuantity;
    private final String unit;

    public InsufficientStockException(String productName, BigDecimal availableQuantity, BigDecimal requestedQuantity, String unit) {
        super(String.format("Insufficient stock for %s. Available: %s %s, Requested: %s %s",
                productName, availableQuantity.stripTrailingZeros().toPlainString(), unit,
                requestedQuantity.stripTrailingZeros().toPlainString(), unit));
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
        this.unit = unit;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getUnit() {
        return unit;
    }
}

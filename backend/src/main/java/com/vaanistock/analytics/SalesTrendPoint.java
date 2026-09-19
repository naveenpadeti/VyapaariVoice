package com.vaanistock.analytics;

import java.math.BigDecimal;

public class SalesTrendPoint {
    private String label;
    private String date;
    private BigDecimal quantity;
    private long count;

    public SalesTrendPoint() {}

    public SalesTrendPoint(String label, String date, BigDecimal quantity, long count) {
        this.label = label;
        this.date = date;
        this.quantity = quantity;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

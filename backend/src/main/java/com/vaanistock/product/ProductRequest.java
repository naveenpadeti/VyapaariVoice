package com.vaanistock.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @NotBlank(message = "Unit is required (e.g., bag, kg, box, bottle)")
    @Size(max = 30)
    private String unit;

    @DecimalMin(value = "0.0", message = "Initial quantity cannot be negative")
    private BigDecimal initialQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Minimum stock cannot be negative")
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Target stock cannot be negative")
    private BigDecimal targetStock = BigDecimal.ZERO;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
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
}

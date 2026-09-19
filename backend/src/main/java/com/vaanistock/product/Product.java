package com.vaanistock.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String unit; // bag, kg, box, packet, bottle, etc.

    @Column(name = "minimum_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "target_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetStock = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Product() {}

    public Product(Long businessId, Long categoryId, String name, String description,
                   String unit, BigDecimal minimumStock, BigDecimal reorderLevel, BigDecimal targetStock) {
        this.businessId = businessId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
        this.minimumStock = minimumStock != null ? minimumStock : BigDecimal.ZERO;
        this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
        this.targetStock = targetStock != null ? targetStock : BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

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
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock != null ? minimumStock : BigDecimal.ZERO;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
    }

    public BigDecimal getTargetStock() {
        return targetStock;
    }

    public void setTargetStock(BigDecimal targetStock) {
        this.targetStock = targetStock != null ? targetStock : BigDecimal.ZERO;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

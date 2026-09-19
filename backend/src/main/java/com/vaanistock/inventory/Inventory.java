package com.vaanistock.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id"})
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "current_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Inventory() {}

    public Inventory(Long productId, BigDecimal currentQuantity, String unit) {
        this.productId = productId;
        this.currentQuantity = currentQuantity != null ? currentQuantity : BigDecimal.ZERO;
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity != null ? currentQuantity : BigDecimal.ZERO;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

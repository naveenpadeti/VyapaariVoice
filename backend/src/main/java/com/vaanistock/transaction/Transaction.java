package com.vaanistock.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_business_created", columnList = "business_id, created_at"),
        @Index(name = "idx_tx_product_created", columnList = "product_id, created_at")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionSource source = TransactionSource.MANUAL;

    @Column(length = 255)
    private String reference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Transaction() {}

    public Transaction(Long businessId, Long productId, TransactionType type, BigDecimal quantity,
                       String unit, TransactionSource source, String reference) {
        this.businessId = businessId;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
        this.source = source != null ? source : TransactionSource.MANUAL;
        this.reference = reference;
        this.createdAt = Instant.now();
    }

    public Transaction(Long businessId, Long productId, TransactionType type, BigDecimal quantity,
                       String unit, TransactionSource source, String reference, Instant createdAt) {
        this.businessId = businessId;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
        this.source = source != null ? source : TransactionSource.MANUAL;
        this.reference = reference;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit.toLowerCase().trim() : "unit";
    }

    public TransactionSource getSource() {
        return source;
    }

    public void setSource(TransactionSource source) {
        this.source = source;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

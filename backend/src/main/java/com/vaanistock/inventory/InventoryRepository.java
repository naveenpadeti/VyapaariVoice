package com.vaanistock.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    List<Inventory> findByProductIdIn(Collection<Long> productIds);
    void deleteByProductId(Long productId);
}

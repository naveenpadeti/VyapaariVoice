package com.vaanistock.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBusinessIdOrderByNameAsc(Long businessId);

    List<Product> findByBusinessIdAndCategoryIdOrderByNameAsc(Long businessId, Long categoryId);

    List<Product> findByBusinessIdAndNameContainingIgnoreCaseOrderByNameAsc(Long businessId, String name);

    Optional<Product> findByBusinessIdAndNameIgnoreCase(Long businessId, String name);

    boolean existsByBusinessIdAndNameIgnoreCase(Long businessId, String name);

    long countByBusinessId(Long businessId);

    long countByBusinessIdAndCategoryId(Long businessId, Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("businessId") Long businessId, @Param("query") String query);
}

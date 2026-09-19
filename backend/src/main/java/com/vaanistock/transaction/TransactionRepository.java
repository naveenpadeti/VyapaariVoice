package com.vaanistock.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Page<Transaction> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    List<Transaction> findByBusinessIdAndProductIdOrderByCreatedAtDesc(Long businessId, Long productId);

    Page<Transaction> findByBusinessIdAndProductIdOrderByCreatedAtDesc(Long businessId, Long productId, Pageable pageable);

    List<Transaction> findByBusinessIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            Long businessId, TransactionType type, Instant since);

    List<Transaction> findByBusinessIdAndProductIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            Long businessId, Long productId, TransactionType type, Instant since);

    Optional<Transaction> findFirstByBusinessIdAndProductIdAndTypeOrderByCreatedAtDesc(
            Long businessId, Long productId, TransactionType type);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t WHERE t.businessId = :businessId " +
           "AND t.productId = :productId AND t.type = 'SALE' AND t.createdAt >= :since")
    BigDecimal sumSalesSince(@Param("businessId") Long businessId,
                             @Param("productId") Long productId,
                             @Param("since") Instant since);

    @Query("SELECT t.productId as productId, COALESCE(SUM(t.quantity), 0) as totalSold " +
           "FROM Transaction t WHERE t.businessId = :businessId AND t.type = 'SALE' AND t.createdAt >= :since " +
           "GROUP BY t.productId ORDER BY totalSold DESC")
    List<Object[]> sumSalesByProductSince(@Param("businessId") Long businessId, @Param("since") Instant since);

    @Query("SELECT t.type as txType, COALESCE(SUM(t.quantity), 0) as totalQty " +
           "FROM Transaction t WHERE t.businessId = :businessId AND t.createdAt >= :since " +
           "GROUP BY t.type")
    List<Object[]> sumQuantitiesByTypeSince(@Param("businessId") Long businessId, @Param("since") Instant since);

    long countByBusinessId(Long businessId);
}

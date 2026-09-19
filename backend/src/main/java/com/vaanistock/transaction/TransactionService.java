package com.vaanistock.transaction;

import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              ProductRepository productRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getRecentTransactions(Long businessId, Long productId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100));
        Page<Transaction> page;
        if (productId != null) {
            page = transactionRepository.findByBusinessIdAndProductIdOrderByCreatedAtDesc(businessId, productId, pageable);
        } else {
            page = transactionRepository.findByBusinessIdOrderByCreatedAtDesc(businessId, pageable);
        }

        List<Transaction> content = page.getContent();
        if (content.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> productIds = content.stream().map(Transaction::getProductId).collect(Collectors.toSet());
        Map<Long, String> productNameMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        return content.stream().map(tx -> {
            TransactionDto dto = new TransactionDto();
            dto.setId(tx.getId());
            dto.setBusinessId(tx.getBusinessId());
            dto.setProductId(tx.getProductId());
            dto.setProductName(productNameMap.getOrDefault(tx.getProductId(), "Unknown Product"));
            dto.setType(tx.getType());
            dto.setQuantity(tx.getQuantity());
            dto.setUnit(tx.getUnit());
            dto.setSource(tx.getSource());
            dto.setReference(tx.getReference());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        }).toList();
    }
}

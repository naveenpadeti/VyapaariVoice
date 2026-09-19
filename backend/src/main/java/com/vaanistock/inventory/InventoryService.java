package com.vaanistock.inventory;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.common.BusinessException;
import com.vaanistock.common.InsufficientStockException;
import com.vaanistock.common.ResourceNotFoundException;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.product.ProductService;
import com.vaanistock.transaction.Transaction;
import com.vaanistock.transaction.TransactionRepository;
import com.vaanistock.transaction.TransactionSource;
import com.vaanistock.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            TransactionRepository transactionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public InventoryDto addStock(Long businessId, Long productId, BigDecimal quantity, String unit,
                                 TransactionSource source, String reason) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity to add must be greater than zero");
        }

        Product product = getProductForBusiness(businessId, productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> new Inventory(productId, BigDecimal.ZERO, product.getUnit()));

        BigDecimal newQuantity = inventory.getCurrentQuantity().add(quantity);
        inventory.setCurrentQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        // Record immutable transaction
        String ref = (reason != null && !reason.isBlank()) ? reason : "Stock added";
        Transaction tx = new Transaction(
                businessId,
                productId,
                TransactionType.ADD,
                quantity,
                product.getUnit(),
                source != null ? source : TransactionSource.MANUAL,
                ref
        );
        transactionRepository.save(tx);

        String categoryName = categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("");
        return mapToDto(saved, product, categoryName);
    }

    @Transactional
    public InventoryDto removeStock(Long businessId, Long productId, BigDecimal quantity, String unit,
                                    TransactionSource source, String reason, boolean isSale) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity to remove must be greater than zero");
        }

        Product product = getProductForBusiness(businessId, productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InsufficientStockException(product.getName(), BigDecimal.ZERO, quantity, product.getUnit()));

        if (inventory.getCurrentQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException(product.getName(), inventory.getCurrentQuantity(), quantity, product.getUnit());
        }

        BigDecimal newQuantity = inventory.getCurrentQuantity().subtract(quantity);
        inventory.setCurrentQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        // Record immutable transaction
        TransactionType txType = isSale ? TransactionType.SALE : TransactionType.REMOVE;
        String ref = (reason != null && !reason.isBlank()) ? reason : (isSale ? "Customer sale" : "Stock removed");
        Transaction tx = new Transaction(
                businessId,
                productId,
                txType,
                quantity,
                product.getUnit(),
                source != null ? source : TransactionSource.MANUAL,
                ref
        );
        transactionRepository.save(tx);

        String categoryName = categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("");
        return mapToDto(saved, product, categoryName);
    }

    @Transactional(readOnly = true)
    public List<InventoryDto> getInventory(Long businessId, Long categoryId, String statusFilter) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByBusinessIdAndCategoryIdOrderByNameAsc(businessId, categoryId);
        } else {
            products = productRepository.findByBusinessIdOrderByNameAsc(businessId);
        }

        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryMap = categoryRepository.findByBusinessIdOrderByNameAsc(businessId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, Inventory> inventoryMap = inventoryRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Inventory::getProductId, inv -> inv));

        List<InventoryDto> list = new ArrayList<>();
        for (Product product : products) {
            Inventory inv = inventoryMap.get(product.getId());
            BigDecimal currentQty = inv != null ? inv.getCurrentQuantity() : BigDecimal.ZERO;
            String status = ProductService.computeStockStatus(currentQty, product.getReorderLevel());

            if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("ALL")) {
                if (!status.equalsIgnoreCase(statusFilter.trim())) {
                    continue;
                }
            }

            InventoryDto dto = new InventoryDto();
            dto.setId(inv != null ? inv.getId() : null);
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setCategoryId(product.getCategoryId());
            dto.setCategoryName(categoryMap.getOrDefault(product.getCategoryId(), "Uncategorized"));
            dto.setCurrentQuantity(currentQty);
            dto.setUnit(product.getUnit());
            dto.setMinimumStock(product.getMinimumStock());
            dto.setReorderLevel(product.getReorderLevel());
            dto.setTargetStock(product.getTargetStock());
            dto.setStockStatus(status);
            dto.setUpdatedAt(inv != null ? inv.getUpdatedAt() : product.getUpdatedAt());

            list.add(dto);
        }

        return list;
    }

    @Transactional(readOnly = true)
    public InventoryDto getInventoryByProductId(Long businessId, Long productId) {
        Product product = getProductForBusiness(businessId, productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> new Inventory(productId, BigDecimal.ZERO, product.getUnit()));
        String categoryName = categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("");
        return mapToDto(inventory, product, categoryName);
    }

    private Product getProductForBusiness(Long businessId, Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getBusinessId().equals(businessId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private InventoryDto mapToDto(Inventory inventory, Product product, String categoryName) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setCurrentQuantity(inventory.getCurrentQuantity());
        dto.setUnit(product.getUnit());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setReorderLevel(product.getReorderLevel());
        dto.setTargetStock(product.getTargetStock());
        dto.setStockStatus(ProductService.computeStockStatus(inventory.getCurrentQuantity(), product.getReorderLevel()));
        dto.setUpdatedAt(inventory.getUpdatedAt());
        return dto;
    }
}

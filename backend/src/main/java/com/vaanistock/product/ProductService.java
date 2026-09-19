package com.vaanistock.product;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.common.BusinessException;
import com.vaanistock.common.ResourceNotFoundException;
import com.vaanistock.inventory.Inventory;
import com.vaanistock.inventory.InventoryRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          InventoryRepository inventoryRepository,
                          TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public ProductResponse createProduct(Long businessId, ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(cat -> cat.getBusinessId().equals(businessId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String normalizedName = request.getName().trim();
        if (productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, normalizedName)) {
            throw new BusinessException("Product '" + normalizedName + "' already exists in your inventory");
        }

        Product product = new Product(
                businessId,
                category.getId(),
                normalizedName,
                request.getDescription(),
                request.getUnit(),
                request.getMinimumStock(),
                request.getReorderLevel(),
                request.getTargetStock()
        );
        Product savedProduct = productRepository.save(product);

        BigDecimal initialQty = request.getInitialQuantity() != null ? request.getInitialQuantity() : BigDecimal.ZERO;
        Inventory inventory = new Inventory(savedProduct.getId(), initialQty, savedProduct.getUnit());
        inventoryRepository.save(inventory);

        if (initialQty.compareTo(BigDecimal.ZERO) > 0) {
            Transaction tx = new Transaction(
                    businessId,
                    savedProduct.getId(),
                    TransactionType.ADD,
                    initialQty,
                    savedProduct.getUnit(),
                    TransactionSource.SYSTEM,
                    "Initial stock"
            );
            transactionRepository.save(tx);
        }

        return mapToProductResponse(savedProduct, category.getName(), inventory.getCurrentQuantity());
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(Long businessId, Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getBusinessId().equals(businessId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long businessId, Long productId) {
        Product product = getProductEntity(businessId, productId);
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse("Uncategorized");
        BigDecimal currentStock = inventoryRepository.findByProductId(productId)
                .map(Inventory::getCurrentQuantity)
                .orElse(BigDecimal.ZERO);

        return mapToProductResponse(product, categoryName, currentStock);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(Long businessId, Long categoryId, String search) {
        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchProducts(businessId, search.trim());
        } else if (categoryId != null) {
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
        Map<Long, BigDecimal> stockMap = inventoryRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Inventory::getProductId, Inventory::getCurrentQuantity));

        return products.stream().map(p -> {
            String catName = categoryMap.getOrDefault(p.getCategoryId(), "Uncategorized");
            BigDecimal stock = stockMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            return mapToProductResponse(p, catName, stock);
        }).toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long businessId, Long productId, ProductRequest request) {
        Product product = getProductEntity(businessId, productId);

        if (!product.getCategoryId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .filter(cat -> cat.getBusinessId().equals(businessId))
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategoryId(category.getId());
        }

        String newName = request.getName().trim();
        if (!product.getName().equalsIgnoreCase(newName) &&
                productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, newName)) {
            throw new BusinessException("Product '" + newName + "' already exists");
        }

        product.setName(newName);
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setMinimumStock(request.getMinimumStock());
        product.setReorderLevel(request.getReorderLevel());
        product.setTargetStock(request.getTargetStock());

        Product saved = productRepository.save(product);
        String categoryName = categoryRepository.findById(saved.getCategoryId()).map(Category::getName).orElse("");
        BigDecimal currentStock = inventoryRepository.findByProductId(productId).map(Inventory::getCurrentQuantity).orElse(BigDecimal.ZERO);

        return mapToProductResponse(saved, categoryName, currentStock);
    }

    @Transactional
    public void deleteProduct(Long businessId, Long productId) {
        Product product = getProductEntity(businessId, productId);
        inventoryRepository.deleteByProductId(productId);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findByNameFuzzy(Long businessId, String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        String q = query.trim().toLowerCase();

        // 1. Exact match
        Optional<Product> exact = productRepository.findByBusinessIdAndNameIgnoreCase(businessId, q);
        if (exact.isPresent()) return exact;

        // 2. Search containment
        List<Product> products = productRepository.findByBusinessIdOrderByNameAsc(businessId);
        for (Product p : products) {
            String pName = p.getName().toLowerCase();
            if (pName.equals(q) || pName.startsWith(q) || q.startsWith(pName) || pName.contains(q) || q.contains(pName)) {
                return Optional.of(p);
            }
        }

        // 3. Normalized token overlap (e.g. "basmati rice 25kg" vs "rice")
        String[] qTokens = q.split("\\s+");
        for (Product p : products) {
            String pName = p.getName().toLowerCase();
            for (String token : qTokens) {
                if (token.length() >= 3 && pName.contains(token)) {
                    return Optional.of(p);
                }
            }
        }

        return Optional.empty();
    }

    public ProductResponse mapToProductResponse(Product product, String categoryName, BigDecimal currentStock) {
        ProductResponse res = new ProductResponse();
        res.setId(product.getId());
        res.setBusinessId(product.getBusinessId());
        res.setCategoryId(product.getCategoryId());
        res.setCategoryName(categoryName);
        res.setName(product.getName());
        res.setDescription(product.getDescription());
        res.setUnit(product.getUnit());
        res.setMinimumStock(product.getMinimumStock());
        res.setReorderLevel(product.getReorderLevel());
        res.setTargetStock(product.getTargetStock());
        res.setCurrentStock(currentStock != null ? currentStock : BigDecimal.ZERO);
        res.setStockStatus(computeStockStatus(res.getCurrentStock(), product.getReorderLevel()));
        res.setCreatedAt(product.getCreatedAt());
        res.setUpdatedAt(product.getUpdatedAt());
        return res;
    }

    public static String computeStockStatus(BigDecimal currentStock, BigDecimal reorderLevel) {
        if (currentStock == null || currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            return "OUT_OF_STOCK";
        }
        if (reorderLevel != null && currentStock.compareTo(reorderLevel) <= 0) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }
}

package com.vaanistock.recommendation;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.inventory.Inventory;
import com.vaanistock.inventory.InventoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private RecommendationService recommendationService;

    private Product riceProduct;
    private Inventory riceInventory;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                productRepository, categoryRepository, inventoryRepository, transactionRepository, 15
        );

        riceProduct = new Product(1L, 1L, "Rice", "25kg Bag", "bags",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("75"));
        riceProduct.setId(100L);

        riceInventory = new Inventory(100L, new BigDecimal("25"), "bags");
    }

    @Test
    @DisplayName("Acceptance Scenario: Rice with 25 bags stock and 5 bags/day sales calculates 5 days coverage and 50 bags refill")
    void testAcceptanceScenarioCalculations() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(riceProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category(1L, "Grocery")));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(riceInventory));

        // 150 bags sold in 30 days -> 5 bags/day
        when(transactionRepository.sumSalesSince(eq(1L), eq(100L), any(Instant.class)))
                .thenReturn(new BigDecimal("150"));

        StockIntelligenceDto intel = recommendationService.getStockIntelligence(1L, 100L);

        assertNotNull(intel);
        assertEquals(new BigDecimal("25"), intel.getCurrentStock());
        assertEquals(new BigDecimal("5.00"), intel.getAverageDailySales());
        assertEquals(new BigDecimal("5.0"), intel.getStockCoverageDays());
        assertEquals(new BigDecimal("50"), intel.getRecommendedRefillQuantity());
        assertEquals("FAST_MOVING", intel.getSalesVelocityCategory());
        assertTrue(intel.getReason().contains("Rice currently has 25 bags"));
        assertTrue(intel.getReason().contains("Refill ~50 bags"));
    }

    @Test
    @DisplayName("Refill not required when current stock exceeds target stock")
    void testRefillNotRequired() {
        // Current stock: 100 bags (greater than target stock 75)
        riceInventory.setCurrentQuantity(new BigDecimal("100"));

        when(productRepository.findById(100L)).thenReturn(Optional.of(riceProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category(1L, "Grocery")));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(riceInventory));
        when(transactionRepository.sumSalesSince(eq(1L), eq(100L), any(Instant.class)))
                .thenReturn(new BigDecimal("150"));

        StockIntelligenceDto intel = recommendationService.getStockIntelligence(1L, 100L);

        assertNotNull(intel);
        assertEquals(BigDecimal.ZERO, intel.getRecommendedRefillQuantity());
        assertEquals("IN_STOCK", intel.getReorderStatus());
        assertTrue(intel.getReason().contains("Stock is currently sufficient"));
    }

    @Test
    @DisplayName("Insufficient sales data handled gracefully without division by zero")
    void testInsufficientSalesData() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(riceProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category(1L, "Grocery")));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(riceInventory));
        when(transactionRepository.sumSalesSince(eq(1L), eq(100L), any(Instant.class)))
                .thenReturn(BigDecimal.ZERO);

        StockIntelligenceDto intel = recommendationService.getStockIntelligence(1L, 100L);

        assertNotNull(intel);
        assertEquals(BigDecimal.ZERO, intel.getAverageDailySales());
        assertNull(intel.getStockCoverageDays());
        assertEquals("NO_SALES", intel.getSalesVelocityCategory());
        assertTrue(intel.getReason().contains("Not enough recent sales data"));
    }
}

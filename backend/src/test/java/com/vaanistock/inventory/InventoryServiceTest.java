package com.vaanistock.inventory;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.common.InsufficientStockException;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.transaction.Transaction;
import com.vaanistock.transaction.TransactionRepository;
import com.vaanistock.transaction.TransactionSource;
import com.vaanistock.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1L, 1L, "Rice", "25kg Bag", "bags",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("75"));
        testProduct.setId(100L);

        testInventory = new Inventory(100L, new BigDecimal("25"), "bags");
        testInventory.setId(500L);
    }

    @Test
    @DisplayName("Add Stock successfully increases inventory and records transaction")
    void testAddStock() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(new Category(1L, "Grocery")));

        InventoryDto result = inventoryService.addStock(
                1L, 100L, new BigDecimal("20"), "bags", TransactionSource.VOICE, "Voice: Rice 20 bags vachayi"
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("45"), result.getCurrentQuantity());
        verify(inventoryRepository).save(argThat(inv -> inv.getCurrentQuantity().compareTo(new BigDecimal("45")) == 0));
        verify(transactionRepository).save(argThat(tx ->
                tx.getType() == TransactionType.ADD &&
                tx.getQuantity().compareTo(new BigDecimal("20")) == 0 &&
                tx.getSource() == TransactionSource.VOICE
        ));
    }

    @Test
    @DisplayName("Remove Stock successfully decreases inventory for sale")
    void testRemoveStockSuccess() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(new Category(1L, "Grocery")));

        InventoryDto result = inventoryService.removeStock(
                1L, 100L, new BigDecimal("5"), "bags", TransactionSource.VOICE, "5 bags rice ammamu", true
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("20"), result.getCurrentQuantity());
        verify(inventoryRepository).save(argThat(inv -> inv.getCurrentQuantity().compareTo(new BigDecimal("20")) == 0));
        verify(transactionRepository).save(argThat(tx ->
                tx.getType() == TransactionType.SALE &&
                tx.getQuantity().compareTo(new BigDecimal("5")) == 0
        ));
    }

    @Test
    @DisplayName("Remove Stock prevents negative stock and throws InsufficientStockException")
    void testPreventNegativeStock() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // Available is 25, requesting 50
        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () ->
                inventoryService.removeStock(
                        1L, 100L, new BigDecimal("50"), "bags", TransactionSource.VOICE, "Rice 50 bags remove cheyyi", false
                )
        );

        assertTrue(ex.getMessage().contains("Insufficient stock for Rice"));
        assertTrue(ex.getMessage().contains("Available: 25"));
        assertTrue(ex.getMessage().contains("Requested: 50"));
        verify(inventoryRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}

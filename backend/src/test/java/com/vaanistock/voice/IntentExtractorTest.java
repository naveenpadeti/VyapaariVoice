package com.vaanistock.voice;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentExtractorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private IntentExtractor intentExtractor;

    @BeforeEach
    void setUp() {
        Product rice = new Product(1L, 1L, "Rice", "25kg Bag", "bags",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("75"));
        Product sugar = new Product(1L, 1L, "Sugar", "Sugar", "kg",
                new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("80"));
        Product oil = new Product(1L, 1L, "Cooking Oil", "Cooking Oil", "bottles",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("50"));

        when(productRepository.findByBusinessIdOrderByNameAsc(1L)).thenReturn(List.of(rice, sugar, oil));
        when(categoryRepository.findByBusinessIdOrderByNameAsc(1L)).thenReturn(List.of(new Category(1L, "Grocery")));
    }

    @Test
    @DisplayName("Test: 'Rice 20 bags vachayi' extracts ADD_STOCK, Rice, 20, bags")
    void testAddStockTelugu() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "Rice 20 bags vachayi", null);

        assertEquals(VoiceIntent.ADD_STOCK, cmd.getIntent());
        assertEquals("Rice", cmd.getProductName());
        assertEquals(new BigDecimal("20"), cmd.getQuantity());
        assertEquals("bags", cmd.getUnit());
        assertEquals("te", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test: '5 bags rice ammamu' extracts REMOVE_STOCK, Rice, 5, bags")
    void testRemoveStockTelugu() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "5 bags rice ammamu", null);

        assertEquals(VoiceIntent.REMOVE_STOCK, cmd.getIntent());
        assertEquals("Rice", cmd.getProductName());
        assertEquals(new BigDecimal("5"), cmd.getQuantity());
        assertEquals("bags", cmd.getUnit());
        assertEquals("te", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test: 'Rice stock entha undi?' extracts CHECK_STOCK, Rice")
    void testCheckStockTelugu() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "Rice stock entha undi?", null);

        assertEquals(VoiceIntent.CHECK_STOCK, cmd.getIntent());
        assertEquals("Rice", cmd.getProductName());
        assertEquals("te", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test: 'Which products are low?' extracts LOW_STOCK")
    void testLowStockEnglish() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "Which products are low?", null);

        assertEquals(VoiceIntent.LOW_STOCK, cmd.getIntent());
        assertEquals("en", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test: 'Rice refill cheyyala?' extracts REORDER_RECOMMENDATION, Rice")
    void testReorderRecommendationTelugu() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "Rice refill cheyyala?", null);

        assertEquals(VoiceIntent.REORDER_RECOMMENDATION, cmd.getIntent());
        assertEquals("Rice", cmd.getProductName());
        assertEquals("te", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test: 'Na shop inventory summary cheppu' extracts INVENTORY_SUMMARY")
    void testInventorySummaryTelugu() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "Na shop inventory summary cheppu", null);

        assertEquals(VoiceIntent.INVENTORY_SUMMARY, cmd.getIntent());
        assertEquals("te", cmd.getDetectedLanguage());
    }

    @Test
    @DisplayName("Test Hindi: '5 packet sugar becha' extracts REMOVE_STOCK, Sugar, 5, packets")
    void testRemoveStockHindi() {
        ParsedVoiceCommand cmd = intentExtractor.parse(1L, "5 packet sugar becha", null);

        assertEquals(VoiceIntent.REMOVE_STOCK, cmd.getIntent());
        assertEquals("Sugar", cmd.getProductName());
        assertEquals(new BigDecimal("5"), cmd.getQuantity());
        assertEquals("packets", cmd.getUnit());
        assertEquals("hi", cmd.getDetectedLanguage());
    }
}

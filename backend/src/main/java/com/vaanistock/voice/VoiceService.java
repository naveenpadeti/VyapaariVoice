package com.vaanistock.voice;

import com.vaanistock.analytics.AnalyticsService;
import com.vaanistock.analytics.AnalyticsSummaryDto;
import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.common.BusinessException;
import com.vaanistock.common.InsufficientStockException;
import com.vaanistock.common.ResourceNotFoundException;
import com.vaanistock.inventory.InventoryDto;
import com.vaanistock.inventory.InventoryService;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductService;
import com.vaanistock.recommendation.RecommendationService;
import com.vaanistock.recommendation.StockIntelligenceDto;
import com.vaanistock.transaction.TransactionSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class VoiceService {

    private final IntentExtractor intentExtractor;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final RecommendationService recommendationService;
    private final AnalyticsService analyticsService;
    private final CategoryRepository categoryRepository;
    private final VoiceResponseGenerator responseGenerator;
    private final VoiceCommandRepository voiceCommandRepository;
    private final SpeechToTextService speechToTextService;

    public VoiceService(IntentExtractor intentExtractor,
                        ProductService productService,
                        InventoryService inventoryService,
                        RecommendationService recommendationService,
                        AnalyticsService analyticsService,
                        CategoryRepository categoryRepository,
                        VoiceResponseGenerator responseGenerator,
                        VoiceCommandRepository voiceCommandRepository,
                        SpeechToTextService speechToTextService) {
        this.intentExtractor = intentExtractor;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.recommendationService = recommendationService;
        this.analyticsService = analyticsService;
        this.categoryRepository = categoryRepository;
        this.responseGenerator = responseGenerator;
        this.voiceCommandRepository = voiceCommandRepository;
        this.speechToTextService = speechToTextService;
    }

    @Transactional
    public VoiceResponse processCommand(Long businessId, Long userId, VoiceRequest request) {
        String transcript = request.getText();
        if ((transcript == null || transcript.isBlank()) && request.getAudioBase64() != null) {
            byte[] audio = Base64.getDecoder().decode(request.getAudioBase64());
            transcript = speechToTextService.transcribe(audio, request.getLanguage());
        }

        if (transcript == null || transcript.isBlank()) {
            VoiceResponse res = new VoiceResponse();
            res.setIntent(VoiceIntent.UNKNOWN);
            res.setLanguage(request.getLanguage() != null ? request.getLanguage() : "te");
            res.setStatus("ERROR");
            res.setResponse("Voice input was empty. Please try speaking again.");
            return res;
        }

        ParsedVoiceCommand parsed = intentExtractor.parse(businessId, transcript, request.getLanguage());
        String lang = parsed.getDetectedLanguage();

        VoiceResponse response = new VoiceResponse();
        response.setIntent(parsed.getIntent());
        response.setLanguage(lang);
        response.setStatus("SUCCESS");

        Map<String, Object> details = new HashMap<>();
        details.put("rawTranscript", transcript);
        details.put("parsedProduct", parsed.getProductName());
        details.put("parsedQuantity", parsed.getQuantity());
        details.put("parsedUnit", parsed.getUnit());

        try {
            switch (parsed.getIntent()) {
                case ADD_STOCK -> handleAddStock(businessId, parsed, lang, response, details);
                case REMOVE_STOCK -> handleRemoveStock(businessId, parsed, lang, response, details);
                case CHECK_STOCK -> handleCheckStock(businessId, parsed, lang, response, details);
                case REORDER_RECOMMENDATION -> handleReorderRecommendation(businessId, parsed, lang, response, details);
                case LOW_STOCK -> handleLowStock(businessId, lang, response, details);
                case CHECK_CATEGORY -> handleCheckCategory(businessId, parsed, lang, response, details);
                case INVENTORY_SUMMARY -> handleInventorySummary(businessId, lang, response, details);
                case FAST_MOVING -> handleFastMoving(businessId, lang, response, details);
                case SLOW_MOVING -> handleSlowMoving(businessId, lang, response, details);
                case NO_RECENT_SALES -> handleNoRecentSales(businessId, lang, response, details);
                case SEARCH_PRODUCT -> handleSearchProduct(businessId, parsed, lang, response, details);
                default -> handleUnknown(parsed, lang, response);
            }
        } catch (InsufficientStockException ex) {
            response.setStatus("CONFIRMATION_REQUIRED");
            response.setResponse(responseGenerator.generateInsufficientStockWarning(
                    parsed.getProductName(), ex.getAvailableQuantity(), ex.getRequestedQuantity(), ex.getUnit(), lang));
            details.put("error", ex.getMessage());
        } catch (Exception ex) {
            response.setStatus("ERROR");
            response.setResponse("Sorry, could not complete the operation: " + ex.getMessage());
            details.put("error", ex.getMessage());
        }

        response.setDetails(details);

        // Audit Trail
        VoiceCommand audit = new VoiceCommand(
                businessId,
                userId,
                transcript,
                lang,
                parsed.getIntent(),
                details.toString(),
                response.getResponse(),
                response.getStatus()
        );
        voiceCommandRepository.save(audit);

        return response;
    }

    private void handleAddStock(Long businessId, ParsedVoiceCommand parsed, String lang,
                                VoiceResponse response, Map<String, Object> details) {
        if (parsed.getProductName() == null || parsed.getQuantity() == null) {
            response.setStatus("ERROR");
            response.setResponse(switch (lang) {
                case "te" -> "Product peru mariyu quantity spashtanga cheppandi (Udaaharana: Rice 20 bags vachayi).";
                case "hi" -> "Product ka naam aur quantity spasht roop se kahein (Udaharan: Rice 20 bags aaya).";
                default -> "Please specify both the product name and quantity (e.g. Rice 20 bags received).";
            });
            return;
        }

        Product product = findProductOrThrow(businessId, parsed.getProductName());
        BigDecimal qty = parsed.getQuantity();
        String unit = parsed.getUnit() != null ? parsed.getUnit() : product.getUnit();

        InventoryDto updated = inventoryService.addStock(
                businessId,
                product.getId(),
                qty,
                unit,
                TransactionSource.VOICE,
                "Voice: " + parsed.getRawTranscript()
        );

        response.setProduct(product.getName());
        response.setCurrentStock(updated.getCurrentQuantity());
        response.setUnit(updated.getUnit());
        response.setResponse(responseGenerator.generateAddStockResponse(
                product.getName(), qty, updated.getCurrentQuantity(), updated.getUnit(), lang));
    }

    private void handleRemoveStock(Long businessId, ParsedVoiceCommand parsed, String lang,
                                   VoiceResponse response, Map<String, Object> details) {
        if (parsed.getProductName() == null || parsed.getQuantity() == null) {
            response.setStatus("ERROR");
            response.setResponse(switch (lang) {
                case "te" -> "Ammina product peru mariyu quantity spashtanga cheppandi (Udaaharana: 5 bags rice ammamu).";
                case "hi" -> "Beche gaye product ka naam aur quantity spasht kahein (Udaharan: 5 bags rice becha).";
                default -> "Please specify both product name and quantity sold (e.g. Sold 5 bags rice).";
            });
            return;
        }

        Product product = findProductOrThrow(businessId, parsed.getProductName());
        BigDecimal qty = parsed.getQuantity();
        String unit = parsed.getUnit() != null ? parsed.getUnit() : product.getUnit();

        InventoryDto currentInv = inventoryService.getInventoryByProductId(businessId, product.getId());
        if (currentInv.getCurrentQuantity().compareTo(qty) < 0) {
            response.setStatus("CONFIRMATION_REQUIRED");
            response.setProduct(product.getName());
            response.setCurrentStock(currentInv.getCurrentQuantity());
            response.setUnit(currentInv.getUnit());
            response.setResponse(responseGenerator.generateInsufficientStockWarning(
                    product.getName(), currentInv.getCurrentQuantity(), qty, product.getUnit(), lang));
            details.put("warning", "Requested quantity exceeds available stock");
            return;
        }

        InventoryDto updated = inventoryService.removeStock(
                businessId,
                product.getId(),
                qty,
                unit,
                TransactionSource.VOICE,
                "Voice: " + parsed.getRawTranscript(),
                true
        );

        response.setProduct(product.getName());
        response.setCurrentStock(updated.getCurrentQuantity());
        response.setUnit(updated.getUnit());
        response.setResponse(responseGenerator.generateRemoveStockResponse(
                product.getName(), qty, updated.getCurrentQuantity(), updated.getUnit(), lang));
    }

    private void handleCheckStock(Long businessId, ParsedVoiceCommand parsed, String lang,
                                  VoiceResponse response, Map<String, Object> details) {
        if (parsed.getProductName() == null) {
            // If no specific product was mentioned, provide overall inventory summary
            handleInventorySummary(businessId, lang, response, details);
            return;
        }

        Product product = findProductOrThrow(businessId, parsed.getProductName());
        StockIntelligenceDto intel = recommendationService.getStockIntelligence(businessId, product.getId());

        response.setProduct(intel.getProductName());
        response.setCurrentStock(intel.getCurrentStock());
        response.setUnit(intel.getUnit());
        response.setAverageDailySales(intel.getAverageDailySales());
        response.setStockCoverageDays(intel.getStockCoverageDays());
        response.setRecommendedRefill(intel.getRecommendedRefillQuantity());
        response.setResponse(responseGenerator.generateCheckStockResponse(intel, lang));

        details.put("stockCoverageDays", intel.getStockCoverageDays());
        details.put("reorderStatus", intel.getReorderStatus());
    }

    private void handleReorderRecommendation(Long businessId, ParsedVoiceCommand parsed, String lang,
                                             VoiceResponse response, Map<String, Object> details) {
        if (parsed.getProductName() != null) {
            Product product = findProductOrThrow(businessId, parsed.getProductName());
            StockIntelligenceDto intel = recommendationService.getStockIntelligence(businessId, product.getId());
            response.setProduct(intel.getProductName());
            response.setCurrentStock(intel.getCurrentStock());
            response.setUnit(intel.getUnit());
            response.setRecommendedRefill(intel.getRecommendedRefillQuantity());
            response.setResponse(responseGenerator.generateReorderResponse(intel, lang));
        } else {
            // General reorder recommendation query: list all items needing refill
            List<StockIntelligenceDto> reorders = recommendationService.getReorderRecommendations(businessId);
            response.setResponse(responseGenerator.generateLowStockResponse(reorders, lang));
        }
    }

    private void handleLowStock(Long businessId, String lang, VoiceResponse response, Map<String, Object> details) {
        List<StockIntelligenceDto> lowStock = analyticsService.getLowStock(businessId);
        response.setResponse(responseGenerator.generateLowStockResponse(lowStock, lang));
        details.put("lowStockCount", lowStock.size());
    }

    private void handleCheckCategory(Long businessId, ParsedVoiceCommand parsed, String lang,
                                     VoiceResponse response, Map<String, Object> details) {
        String catName = parsed.getCategoryName();
        Category category = categoryRepository.findByBusinessIdAndNameIgnoreCase(businessId, catName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + catName));

        List<InventoryDto> items = inventoryService.getInventory(businessId, category.getId(), null);
        response.setResponse(responseGenerator.generateCategoryResponse(category.getName(), items, lang));
        details.put("itemCount", items.size());
    }

    private void handleInventorySummary(Long businessId, String lang, VoiceResponse response, Map<String, Object> details) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(businessId);
        response.setResponse(responseGenerator.generateSummaryResponse(summary, lang));
        details.put("summary", summary);
    }

    private void handleFastMoving(Long businessId, String lang, VoiceResponse response, Map<String, Object> details) {
        List<StockIntelligenceDto> list = analyticsService.getFastMoving(businessId);
        if (list.isEmpty()) {
            response.setResponse(switch (lang) {
                case "te" -> "Prastutaniki fast-moving products levu.";
                case "hi" -> "Abhi koi fast moving product nahi hai.";
                default -> "There are no fast-moving products recorded yet.";
            });
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(switch (lang) {
            case "te" -> "Mee shop lo fast-selling products:\n";
            case "hi" -> "Aapki shop ke fast moving products:\n";
            default -> "Your fast-moving products by daily velocity:\n";
        });
        for (StockIntelligenceDto item : list) {
            sb.append(String.format("• %s (~%s %s/day)\n", item.getProductName(),
                    item.getAverageDailySales().stripTrailingZeros().toPlainString(), item.getUnit()));
        }
        response.setResponse(sb.toString());
    }

    private void handleSlowMoving(Long businessId, String lang, VoiceResponse response, Map<String, Object> details) {
        List<StockIntelligenceDto> list = analyticsService.getSlowMoving(businessId);
        if (list.isEmpty()) {
            response.setResponse(switch (lang) {
                case "te" -> "Slow-moving products emi levu.";
                case "hi" -> "Koi slow moving product nahi hai.";
                default -> "There are no slow-moving products currently.";
            });
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(switch (lang) {
            case "te" -> "Mella ga ammudupoye products:\n";
            case "hi" -> "Dheere bikne wale products:\n";
            default -> "Slow-moving products with low sales velocity:\n";
        });
        for (StockIntelligenceDto item : list) {
            sb.append(String.format("• %s (~%s %s/day)\n", item.getProductName(),
                    item.getAverageDailySales().stripTrailingZeros().toPlainString(), item.getUnit()));
        }
        response.setResponse(sb.toString());
    }

    private void handleNoRecentSales(Long businessId, String lang, VoiceResponse response, Map<String, Object> details) {
        List<StockIntelligenceDto> list = analyticsService.getNoRecentSales(businessId);
        if (list.isEmpty()) {
            response.setResponse(switch (lang) {
                case "te" -> "Anni products ki recent sales unnai.";
                case "hi" -> "Sabhi products ki haal hi me bikri hui hai.";
                default -> "All products have recorded recent sales.";
            });
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(switch (lang) {
            case "te" -> "Gata 30 rojullo sales leni products:\n";
            case "hi" -> "Pichle 30 dino me bina bikri wale products:\n";
            default -> "Products with zero sales in the last 30 days:\n";
        });
        for (StockIntelligenceDto item : list) {
            sb.append(String.format("• %s (Stock: %s %s)\n", item.getProductName(),
                    item.getCurrentStock().stripTrailingZeros().toPlainString(), item.getUnit()));
        }
        response.setResponse(sb.toString());
    }

    private void handleSearchProduct(Long businessId, ParsedVoiceCommand parsed, String lang,
                                     VoiceResponse response, Map<String, Object> details) {
        if (parsed.getProductName() == null) {
            handleCheckStock(businessId, parsed, lang, response, details);
            return;
        }
        Product product = findProductOrThrow(businessId, parsed.getProductName());
        StockIntelligenceDto intel = recommendationService.getStockIntelligence(businessId, product.getId());
        response.setResponse(responseGenerator.generateCheckStockResponse(intel, lang));
    }

    private void handleUnknown(ParsedVoiceCommand parsed, String lang, VoiceResponse response) {
        response.setResponse(switch (lang) {
            case "te" -> "Meeru cheppindi artham kaledu. Udaaharana: 'Rice 20 bags vachayi', '5 bags rice ammamu', 'Rice stock entha undi?' ani adagandi.";
            case "hi" -> "Samajh nahi aaya. Kripya aise kahein: 'Rice 20 bags aaya', '5 bags rice becha', 'Rice ka kitna stock hai?'";
            default -> "I didn't quite understand. Try asking: 'Rice 20 bags received', 'Sold 5 bags rice', or 'How much rice stock is left?'";
        });
    }

    private Product findProductOrThrow(Long businessId, String productName) {
        return productService.findByNameFuzzy(businessId, productName)
                .orElseThrow(() -> new ResourceNotFoundException("Product '" + productName + "' not found in inventory"));
    }

    @Transactional(readOnly = true)
    public List<VoiceCommand> getRecentVoiceCommands(Long businessId) {
        return voiceCommandRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }
}

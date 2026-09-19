package com.vaanistock.voice;

import com.vaanistock.analytics.AnalyticsSummaryDto;
import com.vaanistock.inventory.InventoryDto;
import com.vaanistock.recommendation.StockIntelligenceDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class VoiceResponseGenerator {

    public String generateAddStockResponse(String productName, BigDecimal addedQty, BigDecimal currentStock, String unit, String lang) {
        String addedStr = addedQty.stripTrailingZeros().toPlainString() + " " + unit;
        String totalStr = currentStock.stripTrailingZeros().toPlainString() + " " + unit;

        return switch (lang) {
            case "te" -> String.format("%s %s successfully add ayyayi. Ippudu mottham stock %s undi.",
                    productName, addedStr, totalStr);
            case "hi" -> String.format("%s ke %s successfully add ho gaye. Abhi kul stock %s hai.",
                    productName, addedStr, totalStr);
            default -> String.format("Successfully added %s of %s. Current stock is now %s.",
                    addedStr, productName, totalStr);
        };
    }

    public String generateRemoveStockResponse(String productName, BigDecimal removedQty, BigDecimal currentStock, String unit, String lang) {
        String removedStr = removedQty.stripTrailingZeros().toPlainString() + " " + unit;
        String totalStr = currentStock.stripTrailingZeros().toPlainString() + " " + unit;

        return switch (lang) {
            case "te" -> String.format("%s %s sale ga record ayyindi. Migilina stock %s undi.",
                    productName, removedStr, totalStr);
            case "hi" -> String.format("%s ke %s sale record ho gaya. Bacha hua stock %s hai.",
                    productName, removedStr, totalStr);
            default -> String.format("Recorded sale of %s of %s. Remaining stock is %s.",
                    removedStr, productName, totalStr);
        };
    }

    public String generateCheckStockResponse(StockIntelligenceDto intel, String lang) {
        String productName = intel.getProductName();
        String stockStr = intel.getCurrentStock().stripTrailingZeros().toPlainString() + " " + intel.getUnit();
        BigDecimal avgSales = intel.getAverageDailySales();
        BigDecimal coverageDays = intel.getStockCoverageDays();
        BigDecimal refill = intel.getRecommendedRefillQuantity();

        if (avgSales == null || avgSales.compareTo(BigDecimal.ZERO) <= 0) {
            return switch (lang) {
                case "te" -> String.format("%s stock %s undi. Recent sales data lekapovadam valla refill estimate cheppalem.",
                        productName, stockStr);
                case "hi" -> String.format("%s ka stock %s hai. Haal hi me koi sales na hone ke karan refill estimate uplabdh nahi hai.",
                        productName, stockStr);
                default -> String.format("%s stock is %s. Not enough recent sales data to provide a reliable refill estimate.",
                        productName, stockStr);
            };
        }

        String salesStr = avgSales.stripTrailingZeros().toPlainString() + " " + intel.getUnit();
        String covStr = coverageDays != null ? coverageDays.stripTrailingZeros().toPlainString() : "0";
        String refillStr = refill != null ? refill.stripTrailingZeros().toPlainString() + " " + intel.getUnit() : "0 " + intel.getUnit();

        return switch (lang) {
            case "te" -> {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s stock %s undi. ", productName, stockStr));
                sb.append(String.format("Mee recent sales prakaram rojuki approximately %s sale avutunnayi. ", salesStr));
                sb.append(String.format("Current stock around %s days ki saripovachu. ", covStr));
                if (refill != null && refill.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("Mee target stock maintain cheyyadaniki approximately %s refill cheyyadam consider cheyyachu.", refillStr));
                } else {
                    sb.append("Current stock target coverage ki saripothundi.");
                }
                yield sb.toString();
            }
            case "hi" -> {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Abhi %s ka stock %s hai. ", productName, stockStr));
                sb.append(String.format("Aapki recent sales ke hisaab se lagbhag %s per day bik rahe hain. ", salesStr));
                sb.append(String.format("Yeh stock lagbhag %s din ke liye kaafi ho sakta hai. ", covStr));
                if (refill != null && refill.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("Target stock maintain karne ke liye lagbhag %s refill karne ki zarurat ho sakti hai.", refillStr));
                } else {
                    sb.append("Current stock abhi ke liye kaafi hai.");
                }
                yield sb.toString();
            }
            default -> {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("You currently have %s of %s. ", stockStr, productName));
                sb.append(String.format("Based on your recent sales, you sell around %s per day. ", salesStr));
                sb.append(String.format("Your current stock may last approximately %s days. ", covStr));
                if (refill != null && refill.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("To maintain target coverage, you may need to refill about %s.", refillStr));
                } else {
                    sb.append("Current stock is sufficient for target coverage.");
                }
                yield sb.toString();
            }
        };
    }

    public String generateReorderResponse(StockIntelligenceDto intel, String lang) {
        String productName = intel.getProductName();
        BigDecimal refill = intel.getRecommendedRefillQuantity();
        String unit = intel.getUnit();

        if (refill != null && refill.compareTo(BigDecimal.ZERO) > 0) {
            String refillStr = refill.stripTrailingZeros().toPlainString() + " " + unit;
            return switch (lang) {
                case "te" -> String.format("Avunu, %s refill cheyyadam manchidi. Recent sales velocity batti approximately %s refill cheyyandi.",
                        productName, refillStr);
                case "hi" -> String.format("Haan, %s refill karna chahiye. Recent sales velocity ke hisaab se lagbhag %s refill karein.",
                        productName, refillStr);
                default -> String.format("Yes, refilling %s is recommended. Based on recent sales velocity, consider refilling approximately %s.",
                        productName, refillStr);
            };
        } else {
            return switch (lang) {
                case "te" -> String.format("Prastutaniki %s refill avasaram ledu. Current stock target ki saripothundi.", productName);
                case "hi" -> String.format("Abhi %s refill karne ki zarurat nahi hai. Current stock kaafi hai.", productName);
                default -> String.format("Refill is not required for %s right now. Current stock is sufficient for your target coverage.", productName);
            };
        }
    }

    public String generateLowStockResponse(List<StockIntelligenceDto> lowItems, String lang) {
        if (lowItems.isEmpty()) {
            return switch (lang) {
                case "te" -> "Mee shop lo ippudu low-stock products emi levu. Stock baagundi!";
                case "hi" -> "Aapki shop me abhi koi low stock product nahi hai. Sabhi stock sahi hain!";
                default -> "You currently have no low-stock products. All inventory levels look healthy!";
            };
        }

        StringBuilder sb = new StringBuilder();
        switch (lang) {
            case "te" -> {
                sb.append(String.format("Mee shop lo %d low-stock products unnai:\n", lowItems.size()));
                for (StockIntelligenceDto item : lowItems) {
                    sb.append(String.format("• %s: %s %s remaining\n", item.getProductName(),
                            item.getCurrentStock().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
                sb.append("Ivaanni reorder level kanna takkuva unnai, twaralo refill cheyyadam better.");
            }
            case "hi" -> {
                sb.append(String.format("Aapki shop me %d products low stock me hain:\n", lowItems.size()));
                for (StockIntelligenceDto item : lowItems) {
                    sb.append(String.format("• %s: %s %s bacha hai\n", item.getProductName(),
                            item.getCurrentStock().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
                sb.append("Inhe jaldi refill karne par vichaar karein.");
            }
            default -> {
                sb.append(String.format("You currently have %d low-stock products:\n", lowItems.size()));
                for (StockIntelligenceDto item : lowItems) {
                    sb.append(String.format("• %s: %s %s remaining\n", item.getProductName(),
                            item.getCurrentStock().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
                sb.append("These products are approaching or below their reorder level.");
            }
        }
        return sb.toString();
    }

    public String generateCategoryResponse(String categoryName, List<InventoryDto> items, String lang) {
        if (items.isEmpty()) {
            return switch (lang) {
                case "te" -> categoryName + " category lo prastutaniki products emi levu.";
                case "hi" -> categoryName + " category me abhi koi product nahi hai.";
                default -> "No products found in " + categoryName + " category.";
            };
        }

        StringBuilder sb = new StringBuilder();
        switch (lang) {
            case "te" -> {
                sb.append(String.format("%s category lo %d products unnai:\n", categoryName, items.size()));
                for (InventoryDto item : items) {
                    sb.append(String.format("• %s: %s %s\n", item.getProductName(),
                            item.getCurrentQuantity().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
            }
            case "hi" -> {
                sb.append(String.format("%s category me %d products hain:\n", categoryName, items.size()));
                for (InventoryDto item : items) {
                    sb.append(String.format("• %s: %s %s\n", item.getProductName(),
                            item.getCurrentQuantity().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
            }
            default -> {
                sb.append(String.format("%s category has %d products:\n", categoryName, items.size()));
                for (InventoryDto item : items) {
                    sb.append(String.format("• %s: %s %s\n", item.getProductName(),
                            item.getCurrentQuantity().stripTrailingZeros().toPlainString(), item.getUnit()));
                }
            }
        }
        return sb.toString();
    }

    public String generateSummaryResponse(AnalyticsSummaryDto summary, String lang) {
        return switch (lang) {
            case "te" -> String.format(
                    "Mee shop summary:\n• Mottham Products: %d\n• Low Stock Items: %d\n• Out of Stock: %d\n• Fast Moving: %d\n• Last 30 days sales: %s units.",
                    summary.getTotalProducts(), summary.getLowStockCount(), summary.getOutOfStockCount(),
                    summary.getFastMovingCount(), summary.getSalesLast30Days().stripTrailingZeros().toPlainString());
            case "hi" -> String.format(
                    "Aapki shop ka summary:\n• Kul Products: %d\n• Low Stock Items: %d\n• Out of Stock: %d\n• Fast Moving: %d\n• Pichle 30 dino ki bikri: %s units.",
                    summary.getTotalProducts(), summary.getLowStockCount(), summary.getOutOfStockCount(),
                    summary.getFastMovingCount(), summary.getSalesLast30Days().stripTrailingZeros().toPlainString());
            default -> String.format(
                    "Your shop summary:\n• Total Products: %d\n• Low Stock Items: %d\n• Out of Stock: %d\n• Fast Moving: %d\n• Last 30 days sales: %s units.",
                    summary.getTotalProducts(), summary.getLowStockCount(), summary.getOutOfStockCount(),
                    summary.getFastMovingCount(), summary.getSalesLast30Days().stripTrailingZeros().toPlainString());
        };
    }

    public String generateInsufficientStockWarning(String productName, BigDecimal available, BigDecimal requested, String unit, String lang) {
        String availStr = available.stripTrailingZeros().toPlainString() + " " + unit;
        String reqStr = requested.stripTrailingZeros().toPlainString() + " " + unit;

        return switch (lang) {
            case "te" -> String.format("%s lo kevalam %s matrame available undi. Meeru %s remove cheyyamani adigaru. Negative stock raakunda ee action aapi uncham.",
                    productName, availStr, reqStr);
            case "hi" -> String.format("%s me sirf %s uplabdh hai. Aapne %s hatane ke liye kaha. Negative stock rokne ke liye yeh operation rok diya gaya hai.",
                    productName, availStr, reqStr);
            default -> String.format("Only %s of %s are available. You requested to remove %s. Operation was halted to prevent negative inventory.",
                    availStr, productName, reqStr);
        };
    }
}

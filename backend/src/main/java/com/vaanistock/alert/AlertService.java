package com.vaanistock.alert;

import com.vaanistock.recommendation.RecommendationService;
import com.vaanistock.recommendation.StockIntelligenceDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertService {

    private final RecommendationService recommendationService;
    // Track dismissed alerts per business: businessId -> Set of productIds
    private final Map<Long, Set<Long>> dismissedProductAlerts = new ConcurrentHashMap<>();

    public AlertService(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public List<LowStockAlertDto> getActiveAlerts(Long businessId) {
        List<StockIntelligenceDto> intelligenceList = recommendationService.getAllStockIntelligence(businessId);
        Set<Long> dismissed = dismissedProductAlerts.getOrDefault(businessId, Collections.emptySet());

        List<LowStockAlertDto> alerts = new ArrayList<>();
        for (StockIntelligenceDto dto : intelligenceList) {
            boolean isLow = "LOW_STOCK".equals(dto.getReorderStatus());
            boolean isOut = "OUT_OF_STOCK".equals(dto.getReorderStatus());

            if (isLow || isOut) {
                if (dismissed.contains(dto.getProductId())) {
                    continue;
                }

                String severity;
                if (isOut) {
                    severity = "CRITICAL";
                } else if (dto.getReorderLevel() != null &&
                        dto.getCurrentStock().compareTo(dto.getReorderLevel().multiply(BigDecimal.valueOf(0.5))) <= 0) {
                    severity = "HIGH";
                } else {
                    severity = "MEDIUM";
                }

                LowStockAlertDto alert = new LowStockAlertDto();
                alert.setProductId(dto.getProductId());
                alert.setProductName(dto.getProductName());
                alert.setCategoryName(dto.getCategoryName());
                alert.setCurrentStock(dto.getCurrentStock());
                alert.setUnit(dto.getUnit());
                alert.setReorderLevel(dto.getReorderLevel());
                alert.setTargetStock(dto.getTargetStock());
                alert.setSuggestedRefill(dto.getRecommendedRefillQuantity());
                alert.setSeverity(severity);
                alert.setCoverageDays(dto.getStockCoverageDays());
                alert.setReason(dto.getReason());
                alert.setStatus("ACTIVE");

                alerts.add(alert);
            }
        }

        alerts.sort((a, b) -> {
            int sevComp = severityOrder(a.getSeverity()) - severityOrder(b.getSeverity());
            if (sevComp != 0) return sevComp;
            return a.getCurrentStock().compareTo(b.getCurrentStock());
        });

        return alerts;
    }

    public void dismissAlert(Long businessId, Long productId) {
        dismissedProductAlerts.computeIfAbsent(businessId, k -> ConcurrentHashMap.newKeySet()).add(productId);
    }

    public void resetDismissedAlerts(Long businessId) {
        dismissedProductAlerts.remove(businessId);
    }

    private int severityOrder(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            default -> 3;
        };
    }
}

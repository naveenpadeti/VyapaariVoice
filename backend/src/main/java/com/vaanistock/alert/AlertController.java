package com.vaanistock.alert;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Low stock alert notifications and management")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Get active low stock alerts")
    public ResponseEntity<ApiResponse<List<LowStockAlertDto>>> getAlerts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LowStockAlertDto> alerts = alertService.getActiveAlerts(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @PostMapping("/{productId}/dismiss")
    @Operation(summary = "Dismiss an alert for a specific product")
    public ResponseEntity<ApiResponse<Void>> dismissAlert(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {
        alertService.dismissAlert(principal.getBusinessId(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Alert dismissed", null));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset dismissed alerts")
    public ResponseEntity<ApiResponse<Void>> resetAlerts(
            @AuthenticationPrincipal UserPrincipal principal) {
        alertService.resetDismissedAlerts(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok("Alerts reset", null));
    }
}

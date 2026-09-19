package com.vaanistock.user;

import com.vaanistock.auth.AuthResponse;
import com.vaanistock.auth.AuthService;
import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile and business settings")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get user and business profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        AuthResponse response = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping
    @Operation(summary = "Update user and business profile")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ProfileUpdateRequest request) {
        AuthResponse response = authService.updateProfile(
                principal.getUserId(),
                principal.getBusinessId(),
                request.getName(),
                request.getPreferredLanguage(),
                request.getBusinessName(),
                request.getLocation()
        );
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", response));
    }

    public static class ProfileUpdateRequest {
        private String name;
        private String preferredLanguage;
        private String businessName;
        private String location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
        }

        public String getBusinessName() {
            return businessName;
        }

        public void setBusinessName(String businessName) {
            this.businessName = businessName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}

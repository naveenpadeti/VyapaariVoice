package com.vaanistock.auth;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new shop owner and business")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account created successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login using email or mobile number and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/demo-login")
    @Operation(summary = "Login to pre-configured demo account (Sri Lakshmi Wholesale)")
    public ResponseEntity<ApiResponse<AuthResponse>> demoLogin() {
        LoginRequest demoRequest = new LoginRequest();
        demoRequest.setUsername("demo@vaanistock.com");
        demoRequest.setPassword("demo123");
        AuthResponse response = authService.login(demoRequest);
        return ResponseEntity.ok(ApiResponse.ok("Welcome to Sri Lakshmi Wholesale Demo", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<ApiResponse<AuthResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        AuthResponse response = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

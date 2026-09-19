package com.vaanistock.voice;

import com.vaanistock.common.ApiResponse;
import com.vaanistock.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice")
@Tag(name = "Voice Assistant", description = "Multilingual voice and conversational stock assistant endpoints")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @PostMapping("/process")
    @Operation(summary = "Process speech audio or text transcript into structured inventory action")
    public ResponseEntity<ApiResponse<VoiceResponse>> processCommand(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody VoiceRequest request) {
        VoiceResponse response = voiceService.processCommand(principal.getBusinessId(), principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get audit log of voice commands for the business")
    public ResponseEntity<ApiResponse<List<VoiceCommand>>> getVoiceHistory(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<VoiceCommand> history = voiceService.getRecentVoiceCommands(principal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}

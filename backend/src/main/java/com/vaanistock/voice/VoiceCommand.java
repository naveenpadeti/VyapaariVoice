package com.vaanistock.voice;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "voice_commands", indexes = {
        @Index(name = "idx_voice_business_created", columnList = "business_id, created_at")
})
public class VoiceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String transcript;

    @Column(length = 10)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private VoiceIntent intent;

    @Column(name = "extracted_data", columnDefinition = "TEXT")
    private String extractedData;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(length = 30)
    private String status; // SUCCESS, FAILED, CONFIRMATION_REQUIRED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public VoiceCommand() {}

    public VoiceCommand(Long businessId, Long userId, String transcript, String language,
                        VoiceIntent intent, String extractedData, String responseText, String status) {
        this.businessId = businessId;
        this.userId = userId;
        this.transcript = transcript;
        this.language = language;
        this.intent = intent;
        this.extractedData = extractedData;
        this.responseText = responseText;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public VoiceIntent getIntent() {
        return intent;
    }

    public void setIntent(VoiceIntent intent) {
        this.intent = intent;
    }

    public String getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

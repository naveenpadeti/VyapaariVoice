package com.vaanistock.voice;

import java.math.BigDecimal;

public class ParsedVoiceCommand {
    private VoiceIntent intent = VoiceIntent.UNKNOWN;
    private String productName;
    private BigDecimal quantity;
    private String unit;
    private String categoryName;
    private String detectedLanguage = "te";
    private String rawTranscript;
    private boolean requiresConfirmation = false;
    private String confirmationPrompt;

    public ParsedVoiceCommand() {}

    public VoiceIntent getIntent() {
        return intent;
    }

    public void setIntent(VoiceIntent intent) {
        this.intent = intent;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public String getRawTranscript() {
        return rawTranscript;
    }

    public void setRawTranscript(String rawTranscript) {
        this.rawTranscript = rawTranscript;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public String getConfirmationPrompt() {
        return confirmationPrompt;
    }

    public void setConfirmationPrompt(String confirmationPrompt) {
        this.confirmationPrompt = confirmationPrompt;
    }
}

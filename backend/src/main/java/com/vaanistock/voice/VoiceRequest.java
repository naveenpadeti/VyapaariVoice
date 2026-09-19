package com.vaanistock.voice;

public class VoiceRequest {
    private String text;
    private String language; // "te", "hi", "en"
    private String audioBase64;

    public VoiceRequest() {}

    public VoiceRequest(String text, String language) {
        this.text = text;
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}

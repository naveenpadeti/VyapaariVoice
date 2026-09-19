package com.vaanistock.voice;

import org.springframework.stereotype.Service;

@Service
public class DefaultSpeechToTextService implements SpeechToTextService {

    @Override
    public String transcribe(byte[] audioData, String languageHint) {
        if (audioData == null || audioData.length == 0) {
            return "";
        }
        // Pluggable STT provider implementation
        // For audio uploads where client speech recognition is unavailable,
        // can connect to external STT API (Google Cloud Speech / Whisper / Gemini)
        return "";
    }
}

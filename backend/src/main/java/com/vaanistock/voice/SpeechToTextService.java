package com.vaanistock.voice;

public interface SpeechToTextService {
    /**
     * Transcribes audio bytes into text.
     * @param audioData raw or encoded audio bytes
     * @param languageHint optional language code ('te', 'hi', 'en')
     * @return transcribed string
     */
    String transcribe(byte[] audioData, String languageHint);
}

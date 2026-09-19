package com.vaanistock.voice;

public interface TextToSpeechService {
    /**
     * Synthesizes text to speech audio bytes or returns null if client-side synthesis is preferred.
     * @param text message text to speak
     * @param language language code ('te', 'hi', 'en')
     * @return audio bytes or base64 audio payload
     */
    byte[] synthesize(String text, String language);
}

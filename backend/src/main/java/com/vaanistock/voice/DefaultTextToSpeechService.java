package com.vaanistock.voice;

import org.springframework.stereotype.Service;

@Service
public class DefaultTextToSpeechService implements TextToSpeechService {

    @Override
    public byte[] synthesize(String text, String language) {
        // Returns null indicating the client-side Web Speech Synthesis (SpeechSynthesisUtterance)
        // should speak the localized text natively in Telugu, Hindi, or English,
        // which gives the lowest latency, zero bandwidth overhead, and works directly in browsers.
        // Can be easily swapped with Google Cloud TTS or ElevenLabs if server-side audio generation is desired.
        return null;
    }
}

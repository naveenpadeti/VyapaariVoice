import React, { createContext, useContext, useState, useRef } from 'react';
import { voiceApi } from '../services/api';
import { startListening as initSpeech, speakText } from '../services/speech';
import { useAuth } from './AuthContext';

const VoiceContext = createContext(null);

export const VoiceProvider = ({ children }) => {
  const { language } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [voiceError, setVoiceError] = useState('');
  const [messages, setMessages] = useState([
    {
      sender: 'vyapaarivoice',
      text: language === 'te' 
        ? "నమస్కారం! నేను వ్యాపారి వాయిస్ (VyapaariVoice). 'Rice 20 bags vachayi' లేదా 'Rice stock entha undi?' అని అడగండి."
        : language === 'hi'
        ? "नमस्ते! मैं व्यापारी वॉयस (VyapaariVoice) हूँ। 'Rice 20 bags aaya' या 'Rice ka stock kitna hai?' पूछें।"
        : "Hello! I am VyapaariVoice. Speak naturally like 'Rice 20 bags received' or 'How much rice stock is left?'",
      timestamp: new Date()
    }
  ]);

  const recognitionRef = useRef(null);

  const openVoiceModal = (autoListen = false) => {
    setIsModalOpen(true);
    setVoiceError('');
    if (autoListen === true) {
      setTimeout(() => {
        startVoiceInput();
      }, 200);
    }
  };

  const closeVoiceModal = () => {
    stopListening();
    setIsModalOpen(false);
  };

  const startVoiceInput = () => {
    if (isListening) {
      stopListening();
      return;
    }

    setTranscript('');
    setVoiceError('');
    setIsListening(true);

    recognitionRef.current = initSpeech({
      language: language || 'te',
      onStart: () => setIsListening(true),
      onResult: ({ transcript: text, isFinal }) => {
        setTranscript(text);
        if (isFinal) {
          stopListening();
          executeVoiceCommand(text);
        }
      },
      onError: (err) => {
        console.warn('Speech recognition notice:', err);
        setIsListening(false);
        if (err?.message) {
          setVoiceError(err.message);
        }
      },
      onEnd: () => {
        setIsListening(false);
      }
    });
  };

  const stopListening = () => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch (e) {}
      recognitionRef.current = null;
    }
    setIsListening(false);
  };

  const executeVoiceCommand = async (commandText) => {
    if (!commandText || !commandText.trim()) return;
    const text = commandText.trim();

    // 1. Add user message
    const userMsg = {
      sender: 'user',
      text: text,
      timestamp: new Date()
    };
    setMessages(prev => [...prev, userMsg]);
    setTranscript('');
    setVoiceError('');
    setIsProcessing(true);

    try {
      const res = await voiceApi.process({
        text: text,
        language: language || 'te'
      });

      if (res.success && res.data) {
        const voiceData = res.data;
        const botMsg = {
          sender: 'vyapaarivoice',
          text: voiceData.response,
          structuredData: voiceData,
          timestamp: new Date()
        };
        setMessages(prev => [...prev, botMsg]);

        // Speak the response back in user's language
        speakText(voiceData.response, voiceData.language || language);
      }
    } catch (err) {
      const errorMsg = {
        sender: 'vyapaarivoice',
        text: err.message || "క్షమించండి, మీ కమాండ్ ప్రాసెస్ చేయడంలో లోపం జరిగింది.",
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <VoiceContext.Provider value={{
      isModalOpen,
      openVoiceModal,
      closeVoiceModal,
      isListening,
      isProcessing,
      transcript,
      setTranscript,
      voiceError,
      setVoiceError,
      messages,
      startVoiceInput,
      stopListening,
      executeVoiceCommand,
      speakText
    }}>
      {children}
    </VoiceContext.Provider>
  );
};

export const useVoice = () => useContext(VoiceContext);

import React from 'react';
import { Mic } from 'lucide-react';
import { useVoice } from '../context/VoiceContext';

export default function GlobalVoiceButton() {
  const { openVoiceModal, isListening } = useVoice();

  return (
    <div className="fixed bottom-6 right-6 z-30 hidden md:block">
      <button
        onClick={() => openVoiceModal(true)}
        className={`group relative flex items-center space-x-3 px-5 py-3.5 rounded-full text-white font-semibold shadow-xl transition-all duration-300 hover:scale-105 active:scale-95 cursor-pointer ${
          isListening
            ? 'bg-rose-600 shadow-rose-500/40 animate-pulse'
            : 'bg-gradient-to-r from-brand-600 via-amber-500 to-amber-600 shadow-amber-500/35 hover:shadow-amber-500/50'
        }`}
      >
        <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
          <Mic className="w-5 h-5 text-white animate-bounce" />
        </div>
        <span className="text-sm tracking-wide">
          {isListening ? 'Listening...' : 'Ask VyapaariVoice 🎤'}
        </span>
      </button>
    </div>
  );
}

import React, { useState } from 'react';
import { useVoice } from '../context/VoiceContext';
import { useAuth } from '../context/AuthContext';
import {
  Mic,
  MicOff,
  X,
  Send,
  Volume2,
  Sparkles,
  TrendingUp,
  Clock,
  Package,
  AlertCircle,
  CheckCircle2
} from 'lucide-react';

const SAMPLE_COMMANDS = {
  te: [
    "Rice 20 bags vachayi",
    "5 bags rice ammamu",
    "Rice stock entha undi?",
    "Rice refill cheyyala?",
    "Which items are low?",
    "Grocery lo em stock undi?",
    "Na shop inventory summary cheppu"
  ],
  hi: [
    "Rice 20 bags aaya",
    "5 bags rice becha",
    "Rice ka stock kitna hai?",
    "Rice refill karna hai kya?",
    "Kaunse products low hain?",
    "Grocery me kya kya hai?",
    "Dukaan ka summary batao"
  ],
  en: [
    "Rice 20 bags received",
    "Sold 5 bags rice",
    "How much rice stock is left?",
    "Should I refill rice?",
    "Which products are low stock?",
    "What is in grocery?",
    "Give me shop inventory summary"
  ]
};

export default function VoiceModal() {
  const { language } = useAuth();
  const {
    isModalOpen,
    closeVoiceModal,
    isListening,
    isProcessing,
    transcript,
    voiceError,
    messages,
    startVoiceInput,
    stopListening,
    executeVoiceCommand,
    speakText
  } = useVoice();

  const [inputVal, setInputVal] = useState('');

  if (!isModalOpen) return null;

  const currentLang = language || 'te';
  const samplePrompts = SAMPLE_COMMANDS[currentLang] || SAMPLE_COMMANDS.en;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!inputVal.trim() || isProcessing) return;
    executeVoiceCommand(inputVal);
    setInputVal('');
  };

  const handleChipClick = (prompt) => {
    executeVoiceCommand(prompt);
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-3 sm:p-4 animate-in fade-in duration-200">
      <div className="relative w-full max-w-2xl bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-gradient-to-r from-amber-50/70 via-white to-amber-50/40">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-brand-500 to-amber-400 text-white flex items-center justify-center shadow-md shadow-amber-500/20">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 flex items-center space-x-2">
                <span>VyapaariVoice Voice Assistant</span>
                <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800">
                  {currentLang === 'te' ? 'తెలుగు' : currentLang === 'hi' ? 'हिन्दी' : 'English'}
                </span>
              </h3>
              <p className="text-xs text-slate-500">
                Voice-first inventory management & stock intelligence
              </p>
            </div>
          </div>
          <button
            onClick={closeVoiceModal}
            className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Conversation Stream */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 min-h-[260px] max-h-[420px] bg-slate-50/50">
          {messages.map((msg, idx) => (
            <div
              key={idx}
              className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[85%] sm:max-w-[78%] rounded-2xl px-4 py-3 text-sm shadow-xs ${
                  msg.sender === 'user'
                    ? 'bg-gradient-to-r from-amber-500 to-brand-600 text-white rounded-br-none'
                    : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-none'
                }`}
              >
                <div className="flex items-start justify-between space-x-3">
                  <p className="whitespace-pre-line leading-relaxed font-normal">
                    {msg.text}
                  </p>
                  {msg.sender !== 'user' && (
                    <button
                      onClick={() => speakText(msg.text, currentLang)}
                      className="p-1 text-slate-400 hover:text-amber-600 transition-colors flex-shrink-0 cursor-pointer"
                      title="Replay Audio"
                    >
                      <Volume2 className="w-4 h-4" />
                    </button>
                  )}
                </div>

                {/* Structured Inventory Card when Available */}
                {msg.structuredData && msg.structuredData.product && (
                  <div className="mt-3 pt-3 border-t border-slate-100 grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                    <div className="bg-slate-50 rounded-xl p-2">
                      <div className="flex items-center space-x-1 text-slate-500 text-[10px]">
                        <Package className="w-3 h-3 text-amber-500" />
                        <span>Product</span>
                      </div>
                      <div className="font-bold text-slate-800 truncate mt-0.5">
                        {msg.structuredData.product}
                      </div>
                    </div>

                    <div className="bg-slate-50 rounded-xl p-2">
                      <div className="flex items-center space-x-1 text-slate-500 text-[10px]">
                        <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                        <span>Current Stock</span>
                      </div>
                      <div className="font-bold text-slate-800 truncate mt-0.5">
                        {msg.structuredData.currentStock != null ? `${msg.structuredData.currentStock} ${msg.structuredData.unit || ''}` : '-'}
                      </div>
                    </div>

                    <div className="bg-slate-50 rounded-xl p-2">
                      <div className="flex items-center space-x-1 text-slate-500 text-[10px]">
                        <TrendingUp className="w-3 h-3 text-indigo-500" />
                        <span>Avg Velocity</span>
                      </div>
                      <div className="font-bold text-slate-800 truncate mt-0.5">
                        {msg.structuredData.averageDailySales != null ? `${msg.structuredData.averageDailySales} /day` : '-'}
                      </div>
                    </div>

                    <div className="bg-slate-50 rounded-xl p-2">
                      <div className="flex items-center space-x-1 text-slate-500 text-[10px]">
                        <Clock className="w-3 h-3 text-amber-500" />
                        <span>Coverage</span>
                      </div>
                      <div className="font-bold text-slate-800 truncate mt-0.5">
                        {msg.structuredData.stockCoverageDays != null ? `~${msg.structuredData.stockCoverageDays} days` : 'No data'}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}

          {/* Live Transcript / Processing Indicator */}
          {(isListening || isProcessing) && (
            <div className="flex justify-start">
              <div className="bg-amber-50 border border-amber-200 text-amber-900 rounded-2xl px-4 py-3 text-sm flex items-center space-x-3 animate-in fade-in">
                <div className="w-3 h-3 rounded-full bg-amber-500 animate-ping" />
                <span className="font-medium italic">
                  {isProcessing
                    ? 'Processing stock command & calculating intelligence...'
                    : transcript
                    ? `"${transcript}"`
                    : 'Listening... Please speak your inventory command now.'}
                </span>
              </div>
            </div>
          )}

          {/* Voice Recognition Notice / Error */}
          {voiceError && !isListening && !isProcessing && (
            <div className="flex justify-start">
              <div className="bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl px-4 py-2.5 text-xs flex items-center space-x-2 animate-in fade-in">
                <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0" />
                <span>{voiceError} — You can also type your command below.</span>
              </div>
            </div>
          )}
        </div>

        {/* Suggested Quick Prompts */}
        <div className="px-4 sm:px-6 py-2 bg-white border-t border-slate-100 flex items-center space-x-2 overflow-x-auto no-scrollbar">
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex-shrink-0">
            Try saying:
          </span>
          {samplePrompts.map((p, i) => (
            <button
              key={i}
              onClick={() => handleChipClick(p)}
              className="text-xs bg-slate-100 hover:bg-amber-50 hover:text-amber-800 text-slate-700 px-3 py-1 rounded-full whitespace-nowrap transition-colors flex-shrink-0 border border-slate-200/60"
            >
              "{p}"
            </button>
          ))}
        </div>

        {/* Input & Microphone Controller */}
        <div className="p-4 sm:p-6 bg-white border-t border-slate-100">
          <div className="flex items-center space-x-3">
            {/* Big Mic Button */}
            <button
              type="button"
              onClick={isListening ? stopListening : startVoiceInput}
              className={`w-14 h-14 rounded-2xl flex items-center justify-center text-white shadow-lg transition-all active:scale-95 flex-shrink-0 ${
                isListening
                  ? 'bg-rose-500 shadow-rose-500/30 animate-pulse'
                  : 'bg-gradient-to-tr from-brand-600 to-amber-500 hover:from-brand-700 hover:to-amber-600 shadow-amber-500/25'
              }`}
              title={isListening ? 'Stop Listening' : 'Click & Speak'}
            >
              {isListening ? <MicOff className="w-7 h-7" /> : <Mic className="w-7 h-7" />}
            </button>

            {/* Text input form for silent testing */}
            <form onSubmit={handleSubmit} className="flex-1 flex items-center space-x-2">
              <input
                type="text"
                value={inputVal}
                onChange={(e) => setInputVal(e.target.value)}
                placeholder={
                  isListening
                    ? "Listening to voice input..."
                    : currentLang === 'te'
                    ? "లేదా ఇక్కడ టైప్ చేయండి (ఉదా: Rice 20 bags vachayi)..."
                    : currentLang === 'hi'
                    ? "या यहाँ टाइप करें (उदा: Rice 20 bags aaya)..."
                    : "Or type a command (e.g. Rice 20 bags received)..."
                }
                className="w-full bg-slate-100/90 border border-slate-200 rounded-2xl px-4 py-3.5 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/40 focus:bg-white transition-all text-slate-800"
              />
              <button
                type="submit"
                disabled={!inputVal.trim() || isProcessing}
                className="w-12 h-12 rounded-2xl bg-amber-500 hover:bg-amber-600 disabled:opacity-40 text-white flex items-center justify-center shadow-sm transition-all active:scale-95 flex-shrink-0"
              >
                <Send className="w-5 h-5" />
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

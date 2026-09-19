import React, { useState, useEffect } from 'react';
import { useVoice } from '../context/VoiceContext';
import { useAuth } from '../context/AuthContext';
import { voiceApi } from '../services/api';
import {
  Mic,
  MicOff,
  Volume2,
  Send,
  Sparkles,
  Package,
  TrendingUp,
  Clock,
  CheckCircle2,
  AlertCircle,
  History
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

export default function VoiceAssistant() {
  const { language } = useAuth();
  const {
    isListening,
    isProcessing,
    transcript,
    messages,
    startVoiceInput,
    stopListening,
    executeVoiceCommand,
    speakText
  } = useVoice();

  const [inputVal, setInputVal] = useState('');
  const [history, setHistory] = useState([]);

  const currentLang = language || 'te';
  const samplePrompts = SAMPLE_COMMANDS[currentLang] || SAMPLE_COMMANDS.en;

  const loadHistory = async () => {
    try {
      const res = await voiceApi.getHistory();
      if (res.success && res.data) {
        setHistory(res.data);
      }
    } catch (err) {
      // silent
    }
  };

  useEffect(() => {
    loadHistory();
  }, [messages]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!inputVal.trim() || isProcessing) return;
    executeVoiceCommand(inputVal);
    setInputVal('');
  };

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
          <Mic className="w-6 h-6 text-amber-500" />
          <span>Multilingual Voice Assistant</span>
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Speak in Telugu, Hindi, English, or mixed language to manage inventory naturally
        </p>
      </div>

      {/* Main Conversation Container */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden flex flex-col">
        {/* Messages Stream */}
        <div className="p-4 sm:p-6 space-y-4 min-h-[320px] max-h-[480px] overflow-y-auto bg-slate-50/50">
          {messages.map((msg, idx) => (
            <div
              key={idx}
              className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[85%] sm:max-w-[80%] rounded-2xl px-4 py-3 text-sm shadow-xs ${
                  msg.sender === 'user'
                    ? 'bg-gradient-to-r from-amber-500 to-brand-600 text-white rounded-br-none'
                    : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-none'
                }`}
              >
                <div className="flex items-start justify-between space-x-3">
                  <p className="whitespace-pre-line leading-relaxed">
                    {msg.text}
                  </p>
                  {msg.sender !== 'user' && (
                    <button
                      onClick={() => speakText(msg.text, currentLang)}
                      className="p-1 text-slate-400 hover:text-amber-600 transition-colors flex-shrink-0 cursor-pointer"
                      title="Replay Spoken Audio"
                    >
                      <Volume2 className="w-4 h-4" />
                    </button>
                  )}
                </div>

                {/* Structured Inventory Card */}
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
        </div>

        {/* Quick Example Chips */}
        <div className="px-6 py-2.5 bg-white border-t border-slate-100 flex items-center space-x-2 overflow-x-auto no-scrollbar">
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex-shrink-0">
            Tap to test:
          </span>
          {samplePrompts.map((p, i) => (
            <button
              key={i}
              onClick={() => executeVoiceCommand(p)}
              className="text-xs bg-slate-100 hover:bg-amber-50 hover:text-amber-800 text-slate-700 px-3 py-1 rounded-full whitespace-nowrap transition-colors flex-shrink-0 border border-slate-200/60"
            >
              "{p}"
            </button>
          ))}
        </div>

        {/* Big Mic & Input Form */}
        <div className="p-4 sm:p-6 bg-white border-t border-slate-100">
          <div className="flex items-center space-x-3">
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

      {/* Audit Log / Recent Voice Commands Table */}
      <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs">
        <div className="flex items-center space-x-2 mb-4">
          <History className="w-5 h-5 text-amber-500" />
          <h3 className="font-bold text-base text-slate-900">Voice Command Audit History</h3>
        </div>

        {history.length === 0 ? (
          <p className="text-xs text-slate-400 py-4 text-center">No voice commands logged yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-100 text-slate-400 uppercase tracking-wider text-[10px]">
                  <th className="pb-3 font-bold">Time</th>
                  <th className="pb-3 font-bold">Transcript</th>
                  <th className="pb-3 font-bold">Language</th>
                  <th className="pb-3 font-bold">Extracted Intent</th>
                  <th className="pb-3 font-bold">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {history.slice(0, 10).map((h) => (
                  <tr key={h.id} className="hover:bg-slate-50/60">
                    <td className="py-3 text-slate-500">
                      {new Date(h.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                    </td>
                    <td className="py-3 font-semibold text-slate-900 max-w-xs truncate">
                      "{h.transcript}"
                    </td>
                    <td className="py-3 uppercase font-bold text-[10px] text-slate-500">
                      {h.language}
                    </td>
                    <td className="py-3">
                      <span className="bg-amber-50 text-amber-900 font-bold px-2 py-0.5 rounded-md text-[10px]">
                        {h.intent}
                      </span>
                    </td>
                    <td className="py-3">
                      <span className={`px-2 py-0.5 rounded-full font-bold text-[10px] ${
                        h.status === 'SUCCESS' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                      }`}>
                        {h.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

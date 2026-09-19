// Speech Recognition & Speech Synthesis Utility

const getSpeechRecognition = () => {
  if (typeof window === 'undefined') return null;
  return window.SpeechRecognition || window.webkitSpeechRecognition || null;
};

export const isSpeechRecognitionSupported = () => {
  return !!getSpeechRecognition();
};

export const mapLangCodeToSpeechLang = (langCode) => {
  switch (langCode) {
    case 'te':
      return 'te-IN';
    case 'hi':
      return 'hi-IN';
    case 'en':
    default:
      return 'en-IN';
  }
};

export const startListening = ({
  language = 'te',
  onResult,
  onError,
  onEnd,
  onStart
}) => {
  const SpeechRecognition = getSpeechRecognition();
  if (!SpeechRecognition) {
    if (onError) onError(new Error('Speech recognition is not supported in this browser. Please type your query below.'));
    return null;
  }

  const recognition = new SpeechRecognition();
  recognition.lang = mapLangCodeToSpeechLang(language);
  recognition.continuous = false;
  recognition.interimResults = true;
  recognition.maxAlternatives = 1;

  recognition.onstart = () => {
    if (onStart) onStart();
  };

  recognition.onresult = (event) => {
    let interimTranscript = '';
    let finalTranscript = '';

    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        finalTranscript += event.results[i][0].transcript;
      } else {
        interimTranscript += event.results[i][0].transcript;
      }
    }

    const currentText = finalTranscript || interimTranscript;
    if (onResult && currentText) {
      onResult({
        transcript: currentText,
        isFinal: !!finalTranscript
      });
    }
  };

  recognition.onerror = (event) => {
    if (onError) onError(event);
  };

  recognition.onend = () => {
    if (onEnd) onEnd();
  };

  try {
    recognition.start();
    return recognition;
  } catch (err) {
    if (onError) onError(err);
    return null;
  }
};

export const speakText = (text, language = 'te') => {
  if (typeof window === 'undefined' || !window.speechSynthesis) return;

  // Cancel any ongoing speech
  window.speechSynthesis.cancel();

  if (!text) return;

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = mapLangCodeToSpeechLang(language);
  utterance.rate = 1.0;
  utterance.pitch = 1.0;

  // Try to pick a voice matching the language if available
  const voices = window.speechSynthesis.getVoices();
  const matchedVoice = voices.find(v => v.lang.startsWith(language) || v.lang === mapLangCodeToSpeechLang(language));
  if (matchedVoice) {
    utterance.voice = matchedVoice;
  }

  window.speechSynthesis.speak(utterance);
};

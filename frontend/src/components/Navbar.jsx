import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useVoice } from '../context/VoiceContext';
import { alertApi } from '../services/api';
import { Mic, Bell, User, LogOut, ChevronDown, Store, Globe } from 'lucide-react';

export default function Navbar() {
  const { user, business, language, setLanguage, logout } = useAuth();
  const { openVoiceModal } = useVoice();
  const navigate = useNavigate();
  const [alertCount, setAlertCount] = useState(0);
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
        const res = await alertApi.getAll();
        if (res.success && res.data) {
          setAlertCount(res.data.length);
        }
      } catch (err) {
        // silent catch
      }
    };
    fetchAlerts();
    const interval = setInterval(fetchAlerts, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-slate-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Brand Logo & Shop Name */}
          <div className="flex items-center space-x-3">
            <Link to="/dashboard" className="flex items-center space-x-2.5">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-amber-400 flex items-center justify-center text-white shadow-md shadow-amber-500/20">
                <Mic className="w-5 h-5 text-white" />
              </div>
              <div>
                <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-amber-600 to-brand-700 bg-clip-text text-transparent">
                  VyapaariVoice
                </span>
                <span className="hidden sm:inline-block ml-1.5 text-[10px] font-semibold px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 uppercase tracking-wide">
                  Voice First
                </span>
              </div>
            </Link>

            {business && (
              <div className="hidden md:flex items-center space-x-2 pl-4 ml-4 border-l border-slate-200">
                <Store className="w-4 h-4 text-slate-400" />
                <span className="text-sm font-semibold text-slate-800 truncate max-w-[200px]">
                  {business.businessName}
                </span>
                <span className="text-[11px] px-2 py-0.5 font-medium rounded-full bg-slate-100 text-slate-600">
                  {business.businessType}
                </span>
              </div>
            )}
          </div>

          {/* Quick Voice Bar & Right Actions */}
          <div className="flex items-center space-x-2 sm:space-x-3">
            {/* Ask VyapaariVoice Header Button */}
            <button
              onClick={() => openVoiceModal(true)}
              className="flex items-center space-x-2 bg-gradient-to-r from-brand-500 to-amber-500 hover:from-brand-600 hover:to-amber-600 text-white px-3 sm:px-4 py-2 rounded-xl text-sm font-semibold shadow-sm transition-all active:scale-95 cursor-pointer"
            >
              <Mic className="w-4 h-4 animate-pulse" />
              <span className="hidden sm:inline">Ask VyapaariVoice</span>
            </button>

            {/* Language Selector */}
            <div className="flex items-center bg-slate-100 rounded-lg p-0.5 text-xs font-semibold">
              <button
                onClick={() => setLanguage('te')}
                className={`px-2 py-1 rounded-md transition-all ${language === 'te' ? 'bg-white text-brand-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
                title="Telugu"
              >
                తెలుగు
              </button>
              <button
                onClick={() => setLanguage('hi')}
                className={`px-2 py-1 rounded-md transition-all ${language === 'hi' ? 'bg-white text-brand-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
                title="Hindi"
              >
                हिन्दी
              </button>
              <button
                onClick={() => setLanguage('en')}
                className={`px-2 py-1 rounded-md transition-all ${language === 'en' ? 'bg-white text-brand-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}`}
                title="English"
              >
                EN
              </button>
            </div>

            {/* Alerts Bell */}
            <Link
              to="/alerts"
              className="relative p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors"
              title="Low Stock Alerts"
            >
              <Bell className="w-5 h-5" />
              {alertCount > 0 && (
                <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-rose-500 text-white rounded-full text-[10px] font-bold flex items-center justify-center">
                  {alertCount > 9 ? '9+' : alertCount}
                </span>
              )}
            </Link>

            {/* Profile Dropdown */}
            <div className="relative">
              <button
                onClick={() => setShowProfileMenu(!showProfileMenu)}
                className="flex items-center space-x-1.5 p-1.5 rounded-lg hover:bg-slate-100 transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-700 font-bold flex items-center justify-center text-xs">
                  {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
                </div>
                <ChevronDown className="w-3.5 h-3.5 text-slate-500 hidden sm:block" />
              </button>

              {showProfileMenu && (
                <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-slate-200 py-1.5 z-50 animate-in fade-in slide-in-from-top-2">
                  <div className="px-4 py-2 border-b border-slate-100">
                    <p className="text-xs text-slate-400 font-medium">Signed in as</p>
                    <p className="text-sm font-semibold text-slate-900 truncate">{user?.name}</p>
                    <p className="text-xs text-slate-500 truncate">{user?.email || user?.mobile}</p>
                  </div>
                  <Link
                    to="/profile"
                    onClick={() => setShowProfileMenu(false)}
                    className="flex items-center space-x-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  >
                    <User className="w-4 h-4 text-slate-400" />
                    <span>Business Profile</span>
                  </Link>
                  <button
                    onClick={() => {
                      setShowProfileMenu(false);
                      logout();
                      navigate('/login');
                    }}
                    className="w-full flex items-center space-x-2 px-4 py-2 text-sm text-rose-600 hover:bg-rose-50 text-left"
                  >
                    <LogOut className="w-4 h-4 text-rose-500" />
                    <span>Log Out</span>
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

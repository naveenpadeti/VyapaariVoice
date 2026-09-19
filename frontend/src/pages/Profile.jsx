import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { profileApi } from '../services/api';
import { Settings, User, Building2, MapPin, Globe, Check, AlertCircle } from 'lucide-react';

export default function Profile() {
  const { user, business, language, setLanguage } = useAuth();

  const [formData, setFormData] = useState({
    name: '',
    businessName: '',
    location: '',
    preferredLanguage: 'te'
  });
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (user && business) {
      setFormData({
        name: user.name || '',
        businessName: business.businessName || '',
        location: business.location || '',
        preferredLanguage: user.preferredLanguage || language || 'te'
      });
    }
  }, [user, business]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccessMsg('');
    setErrorMsg('');
    setLoading(true);

    try {
      await profileApi.update(formData);
      setLanguage(formData.preferredLanguage);
      setSuccessMsg('Profile and language preferences updated successfully!');
    } catch (err) {
      setErrorMsg(err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 pb-12 max-w-2xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
          <Settings className="w-6 h-6 text-amber-500" />
          <span>Business Profile & Preferences</span>
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Manage shop details, ownership info, and preferred voice interaction language
        </p>
      </div>

      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs">
        {successMsg && (
          <div className="mb-6 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl p-3 flex items-center space-x-2">
            <Check className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        {errorMsg && (
          <div className="mb-6 bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-center space-x-2">
            <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Owner Name */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Owner Name
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <User className="w-4 h-4" />
              </div>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
              />
            </div>
          </div>

          {/* Business Name */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Business / Shop Name
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Building2 className="w-4 h-4" />
              </div>
              <input
                type="text"
                required
                value={formData.businessName}
                onChange={(e) => setFormData({ ...formData, businessName: e.target.value })}
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
              />
            </div>
          </div>

          {/* Location */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              Location / City
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <MapPin className="w-4 h-4" />
              </div>
              <input
                type="text"
                required
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
              />
            </div>
          </div>

          {/* Preferred Language */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
              Default Voice & Display Language
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { id: 'te', label: 'తెలుగు (Telugu)' },
                { id: 'hi', label: 'हिन्दी (Hindi)' },
                { id: 'en', label: 'English' }
              ].map(lang => (
                <button
                  key={lang.id}
                  type="button"
                  onClick={() => setFormData({ ...formData, preferredLanguage: lang.id })}
                  className={`py-2.5 px-3 text-xs font-bold rounded-xl border transition-all ${
                    formData.preferredLanguage === lang.id
                      ? 'border-amber-500 bg-amber-50 text-amber-900 ring-2 ring-amber-500/20 shadow-xs'
                      : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  {lang.label}
                </button>
              ))}
            </div>
          </div>

          {/* Readonly contact details */}
          <div className="pt-2 border-t border-slate-100 grid grid-cols-2 gap-4 text-xs text-slate-500">
            <div>
              <span className="font-bold text-slate-400 block uppercase text-[10px]">Mobile Number</span>
              <span className="font-medium text-slate-800">{user?.mobile}</span>
            </div>
            <div>
              <span className="font-bold text-slate-400 block uppercase text-[10px]">Email</span>
              <span className="font-medium text-slate-800">{user?.email}</span>
            </div>
          </div>

          <div className="pt-3">
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-xs font-bold shadow-md shadow-amber-500/20 active:scale-95 disabled:opacity-50 transition-all"
            >
              {loading ? 'Saving Changes...' : 'Save Profile Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

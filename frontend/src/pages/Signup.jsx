import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mic, Building2, User, Phone, Mail, Lock, MapPin, Globe, ArrowRight, AlertCircle } from 'lucide-react';

export default function Signup() {
  const { signup } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    businessName: '',
    businessType: 'WHOLESALE',
    location: '',
    preferredLanguage: 'te',
    mobile: '',
    email: '',
    password: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await signup(formData);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/60 via-slate-50 to-orange-50/40 flex flex-col justify-center py-10 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-xl text-center">
        <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-tr from-brand-600 to-amber-500 items-center justify-center text-white shadow-lg shadow-amber-500/20 mb-3">
          <Mic className="w-7 h-7" />
        </div>
        <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900">
          Register Your Business on VyapaariVoice
        </h2>
        <p className="mt-1 text-xs sm:text-sm text-slate-500">
          Set up voice-first stock management in less than 2 minutes
        </p>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-xl px-4 sm:px-0">
        <div className="bg-white py-8 px-6 sm:px-10 rounded-3xl shadow-xl shadow-slate-200/50 border border-slate-100">
          {error && (
            <div className="mb-6 bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-start space-x-2">
              <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="e.g. Ravi Kumar"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
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
                    name="businessName"
                    value={formData.businessName}
                    onChange={handleChange}
                    placeholder="e.g. Sri Lakshmi Wholesale"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>

              {/* Business Type */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Business Type
                </label>
                <select
                  name="businessType"
                  value={formData.businessType}
                  onChange={handleChange}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                >
                  <option value="WHOLESALE">Wholesale Shop / Distributor</option>
                  <option value="RETAIL">Retail Store / Kirana / Supermarket</option>
                </select>
              </div>

              {/* Location */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  City / Location
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <MapPin className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    required
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    placeholder="e.g. Vijayawada, Hyderabad"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>

              {/* Preferred Language */}
              <div className="sm:col-span-2">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Preferred Voice & Display Language
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {[
                    { id: 'te', label: 'తెలుగు (Telugu)' },
                    { id: 'hi', label: 'हिन्दी (Hindi)' },
                    { id: 'en', label: 'English' }
                  ].map(lang => (
                    <button
                      key={lang.id}
                      type="button"
                      onClick={() => setFormData({ ...formData, preferredLanguage: lang.id })}
                      className={`py-2 px-3 text-xs font-bold rounded-xl border transition-all ${
                        formData.preferredLanguage === lang.id
                          ? 'border-amber-500 bg-amber-50 text-amber-900 ring-2 ring-amber-500/20'
                          : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                      }`}
                    >
                      {lang.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Mobile Number */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Mobile Number
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <Phone className="w-4 h-4" />
                  </div>
                  <input
                    type="tel"
                    required
                    name="mobile"
                    value={formData.mobile}
                    onChange={handleChange}
                    placeholder="9876543210"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>

              {/* Email */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Email Address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <Mail className="w-4 h-4" />
                  </div>
                  <input
                    type="email"
                    required
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="ravi@example.com"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>

              {/* Password */}
              <div className="sm:col-span-2">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type="password"
                    required
                    minLength={6}
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="At least 6 characters"
                    className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center space-x-2 bg-gradient-to-r from-amber-500 to-brand-600 hover:from-amber-600 hover:to-brand-700 text-white font-bold py-3 px-4 rounded-xl shadow-md shadow-amber-500/20 active:scale-98 transition-all disabled:opacity-50"
              >
                <span>{loading ? 'Setting up business...' : 'Complete Registration & Open Dashboard'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>

          <div className="mt-4 text-center">
            <p className="text-xs text-slate-500">
              Already registered?{' '}
              <Link to="/login" className="font-bold text-amber-700 hover:text-amber-800">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

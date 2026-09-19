import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mic, Lock, Phone, Sparkles, ArrowRight, AlertCircle } from 'lucide-react';

export default function Login() {
  const { login, demoLogin } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [demoLoading, setDemoLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Invalid email/mobile or password');
    } finally {
      setLoading(false);
    }
  };

  const handleDemo = async () => {
    setError('');
    setDemoLoading(true);
    try {
      await demoLogin();
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to enter demo shop');
    } finally {
      setDemoLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50/60 via-slate-50 to-orange-50/40 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex w-16 h-16 rounded-3xl bg-gradient-to-tr from-brand-600 to-amber-500 items-center justify-center text-white shadow-xl shadow-amber-500/25 mb-4">
          <Mic className="w-8 h-8" />
        </div>
        <h2 className="text-3xl font-extrabold tracking-tight text-slate-900">
          VyapaariVoice
        </h2>
        <p className="mt-1 text-sm font-medium text-amber-700">
          Voice-First Multilingual Inventory Management
        </p>
        <p className="mt-0.5 text-xs text-slate-500">
          For Indian Wholesale & Retail Businesses
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 sm:px-10 rounded-3xl shadow-xl shadow-slate-200/50 border border-slate-100">
          {/* Quick Demo Shop Button */}
          <div className="mb-6">
            <button
              type="button"
              onClick={handleDemo}
              disabled={demoLoading}
              className="w-full flex items-center justify-center space-x-2 bg-gradient-to-r from-amber-500 to-brand-600 hover:from-amber-600 hover:to-brand-700 text-white font-bold py-3 px-4 rounded-2xl shadow-md shadow-amber-500/20 active:scale-98 transition-all disabled:opacity-50"
            >
              <Sparkles className="w-5 h-5 text-amber-200" />
              <span>{demoLoading ? 'Loading Demo Shop...' : '⚡ Explore Demo Shop (Sri Lakshmi Wholesale)'}</span>
            </button>
            <p className="text-center text-[11px] text-slate-400 mt-1.5">
              1-click instant access with 30-day pre-seeded inventory & sales
            </p>
          </div>

          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-white px-3 text-slate-400 font-semibold tracking-wider">
                Or Sign In With Account
              </span>
            </div>
          </div>

          {error && (
            <div className="mb-4 bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-start space-x-2">
              <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Mobile Number or Email
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Phone className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="e.g. 9876543210 or demo@vyapaarivoice.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center space-x-2 bg-slate-900 hover:bg-slate-800 text-white font-bold py-2.5 px-4 rounded-xl shadow-sm active:scale-98 transition-all disabled:opacity-50"
            >
              <span>{loading ? 'Signing in...' : 'Sign In'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-xs text-slate-500">
              New shop owner?{' '}
              <Link to="/signup" className="font-bold text-amber-700 hover:text-amber-800">
                Register Your Business
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

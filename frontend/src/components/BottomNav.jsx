import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Boxes, Package, Bell, Mic } from 'lucide-react';
import { useVoice } from '../context/VoiceContext';

export default function BottomNav() {
  const { openVoiceModal } = useVoice();

  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-200 shadow-lg px-2 py-1.5 flex justify-around items-center">
      <NavLink
        to="/dashboard"
        className={({ isActive }) =>
          `flex flex-col items-center py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-amber-600 font-bold' : 'text-slate-500'
          }`
        }
      >
        <LayoutDashboard className="w-5 h-5 mb-0.5" />
        <span>Dashboard</span>
      </NavLink>

      <NavLink
        to="/inventory"
        className={({ isActive }) =>
          `flex flex-col items-center py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-amber-600 font-bold' : 'text-slate-500'
          }`
        }
      >
        <Boxes className="w-5 h-5 mb-0.5" />
        <span>Stock</span>
      </NavLink>

      {/* Center Voice Button */}
      <button
        onClick={openVoiceModal}
        className="-mt-5 w-12 h-12 rounded-full bg-gradient-to-tr from-brand-600 to-amber-500 text-white flex items-center justify-center shadow-lg shadow-amber-500/30 active:scale-95 transition-transform"
        aria-label="Open Voice Assistant"
      >
        <Mic className="w-6 h-6 animate-pulse" />
      </button>

      <NavLink
        to="/products"
        className={({ isActive }) =>
          `flex flex-col items-center py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-amber-600 font-bold' : 'text-slate-500'
          }`
        }
      >
        <Package className="w-5 h-5 mb-0.5" />
        <span>Products</span>
      </NavLink>

      <NavLink
        to="/alerts"
        className={({ isActive }) =>
          `flex flex-col items-center py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-amber-600 font-bold' : 'text-slate-500'
          }`
        }
      >
        <Bell className="w-5 h-5 mb-0.5" />
        <span>Alerts</span>
      </NavLink>
    </nav>
  );
}

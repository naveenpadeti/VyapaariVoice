import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Boxes,
  Package,
  Layers,
  ReceiptText,
  LineChart,
  Bell,
  Mic,
  Settings
} from 'lucide-react';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/inventory', label: 'Inventory', icon: Boxes },
  { to: '/products', label: 'Products', icon: Package },
  { to: '/categories', label: 'Categories', icon: Layers },
  { to: '/transactions', label: 'Transactions', icon: ReceiptText },
  { to: '/analytics', label: 'Stock Intelligence', icon: LineChart },
  { to: '/alerts', label: 'Low Stock Alerts', icon: Bell },
  { to: '/voice', label: 'Voice Assistant', icon: Mic, highlight: true },
  { to: '/profile', label: 'Profile & Settings', icon: Settings },
];

export default function Sidebar() {
  return (
    <aside className="hidden lg:flex flex-col w-64 bg-white border-r border-slate-200 min-h-[calc(100vh-4rem)] p-4 space-y-1">
      <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 py-2">
        Main Navigation
      </div>
      {NAV_ITEMS.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                isActive
                  ? 'bg-amber-50 text-amber-900 font-semibold shadow-xs'
                  : item.highlight
                  ? 'text-brand-700 bg-brand-50/50 hover:bg-brand-50'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
              }`
            }
          >
            <Icon className="w-5 h-5 flex-shrink-0" />
            <span>{item.label}</span>
          </NavLink>
        );
      })}

      <div className="pt-6 mt-auto">
        <div className="bg-gradient-to-br from-amber-50 to-orange-50 border border-amber-200/60 rounded-2xl p-3.5 text-center">
          <div className="w-8 h-8 rounded-full bg-amber-500 text-white mx-auto flex items-center justify-center mb-2 shadow-xs">
            <Mic className="w-4 h-4" />
          </div>
          <h4 className="text-xs font-bold text-amber-900">Voice Assistant</h4>
          <p className="text-[11px] text-amber-700/80 mt-0.5">
            Tap mic or speak: "Rice 20 bags vachayi"
          </p>
        </div>
      </div>
    </aside>
  );
}

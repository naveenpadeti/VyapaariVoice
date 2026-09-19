import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi, analyticsApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useVoice } from '../context/VoiceContext';
import StockAdjustmentModal from '../components/StockAdjustmentModal';
import {
  Package,
  Layers,
  Boxes,
  AlertTriangle,
  Flame,
  Turtle,
  Mic,
  Plus,
  Minus,
  ArrowRight,
  TrendingUp,
  Clock,
  Sparkles
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid
} from 'recharts';

export default function Dashboard() {
  const { business, language } = useAuth();
  const { openVoiceModal } = useVoice();

  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [trendRange, setTrendRange] = useState('7days');
  const [trendData, setTrendData] = useState([]);

  // Stock Adjustment Modal state
  const [stockModal, setStockModal] = useState({
    isOpen: false,
    mode: 'ADD',
    product: null
  });

  const fetchDashboardData = async () => {
    try {
      const res = await dashboardApi.getOverview();
      if (res.success && res.data) {
        setData(res.data);
        setTrendData(res.data.salesTrend || []);
      }
    } catch (err) {
      console.error('Failed to load dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleTrendRangeChange = async (range) => {
    setTrendRange(range);
    try {
      const res = await analyticsApi.getSalesTrend(range);
      if (res.success && res.data) {
        setTrendData(res.data);
      }
    } catch (err) {
      console.error('Failed to change trend range', err);
    }
  };

  const openStockModal = (mode, product = null) => {
    setStockModal({
      isOpen: true,
      mode,
      product: product
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center space-y-3">
          <div className="w-10 h-10 border-4 border-amber-500 border-t-transparent rounded-full animate-spin" />
          <p className="text-sm font-medium text-slate-500">Loading shop intelligence...</p>
        </div>
      </div>
    );
  }

  const summary = data?.summary || {};
  const lowStock = data?.lowStock || [];
  const topSelling = data?.topSelling || [];
  const recentTx = data?.recentTransactions || [];

  return (
    <div className="space-y-6 pb-12">
      {/* Welcome & Quick Action Bar */}
      <div className="bg-gradient-to-r from-amber-500 via-brand-600 to-amber-700 rounded-3xl p-6 sm:p-8 text-white shadow-lg shadow-amber-500/15 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div>
          <div className="inline-flex items-center space-x-2 bg-white/20 backdrop-blur-md px-3 py-1 rounded-full text-xs font-semibold mb-2">
            <Sparkles className="w-3.5 h-3.5 text-amber-200" />
            <span>Stock Intelligence Active</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            {business?.businessName || 'My Wholesale Shop'}
          </h1>
          <p className="text-amber-100 text-xs sm:text-sm mt-1 max-w-xl">
            {language === 'te'
              ? 'మీ స్టాక్, అమ్మకాలు మరియు రీఫిల్ రికమండేషన్స్ ప్రత్యక్షంగా వినండి మరియు చూడండి.'
              : language === 'hi'
              ? 'अपनी दुकान का स्टॉक, बिक्री और रीऑर्डर जानकारी यहाँ देखें।'
              : 'Voice-first inventory intelligence, sales velocity, and deterministic reorder tracking.'}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2.5">
          <button
            onClick={() => openVoiceModal(true)}
            className="flex items-center space-x-2 bg-white text-slate-900 hover:bg-amber-50 px-4 py-2.5 rounded-2xl font-bold text-sm shadow-md active:scale-95 transition-all cursor-pointer"
          >
            <Mic className="w-4 h-4 text-amber-600 animate-pulse" />
            <span>Speak Command</span>
          </button>
          <button
            onClick={() => openStockModal('ADD')}
            className="flex items-center space-x-1.5 bg-white/20 hover:bg-white/30 backdrop-blur-md text-white px-3.5 py-2.5 rounded-2xl font-semibold text-sm transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Add Stock</span>
          </button>
          <button
            onClick={() => openStockModal('REMOVE')}
            className="flex items-center space-x-1.5 bg-white/20 hover:bg-white/30 backdrop-blur-md text-white px-3.5 py-2.5 rounded-2xl font-semibold text-sm transition-all cursor-pointer"
          >
            <Minus className="w-4 h-4" />
            <span>Record Sale</span>
          </button>
        </div>
      </div>

      {/* Summary KPI Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        {/* Total Products */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Products</span>
            <Package className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{summary.totalProducts || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Catalog items</p>
        </div>

        {/* Categories */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Categories</span>
            <Layers className="w-4 h-4 text-indigo-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{summary.totalCategories || 0}</div>
          <p className="text-[11px] text-slate-400 mt-1">Product groups</p>
        </div>

        {/* Total Stock Units */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Total Units</span>
            <Boxes className="w-4 h-4 text-emerald-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{summary.totalStockUnits || 0}</div>
          <p className="text-[11px] text-emerald-600 font-medium mt-1">Units on hand</p>
        </div>

        {/* Low Stock Alert */}
        <Link to="/alerts" className="bg-white rounded-2xl p-4 border border-rose-200 shadow-xs hover:border-rose-400 transition-colors block">
          <div className="flex items-center justify-between text-rose-500 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Low Stock</span>
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div className="text-2xl font-extrabold text-rose-600">{summary.lowStockCount || 0}</div>
          <p className="text-[11px] text-rose-500 font-semibold mt-1">Refill urgently &rarr;</p>
        </Link>

        {/* Fast Moving */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Fast Moving</span>
            <Flame className="w-4 h-4 text-orange-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{summary.fastMovingCount || 0}</div>
          <p className="text-[11px] text-orange-600 font-medium mt-1">&ge; 2 units / day</p>
        </div>

        {/* Slow Moving */}
        <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider">Slow Moving</span>
            <Turtle className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900">{summary.slowMovingCount || 0}</div>
          <p className="text-[11px] text-amber-600 font-medium mt-1">&lt; 0.5 units / day</p>
        </div>
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sales Trend Chart (2 cols) */}
        <div className="lg:col-span-2 bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
            <div>
              <h3 className="font-bold text-base text-slate-900 flex items-center space-x-2">
                <TrendingUp className="w-4 h-4 text-amber-500" />
                <span>Sales Movement Trend</span>
              </h3>
              <p className="text-xs text-slate-500">Historical customer sales quantities</p>
            </div>
            {/* Range Toggle */}
            <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-semibold self-start">
              {['today', '7days', '30days'].map((r) => (
                <button
                  key={r}
                  onClick={() => handleTrendRangeChange(r)}
                  className={`px-3 py-1.5 rounded-lg transition-all ${
                    trendRange === r
                      ? 'bg-white text-amber-800 shadow-xs font-bold'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  {r === 'today' ? 'Today' : r === '7days' ? 'Last 7 Days' : 'Last 30 Days'}
                </button>
              ))}
            </div>
          </div>

          <div className="h-64 sm:h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="salesGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#e58e0a" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#e58e0a" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', borderRadius: '12px', border: 'none', color: '#fff' }}
                  formatter={(val) => [`${val} units`, 'Quantity Sold']}
                />
                <Area type="monotone" dataKey="quantity" stroke="#e58e0a" strokeWidth={3} fillOpacity={1} fill="url(#salesGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Top Selling Products Bar Chart */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs">
          <div className="mb-4">
            <h3 className="font-bold text-base text-slate-900">Top Selling Products</h3>
            <p className="text-xs text-slate-500">Highest quantity sold (Last 30 days)</p>
          </div>

          <div className="h-64 sm:h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={topSelling} layout="vertical" margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" stroke="#94a3b8" fontSize={11} />
                <YAxis dataKey="productName" type="category" stroke="#475569" fontSize={11} width={80} tickLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', borderRadius: '12px', border: 'none', color: '#fff' }}
                  formatter={(val, name, item) => [`${val} ${item.payload.unit}`, 'Sold']}
                />
                <Bar dataKey="totalSold" fill="#22c55e" radius={[0, 8, 8, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Two Column Layout: Urgent Low Stock & Recent Movements */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Urgent Low Stock Alerts */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-base text-slate-900 flex items-center space-x-2">
                <AlertTriangle className="w-4 h-4 text-rose-500" />
                <span>Urgent Low-Stock Items</span>
              </h3>
              <p className="text-xs text-slate-500">Calculated reorder recommendations</p>
            </div>
            <Link to="/alerts" className="text-xs font-bold text-amber-700 hover:text-amber-800">
              View All Alerts &rarr;
            </Link>
          </div>

          {lowStock.length === 0 ? (
            <div className="text-center py-8 text-slate-400 text-sm">
              All inventory levels are healthy! No low stock alerts.
            </div>
          ) : (
            <div className="space-y-3">
              {lowStock.slice(0, 4).map((item) => (
                <div
                  key={item.productId}
                  className="bg-rose-50/50 border border-rose-100 rounded-2xl p-4 flex items-center justify-between gap-3"
                >
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="font-bold text-slate-900 text-sm">{item.productName}</span>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-100 text-rose-800 uppercase">
                        Low Stock
                      </span>
                    </div>
                    <div className="text-xs text-slate-600 mt-1">
                      Current: <strong className="text-slate-800">{item.currentStock} {item.unit}</strong> | Reorder level: {item.reorderLevel} {item.unit}
                    </div>
                    <p className="text-[11px] text-amber-800 font-medium mt-1">
                      💡 Refill recommendation: <strong>{item.recommendedRefillQuantity} {item.unit}</strong>
                    </p>
                  </div>

                  <button
                    onClick={() => openStockModal('ADD', {
                      id: item.productId,
                      name: item.productName,
                      unit: item.unit,
                      currentStock: item.currentStock
                    })}
                    className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-xs flex-shrink-0 transition-colors"
                  >
                    + Refill
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Transactions Table */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-base text-slate-900 flex items-center space-x-2">
                <Clock className="w-4 h-4 text-indigo-500" />
                <span>Recent Movements</span>
              </h3>
              <p className="text-xs text-slate-500">Live transaction history</p>
            </div>
            <Link to="/transactions" className="text-xs font-bold text-amber-700 hover:text-amber-800">
              View All &rarr;
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="text-slate-400 border-b border-slate-100 uppercase tracking-wider text-[10px]">
                  <th className="pb-2 font-bold">Product</th>
                  <th className="pb-2 font-bold">Action</th>
                  <th className="pb-2 font-bold">Qty</th>
                  <th className="pb-2 font-bold">Source</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {recentTx.slice(0, 6).map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/60">
                    <td className="py-2.5 font-semibold text-slate-800">{tx.productName}</td>
                    <td className="py-2.5">
                      <span className={`px-2 py-0.5 rounded-md font-bold text-[10px] ${
                        tx.type === 'SALE'
                          ? 'bg-rose-100 text-rose-800'
                          : tx.type === 'ADD'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-800'
                      }`}>
                        {tx.type}
                      </span>
                    </td>
                    <td className="py-2.5 font-bold text-slate-900">
                      {tx.type === 'SALE' ? `-${tx.quantity}` : `+${tx.quantity}`} {tx.unit}
                    </td>
                    <td className="py-2.5 text-slate-500">
                      <span className="inline-flex items-center space-x-1">
                        {tx.source === 'VOICE' && <Mic className="w-3 h-3 text-amber-600" />}
                        <span>{tx.source}</span>
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Stock Adjustment Modal */}
      <StockAdjustmentModal
        isOpen={stockModal.isOpen}
        mode={stockModal.mode}
        product={stockModal.product}
        onClose={() => setStockModal({ ...stockModal, isOpen: false })}
        onSuccess={fetchDashboardData}
      />
    </div>
  );
}

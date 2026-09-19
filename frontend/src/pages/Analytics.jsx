import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { recommendationApi, analyticsApi } from '../services/api';
import {
  LineChart,
  Flame,
  Turtle,
  Moon,
  TrendingUp,
  Clock,
  Sparkles,
  ExternalLink,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';

export default function Analytics() {
  const [intelligenceList, setIntelligenceList] = useState([]);
  const [fastMoving, setFastMoving] = useState([]);
  const [slowMoving, setSlowMoving] = useState([]);
  const [noRecentSales, setNoRecentSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [intelRes, fastRes, slowRes, staleRes] = await Promise.all([
          recommendationApi.getIntelligence(),
          analyticsApi.getFastMoving(),
          analyticsApi.getSlowMoving(),
          analyticsApi.getNoRecentSales()
        ]);

        if (intelRes.success) setIntelligenceList(intelRes.data);
        if (fastRes.success) setFastMoving(fastRes.data);
        if (slowRes.success) setSlowMoving(slowRes.data);
        if (staleRes.success) setNoRecentSales(staleRes.data);
      } catch (err) {
        console.error('Failed to load stock intelligence', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const getFilteredList = () => {
    switch (activeTab) {
      case 'FAST': return fastMoving;
      case 'SLOW': return slowMoving;
      case 'NO_SALES': return noRecentSales;
      default: return intelligenceList;
    }
  };

  const displayedList = getFilteredList();

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
          <LineChart className="w-6 h-6 text-amber-500" />
          <span>Stock Intelligence & Velocity Engine</span>
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Deterministic 30-day sales velocity, estimated inventory coverage days, and refill calculations
        </p>
      </div>

      {/* 3 Intelligence Category Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Fast Moving */}
        <div
          onClick={() => setActiveTab('FAST')}
          className={`cursor-pointer rounded-3xl p-5 border transition-all ${
            activeTab === 'FAST'
              ? 'bg-orange-50/70 border-orange-300 ring-2 ring-orange-500/20 shadow-md'
              : 'bg-white border-slate-200/80 hover:border-orange-200 shadow-xs'
          }`}
        >
          <div className="flex items-center justify-between mb-3">
            <div className="w-10 h-10 rounded-2xl bg-orange-100 text-orange-600 flex items-center justify-center font-bold">
              <Flame className="w-5 h-5" />
            </div>
            <span className="text-2xl font-extrabold text-orange-600">
              {fastMoving.length}
            </span>
          </div>
          <h3 className="font-bold text-slate-900 text-sm">Fast-Moving Products</h3>
          <p className="text-xs text-slate-500 mt-0.5">High sales velocity (&ge; 2 units/day)</p>
        </div>

        {/* Slow Moving */}
        <div
          onClick={() => setActiveTab('SLOW')}
          className={`cursor-pointer rounded-3xl p-5 border transition-all ${
            activeTab === 'SLOW'
              ? 'bg-amber-50/70 border-amber-300 ring-2 ring-amber-500/20 shadow-md'
              : 'bg-white border-slate-200/80 hover:border-amber-200 shadow-xs'
          }`}
        >
          <div className="flex items-center justify-between mb-3">
            <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center font-bold">
              <Turtle className="w-5 h-5" />
            </div>
            <span className="text-2xl font-extrabold text-amber-700">
              {slowMoving.length}
            </span>
          </div>
          <h3 className="font-bold text-slate-900 text-sm">Slow-Moving Products</h3>
          <p className="text-xs text-slate-500 mt-0.5">Low velocity (&lt; 0.5 units/day)</p>
        </div>

        {/* No Recent Sales */}
        <div
          onClick={() => setActiveTab('NO_SALES')}
          className={`cursor-pointer rounded-3xl p-5 border transition-all ${
            activeTab === 'NO_SALES'
              ? 'bg-slate-100 border-slate-300 ring-2 ring-slate-500/20 shadow-md'
              : 'bg-white border-slate-200/80 hover:border-slate-300 shadow-xs'
          }`}
        >
          <div className="flex items-center justify-between mb-3">
            <div className="w-10 h-10 rounded-2xl bg-slate-100 text-slate-600 flex items-center justify-center font-bold">
              <Moon className="w-5 h-5" />
            </div>
            <span className="text-2xl font-extrabold text-slate-700">
              {noRecentSales.length}
            </span>
          </div>
          <h3 className="font-bold text-slate-900 text-sm">No Recent Sales</h3>
          <p className="text-xs text-slate-500 mt-0.5">Zero sales in the last 30 days</p>
        </div>
      </div>

      {/* Filter Tabs Bar */}
      <div className="flex items-center bg-white rounded-2xl p-2 border border-slate-200/80 shadow-xs w-full sm:w-auto self-start space-x-1">
        {[
          { id: 'ALL', label: `All Products (${intelligenceList.length})` },
          { id: 'FAST', label: `Fast Moving (${fastMoving.length})` },
          { id: 'SLOW', label: `Slow Moving (${slowMoving.length})` },
          { id: 'NO_SALES', label: `No Recent Sales (${noRecentSales.length})` },
        ].map((t) => (
          <button
            key={t.id}
            onClick={() => setActiveTab(t.id)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
              activeTab === t.id
                ? 'bg-amber-500 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Intelligence Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="bg-slate-50/70 border-b border-slate-200 text-slate-500 uppercase tracking-wider text-[10px]">
                <th className="px-6 py-3.5 font-bold">Product</th>
                <th className="px-6 py-3.5 font-bold">Current Stock</th>
                <th className="px-6 py-3.5 font-bold">Avg Velocity</th>
                <th className="px-6 py-3.5 font-bold">Estimated Coverage</th>
                <th className="px-6 py-3.5 font-bold">Stock Status</th>
                <th className="px-6 py-3.5 font-bold">Refill Recommendation</th>
                <th className="px-6 py-3.5 font-bold text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-12 text-center text-slate-400">Loading intelligence data...</td>
                </tr>
              ) : displayedList.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-12 text-center text-slate-400">No items in this category.</td>
                </tr>
              ) : (
                displayedList.map((item) => (
                  <tr key={item.productId} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4">
                      <Link
                        to={`/products/${item.productId}`}
                        className="font-bold text-slate-900 hover:text-amber-600"
                      >
                        {item.productName}
                      </Link>
                      <span className="text-[10px] text-slate-400 block">{item.categoryName}</span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-sm font-extrabold text-slate-900">
                        {item.currentStock}
                      </span>
                      <span className="ml-1 text-[11px] text-slate-500">{item.unit}</span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-bold text-slate-800">
                        {item.averageDailySales}
                      </span>
                      <span className="text-[11px] text-slate-500 ml-1">{item.unit}/day</span>
                    </td>
                    <td className="px-6 py-4">
                      {item.stockCoverageDays != null ? (
                        <span className="font-bold text-amber-900 bg-amber-50 px-2.5 py-1 rounded-lg">
                          ~{item.stockCoverageDays} days
                        </span>
                      ) : (
                        <span className="text-slate-400 italic">No recent sales</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded-md font-bold text-[10px] uppercase ${
                        item.reorderStatus === 'OUT_OF_STOCK'
                          ? 'bg-rose-100 text-rose-800'
                          : item.reorderStatus === 'LOW_STOCK'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}>
                        {item.reorderStatus?.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {item.recommendedRefillQuantity && item.recommendedRefillQuantity > 0 ? (
                        <span className="font-bold text-emerald-800 bg-emerald-50 px-2.5 py-1 rounded-lg">
                          + Refill {item.recommendedRefillQuantity} {item.unit}
                        </span>
                      ) : (
                        <span className="text-slate-500 text-[11px]">Sufficient</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link
                        to={`/products/${item.productId}`}
                        className="text-amber-700 hover:text-amber-800 font-bold inline-flex items-center space-x-1"
                      >
                        <span>View</span>
                        <ExternalLink className="w-3 h-3" />
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

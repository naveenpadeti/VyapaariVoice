import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { productApi, recommendationApi, transactionApi } from '../services/api';
import StockAdjustmentModal from '../components/StockAdjustmentModal';
import {
  Package,
  Clock,
  TrendingUp,
  AlertTriangle,
  Boxes,
  Plus,
  Minus,
  ArrowLeft,
  Calendar,
  Sparkles,
  CheckCircle2,
  Mic
} from 'lucide-react';

export default function ProductDetails() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [intel, setIntel] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modal, setModal] = useState({
    isOpen: false,
    mode: 'ADD'
  });

  const loadData = async () => {
    try {
      const [prodRes, intelRes, txRes] = await Promise.all([
        productApi.get(id),
        recommendationApi.getProductIntelligence(id),
        transactionApi.getByProduct(id, { limit: 20 })
      ]);

      if (prodRes.success) setProduct(prodRes.data);
      if (intelRes.success) setIntel(intelRes.data);
      if (txRes.success) setTransactions(txRes.data);
    } catch (err) {
      console.error('Failed to load product details', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-8 h-8 border-4 border-amber-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="text-center py-16">
        <p className="text-slate-500">Product not found.</p>
        <Link to="/products" className="text-amber-600 font-bold mt-2 inline-block">
          &larr; Back to Catalog
        </Link>
      </div>
    );
  }

  const isLowStock = intel?.reorderStatus === 'LOW_STOCK' || intel?.reorderStatus === 'OUT_OF_STOCK';
  const refillRecommended = intel?.recommendedRefillQuantity && intel.recommendedRefillQuantity > 0;

  return (
    <div className="space-y-6 pb-12 max-w-5xl mx-auto">
      {/* Back button */}
      <Link
        to="/products"
        className="inline-flex items-center space-x-1.5 text-xs font-bold text-slate-500 hover:text-slate-800 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to Product Catalog</span>
      </Link>

      {/* Header Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div>
          <div className="flex items-center space-x-2 mb-2">
            <span className="text-[10px] font-bold px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-600 uppercase tracking-wide">
              {product.categoryName}
            </span>
            <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase ${
              isLowStock ? 'bg-rose-100 text-rose-800' : 'bg-emerald-100 text-emerald-800'
            }`}>
              {intel?.reorderStatus?.replace('_', ' ')}
            </span>
          </div>

          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            {product.name}
          </h1>
          {product.description && (
            <p className="text-sm text-slate-500 mt-1">{product.description}</p>
          )}
        </div>

        {/* Quick Stock Action Buttons */}
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setModal({ isOpen: true, mode: 'ADD' })}
            className="flex items-center space-x-1.5 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-2xl text-xs font-bold shadow-sm transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Add Stock</span>
          </button>
          <button
            onClick={() => setModal({ isOpen: true, mode: 'REMOVE' })}
            className="flex items-center space-x-1.5 bg-rose-600 hover:bg-rose-700 text-white px-4 py-2.5 rounded-2xl text-xs font-bold shadow-sm transition-all"
          >
            <Minus className="w-4 h-4" />
            <span>Record Sale</span>
          </button>
        </div>
      </div>

      {/* Stock Intelligence KPI Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {/* Current Stock */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
          <div className="flex items-center space-x-1.5 text-slate-400 text-xs font-bold uppercase mb-1">
            <Boxes className="w-4 h-4 text-emerald-500" />
            <span>Current Stock</span>
          </div>
          <div className="text-2xl font-extrabold text-slate-900">
            {product.currentStock}
            <span className="text-xs font-medium text-slate-500 ml-1.5">{product.unit}</span>
          </div>
          <span className="text-[11px] text-slate-400 mt-1 block">
            Target: {product.targetStock} {product.unit}
          </span>
        </div>

        {/* Average Daily Sales */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
          <div className="flex items-center space-x-1.5 text-slate-400 text-xs font-bold uppercase mb-1">
            <TrendingUp className="w-4 h-4 text-indigo-500" />
            <span>Avg Daily Sales</span>
          </div>
          <div className="text-2xl font-extrabold text-slate-900">
            {intel?.averageDailySales != null ? intel.averageDailySales : 0}
            <span className="text-xs font-medium text-slate-500 ml-1.5">{product.unit}/day</span>
          </div>
          <span className="text-[11px] text-indigo-600 font-semibold mt-1 block">
            {intel?.salesVelocityCategory?.replace('_', ' ')}
          </span>
        </div>

        {/* Stock Coverage Days */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
          <div className="flex items-center space-x-1.5 text-slate-400 text-xs font-bold uppercase mb-1">
            <Clock className="w-4 h-4 text-amber-500" />
            <span>Estimated Coverage</span>
          </div>
          <div className="text-2xl font-extrabold text-slate-900">
            {intel?.stockCoverageDays != null ? `~${intel.stockCoverageDays}` : '—'}
            <span className="text-xs font-medium text-slate-500 ml-1.5">days</span>
          </div>
          <span className="text-[11px] text-slate-400 mt-1 block">
            Based on 30-day velocity
          </span>
        </div>

        {/* Reorder Level */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs">
          <div className="flex items-center space-x-1.5 text-slate-400 text-xs font-bold uppercase mb-1">
            <AlertTriangle className="w-4 h-4 text-rose-500" />
            <span>Safety Reorder Level</span>
          </div>
          <div className="text-2xl font-extrabold text-slate-900">
            {product.reorderLevel}
            <span className="text-xs font-medium text-slate-500 ml-1.5">{product.unit}</span>
          </div>
          <span className="text-[11px] text-slate-400 mt-1 block">
            Min stock: {product.minimumStock} {product.unit}
          </span>
        </div>
      </div>

      {/* Deterministic Reorder Recommendation Card */}
      <div className={`rounded-3xl p-6 border shadow-xs ${
        refillRecommended
          ? 'bg-amber-50/70 border-amber-200 text-amber-950'
          : 'bg-emerald-50/60 border-emerald-200 text-emerald-950'
      }`}>
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-start space-x-3">
            <div className={`w-10 h-10 rounded-2xl flex items-center justify-center text-white flex-shrink-0 ${
              refillRecommended ? 'bg-amber-500 shadow-md shadow-amber-500/20' : 'bg-emerald-500'
            }`}>
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base">
                {refillRecommended
                  ? `Recommended Refill: ${intel.recommendedRefillQuantity} ${product.unit}`
                  : 'Current Stock Is Sufficient'}
              </h3>
              <p className="text-xs sm:text-sm mt-1 leading-relaxed opacity-90">
                {intel?.reason}
              </p>
            </div>
          </div>

          {refillRecommended && (
            <button
              onClick={() => setModal({ isOpen: true, mode: 'ADD' })}
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-xs font-bold shadow-xs flex-shrink-0 transition-colors"
            >
              + Refill Now
            </button>
          )}
        </div>
      </div>

      {/* Movement History Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs p-6">
        <h3 className="font-bold text-base text-slate-900 mb-4">Stock Movement History</h3>
        {transactions.length === 0 ? (
          <p className="text-center py-8 text-slate-400 text-xs">No transactions recorded for this product yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-100 text-slate-400 uppercase tracking-wider text-[10px]">
                  <th className="pb-3 font-bold">Date & Time</th>
                  <th className="pb-3 font-bold">Action</th>
                  <th className="pb-3 font-bold">Quantity</th>
                  <th className="pb-3 font-bold">Source</th>
                  <th className="pb-3 font-bold">Notes / Reference</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/60">
                    <td className="py-3 text-slate-600">
                      {new Date(tx.createdAt).toLocaleDateString()} {new Date(tx.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td className="py-3">
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
                    <td className="py-3 font-extrabold text-slate-900">
                      {tx.type === 'SALE' ? `-${tx.quantity}` : `+${tx.quantity}`} {tx.unit}
                    </td>
                    <td className="py-3 text-slate-500">
                      <span className="inline-flex items-center space-x-1">
                        {tx.source === 'VOICE' && <Mic className="w-3 h-3 text-amber-600" />}
                        <span>{tx.source}</span>
                      </span>
                    </td>
                    <td className="py-3 text-slate-500 italic">
                      {tx.reference || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      <StockAdjustmentModal
        isOpen={modal.isOpen}
        mode={modal.mode}
        product={{
          id: product.id,
          name: product.name,
          unit: product.unit,
          currentStock: product.currentStock
        }}
        onClose={() => setModal({ ...modal, isOpen: false })}
        onSuccess={loadData}
      />
    </div>
  );
}

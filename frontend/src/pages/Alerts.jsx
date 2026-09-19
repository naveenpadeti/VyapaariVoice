import React, { useState, useEffect } from 'react';
import { alertApi } from '../services/api';
import StockAdjustmentModal from '../components/StockAdjustmentModal';
import { Bell, AlertTriangle, Check, RefreshCw, Plus, Sparkles, Clock } from 'lucide-react';

export default function Alerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modal, setModal] = useState({
    isOpen: false,
    product: null
  });

  const loadAlerts = async () => {
    try {
      const res = await alertApi.getAll();
      if (res.success && res.data) {
        setAlerts(res.data);
      }
    } catch (err) {
      console.error('Failed to load alerts', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts();
  }, []);

  const handleDismiss = async (productId) => {
    try {
      await alertApi.dismiss(productId);
      setAlerts(prev => prev.filter(a => a.productId !== productId));
    } catch (err) {
      console.error('Failed to dismiss alert', err);
    }
  };

  const handleReset = async () => {
    try {
      await alertApi.reset();
      loadAlerts();
    } catch (err) {
      console.error('Failed to reset alerts', err);
    }
  };

  const handleOpenRefill = (alert) => {
    setModal({
      isOpen: true,
      product: {
        id: alert.productId,
        name: alert.productName,
        unit: alert.unit,
        currentStock: alert.currentStock
      }
    });
  };

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
            <Bell className="w-6 h-6 text-rose-500" />
            <span>Low-Stock Alert Center</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Products approaching or below reorder levels requiring restock attention
          </p>
        </div>

        <button
          onClick={handleReset}
          className="inline-flex items-center space-x-1.5 text-xs font-bold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3 py-2 rounded-xl shadow-xs self-start"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Reset Dismissed Alerts</span>
        </button>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-400 text-xs">Loading alerts...</div>
      ) : alerts.length === 0 ? (
        <div className="bg-white rounded-3xl p-12 border border-slate-200/80 text-center shadow-xs">
          <div className="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-3">
            <Check className="w-7 h-7" />
          </div>
          <h3 className="font-bold text-slate-900 text-base">All Inventory Levels Healthy!</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            None of your products are currently below their safety reorder levels.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {alerts.map((a) => (
            <div
              key={a.productId}
              className={`bg-white rounded-3xl p-6 border shadow-xs transition-all ${
                a.severity === 'CRITICAL'
                  ? 'border-rose-300 ring-2 ring-rose-500/10'
                  : 'border-slate-200/80'
              }`}
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="space-y-1.5">
                  <div className="flex items-center space-x-2">
                    <h3 className="font-extrabold text-slate-900 text-base">
                      {a.productName}
                    </h3>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 uppercase">
                      {a.categoryName}
                    </span>
                    <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase ${
                      a.severity === 'CRITICAL'
                        ? 'bg-rose-100 text-rose-800'
                        : a.severity === 'HIGH'
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {a.severity} Priority
                    </span>
                  </div>

                  <div className="text-xs text-slate-600 flex items-center space-x-3">
                    <span>
                      Current Stock: <strong className="text-rose-600 font-bold">{a.currentStock} {a.unit}</strong>
                    </span>
                    <span>•</span>
                    <span>
                      Reorder Level: <strong>{a.reorderLevel} {a.unit}</strong>
                    </span>
                    {a.coverageDays != null && (
                      <>
                        <span>•</span>
                        <span className="flex items-center space-x-1 text-amber-800 font-semibold">
                          <Clock className="w-3 h-3" />
                          <span>~{a.coverageDays} days left</span>
                        </span>
                      </>
                    )}
                  </div>

                  <p className="text-xs text-slate-500 leading-relaxed pt-1">
                    {a.reason}
                  </p>
                </div>

                {/* Actions */}
                <div className="flex items-center space-x-2.5 flex-shrink-0">
                  <button
                    onClick={() => handleDismiss(a.productId)}
                    className="px-3 py-2 rounded-xl text-xs font-semibold text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
                  >
                    Dismiss
                  </button>
                  <button
                    onClick={() => handleOpenRefill(a)}
                    className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-xs transition-colors"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>+ Refill ({a.suggestedRefill} {a.unit})</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Stock Adjustment Modal */}
      <StockAdjustmentModal
        isOpen={modal.isOpen}
        mode="ADD"
        product={modal.product}
        onClose={() => setModal({ ...modal, isOpen: false })}
        onSuccess={loadAlerts}
      />
    </div>
  );
}

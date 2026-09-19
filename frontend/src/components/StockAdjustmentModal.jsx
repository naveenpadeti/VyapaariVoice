import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { inventoryApi, productApi } from '../services/api';
import { Plus, Minus, X, AlertCircle, Package, ArrowRight } from 'lucide-react';

export default function StockAdjustmentModal({ isOpen, onClose, mode = 'ADD', product = null, onSuccess }) {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [fetchingProducts, setFetchingProducts] = useState(false);

  const [quantity, setQuantity] = useState('');
  const [reason, setReason] = useState('');
  const [isSale, setIsSale] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      setQuantity('');
      setReason('');
      setIsSale(true);
      setError('');

      if (product) {
        setSelectedProductId(product.id || '');
      } else {
        // Fetch all products so user can choose
        setFetchingProducts(true);
        productApi.getAll()
          .then((res) => {
            if (res.success && res.data) {
              setProducts(res.data);
              if (res.data.length > 0) {
                setSelectedProductId(res.data[0].id);
              }
            }
          })
          .catch((err) => {
            console.error('Failed to load products for stock modal', err);
            setError('Could not load products list.');
          })
          .finally(() => {
            setFetchingProducts(false);
          });
      }
    }
  }, [isOpen, mode, product]);

  if (!isOpen) return null;

  const isAdd = mode === 'ADD';
  const activeProduct = product || products.find((p) => String(p.id) === String(selectedProductId)) || null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!activeProduct) {
      setError('Please select a product first.');
      return;
    }

    const qty = parseFloat(quantity);
    if (isNaN(qty) || qty <= 0) {
      setError('Please enter a valid positive quantity.');
      return;
    }

    if (!isAdd && activeProduct.currentStock != null && qty > activeProduct.currentStock) {
      setError(`Insufficient stock. Available: ${activeProduct.currentStock} ${activeProduct.unit}, Requested: ${qty} ${activeProduct.unit}`);
      return;
    }

    setLoading(true);
    try {
      if (isAdd) {
        await inventoryApi.addStock({
          productId: activeProduct.id,
          quantity: qty,
          unit: activeProduct.unit,
          reason: reason.trim() || 'Manual stock entry'
        });
      } else {
        await inventoryApi.removeStock({
          productId: activeProduct.id,
          quantity: qty,
          unit: activeProduct.unit,
          reason: reason.trim() || (isSale ? 'Customer sale' : 'Stock adjustment'),
          isSale: isSale
        });
      }

      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to update stock');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-3xl shadow-xl border border-slate-100 w-full max-w-md overflow-hidden">
        {/* Header */}
        <div className={`px-6 py-4 flex items-center justify-between border-b ${
          isAdd ? 'bg-emerald-50/70 border-emerald-100 text-emerald-950' : 'bg-rose-50/70 border-rose-100 text-rose-950'
        }`}>
          <div className="flex items-center space-x-2.5">
            <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-white ${
              isAdd ? 'bg-emerald-500' : 'bg-rose-500'
            }`}>
              {isAdd ? <Plus className="w-5 h-5" /> : <Minus className="w-5 h-5" />}
            </div>
            <div>
              <h3 className="font-bold text-base">
                {isAdd ? '+ Add Stock' : '- Record Sale'}
              </h3>
              <p className="text-xs opacity-80">
                {activeProduct ? activeProduct.name : 'Select a product to continue'}
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg hover:bg-black/5 text-slate-500 cursor-pointer">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        {fetchingProducts ? (
          <div className="p-8 text-center space-y-3">
            <div className="w-8 h-8 border-3 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto" />
            <p className="text-xs font-medium text-slate-500">Loading catalog items...</p>
          </div>
        ) : !product && products.length === 0 ? (
          /* Empty state: No products found in shop */
          <div className="p-8 text-center space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-600 flex items-center justify-center mx-auto">
              <Package className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-base">No Products in Shop Yet</h4>
              <p className="text-xs text-slate-500 mt-1 max-w-xs mx-auto leading-relaxed">
                You need to create products in your catalog before you can add stock or record sales.
              </p>
            </div>
            <div className="pt-2 flex items-center justify-center space-x-3">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => {
                  onClose();
                  navigate('/products');
                }}
                className="inline-flex items-center space-x-1.5 px-4 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs shadow-md transition-all active:scale-95 cursor-pointer"
              >
                <span>Go to Products</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ) : (
          /* Form */
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            {error && (
              <div className="bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-start space-x-2">
                <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            {/* Product Selector (Only rendered if modal was opened without a fixed product) */}
            {!product && (
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Select Product
                </label>
                <select
                  value={selectedProductId}
                  onChange={(e) => setSelectedProductId(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2.5 text-sm font-medium focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white text-slate-800"
                  required
                >
                  <option value="" disabled>-- Choose a Product --</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} ({p.currentStock != null ? p.currentStock : 0} {p.unit || ''} available)
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Current Stock Banner */}
            {activeProduct && (
              <div className="bg-slate-50 rounded-2xl p-3 flex justify-between items-center text-xs">
                <span className="text-slate-500">Current In Stock:</span>
                <span className="font-bold text-slate-800 text-sm">
                  {activeProduct.currentStock != null ? `${activeProduct.currentStock} ${activeProduct.unit}` : `0 ${activeProduct.unit || ''}`}
                </span>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Quantity ({activeProduct?.unit || 'Units'})
              </label>
              <input
                type="number"
                step="any"
                min="0.01"
                required
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                placeholder="e.g. 10"
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 text-base font-semibold focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
              />
            </div>

            {!isAdd && (
              <div className="flex items-center space-x-2 pt-1">
                <input
                  type="checkbox"
                  id="isSaleCheck"
                  checked={isSale}
                  onChange={(e) => setIsSale(e.target.checked)}
                  className="w-4 h-4 text-amber-600 rounded border-slate-300 focus:ring-amber-500"
                />
                <label htmlFor="isSaleCheck" className="text-xs font-medium text-slate-700 cursor-pointer">
                  Record as Customer Sale (contributes to sales velocity)
                </label>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Reason / Notes (Optional)
              </label>
              <input
                type="text"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder={isAdd ? "e.g. Received new stock delivery" : "e.g. Counter sale or damage"}
                className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-xs focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
              />
            </div>

            <div className="pt-2 flex items-center space-x-3">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 px-4 py-2.5 rounded-xl border border-slate-200 text-slate-700 text-xs font-semibold hover:bg-slate-50 transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading || !activeProduct}
                className={`flex-1 px-4 py-2.5 rounded-xl text-white text-xs font-bold shadow-md transition-all active:scale-95 disabled:opacity-50 cursor-pointer ${
                  isAdd ? 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20' : 'bg-rose-600 hover:bg-rose-700 shadow-rose-600/20'
                }`}
              >
                {loading ? 'Updating...' : isAdd ? 'Confirm Add Stock' : 'Confirm Remove Stock'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

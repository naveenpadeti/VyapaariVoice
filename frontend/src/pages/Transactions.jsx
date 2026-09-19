import React, { useState, useEffect } from 'react';
import { transactionApi, productApi } from '../services/api';
import { ReceiptText, Mic, Search, Filter } from 'lucide-react';

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState('');
  const [selectedType, setSelectedType] = useState('');
  const [loading, setLoading] = useState(true);

  const loadTransactions = async () => {
    try {
      const [txRes, prodRes] = await Promise.all([
        transactionApi.getAll({
          productId: selectedProduct || undefined,
          limit: 100
        }),
        productApi.getAll()
      ]);

      if (txRes.success && txRes.data) {
        setTransactions(txRes.data);
      }
      if (prodRes.success && prodRes.data) {
        setProducts(prodRes.data);
      }
    } catch (err) {
      console.error('Failed to load transactions', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTransactions();
  }, [selectedProduct]);

  const filtered = transactions.filter(t => {
    if (selectedType && t.type !== selectedType) return false;
    return true;
  });

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
            <ReceiptText className="w-6 h-6 text-amber-500" />
            <span>Stock Movement Ledger</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Complete immutable transaction history of incoming stock, sales, and voice actions
          </p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
          <select
            value={selectedProduct}
            onChange={(e) => setSelectedProduct(e.target.value)}
            className="bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-medium focus:outline-hidden"
          >
            <option value="">All Products</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>

          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-medium focus:outline-hidden"
          >
            <option value="">All Movement Types</option>
            <option value="SALE">SALE</option>
            <option value="ADD">ADD</option>
            <option value="REMOVE">REMOVE</option>
          </select>
        </div>

        <span className="text-xs text-slate-400 font-semibold">
          Showing {filtered.length} transactions
        </span>
      </div>

      {/* Ledger Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="bg-slate-50/70 border-b border-slate-200 text-slate-500 uppercase tracking-wider text-[10px]">
                <th className="px-6 py-3.5 font-bold">Date & Time</th>
                <th className="px-6 py-3.5 font-bold">Product</th>
                <th className="px-6 py-3.5 font-bold">Movement Type</th>
                <th className="px-6 py-3.5 font-bold">Quantity</th>
                <th className="px-6 py-3.5 font-bold">Channel / Source</th>
                <th className="px-6 py-3.5 font-bold">Reference / Reason</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-slate-400">Loading ledger...</td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-slate-400">No transactions recorded.</td>
                </tr>
              ) : (
                filtered.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4 text-slate-600 font-medium">
                      {new Date(tx.createdAt).toLocaleDateString()} {new Date(tx.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td className="px-6 py-4 font-bold text-slate-900">
                      {tx.productName}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full font-bold text-[10px] uppercase tracking-wide ${
                        tx.type === 'SALE'
                          ? 'bg-rose-100 text-rose-800'
                          : tx.type === 'ADD'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-800'
                      }`}>
                        {tx.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-extrabold text-slate-900 text-sm">
                      {tx.type === 'SALE' ? `-${tx.quantity}` : `+${tx.quantity}`} {tx.unit}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-lg text-xs font-bold ${
                        tx.source === 'VOICE'
                          ? 'bg-amber-100 text-amber-900'
                          : 'bg-slate-100 text-slate-700'
                      }`}>
                        {tx.source === 'VOICE' && <Mic className="w-3.5 h-3.5 text-amber-600 animate-pulse" />}
                        <span>{tx.source}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-500 italic max-w-xs truncate">
                      {tx.reference || '—'}
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

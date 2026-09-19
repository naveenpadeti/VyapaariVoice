import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { inventoryApi, categoryApi } from '../services/api';
import StockAdjustmentModal from '../components/StockAdjustmentModal';
import { Boxes, Plus, Minus, Search, Filter, ExternalLink } from 'lucide-react';

export default function Inventory() {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const [modal, setModal] = useState({
    isOpen: false,
    mode: 'ADD',
    product: null
  });

  const loadData = async () => {
    try {
      const [invRes, catRes] = await Promise.all([
        inventoryApi.getAll({
          categoryId: selectedCategory || undefined,
          status: statusFilter !== 'ALL' ? statusFilter : undefined
        }),
        categoryApi.getAll()
      ]);

      if (invRes.success && invRes.data) {
        setItems(invRes.data);
      }
      if (catRes.success && catRes.data) {
        setCategories(catRes.data);
      }
    } catch (err) {
      console.error('Failed to load inventory', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedCategory, statusFilter]);

  const openModal = (mode, item) => {
    setModal({
      isOpen: true,
      mode,
      product: {
        id: item.productId,
        name: item.productName,
        unit: item.unit,
        currentStock: item.currentQuantity
      }
    });
  };

  const filteredItems = items.filter(item => {
    if (!search) return true;
    return item.productName.toLowerCase().includes(search.toLowerCase());
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
            <Boxes className="w-6 h-6 text-amber-500" />
            <span>Stock Inventory</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage real-time inventory levels, units, and stock movements
          </p>
        </div>

        <Link
          to="/products"
          className="inline-flex items-center space-x-2 bg-slate-900 hover:bg-slate-800 text-white px-4 py-2.5 rounded-xl text-xs font-bold shadow-xs self-start"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add New Product</span>
        </Link>
      </div>

      {/* Filters Bar */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        {/* Search */}
        <div className="relative w-full md:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute inset-y-0 left-3 my-auto pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search inventory..."
            className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
          />
        </div>

        {/* Category & Status Filters */}
        <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-medium focus:outline-hidden"
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>

          <div className="flex items-center bg-slate-100 p-1 rounded-xl text-xs font-semibold">
            {['ALL', 'LOW_STOCK', 'OUT_OF_STOCK', 'IN_STOCK'].map((st) => (
              <button
                key={st}
                onClick={() => setStatusFilter(st)}
                className={`px-2.5 py-1 rounded-lg transition-all ${
                  statusFilter === st
                    ? 'bg-white text-amber-800 shadow-xs font-bold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {st.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Inventory Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="bg-slate-50/70 border-b border-slate-200 text-slate-500 uppercase tracking-wider text-[10px]">
                <th className="px-6 py-3.5 font-bold">Product</th>
                <th className="px-6 py-3.5 font-bold">Category</th>
                <th className="px-6 py-3.5 font-bold">Current Stock</th>
                <th className="px-6 py-3.5 font-bold">Reorder Level</th>
                <th className="px-6 py-3.5 font-bold">Target Stock</th>
                <th className="px-6 py-3.5 font-bold">Status</th>
                <th className="px-6 py-3.5 font-bold text-right">Stock Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-12 text-center text-slate-400">Loading inventory...</td>
                </tr>
              ) : filteredItems.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-12 text-center text-slate-400">No inventory records found.</td>
                </tr>
              ) : (
                filteredItems.map((item) => (
                  <tr key={item.productId} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4">
                      <Link
                        to={`/products/${item.productId}`}
                        className="font-bold text-slate-900 hover:text-amber-600 flex items-center space-x-1.5"
                      >
                        <span>{item.productName}</span>
                        <ExternalLink className="w-3 h-3 text-slate-400" />
                      </Link>
                    </td>
                    <td className="px-6 py-4 text-slate-500 font-medium">
                      {item.categoryName}
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-sm font-extrabold text-slate-900">
                        {item.currentQuantity}
                      </span>
                      <span className="ml-1 text-[11px] text-slate-500">{item.unit}</span>
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {item.reorderLevel} {item.unit}
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {item.targetStock} {item.unit}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full font-bold text-[10px] uppercase tracking-wide ${
                        item.stockStatus === 'OUT_OF_STOCK'
                          ? 'bg-red-100 text-red-800'
                          : item.stockStatus === 'LOW_STOCK'
                          ? 'bg-rose-100 text-rose-800'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}>
                        {item.stockStatus.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => openModal('ADD', item)}
                          className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100 font-bold text-xs transition-colors"
                          title="Add Stock"
                        >
                          <Plus className="w-3.5 h-3.5" />
                          <span>Add</span>
                        </button>
                        <button
                          onClick={() => openModal('REMOVE', item)}
                          className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-rose-50 text-rose-700 hover:bg-rose-100 font-bold text-xs transition-colors"
                          title="Remove / Sell Stock"
                        >
                          <Minus className="w-3.5 h-3.5" />
                          <span>Sell</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stock Adjustment Modal */}
      <StockAdjustmentModal
        isOpen={modal.isOpen}
        mode={modal.mode}
        product={modal.product}
        onClose={() => setModal({ ...modal, isOpen: false })}
        onSuccess={loadData}
      />
    </div>
  );
}

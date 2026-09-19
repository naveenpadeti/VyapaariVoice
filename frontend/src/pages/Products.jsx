import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productApi, categoryApi } from '../services/api';
import { Package, Plus, Search, Trash2, Edit, AlertCircle, CheckCircle2 } from 'lucide-react';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [loading, setLoading] = useState(true);

  // Add/Edit Product Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    unit: 'bags',
    description: '',
    initialQuantity: 0,
    minimumStock: 10,
    reorderLevel: 15,
    targetStock: 50
  });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const loadProducts = async () => {
    try {
      const [prodRes, catRes] = await Promise.all([
        productApi.getAll({
          categoryId: selectedCategory || undefined,
          search: search || undefined
        }),
        categoryApi.getAll()
      ]);

      if (prodRes.success && prodRes.data) {
        setProducts(prodRes.data);
      }
      if (catRes.success && catRes.data) {
        setCategories(catRes.data);
        if (!formData.categoryId && catRes.data.length > 0) {
          setFormData(prev => ({ ...prev, categoryId: catRes.data[0].id }));
        }
      }
    } catch (err) {
      console.error('Failed to load products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, [selectedCategory, search]);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData({
      name: '',
      categoryId: categories[0]?.id || '',
      unit: 'bags',
      description: '',
      initialQuantity: 0,
      minimumStock: 10,
      reorderLevel: 15,
      targetStock: 50
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (p) => {
    setEditingId(p.id);
    setFormData({
      name: p.name,
      categoryId: p.categoryId,
      unit: p.unit,
      description: p.description || '',
      initialQuantity: p.currentStock || 0,
      minimumStock: p.minimumStock || 0,
      reorderLevel: p.reorderLevel || 0,
      targetStock: p.targetStock || 0
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete ${name}? This will also delete its inventory records.`)) {
      return;
    }
    try {
      await productApi.delete(id);
      loadProducts();
    } catch (err) {
      alert(err.message || 'Failed to delete product');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSaving(true);
    try {
      if (editingId) {
        await productApi.update(editingId, {
          categoryId: formData.categoryId,
          name: formData.name,
          description: formData.description,
          unit: formData.unit,
          minimumStock: formData.minimumStock,
          reorderLevel: formData.reorderLevel,
          targetStock: formData.targetStock
        });
      } else {
        await productApi.create(formData);
      }
      setIsModalOpen(false);
      loadProducts();
    } catch (err) {
      setFormError(err.message || 'Error saving product');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
            <Package className="w-6 h-6 text-amber-500" />
            <span>Product Catalog</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Create and maintain product details, units, reorder levels, and target stock
          </p>
        </div>

        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center space-x-2 bg-gradient-to-r from-amber-500 to-brand-600 hover:from-amber-600 hover:to-brand-700 text-white px-4 py-2.5 rounded-xl text-xs font-bold shadow-md shadow-amber-500/20 active:scale-95 transition-all self-start"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add Product</span>
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-2xl p-4 border border-slate-200/80 shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute inset-y-0 left-3 my-auto pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search products by name or details..."
            className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
          />
        </div>

        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="w-full md:w-auto bg-slate-50 border border-slate-200 rounded-xl px-4 py-2 text-xs font-medium focus:outline-hidden"
        >
          <option value="">All Categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>

      {/* Products Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {loading ? (
          <div className="col-span-full py-12 text-center text-slate-400 text-xs">Loading products...</div>
        ) : products.length === 0 ? (
          <div className="col-span-full py-12 text-center text-slate-400 text-xs">No products found.</div>
        ) : (
          products.map((p) => (
            <div
              key={p.id}
              className="bg-white rounded-3xl border border-slate-200/80 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 uppercase tracking-wide">
                    {p.categoryName}
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                    p.stockStatus === 'OUT_OF_STOCK'
                      ? 'bg-rose-100 text-rose-800'
                      : p.stockStatus === 'LOW_STOCK'
                      ? 'bg-amber-100 text-amber-800'
                      : 'bg-emerald-100 text-emerald-800'
                  }`}>
                    {p.stockStatus?.replace('_', ' ')}
                  </span>
                </div>

                <Link to={`/products/${p.id}`} className="block group">
                  <h3 className="font-bold text-slate-900 text-base group-hover:text-amber-600 transition-colors">
                    {p.name}
                  </h3>
                  {p.description && (
                    <p className="text-xs text-slate-500 mt-1 line-clamp-2">
                      {p.description}
                    </p>
                  )}
                </Link>

                {/* Stock KPI pill */}
                <div className="mt-4 bg-slate-50 rounded-2xl p-3 grid grid-cols-3 gap-1 text-center">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Current</span>
                    <span className="text-sm font-extrabold text-slate-900">{p.currentStock}</span>
                    <span className="text-[10px] text-slate-500 block">{p.unit}</span>
                  </div>
                  <div className="border-x border-slate-200/60">
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Reorder</span>
                    <span className="text-sm font-bold text-slate-700">{p.reorderLevel}</span>
                    <span className="text-[10px] text-slate-500 block">{p.unit}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase font-bold block">Target</span>
                    <span className="text-sm font-bold text-slate-700">{p.targetStock}</span>
                    <span className="text-[10px] text-slate-500 block">{p.unit}</span>
                  </div>
                </div>
              </div>

              {/* Card Footer Actions */}
              <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between">
                <Link
                  to={`/products/${p.id}`}
                  className="text-xs font-bold text-amber-700 hover:text-amber-800"
                >
                  Intelligence &rarr;
                </Link>
                <div className="flex items-center space-x-1">
                  <button
                    onClick={() => handleOpenEdit(p)}
                    className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors"
                    title="Edit Product"
                  >
                    <Edit className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleDelete(p.id, p.name)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 transition-colors"
                    title="Delete Product"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Add / Edit Product Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-100 w-full max-w-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
              <h3 className="font-bold text-base text-slate-900">
                {editingId ? 'Edit Product' : 'Add New Product'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                &times;
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {formError && (
                <div className="bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-start space-x-2">
                  <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
                  <span>{formError}</span>
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Product Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="e.g. Rice, Sugar, Cooking Oil"
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Category *
                  </label>
                  <select
                    required
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-medium focus:outline-hidden"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Trade Unit *
                  </label>
                  <select
                    value={formData.unit}
                    onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-medium focus:outline-hidden"
                  >
                    <option value="bags">bags (e.g. Rice bag)</option>
                    <option value="kg">kg (Kilograms)</option>
                    <option value="boxes">boxes</option>
                    <option value="packets">packets</option>
                    <option value="bottles">bottles</option>
                    <option value="cartons">cartons</option>
                    <option value="pieces">pieces / pcs</option>
                    <option value="liters">liters</option>
                    <option value="quintal">quintal</option>
                  </select>
                </div>

                {!editingId && (
                  <div className="sm:col-span-2">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                      Initial Quantity in Stock
                    </label>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      value={formData.initialQuantity}
                      onChange={(e) => setFormData({ ...formData, initialQuantity: e.target.value })}
                      className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                    />
                  </div>
                )}

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Reorder Level ({formData.unit})
                  </label>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    value={formData.reorderLevel}
                    onChange={(e) => setFormData({ ...formData, reorderLevel: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                  <span className="text-[10px] text-slate-400">Triggers low stock alert</span>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Target Stock ({formData.unit})
                  </label>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    value={formData.targetStock}
                    onChange={(e) => setFormData({ ...formData, targetStock: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                  <span className="text-[10px] text-slate-400">Calculates refill quantity</span>
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Description / Pack Details (Optional)
                  </label>
                  <textarea
                    rows={2}
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="e.g. Premium quality Sona Masoori 25kg bag"
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                  />
                </div>
              </div>

              <div className="pt-3 flex items-center space-x-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-xs font-bold shadow-md shadow-amber-500/20 active:scale-95 disabled:opacity-50"
                >
                  {saving ? 'Saving...' : editingId ? 'Update Product' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

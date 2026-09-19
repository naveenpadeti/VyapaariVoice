import React, { useState, useEffect } from 'react';
import { categoryApi } from '../services/api';
import { Layers, Plus, Trash2, Edit, AlertCircle, Package } from 'lucide-react';

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal
  const [isOpen, setIsOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const loadCategories = async () => {
    try {
      const res = await categoryApi.getAll();
      if (res.success && res.data) {
        setCategories(res.data);
      }
    } catch (err) {
      console.error('Failed to load categories', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const handleOpenCreate = () => {
    setEditingCategory(null);
    setName('');
    setError('');
    setIsOpen(true);
  };

  const handleOpenEdit = (c) => {
    setEditingCategory(c);
    setName(c.name);
    setError('');
    setIsOpen(true);
  };

  const handleDelete = async (id, catName) => {
    if (!window.confirm(`Delete category "${catName}"?`)) return;
    try {
      await categoryApi.delete(id);
      loadCategories();
    } catch (err) {
      alert(err.message || 'Failed to delete category');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setError('');
    setSaving(true);
    try {
      if (editingCategory) {
        await categoryApi.update(editingCategory.id, { name: name.trim() });
      } else {
        await categoryApi.create({ name: name.trim() });
      }
      setIsOpen(false);
      loadCategories();
    } catch (err) {
      setError(err.message || 'Error saving category');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center space-x-2.5">
            <Layers className="w-6 h-6 text-amber-500" />
            <span>Product Categories</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Organize inventory by commodity and product lines
          </p>
        </div>

        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center space-x-2 bg-gradient-to-r from-amber-500 to-brand-600 hover:from-amber-600 hover:to-brand-700 text-white px-4 py-2.5 rounded-xl text-xs font-bold shadow-md shadow-amber-500/20 active:scale-95 transition-all self-start"
        >
          <Plus className="w-4 h-4" />
          <span>+ Add Category</span>
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
        {loading ? (
          <div className="col-span-full py-12 text-center text-slate-400 text-xs">Loading categories...</div>
        ) : categories.length === 0 ? (
          <div className="col-span-full py-12 text-center text-slate-400 text-xs">No categories found.</div>
        ) : (
          categories.map((c) => (
            <div
              key={c.id}
              className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs flex items-center justify-between"
            >
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-900 text-sm">{c.name}</h3>
                  <span className="text-[11px] text-slate-400">Inventory Group</span>
                </div>
              </div>

              <div className="flex items-center space-x-1">
                <button
                  onClick={() => handleOpenEdit(c)}
                  className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors"
                  title="Edit Category"
                >
                  <Edit className="w-3.5 h-3.5" />
                </button>
                <button
                  onClick={() => handleDelete(c.id, c.name)}
                  className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 transition-colors"
                  title="Delete Category"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal */}
      {isOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-xl border border-slate-100 w-full max-w-sm overflow-hidden p-6">
            <h3 className="font-bold text-base text-slate-900 mb-3">
              {editingCategory ? 'Edit Category' : 'Create Category'}
            </h3>

            {error && (
              <div className="mb-4 bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-xl p-3 flex items-start space-x-2">
                <AlertCircle className="w-4 h-4 text-rose-500 flex-shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Category Name
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Grocery, Spices, Dairy"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3.5 py-2 text-sm focus:outline-hidden focus:ring-2 focus:ring-amber-500/30 focus:bg-white"
                />
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="flex-1 py-2 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-xs font-bold shadow-sm disabled:opacity-50"
                >
                  {saving ? 'Saving...' : 'Save Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

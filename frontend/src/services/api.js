import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('vyapaarivoice_token') || localStorage.getItem('vaanistock_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response interceptor: handle 401
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 && window.location.pathname !== '/login' && window.location.pathname !== '/signup') {
      localStorage.removeItem('vyapaarivoice_token');
      localStorage.removeItem('vaanistock_token');
      localStorage.removeItem('vyapaarivoice_user');
      localStorage.removeItem('vaanistock_user');
      localStorage.removeItem('vyapaarivoice_lang');
      localStorage.removeItem('vaanistock_lang');
      window.location.href = '/login';
    }
    const message = error.response?.data?.message || error.message || 'Something went wrong';
    return Promise.reject(new Error(message));
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  signup: (data) => api.post('/auth/signup', data),
  demoLogin: () => api.post('/auth/demo-login'),
  getMe: () => api.get('/auth/me'),
};

export const profileApi = {
  get: () => api.get('/profile'),
  update: (data) => api.put('/profile', data),
};

export const categoryApi = {
  getAll: () => api.get('/categories'),
  get: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`),
};

export const productApi = {
  getAll: (params) => api.get('/products', { params }),
  get: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
};

export const inventoryApi = {
  getAll: (params) => api.get('/inventory', { params }),
  get: (productId) => api.get(`/inventory/${productId}`),
  addStock: (data) => api.post('/inventory/add', data),
  removeStock: (data) => api.post('/inventory/remove', data),
};

export const transactionApi = {
  getAll: (params) => api.get('/transactions', { params }),
  getByProduct: (productId, params) => api.get(`/transactions/${productId}`, { params }),
};

export const analyticsApi = {
  getSummary: () => api.get('/analytics/summary'),
  getSalesTrend: (range = '7days') => api.get('/analytics/sales-trend', { params: { range } }),
  getTopSelling: (limit = 5) => api.get('/analytics/top-selling', { params: { limit } }),
  getFastMoving: () => api.get('/analytics/fast-moving'),
  getSlowMoving: () => api.get('/analytics/slow-moving'),
  getNoRecentSales: () => api.get('/analytics/no-recent-sales'),
  getLowStock: () => api.get('/analytics/low-stock'),
};

export const recommendationApi = {
  getReorders: () => api.get('/recommendations/reorder'),
  getIntelligence: () => api.get('/recommendations/intelligence'),
  getProductIntelligence: (productId) => api.get(`/recommendations/intelligence/${productId}`),
};

export const alertApi = {
  getAll: () => api.get('/alerts'),
  dismiss: (productId) => api.post(`/alerts/${productId}/dismiss`),
  reset: () => api.post('/alerts/reset'),
};

export const voiceApi = {
  process: (data) => api.post('/voice/process', data),
  getHistory: () => api.get('/voice/history'),
};

export const dashboardApi = {
  getOverview: () => api.get('/dashboard/overview'),
};

export default api;

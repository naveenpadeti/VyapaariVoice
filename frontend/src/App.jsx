import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import BottomNav from './components/BottomNav';
import GlobalVoiceButton from './components/GlobalVoiceButton';
import VoiceModal from './components/VoiceModal';

import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import Products from './pages/Products';
import ProductDetails from './pages/ProductDetails';
import Categories from './pages/Categories';
import Transactions from './pages/Transactions';
import Analytics from './pages/Analytics';
import Alerts from './pages/Alerts';
import VoiceAssistant from './pages/VoiceAssistant';
import Profile from './pages/Profile';

const ProtectedLayout = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="w-10 h-10 border-4 border-amber-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar />
      <div className="flex flex-1 max-w-7xl w-full mx-auto">
        <Sidebar />
        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-full overflow-x-hidden">
          {children}
        </main>
      </div>
      <BottomNav />
      <GlobalVoiceButton />
      <VoiceModal />
    </div>
  );
};

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />

      {/* Protected Routes */}
      <Route
        path="/"
        element={
          <ProtectedLayout>
            <Dashboard />
          </ProtectedLayout>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedLayout>
            <Dashboard />
          </ProtectedLayout>
        }
      />
      <Route
        path="/inventory"
        element={
          <ProtectedLayout>
            <Inventory />
          </ProtectedLayout>
        }
      />
      <Route
        path="/products"
        element={
          <ProtectedLayout>
            <Products />
          </ProtectedLayout>
        }
      />
      <Route
        path="/products/:id"
        element={
          <ProtectedLayout>
            <ProductDetails />
          </ProtectedLayout>
        }
      />
      <Route
        path="/categories"
        element={
          <ProtectedLayout>
            <Categories />
          </ProtectedLayout>
        }
      />
      <Route
        path="/transactions"
        element={
          <ProtectedLayout>
            <Transactions />
          </ProtectedLayout>
        }
      />
      <Route
        path="/analytics"
        element={
          <ProtectedLayout>
            <Analytics />
          </ProtectedLayout>
        }
      />
      <Route
        path="/alerts"
        element={
          <ProtectedLayout>
            <Alerts />
          </ProtectedLayout>
        }
      />
      <Route
        path="/voice"
        element={
          <ProtectedLayout>
            <VoiceAssistant />
          </ProtectedLayout>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedLayout>
            <Profile />
          </ProtectedLayout>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

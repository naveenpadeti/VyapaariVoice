import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, profileApi } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [business, setBusiness] = useState(null);
  const getStoredToken = () => localStorage.getItem('vyapaarivoice_token') || localStorage.getItem('vaanistock_token');
  const getStoredLang = () => localStorage.getItem('vyapaarivoice_lang') || localStorage.getItem('vaanistock_lang') || 'te';

  const [token, setToken] = useState(getStoredToken);
  const [loading, setLoading] = useState(true);
  const [language, setLanguage] = useState(getStoredLang);

  useEffect(() => {
    const initAuth = async () => {
      const savedToken = getStoredToken();
      if (savedToken) {
        try {
          const res = await authApi.getMe();
          if (res.success && res.data) {
            setUser(res.data.user);
            setBusiness(res.data.business);
            if (res.data.user?.preferredLanguage) {
              setLanguage(res.data.user.preferredLanguage);
              localStorage.setItem('vyapaarivoice_lang', res.data.user.preferredLanguage);
            }
          }
        } catch (err) {
          console.error('Failed to load current user', err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const handleAuthSuccess = (data) => {
    localStorage.setItem('vyapaarivoice_token', data.token);
    localStorage.removeItem('vaanistock_token');
    setToken(data.token);
    setUser(data.user);
    setBusiness(data.business);
    if (data.user?.preferredLanguage) {
      setLanguage(data.user.preferredLanguage);
      localStorage.setItem('vyapaarivoice_lang', data.user.preferredLanguage);
      localStorage.removeItem('vaanistock_lang');
    }
  };

  const login = async (username, password) => {
    const res = await authApi.login({ username, password });
    if (res.success && res.data) {
      handleAuthSuccess(res.data);
      return res.data;
    }
    throw new Error(res.message || 'Login failed');
  };

  const demoLogin = async () => {
    const res = await authApi.demoLogin();
    if (res.success && res.data) {
      handleAuthSuccess(res.data);
      return res.data;
    }
    throw new Error(res.message || 'Demo login failed');
  };

  const signup = async (formData) => {
    const res = await authApi.signup(formData);
    if (res.success && res.data) {
      handleAuthSuccess(res.data);
      return res.data;
    }
    throw new Error(res.message || 'Registration failed');
  };

  const logout = () => {
    localStorage.removeItem('vyapaarivoice_token');
    localStorage.removeItem('vaanistock_token');
    localStorage.removeItem('vyapaarivoice_user');
    localStorage.removeItem('vaanistock_user');
    localStorage.removeItem('vyapaarivoice_lang');
    localStorage.removeItem('vaanistock_lang');
    setToken(null);
    setUser(null);
    setBusiness(null);
  };

  const changeLanguage = async (newLang) => {
    setLanguage(newLang);
    localStorage.setItem('vyapaarivoice_lang', newLang);
    if (user && business) {
      try {
        await profileApi.update({
          name: user.name,
          preferredLanguage: newLang,
          businessName: business.businessName,
          location: business.location
        });
      } catch (e) {
        console.warn('Could not sync language to backend', e);
      }
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      business,
      token,
      loading,
      language,
      setLanguage: changeLanguage,
      login,
      demoLogin,
      signup,
      logout,
      isAuthenticated: !!token && !!user
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

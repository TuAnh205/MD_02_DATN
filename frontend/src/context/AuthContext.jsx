import React, { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const savedToken = localStorage.getItem('token');
    
    if (savedUser && savedToken) {
      setUser(JSON.parse(savedUser));
      setToken(savedToken);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await authService.login(email, password);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const googleLogin = async (idToken) => {
    const response = await authService.googleLogin(idToken);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const register = async (name, email, password) => {
    const response = await authService.register(name, email, password);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const registerShop = async (name, email, password) => {
    const response = await authService.registerShop(name, email, password);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const sendGoogleRegistrationCode = async (idToken, role = 'user') => {
    return authService.sendGoogleRegistrationCode(idToken, role);
  };

  const verifyGoogleRegistrationCode = async (email, code) => {
    const response = await authService.verifyGoogleRegistrationCode(email, code);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const sendVerificationCode = async (name, email, password, role = 'user', phone = '') => {
    return authService.sendVerificationCode(name, email, password, role, phone);
  };

  const verifyEmailCode = async (email, code) => {
    const response = await authService.verifyEmailCode(email, code);
    setUser(response.user);
    setToken(response.token);
    return response;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  const updateProfile = async (data) => {
    const response = await authService.updateProfile(data);
    setUser(response.user);
    return response;
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, googleLogin, register, registerShop, sendGoogleRegistrationCode, verifyGoogleRegistrationCode, sendVerificationCode, verifyEmailCode, logout, updateProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

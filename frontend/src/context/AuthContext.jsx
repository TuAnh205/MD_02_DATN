import React, { createContext, useContext, useState, useEffect } from "react";
import authService from "../services/authService";
import api from "../services/api";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem("user");
    const savedToken = localStorage.getItem("token");

    if (savedUser && savedToken) {
      setUser(JSON.parse(savedUser));
      setToken(savedToken);
      // Ensure axios has Authorization header for immediate requests
      api.defaults.headers.common.Authorization = `Bearer ${savedToken}`;
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await authService.login(email, password);
    setUser(response.user);
    setToken(response.token);
    // sync axios default header immediately to avoid race on next requests
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const googleLogin = async (idToken) => {
    const response = await authService.googleLogin(idToken);
    setUser(response.user);
    setToken(response.token);
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const register = async (name, email, password) => {
    const response = await authService.register(name, email, password);
    setUser(response.user);
    setToken(response.token);
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const registerShop = async (name, email, password) => {
    const response = await authService.registerShop(name, email, password);
    setUser(response.user);
    setToken(response.token);
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const sendGoogleRegistrationCode = async (idToken, role = "user") => {
    return authService.sendGoogleRegistrationCode(idToken, role);
  };

  const verifyGoogleRegistrationCode = async (email, code) => {
    const response = await authService.verifyGoogleRegistrationCode(
      email,
      code,
    );
    setUser(response.user);
    setToken(response.token);
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const sendVerificationCode = async (
    name,
    email,
    password,
    role = "user",
    phone = "",
  ) => {
    return authService.sendVerificationCode(name, email, password, role, phone);
  };

  const verifyEmailCode = async (email, code) => {
    const response = await authService.verifyEmailCode(email, code);
    setUser(response.user);
    setToken(response.token);
    if (response.token) api.defaults.headers.common.Authorization = `Bearer ${response.token}`;
    return response;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
    // remove axios default header
    delete api.defaults.headers.common.Authorization;
  };

  const fetchProfile = async () => {
    const profile = await authService.getProfile();
    setUser(profile);
    localStorage.setItem("user", JSON.stringify(profile));
    return profile;
  };

  const updateProfile = async (data) => {
    const response = await authService.updateProfile(data);
    if (response.user) {
      setUser(response.user);
      localStorage.setItem("user", JSON.stringify(response.user));
    }
    return response;
  };

  const setPassword = async (password) => {
    return authService.setPassword(password);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        googleLogin,
        register,
        registerShop,
        sendGoogleRegistrationCode,
        verifyGoogleRegistrationCode,
        sendVerificationCode,
        verifyEmailCode,
        logout,
        fetchProfile,
        updateProfile,
        setPassword,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};

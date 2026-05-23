import api from "./api";

const authService = {
  register: async (name, email, password) => {
    const response = await api.post("/auth/register", {
      name,
      email,
      password,
    });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  registerShop: async (name, email, password) => {
    const response = await api.post("/auth/register-shop", {
      name,
      email,
      password,
    });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  sendGoogleRegistrationCode: async (idToken, role = "user") => {
    const response = await api.post("/auth/send-google-registration-code", {
      idToken,
      role,
    });
    return response.data;
  },

  verifyGoogleRegistrationCode: async (email, code) => {
    const response = await api.post("/auth/verify-google-registration-code", {
      email,
      code,
    });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  login: async (email, password) => {
    const response = await api.post("/auth/login", { email, password });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  googleLogin: async (idToken) => {
    const response = await api.post("/auth/google-login", { idToken });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  forgotPassword: async (email) => {
    const response = await api.post("/auth/forgot-password", { email });
    return response.data;
  },

  resetPassword: async (email, token, password) => {
    const response = await api.post("/auth/reset-password", { email, token, password });
    return response.data;
  },

  sendVerificationCode: async (
    name,
    email,
    password,
    role = "user",
    phone = "",
  ) => {
    const response = await api.post("/auth/send-verification-code", {
      name,
      email,
      password,
      role,
      phone,
    });
    return response.data;
  },

  verifyEmailCode: async (email, code) => {
    const response = await api.post("/auth/verify-email-code", { email, code });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    delete api.defaults.headers.common.Authorization;
  },

  getCurrentUser: () => {
    try {
      return JSON.parse(localStorage.getItem("user"));
    } catch {
      return null;
    }
  },

  getToken: () => localStorage.getItem("token"),

  getProfile: async () => {
    const response = await api.get("/auth/me");
    return response.data;
  },

  updateProfile: async (data) => {
    const response = await api.put("/auth/me", data);
    if (response.data.user) {
      localStorage.setItem("user", JSON.stringify(response.data.user));
    }
    return response.data;
  },

  setPassword: async (password) => {
    const response = await api.post("/auth/set-password", { password });
    return response.data;
  },

  changePassword: async (oldPassword, newPassword) => {
    const response = await api.put('/auth/change-password', { oldPassword, newPassword });
    return response.data;
  },

  firebaseSync: async (firebaseUid, email, name) => {
    const response = await api.post('/auth/firebase-sync', {
      firebaseUid,
      email,
      name,
    });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      api.defaults.headers.common.Authorization = `Bearer ${response.data.token}`;
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  },
};

export default authService;

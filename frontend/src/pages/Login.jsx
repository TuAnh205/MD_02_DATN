import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { signInWithGooglePopup, isFirebaseAuthConfigured } from '../services/firebaseAuth';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  
  const navigate = useNavigate();
  const { login, googleLogin, user, loading: authLoading } = useAuth();

  // Navigate sau khi user state được cập nhật (tránh blank page)
  useEffect(() => {
    if (!authLoading && user) {
      if (user.role === 'admin') navigate('/admin', { replace: true });
      else if (user.role === 'shop') navigate('/shop', { replace: true });
      else navigate('/', { replace: true });
    }
  }, [user, authLoading]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!email || !password) {
        setError('Vui lòng nhập email và mật khẩu');
        return;
      }

      await login(email, password);
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng nhập thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      setError('');
      setGoogleLoading(true);
      const { idToken } = await signInWithGooglePopup();
      await googleLogin(idToken);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Đăng nhập Google thất bại');
    } finally {
      setGoogleLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-secondary">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-dark mb-8">Đăng Nhập</h1>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark mb-2">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input-field"
              placeholder="your@email.com"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-dark mb-2">Mật Khẩu</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="input-field"
              placeholder="••••••••"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full btn-primary font-semibold disabled:opacity-50"
          >
            {loading ? 'Đang đăng nhập...' : 'Đăng Nhập'}
          </button>
        </form>

        <div className="my-6 flex items-center gap-3">
          <div className="h-px bg-gray-200 flex-1" />
          <span className="text-xs text-gray-500">hoặc</span>
          <div className="h-px bg-gray-200 flex-1" />
        </div>

        {isFirebaseAuthConfigured ? (
          <button
            type="button"
            onClick={handleGoogleLogin}
            disabled={googleLoading}
            className="w-full border border-gray-300 rounded-full px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition disabled:opacity-50"
          >
            {googleLoading ? 'Đang kết nối Google...' : 'Đăng nhập với Google'}
          </button>
        ) : (
          <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            Chưa bật Firebase Google Auth. Vui lòng cấu hình VITE_FIREBASE_* trong file .env của frontend.
          </div>
        )}

        <div className="mt-6 space-y-2 text-center">
          <p className="text-sm text-gray-600">
            Chưa có tài khoản?{' '}
            <Link to="/register/user" className="text-primary font-semibold hover:underline">
              Đăng Ký Người Mua
            </Link>
          </p>
          <p className="text-sm text-gray-600">
            Bạn là chủ shop?{' '}
            <Link to="/register/shop" className="text-secondary font-semibold hover:underline">
              Đăng Ký Tài Khoản Shop
            </Link>
          </p>
          <p>
            <Link to="/forgot-password" className="text-secondary text-sm hover:underline">
              Quên mật khẩu?
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { signInWithGooglePopup, isFirebaseAuthConfigured } from '../services/firebaseAuth';
import authService from '../services/authService';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [redirectPath, setRedirectPath] = useState(null);
  
  const navigate = useNavigate();
  const location = useLocation();
  const { login, googleLogin, user, loading: authLoading } = useAuth();

  useEffect(() => {
    if (!authLoading && user) {
      if (!redirectPath && location.pathname !== '/login') {
        return;
      }

      // Check if account is locked
      if (user.isLocked) {
        navigate('/account-locked', { replace: true });
        return;
      }

      const target =
        redirectPath ||
        (user.role === 'admin'
          ? '/admin'
          : user.role === 'shop'
          ? '/shop'
          : '/');

      navigate(target, { replace: true });
      setRedirectPath(null);
    }
  }, [authLoading, user, redirectPath, navigate, location.pathname]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!email || !password) {
        setError('Vui lòng nhập email và mật khẩu');
        return;
      }

      const response = await login(email, password);
      const currentUser = response?.user;
      if (currentUser) {
        const target =
          currentUser.role === 'admin'
            ? '/admin'
            : currentUser.role === 'shop'
            ? '/shop'
            : '/';
        setRedirectPath(target);
      }
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
      const { idToken, uid, email, name } = await signInWithGooglePopup();
      let response;
      try {
        response = await googleLogin(idToken);
      } catch (err) {
        // If account doesn't exist on server, attempt firebase-sync to create user
        const msg = err.response?.data?.message || '';
        if (msg && msg.toLowerCase().includes('tai khoan chua ton tai')) {
          const syncRes = await authService.firebaseSync(uid, email, name);
          response = syncRes;
        } else {
          throw err;
        }
      }
      const currentUser = response?.user;
      if (currentUser) {
        const target =
          currentUser.role === 'admin'
            ? '/admin'
            : currentUser.role === 'shop'
            ? '/shop'
            : '/';
        setRedirectPath(target);
      }
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
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 flex items-start" role="alert" aria-live="assertive">
            <div className="flex-1">{error}</div>
            <button
              onClick={() => setError('')}
              aria-label="Đóng thông báo lỗi"
              className="ml-4 text-red-700 font-bold hover:opacity-80"
            >
              ✕
            </button>
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

        {isFirebaseAuthConfigured ? (
          <>
            <div className="my-6 flex items-center gap-3">
              <div className="h-px bg-gray-200 flex-1" />
              <span className="text-xs text-gray-500">hoặc</span>
              <div className="h-px bg-gray-200 flex-1" />
            </div>

            <button
              type="button"
              onClick={handleGoogleLogin}
              disabled={googleLoading}
              className="w-full border border-gray-300 rounded-full px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition disabled:opacity-50"
            >
              {googleLoading ? 'Đang kết nối Google...' : 'Đăng nhập với Google'}
            </button>
          </>
        ) : (
          <div className="my-6 text-center">
            <div className="mb-3 text-sm text-yellow-700 bg-yellow-100 border border-yellow-200 p-3 rounded">
              Google Sign-in chưa được cấu hình. Copy file <strong>.env.example</strong> thành <strong>.env</strong> trong thư mục <strong>frontend</strong> và điền các biến <code>VITE_FIREBASE_API_KEY</code>, <code>VITE_FIREBASE_AUTH_DOMAIN</code>, <code>VITE_FIREBASE_PROJECT_ID</code>, <code>VITE_FIREBASE_APP_ID</code>, sau đó khởi động lại dev server.
            </div>
            <button
              type="button"
              disabled
              className="w-full border border-gray-300 rounded-full px-4 py-2 text-sm font-semibold text-gray-400 bg-gray-50 cursor-not-allowed"
            >
              Đăng nhập với Google (chưa cấu hình)
            </button>
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

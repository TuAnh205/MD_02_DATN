import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { signInWithGooglePopup, isFirebaseAuthConfigured } from '../services/firebaseAuth';

export default function Register({ accountType = 'user' }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [googleEmail, setGoogleEmail] = useState('');
  const [googleCode, setGoogleCode] = useState('');
  const [googleCodeSent, setGoogleCodeSent] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { register, registerShop, sendGoogleRegistrationCode, verifyGoogleRegistrationCode, user, loading: authLoading } = useAuth();
  const isShopAccount = accountType === 'shop';
  const canUseGoogleRegister = isFirebaseAuthConfigured;

  // Navigate sau khi user state được cập nhật (tránh blank page)
  useEffect(() => {
    if (!authLoading && user) {
      if (user.role === 'admin') navigate('/admin', { replace: true });
      else if (user.role === 'shop') navigate('/shop', { replace: true });
      else navigate('/', { replace: true });
    }
  }, [user, authLoading]);

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      setError('');
      setLoading(true);

      if (!name || !email || !password || !confirmPassword) {
        setError('Vui lòng nhập đầy đủ thông tin');
        return;
      }

      if (password.length < 6) {
        setError('Mật khẩu phải có ít nhất 6 ký tự');
        return;
      }

      if (password !== confirmPassword) {
        setError('Mật khẩu xác nhận không khớp');
        return;
      }

      await (isShopAccount
        ? registerShop(name.trim(), email.trim(), password)
        : register(name.trim(), email.trim(), password));
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignUp = async () => {
    try {
      setError('');
      setGoogleLoading(true);
      const { idToken } = await signInWithGooglePopup();
      const response = await sendGoogleRegistrationCode(idToken, isShopAccount ? 'shop' : 'user');
      setGoogleEmail(response?.email || '');
      setGoogleCodeSent(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể gửi mã xác nhận Google');
    } finally {
      setGoogleLoading(false);
    }
  };

  const handleGoogleVerify = async () => {
    try {
      if (!googleEmail || !googleCode.trim()) {
        setError('Vui lòng nhập mã xác nhận đã gửi về email Google');
        return;
      }

      setError('');
      setGoogleLoading(true);
      await verifyGoogleRegistrationCode(googleEmail, googleCode.trim());
    } catch (err) {
      setError(err.response?.data?.message || 'Xác nhận mã Google thất bại');
    } finally {
      setGoogleLoading(false);
    }
  };

  return (
    <div className={`min-h-screen flex items-center justify-center ${isShopAccount ? 'bg-gradient-to-br from-slate-900 via-slate-800 to-secondary' : 'bg-gradient-to-br from-primary to-secondary'}`}>
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-dark mb-8">
          {isShopAccount ? 'Đăng Ký Tài Khoản Shop' : 'Đăng Ký Người Dùng'}
        </h1>

        <p className="text-sm text-gray-600 text-center mb-6">
          {isShopAccount
            ? 'Tạo tài khoản nhà bán để quản lý sản phẩm, đơn hàng và doanh thu.'
            : 'Tạo tài khoản mua sắm để đặt hàng và theo dõi đơn hàng dễ dàng.'}
        </p>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark mb-2">Họ Tên</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="input-field"
              placeholder="Nguyễn Văn A"
              disabled={loading}
            />
          </div>

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

          <div>
            <label className="block text-sm font-medium text-dark mb-2">Xác Nhận Mật Khẩu</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
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
            {loading ? 'Đang đăng ký...' : isShopAccount ? 'Tạo Tài Khoản Shop' : 'Tạo Tài Khoản Người Dùng'}
          </button>
        </form>

        <div className="my-6 flex items-center gap-3">
          <div className="h-px bg-gray-200 flex-1" />
          <span className="text-xs text-gray-500">hoặc</span>
          <div className="h-px bg-gray-200 flex-1" />
        </div>

        <div className="space-y-3">
          <p className="text-sm font-medium text-dark">Đăng ký bằng Google</p>

          {canUseGoogleRegister ? (
            <button
              type="button"
              onClick={handleGoogleSignUp}
              disabled={googleLoading}
              className="w-full border border-gray-300 rounded-full px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition disabled:opacity-50"
            >
              {googleLoading ? 'Đang kết nối Google...' : 'Đăng ký với Google'}
            </button>
          ) : (
            <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              Chưa bật Firebase Google Auth. Vui lòng cấu hình VITE_FIREBASE_* trong file .env của frontend rồi khởi động lại Vite.
            </div>
          )}

          {googleCodeSent && (
            <div className="bg-slate-50 border border-slate-200 rounded-lg p-4 space-y-3">
              <p className="text-xs text-gray-600">
                Mã xác nhận đã gửi đến <span className="font-semibold">{googleEmail}</span>
              </p>
              <input
                type="text"
                value={googleCode}
                onChange={(e) => setGoogleCode(e.target.value)}
                placeholder="Nhập mã xác nhận 6 số"
                className="input-field"
                maxLength={6}
                disabled={googleLoading}
              />
              <button
                type="button"
                onClick={handleGoogleVerify}
                disabled={googleLoading}
                className="w-full btn-secondary font-semibold disabled:opacity-50"
              >
                {googleLoading ? 'Đang xác nhận...' : 'Xác nhận mã Google'}
              </button>
            </div>
          )}
        </div>

        <div className="mt-6 space-y-2 text-center">
          <p className="text-sm text-gray-600">
            Đã có tài khoản?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">
              Đăng Nhập
            </Link>
          </p>
          <p className="text-sm text-gray-600">
            {isShopAccount ? 'Đăng ký cho người dùng?' : 'Đăng ký cho shop?'}{' '}
            <Link
              to={isShopAccount ? '/register/user' : '/register/shop'}
              className="text-secondary font-semibold hover:underline"
            >
              {isShopAccount ? 'Đăng Ký Người Dùng' : 'Đăng Ký Shop'}
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

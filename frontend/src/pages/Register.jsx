import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register({ accountType = 'user' }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { register, registerShop } = useAuth();
  const isShopAccount = accountType === 'shop';

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

      if (isShopAccount) {
        await registerShop(name.trim(), email.trim(), password);
      } else {
        await register(name.trim(), email.trim(), password);
      }
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
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

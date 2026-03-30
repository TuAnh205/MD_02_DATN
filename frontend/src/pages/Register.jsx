import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('user');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { register, registerShop } = useAuth();

  React.useEffect(() => {
    const roleParam = searchParams.get('role');
    if (roleParam === 'shop') {
      setRole('shop');
    }
  }, [searchParams]);

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

      if (role === 'shop') {
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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-secondary">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-dark mb-8">
          {role === 'shop' ? 'Đăng Ký Tài Khoản Shop' : 'Đăng Ký Người Mua'}
        </h1>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark mb-2">Loại Tài Khoản</label>
            <div className="flex space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  value="user"
                  checked={role === 'user'}
                  onChange={(e) => setRole(e.target.value)}
                  className="mr-2"
                  disabled={loading}
                />
                Người Mua
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  value="shop"
                  checked={role === 'shop'}
                  onChange={(e) => setRole(e.target.value)}
                  className="mr-2"
                  disabled={loading}
                />
                Chủ Shop
              </label>
            </div>
          </div>

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
            {loading ? 'Đang đăng ký...' : role === 'shop' ? 'Đăng Ký Shop' : 'Đăng Ký Người Mua'}
          </button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Đã có tài khoản?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">
              Đăng Nhập
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

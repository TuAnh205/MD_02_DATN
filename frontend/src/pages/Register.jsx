import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [codeSent, setCodeSent] = useState(false);
  const [message, setMessage] = useState('');
  const [role, setRole] = useState('user');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { sendVerificationCode, verifyEmailCode } = useAuth();

  useEffect(() => {
    const roleParam = searchParams.get('role');
    if (roleParam === 'shop') {
      setRole('shop');
    }
  }, [searchParams]);

  const validateForm = () => {
    if (!name || !email || !password || !confirmPassword) {
      setError('Vui lòng nhập đầy đủ thông tin');
      return false;
    }

    if (!email.toLowerCase().endsWith('@gmail.com')) {
      setError('Vui lòng dùng tài khoản Gmail');
      return false;
    }

    if (password !== confirmPassword) {
      setError('Mật khẩu không khớp');
      return false;
    }

    if (password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return false;
    }

    return true;
  };

  const handleSendCode = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      await sendVerificationCode(name, email, password, role);
      setCodeSent(true);
      setMessage('Đã gửi mã xác nhận về Gmail của bạn.');
    } catch (err) {
      setError(err.response?.data?.message || 'Gửi mã xác nhận thất bại');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyAndRegister = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!verificationCode) {
      setError('Vui lòng nhập mã xác nhận');
      return;
    }

    setLoading(true);
    try {
      await verifyEmailCode(email, verificationCode);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Xác nhận mã thất bại');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-secondary">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-dark mb-8">Đăng Ký</h1>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {message && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            {message}
          </div>
        )}

        <form className="space-y-4">
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
                Người Mua Hàng
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
            type="button"
            onClick={handleSendCode}
            disabled={loading}
            className="w-full btn-primary font-semibold disabled:opacity-50"
          >
            {loading ? 'Đang gửi mã...' : 'Gửi Mã Xác Nhận Gmail'}
          </button>

          {codeSent && (
            <>
              <div>
                <label className="block text-sm font-medium text-dark mb-2">Mã Xác Nhận</label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  className="input-field"
                  placeholder="Nhập mã 6 số"
                  disabled={loading}
                />
              </div>

              <button
                type="button"
                onClick={handleVerifyAndRegister}
                disabled={loading}
                className="w-full btn-primary font-semibold disabled:opacity-50"
              >
                {loading ? 'Đang xác nhận...' : 'Xác Nhận & Đăng Ký'}
              </button>
            </>
          )}
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

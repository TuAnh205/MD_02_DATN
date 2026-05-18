import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!email) {
      setError('Vui lòng nhập tên tài khoản / email.');
      return;
    }

    try {
      setLoading(true);
      const response = await authService.forgotPassword(email.trim());
      const token = response?.resetPasswordToken;
      const emailValue = response?.email || email.trim();

      setSuccess('Tài khoản hợp lệ. Chuyển đến trang đổi mật khẩu...');
      navigate(
        `/reset-password?email=${encodeURIComponent(emailValue)}&token=${encodeURIComponent(token)}`,
        { replace: true },
      );
    } catch (err) {
      setError(err.response?.data?.message || 'Không tìm thấy tài khoản.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-secondary">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-dark mb-8">Quên Mật Khẩu</h1>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-dark mb-2">Tên tài khoản / Email</label>
            <input
              type="text"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input-field"
              placeholder="Nhập tên tài khoản hoặc email"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full btn-primary font-semibold disabled:opacity-50"
          >
            {loading ? 'Đang kiểm tra...' : 'Tiếp tục'}
          </button>
        </form>
      </div>
    </div>
  );
}

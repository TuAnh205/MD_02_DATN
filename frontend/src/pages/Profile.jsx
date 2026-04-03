import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

import { orderService } from '../services/orderService';
import { favoriteService } from '../services/favoriteService';

export default function Profile() {
  const { user, updateProfile } = useAuth();
  const [formData, setFormData] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    avatar: user?.avatar || ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [orderCount, setOrderCount] = useState('--');
  const [wishlistCount, setWishlistCount] = useState('--');

  useEffect(() => {
    async function fetchCounts() {
      try {
        const orders = await orderService.getOrders();
        setOrderCount(orders.length);
      } catch {
        setOrderCount('--');
      }
      try {
        const favorites = await favoriteService.listFavorites();
        setWishlistCount(favorites.length);
      } catch {
        setWishlistCount('--');
      }
    }
    fetchCounts();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      await updateProfile(formData);
      setMessage('Cập nhật thông tin thành công!');
    } catch (error) {
      setMessage('Có lỗi xảy ra khi cập nhật thông tin');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">Thông tin cá nhân</h1>

          <div className="flex gap-8 mb-6">
            <div className="flex flex-col items-center">
              <span className="text-lg font-semibold text-blue-700">{orderCount}</span>
              <span className="text-gray-500 text-sm">Đơn hàng</span>
            </div>
            <div className="flex flex-col items-center">
              <span className="text-lg font-semibold text-pink-600">{wishlistCount}</span>
              <span className="text-gray-500 text-sm">Đã yêu thích</span>
            </div>
          </div>

          {message && (
            <div className={`mb-4 p-4 rounded ${message.includes('thành công') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Họ và tên
              </label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                value={user?.email || ''}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
              <p className="text-sm text-gray-500 mt-1">Email không thể thay đổi</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Số điện thoại
              </label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Avatar URL
              </label>
              <input
                type="url"
                name="avatar"
                value={formData.avatar}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="https://..."
              />
            </div>

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Đang cập nhật...' : 'Cập nhật thông tin'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
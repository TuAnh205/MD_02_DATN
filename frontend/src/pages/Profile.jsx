import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

import { orderService } from '../services/orderService';
import { favoriteService } from '../services/favoriteService';
import { voucherService } from '../services/voucherService';

export default function Profile() {
  const { user, updateProfile, fetchProfile } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    avatar: user?.avatar || ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [orderCount, setOrderCount] = useState('--');
  const [wishlistCount, setWishlistCount] = useState('--');
  const [myVouchers, setMyVouchers] = useState([]);
  const [availableVouchers, setAvailableVouchers] = useState([]);
  const [voucherMessage, setVoucherMessage] = useState('');
  const [claimingVoucher, setClaimingVoucher] = useState(false);

  const fetchVouchers = async () => {
    try {
      const response = await voucherService.getMyVouchers();
      setMyVouchers(response.vouchers || []);
    } catch {
      setMyVouchers([]);
    }

    try {
      const response = await voucherService.getAvailableVouchers();
      setAvailableVouchers(response.vouchers || []);
    } catch {
      setAvailableVouchers([]);
    }
  };

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
    fetchVouchers();
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

  const handleClaimVoucher = async (code) => {
    setClaimingVoucher(true);
    setVoucherMessage('');

    try {
      const res = await voucherService.claimVoucher(code);
      setVoucherMessage('Nhận voucher thành công!');
      await fetchVouchers();
      try {
        await fetchProfile();
      } catch (e) {
        console.warn('fetchProfile failed after claiming voucher', e);
      }
      if (res && res.voucher) {
        setMyVouchers((prev) => [res.voucher, ...prev]);
        try {
          const raw = localStorage.getItem('user');
          if (raw) {
            const lu = JSON.parse(raw);
            lu.userVouchers = lu.userVouchers || [];
            lu.userVouchers.unshift({
              voucher: res.voucher._id,
              code: res.voucher.code,
              name: res.voucher.name,
              description: res.voucher.description,
              claimedAt: new Date().toISOString(),
              usedCount: 0
            });
            localStorage.setItem('user', JSON.stringify(lu));
            window.dispatchEvent(new Event('userChanged'));
          }
        } catch (e) {
          // ignore
        }
      }
    } catch (error) {
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        (typeof error.response?.data === 'string' ? error.response.data : null) ||
        error.message ||
        'Không thể nhận voucher';
      setVoucherMessage(errorMessage);
    } finally {
      setClaimingVoucher(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">Thông tin cá nhân</h1>

          <div className="flex gap-6 mb-6">
            <button
              onClick={() => navigate('/orders')}
              className="flex flex-col items-center p-3 rounded-lg hover:bg-blue-50 transition-colors group"
            >
              <span className="text-2xl font-bold text-blue-700 group-hover:text-blue-800">{orderCount}</span>
              <span className="text-gray-500 text-sm mt-1">📦 Đơn hàng</span>
            </button>
            <button
              onClick={() => navigate('/favorites')}
              className="flex flex-col items-center p-3 rounded-lg hover:bg-pink-50 transition-colors group"
            >
              <span className="text-2xl font-bold text-pink-600 group-hover:text-pink-700">{wishlistCount}</span>
              <span className="text-gray-500 text-sm mt-1">❤️ Yêu thích</span>
            </button>
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

          {/* Quick access */}
          <div className="mt-8 border-t pt-6">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">Truy cập nhanh</h2>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => navigate('/orders')}
                className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 text-left transition-colors"
              >
                <span className="text-xl">📦</span>
                <div>
                  <p className="font-medium text-gray-800 text-sm">Đơn hàng của tôi</p>
                  <p className="text-xs text-gray-500">Theo dõi đơn hàng</p>
                </div>
              </button>
              <button
                onClick={() => navigate('/favorites')}
                className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 text-left transition-colors"
              >
                <span className="text-xl">❤️</span>
                <div>
                  <p className="font-medium text-gray-800 text-sm">Sản phẩm yêu thích</p>
                  <p className="text-xs text-gray-500">Xem danh sách yêu thích</p>
                </div>
              </button>
              <button
                onClick={() => navigate('/products')}
                className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 text-left transition-colors"
              >
                <span className="text-xl">🛍️</span>
                <div>
                  <p className="font-medium text-gray-800 text-sm">Mua sắm</p>
                  <p className="text-xs text-gray-500">Khám phá sản phẩm</p>
                </div>
              </button>
              <button
                onClick={() => document.getElementById('voucher-section')?.scrollIntoView({ behavior: 'smooth' })}
                className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 hover:bg-gray-50 text-left transition-colors"
              >
                <span className="text-xl">🎟️</span>
                <div>
                  <p className="font-medium text-gray-800 text-sm">Voucher của tôi</p>
                  <p className="text-xs text-gray-500">Xem và nhận voucher</p>
                </div>
              </button>
            </div>
          </div>

          <div className="mt-8 border-t pt-6" id="voucher-section">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Voucher của tôi</h2>

            {voucherMessage && (
              <div className={`mb-4 p-4 rounded ${voucherMessage.includes('thành công') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                {voucherMessage}
              </div>
            )}

            {myVouchers.length > 0 ? (
              <div className="grid gap-4 mb-6">
                {myVouchers.map((voucher) => (
                  <div key={voucher._id} className="border rounded-lg p-4 bg-gray-50">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <div className="text-sm font-semibold text-gray-800">{voucher.name || voucher.code}</div>
                        <div className="text-xs text-gray-500">{voucher.code}</div>
                      </div>
                      <span className="text-xs text-white bg-blue-600 px-2 py-1 rounded">{voucher.type === 'percentage' ? `${voucher.value}%` : `₫${voucher.value?.toLocaleString('vi-VN')}`}</span>
                    </div>
                    <p className="mt-2 text-sm text-gray-600">{voucher.description}</p>
                    <p className="mt-2 text-xs text-gray-500">Hạn dùng: {voucher.endDate ? new Date(voucher.endDate).toLocaleDateString('vi-VN') : 'Không giới hạn'}</p>
                    <p className="mt-1 text-xs text-gray-500">Đã dùng: {voucher.usedCount || 0}</p>
                  </div>
                ))}
              </div>
            ) : (
              <div className="mb-6 p-4 rounded-lg bg-yellow-50 text-sm text-gray-700">
                Bạn chưa có voucher nào. Hãy nhận voucher mới ở bên dưới.
              </div>
            )}

            <h3 className="text-lg font-semibold text-gray-900 mb-3">Nhận voucher</h3>
            <div className="grid gap-4">
              {availableVouchers.length > 0 ? (
                availableVouchers.map((voucher) => {
                  const alreadyClaimed = myVouchers.some((entry) => entry.code === voucher.code);
                  return (
                    <div key={voucher._id} className="border rounded-lg p-4 bg-white shadow-sm">
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <div className="font-semibold text-gray-800">{voucher.name}</div>
                          <div className="text-xs text-gray-500 mt-1">{voucher.code}</div>
                        </div>
                        <div className="text-right">
                          <div className="text-sm font-semibold text-blue-600">{voucher.type === 'percentage' ? `${voucher.value}%` : `₫${voucher.value?.toLocaleString('vi-VN')}`}</div>
                          <div className="text-xs text-gray-500">Từ {voucher.minOrderValue?.toLocaleString('vi-VN') || 0}₫</div>
                        </div>
                      </div>
                      <p className="mt-3 text-sm text-gray-600">{voucher.description}</p>
                      <div className="mt-4 flex items-center justify-between gap-3">
                        <span className="text-xs text-gray-500">Hạn dùng: {voucher.endDate ? new Date(voucher.endDate).toLocaleDateString('vi-VN') : 'Không giới hạn'}</span>
                        <button
                          type="button"
                          disabled={alreadyClaimed || claimingVoucher}
                          onClick={() => handleClaimVoucher(voucher.code)}
                          className="px-4 py-2 rounded-lg text-white bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400"
                        >
                          {alreadyClaimed ? 'Đã nhận' : 'Nhận voucher'}
                        </button>
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="p-4 rounded-lg bg-gray-50 text-sm text-gray-600">Hiện tại không có voucher để nhận.</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
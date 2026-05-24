import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function ShopProfile() {
  const { user, fetchProfile } = useAuth();
  const [shopName, setShopName] = useState(user?.name || '');
  const [wallet, setWallet] = useState(0);
  const [totalOrders, setTotalOrders] = useState(0);
  const [totalProducts, setTotalProducts] = useState(0);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchShopData();
  }, []);

  const fetchShopData = async () => {
    try {
      setLoading(true);
      const [billingSummary, productsResponse, ordersResponse] = await Promise.all([
        api.get('/shop/billing-summary'),
        api.get('/shop/products'),
        api.get('/shop/orders'),
      ]);

      // Set wallet from billing summary
      setWallet(billingSummary.data?.summary?.balance || 0);

      // Count products
      setTotalProducts(productsResponse.data?.length || 0);

      // Count orders
      setTotalOrders(ordersResponse.data?.length || 0);
    } catch (error) {
      console.error('Error fetching shop data:', error);
      setMessage('Không thể tải thông tin shop');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveName = async () => {
    try {
      setIsSaving(true);
      await api.put('/shop/profile', { name: shopName });
      setMessage('Cập nhật tên shop thành công!');
      setEditing(false);
      await fetchProfile();
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error updating shop name:', error);
      setMessage(error.response?.data?.message || 'Không thể cập nhật tên shop');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setShopName(user?.name || '');
    setEditing(false);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Thông tin chi tiết Shop</h1>
        <p className="text-gray-600 mt-1">Quản lý thông tin cơ bản của shop</p>
      </div>

      {/* Message Alert */}
      {message && (
        <div className={`p-4 rounded-lg ${message.includes('thành công') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
          {message}
        </div>
      )}

      {/* Profile Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {/* Header Section */}
        <div className="h-32 bg-gradient-to-r from-blue-500 to-blue-600"></div>

        {/* Content Section */}
        <div className="px-6 pb-6">
          {/* Avatar and Name */}
          <div className="flex flex-col sm:flex-row sm:items-end sm:gap-4 -mt-16 mb-6">
            <div className="w-24 h-24 bg-blue-500 rounded-full flex items-center justify-center text-white font-bold text-4xl border-4 border-white shadow-md">
              {user?.name?.charAt(0)?.toUpperCase()}
            </div>
            <div className="flex-1">
              {editing ? (
                <div className="flex gap-2 items-end">
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-gray-500 mb-1">Tên Shop</label>
                    <input
                      type="text"
                      value={shopName}
                      onChange={(e) => setShopName(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <button
                    onClick={handleSaveName}
                    disabled={isSaving}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    {isSaving ? 'Đang lưu...' : 'Lưu'}
                  </button>
                  <button
                    onClick={handleCancel}
                    disabled={isSaving}
                    className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50"
                  >
                    Hủy
                  </button>
                </div>
              ) : (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">{shopName}</h2>
                  <p className="text-sm text-gray-500 mt-1">{user?.email}</p>
                  <button
                    onClick={() => setEditing(true)}
                    className="mt-2 text-sm text-blue-600 hover:text-blue-700 font-medium"
                  >
                    ✏️ Chỉnh sửa tên shop
                  </button>
                </div>
              )}
            </div>
          </div>

          <hr className="my-6" />

          {/* Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Wallet Card */}
            <div className="bg-gradient-to-br from-emerald-50 to-emerald-100 rounded-lg p-5 border border-emerald-200">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-wide text-emerald-600 font-semibold">Tổng tiền trong ví</p>
                  <p className="text-2xl font-bold text-emerald-700 mt-2">
                    ₫{wallet.toLocaleString('vi-VN')}
                  </p>
                  <p className="text-xs text-emerald-600 mt-1">Tài khoản chính</p>
                </div>
                <div className="text-5xl">💰</div>
              </div>
            </div>

            {/* Orders Card */}
            <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-5 border border-blue-200">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-wide text-blue-600 font-semibold">Tổng đơn hàng</p>
                  <p className="text-2xl font-bold text-blue-700 mt-2">
                    {totalOrders.toLocaleString('vi-VN')}
                  </p>
                  <p className="text-xs text-blue-600 mt-1">Đơn hàng đã bán</p>
                </div>
                <div className="text-5xl">📦</div>
              </div>
            </div>

            {/* Products Card */}
            <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-5 border border-purple-200">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-xs uppercase tracking-wide text-purple-600 font-semibold">Tổng sản phẩm</p>
                  <p className="text-2xl font-bold text-purple-700 mt-2">
                    {totalProducts.toLocaleString('vi-VN')}
                  </p>
                  <p className="text-xs text-purple-600 mt-1">Sản phẩm hiện có</p>
                </div>
                <div className="text-5xl">🛍️</div>
              </div>
            </div>
          </div>

          <hr className="my-6" />

          {/* Account Info */}
          <div>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Thông tin tài khoản</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Email</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {user?.email}
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Vai trò</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  Shop
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Ngày tham gia</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Trạng thái</label>
                <div className="px-3 py-2 bg-green-50 border border-green-200 rounded-lg text-green-700 font-medium">
                  ✓ Hoạt động
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

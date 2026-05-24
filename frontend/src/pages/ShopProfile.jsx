import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const coretechVisuals = [
  {
    id: "ct-1",
    title: "CoreTech Retail Hub",
    image:
      "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-2",
    title: "CoreTech Device Lab",
    image:
      "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-3",
    title: "CoreTech Service Center",
    image:
      "https://images.unsplash.com/photo-1588702547923-7093a6c3ba33?auto=format&fit=crop&w=800&q=80",
  },
];

export default function ShopProfile() {
  const { user, fetchProfile } = useAuth();
  const [shopName, setShopName] = useState(user?.name || '');
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    // Not needed for basic profile info
  }, []);

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
    <div className="space-y-6">
      {/* CoreTech Demo Section */}
      <section className="mb-6 rounded-2xl overflow-hidden border border-blue-100 shadow-sm bg-white">
        <div className="relative">
          <div className="h-36 md:h-44 bg-gradient-to-r from-slate-900 via-blue-900 to-cyan-700" />
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.22),transparent_45%),radial-gradient(circle_at_80%_70%,rgba(255,255,255,0.15),transparent_40%)]" />

          <div className="absolute left-5 top-5 flex items-center gap-2">
            <span className="px-3 py-1 rounded-full bg-white/20 text-white text-xs font-semibold tracking-wide backdrop-blur">
              CORETECH VERIFIED SHOP
            </span>
            <span className="px-3 py-1 rounded-full bg-emerald-400/90 text-slate-900 text-xs font-bold">
              MANAGED BY CORETECH
            </span>
          </div>

          <div className="absolute left-5 bottom-5 right-5 text-white">
            <h2 className="text-xl md:text-2xl font-bold">
              {user?.name} thuộc hệ thống vận hành CORETECH
            </h2>
            <p className="text-sm text-white/90 mt-1">
              Không gian quản trị dành riêng cho đối tác shop chính thức
              trong mạng lưới CORETECH Commerce.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3 bg-slate-50">
          {coretechVisuals.map((item) => (
            <div
              key={item.id}
              className="relative rounded-xl overflow-hidden h-24 group"
            >
              <img
                src={item.image}
                alt={item.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
              <p className="absolute left-3 bottom-2 text-xs font-semibold text-white tracking-wide">
                {item.title}
              </p>
            </div>
          ))}
        </div>
      </section>

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

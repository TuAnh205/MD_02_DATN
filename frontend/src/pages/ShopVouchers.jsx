import React, { useEffect, useState } from 'react';
import { voucherService } from '../services/voucherService';
import { useAuth } from '../context/AuthContext';

export default function ShopVouchers() {
  const { user } = useAuth();
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [editingId, setEditingId] = useState(null);

  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    type: 'percentage',
    value: '',
    minOrderValue: '',
    maxDiscount: '',
    usageLimit: '',
    userLimit: '1',
    startDate: '',
    endDate: ''
  });

  useEffect(() => {
    fetchVouchers();
  }, []);

  const fetchVouchers = async () => {
    try {
      setLoading(true);
      const data = await voucherService.getMyCreatedVouchers();
      setVouchers(data.vouchers || []);
    } catch (err) {
      setError('Lỗi tải voucher: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      const submitData = {
        ...formData,
        value: parseFloat(formData.value),
        minOrderValue: parseFloat(formData.minOrderValue) || 0,
        maxDiscount: formData.maxDiscount ? parseFloat(formData.maxDiscount) : null,
        usageLimit: formData.usageLimit ? parseInt(formData.usageLimit) : null,
        userLimit: parseInt(formData.userLimit) || 1
      };

      if (editingId) {
        // Remove code when updating (code cannot be changed)
        const { code, ...updateData } = submitData;
        await voucherService.updateShopVoucher(editingId, updateData);
        setSuccess('Cập nhật voucher thành công!');
        setEditingId(null);
      } else {
        await voucherService.createShopVoucher(submitData);
        setSuccess('Tạo voucher thành công!');
      }

      setFormData({
        code: '',
        name: '',
        description: '',
        type: 'percentage',
        value: '',
        minOrderValue: '',
        maxDiscount: '',
        usageLimit: '',
        userLimit: '1',
        startDate: '',
        endDate: ''
      });
      setShowForm(false);
      fetchVouchers();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleEdit = (voucher) => {
    setFormData({
      code: voucher.code,
      name: voucher.name,
      description: voucher.description || '',
      type: voucher.type,
      value: voucher.value,
      minOrderValue: voucher.minOrderValue || '',
      maxDiscount: voucher.maxDiscount || '',
      usageLimit: voucher.usageLimit || '',
      userLimit: voucher.userLimit || '1',
      startDate: voucher.startDate ? new Date(voucher.startDate).toISOString().slice(0, 10) : '',
      endDate: voucher.endDate ? new Date(voucher.endDate).toISOString().slice(0, 10) : ''
    });
    setEditingId(voucher._id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Bạn chắc chắn muốn xóa voucher này?')) {
      try {
        await voucherService.deleteShopVoucher(id);
        setSuccess('Xóa voucher thành công!');
        fetchVouchers();
      } catch (err) {
        setError(err.response?.data?.message || err.message);
      }
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="space-y-4 text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-4 border-blue-200 border-t-blue-600 mx-auto"></div>
          <p className="text-slate-600 font-semibold">Đang tải dữ liệu...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header & Create Button */}
      <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
              <span>🎫</span> Quản Lý Voucher
            </h1>
            <p className="text-slate-600 mt-2 font-semibold">
              Tạo và quản lý các voucher khuyến mãi cho khách hàng
            </p>
          </div>
          <button
            onClick={() => {
              setShowForm(!showForm);
              setEditingId(null);
              if (showForm) {
                setFormData({
                  code: '',
                  name: '',
                  description: '',
                  type: 'percentage',
                  value: '',
                  minOrderValue: '',
                  maxDiscount: '',
                  usageLimit: '',
                  userLimit: '1',
                  startDate: '',
                  endDate: ''
                });
              }
            }}
            className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-3 text-white font-bold hover:shadow-lg transition-all duration-200 whitespace-nowrap"
          >
            {showForm ? '✕ Hủy' : '➕ Tạo Voucher'}
          </button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="rounded-2xl border-2 border-rose-300 bg-gradient-to-r from-rose-50 to-pink-50 p-6 shadow-sm">
          <div className="flex items-start gap-4">
            <span className="text-3xl">⚠️</span>
            <div className="flex-1">
              <p className="font-bold text-rose-900">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Success Alert */}
      {success && (
        <div className="rounded-2xl border-2 border-green-300 bg-gradient-to-r from-green-50 to-emerald-50 p-6 shadow-sm">
          <div className="flex items-start gap-4">
            <span className="text-3xl">✓</span>
            <div className="flex-1">
              <p className="font-bold text-green-900">{success}</p>
            </div>
          </div>
        </div>
      )}

      {/* Form */}
      {showForm && (
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-8 shadow-sm">
          <h2 className="text-2xl font-bold text-slate-900 mb-6">
            {editingId ? '✏️ Cập Nhật Voucher' : '🎫 Tạo Voucher Mới'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Code Input */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Mã Voucher *</label>
                  <input
                    type="text"
                    name="code"
                    value={formData.code}
                    onChange={handleInputChange}
                    disabled={editingId}
                    placeholder="VD: SUMMER20"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 disabled:bg-slate-100 disabled:text-slate-500 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    required
                  />
                </div>

                {/* Name Input */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Tên Voucher *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="VD: Giảm 20% mua sắm hè"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    required
                  />
                </div>

                {/* Description */}
                <div className="md:col-span-2">
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Mô Tả</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    placeholder="Mô tả chi tiết về voucher..."
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-medium text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors resize-none"
                    rows="3"
                  />
                </div>

                {/* Type */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Loại Giảm Giá *</label>
                  <select
                    name="type"
                    value={formData.type}
                    onChange={handleInputChange}
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    required
                  >
                    <option value="percentage">📊 Phần Trăm (%)</option>
                    <option value="fixed">💰 Cố Định (₫)</option>
                  </select>
                </div>

                {/* Value */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">
                    Giá Trị {formData.type === 'percentage' ? '(%)' : '(₫)'} *
                  </label>
                  <input
                    type="number"
                    name="value"
                    value={formData.value}
                    onChange={handleInputChange}
                    placeholder="VD: 20"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    step="0.01"
                    required
                  />
                </div>

                {/* Min Order Value */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Giá Trị Đơn Tối Thiểu (₫)</label>
                  <input
                    type="number"
                    name="minOrderValue"
                    value={formData.minOrderValue}
                    onChange={handleInputChange}
                    placeholder="VD: 100000"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                  />
                </div>

                {/* Max Discount */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">
                    {formData.type === 'percentage' ? 'Giảm Tối Đa (₫)' : 'Giảm Tối Đa'}
                  </label>
                  <input
                    type="number"
                    name="maxDiscount"
                    value={formData.maxDiscount}
                    onChange={handleInputChange}
                    placeholder="VD: 500000"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                  />
                </div>

                {/* Usage Limit */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Số Lần Sử Dụng Tối Đa</label>
                  <input
                    type="number"
                    name="usageLimit"
                    value={formData.usageLimit}
                    onChange={handleInputChange}
                    placeholder="Để trống = không giới hạn"
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                  />
                </div>

                {/* User Limit */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Mỗi Người Dùng Được Dùng</label>
                  <input
                    type="number"
                    name="userLimit"
                    value={formData.userLimit}
                    onChange={handleInputChange}
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                  />
                </div>

                {/* Start Date */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Ngày Bắt Đầu *</label>
                  <input
                    type="date"
                    name="startDate"
                    value={formData.startDate}
                    onChange={handleInputChange}
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    required
                  />
                </div>

                {/* End Date */}
                <div>
                  <label className="block text-sm font-bold text-slate-900 mb-2 uppercase tracking-widest">Ngày Kết Thúc *</label>
                  <input
                    type="date"
                    name="endDate"
                    value={formData.endDate}
                    onChange={handleInputChange}
                    className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold text-slate-900 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
                    required
                  />
                </div>
              </div>

              {/* Form Buttons */}
              <div className="flex gap-4 pt-6 border-t-2 border-slate-200">
                <button
                  type="submit"
                  className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 px-8 py-3 text-white font-bold hover:shadow-lg transition-all duration-200"
                >
                  <span>✓</span> {editingId ? 'Cập Nhật' : 'Tạo Voucher'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowForm(false);
                    setEditingId(null);
                  }}
                  className="inline-flex items-center gap-2 rounded-xl bg-slate-100 text-slate-900 px-8 py-3 font-bold hover:bg-slate-200 transition-colors"
                >
                  <span>✕</span> Hủy
                </button>
              </div>
            </form>
          </div>
        )}

      {/* Vouchers Table */}
      <div className="rounded-2xl bg-white border-2 border-slate-200 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-blue-600">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Mã</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tên</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Kiểu</th>
                <th className="px-6 py-4 text-center text-xs font-bold text-slate-800 uppercase tracking-widest">Giá Trị</th>
                <th className="px-6 py-4 text-center text-xs font-bold text-slate-800 uppercase tracking-widest">Đã Dùng</th>
                <th className="px-6 py-4 text-center text-xs font-bold text-slate-800 uppercase tracking-widest">Hạn Dùng</th>
                <th className="px-6 py-4 text-center text-xs font-bold text-slate-800 uppercase tracking-widest">Trạng Thái</th>
                <th className="px-6 py-4 text-center text-xs font-bold text-slate-800 uppercase tracking-widest">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {vouchers.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-16">
                    <div className="text-6xl mb-4 opacity-20">🎫</div>
                    <p className="text-slate-600 font-semibold text-lg">Chưa có voucher nào</p>
                    <p className="text-slate-400 text-sm mt-1">Tạo voucher khuyến mãi cho khách hàng của bạn</p>
                  </td>
                </tr>
              ) : (
                vouchers.map((voucher) => {
                  const now = new Date();
                  const endDate = new Date(voucher.endDate);
                  const isExpired = endDate < now;

                  return (
                    <tr key={voucher._id} className="hover:bg-blue-50 transition-colors">
                      <td className="px-6 py-4 font-bold text-slate-900">{voucher.code}</td>
                      <td className="px-6 py-4">
                        <div className="font-bold text-slate-900">{voucher.name}</div>
                        <div className="text-xs text-slate-500 mt-1 line-clamp-1">{voucher.description}</div>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${
                          voucher.type === 'percentage'
                            ? 'bg-blue-200 text-blue-900'
                            : 'bg-emerald-200 text-emerald-900'
                        }`}>
                          {voucher.type === 'percentage' ? '📊 %' : '💰 ₫'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center font-bold text-slate-900">
                        {voucher.value.toLocaleString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-center font-bold text-slate-900">
                        {voucher.usedCount || 0}
                        <span className="text-slate-500 font-normal">
                          {voucher.usageLimit ? ` / ${voucher.usageLimit}` : ' / ∞'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center text-sm font-bold text-slate-900">
                        {new Date(voucher.endDate).toLocaleDateString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {isExpired ? (
                          <span className="inline-flex px-3 py-1 text-xs font-bold rounded-lg bg-rose-200 text-rose-900">
                            ❌ Hết hạn
                          </span>
                        ) : !voucher.isActive ? (
                          <span className="inline-flex px-3 py-1 text-xs font-bold rounded-lg bg-slate-200 text-slate-900">
                            ⏸️ Ngừng
                          </span>
                        ) : (
                          <span className="inline-flex px-3 py-1 text-xs font-bold rounded-lg bg-green-200 text-green-900">
                            ✓ Hoạt động
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-center space-x-2">
                        <button
                          onClick={() => handleEdit(voucher)}
                          className="inline-flex items-center gap-1 rounded-lg bg-blue-100 text-blue-700 px-3 py-2 text-xs font-bold hover:bg-blue-200 transition-colors"
                        >
                          ✏️ Sửa
                        </button>
                        <button
                          onClick={() => handleDelete(voucher._id)}
                          className="inline-flex items-center gap-1 rounded-lg bg-rose-100 text-rose-700 px-3 py-2 text-xs font-bold hover:bg-rose-200 transition-colors"
                        >
                          🗑️ Xóa
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

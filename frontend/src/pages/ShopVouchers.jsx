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
    return <div className="flex justify-center items-center h-screen">Đang tải...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8 flex justify-between items-center">
          <h1 className="text-3xl font-bold text-dark">Quản Lý Voucher</h1>
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
            className="bg-primary text-white px-6 py-2 rounded font-semibold hover:bg-primary/90"
          >
            {showForm ? 'Hủy' : 'Tạo Voucher Mới'}
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        {success && (
          <div className="mb-4 p-4 bg-green-100 text-green-700 rounded">
            {success}
          </div>
        )}

        {/* Form */}
        {showForm && (
          <div className="bg-white rounded-lg shadow p-8 mb-8">
            <h2 className="text-2xl font-bold mb-6">{editingId ? 'Cập Nhật Voucher' : 'Tạo Voucher Mới'}</h2>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold mb-2">Mã Voucher *</label>
                  <input
                    type="text"
                    name="code"
                    value={formData.code}
                    onChange={handleInputChange}
                    disabled={editingId}
                    placeholder="VD: SUMMER20"
                    className="w-full border rounded px-4 py-2 disabled:bg-gray-100"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Tên Voucher *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="VD: Giảm 20% mua sắm hè"
                    className="w-full border rounded px-4 py-2"
                    required
                  />
                </div>

                <div className="md:col-span-2">
                  <label className="block text-sm font-semibold mb-2">Mô Tả</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    placeholder="Mô tả chi tiết về voucher"
                    className="w-full border rounded px-4 py-2"
                    rows="3"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Loại Giảm Giá *</label>
                  <select
                    name="type"
                    value={formData.type}
                    onChange={handleInputChange}
                    className="w-full border rounded px-4 py-2"
                    required
                  >
                    <option value="percentage">Phần Trăm (%)</option>
                    <option value="fixed">Cố Định (₫)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">
                    Giá Trị {formData.type === 'percentage' ? '(%)' : '(₫)'} *
                  </label>
                  <input
                    type="number"
                    name="value"
                    value={formData.value}
                    onChange={handleInputChange}
                    placeholder="VD: 20"
                    className="w-full border rounded px-4 py-2"
                    step="0.01"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Giá Trị Đơn Hàng Tối Thiểu (₫)</label>
                  <input
                    type="number"
                    name="minOrderValue"
                    value={formData.minOrderValue}
                    onChange={handleInputChange}
                    placeholder="VD: 100000"
                    className="w-full border rounded px-4 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">
                    {formData.type === 'percentage' ? 'Giảm Tối Đa (₫)' : 'Giảm Tối Đa'}
                  </label>
                  <input
                    type="number"
                    name="maxDiscount"
                    value={formData.maxDiscount}
                    onChange={handleInputChange}
                    placeholder="VD: 500000"
                    className="w-full border rounded px-4 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Số Lần Sử Dụng Tối Đa</label>
                  <input
                    type="number"
                    name="usageLimit"
                    value={formData.usageLimit}
                    onChange={handleInputChange}
                    placeholder="Để trống = không giới hạn"
                    className="w-full border rounded px-4 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Mỗi Người Dùng Được Dùng</label>
                  <input
                    type="number"
                    name="userLimit"
                    value={formData.userLimit}
                    onChange={handleInputChange}
                    className="w-full border rounded px-4 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Ngày Bắt Đầu *</label>
                  <input
                    type="date"
                    name="startDate"
                    value={formData.startDate}
                    onChange={handleInputChange}
                    className="w-full border rounded px-4 py-2"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Ngày Kết Thúc *</label>
                  <input
                    type="date"
                    name="endDate"
                    value={formData.endDate}
                    onChange={handleInputChange}
                    className="w-full border rounded px-4 py-2"
                    required
                  />
                </div>
              </div>

              <div className="flex gap-4">
                <button
                  type="submit"
                  className="bg-primary text-white px-8 py-3 rounded font-semibold hover:bg-primary/90"
                >
                  {editingId ? 'Cập Nhật' : 'Tạo Voucher'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowForm(false);
                    setEditingId(null);
                  }}
                  className="bg-gray-200 text-gray-700 px-8 py-3 rounded font-semibold hover:bg-gray-300"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Vouchers List */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-100 border-b">
              <tr>
                <th className="text-left px-6 py-3">Mã</th>
                <th className="text-left px-6 py-3">Tên</th>
                <th className="text-left px-6 py-3">Kiểu</th>
                <th className="text-center px-6 py-3">Giá Trị</th>
                <th className="text-center px-6 py-3">Đã Dùng</th>
                <th className="text-center px-6 py-3">Hạn Dùng</th>
                <th className="text-center px-6 py-3">Trạng Thái</th>
                <th className="text-center px-6 py-3">Hành Động</th>
              </tr>
            </thead>
            <tbody>
              {vouchers.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 text-gray-500">
                    Chưa có voucher nào
                  </td>
                </tr>
              ) : (
                vouchers.map((voucher) => {
                  const now = new Date();
                  const endDate = new Date(voucher.endDate);
                  const isExpired = endDate < now;

                  return (
                    <tr key={voucher._id} className="border-b hover:bg-gray-50">
                      <td className="px-6 py-4 font-semibold">{voucher.code}</td>
                      <td className="px-6 py-4">{voucher.name}</td>
                      <td className="px-6 py-4">
                        {voucher.type === 'percentage' ? '%' : '₫'}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {voucher.value.toLocaleString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {voucher.usedCount || 0}
                        {voucher.usageLimit ? ` / ${voucher.usageLimit}` : ' / ∞'}
                      </td>
                      <td className="px-6 py-4 text-center text-sm">
                        {new Date(voucher.endDate).toLocaleDateString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        {isExpired ? (
                          <span className="text-red-600 font-semibold">Đã hết hạn</span>
                        ) : !voucher.isActive ? (
                          <span className="text-gray-600 font-semibold">Ngừng</span>
                        ) : (
                          <span className="text-green-600 font-semibold">Hoạt động</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-center space-x-2">
                        <button
                          onClick={() => handleEdit(voucher)}
                          className="text-blue-600 hover:text-blue-800 text-sm font-semibold"
                        >
                          Sửa
                        </button>
                        <button
                          onClick={() => handleDelete(voucher._id)}
                          className="text-red-600 hover:text-red-800 text-sm font-semibold"
                        >
                          Xóa
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

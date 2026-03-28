import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function ShopRevenue() {
  const [revenueData, setRevenueData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('month');

  useEffect(() => {
    fetchRevenue();
  }, [period]);

  const fetchRevenue = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/shop/revenue?period=${period}`);
      setRevenueData(response.data);
    } catch (error) {
      console.error('Error fetching revenue:', error);
    } finally {
      setLoading(false);
    }
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
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Thống kê Doanh thu</h1>
        <select
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
          className="border border-gray-300 rounded-md px-3 py-2"
        >
          <option value="day">Hôm nay</option>
          <option value="week">Tuần này</option>
          <option value="month">Tháng này</option>
          <option value="year">Năm này</option>
        </select>
      </div>

      {revenueData && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Tổng Doanh thu</h3>
            <p className="text-3xl font-bold text-green-600">
              {revenueData.totalRevenue.toLocaleString('vi-VN')} VND
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Tổng Đơn hàng</h3>
            <p className="text-3xl font-bold text-blue-600">
              {revenueData.totalOrders}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Tổng Sản phẩm Bán</h3>
            <p className="text-3xl font-bold text-purple-600">
              {revenueData.totalProducts}
            </p>
          </div>
        </div>
      )}

      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Thông tin Chi tiết</h3>
        {revenueData ? (
          <div className="space-y-2">
            <p><strong>Khoảng thời gian:</strong> {period === 'day' ? 'Hôm nay' : period === 'week' ? 'Tuần này' : period === 'month' ? 'Tháng này' : 'Năm này'}</p>
            <p><strong>Ngày bắt đầu:</strong> {new Date(revenueData.startDate).toLocaleDateString('vi-VN')}</p>
          </div>
        ) : (
          <p className="text-gray-500">Không có dữ liệu doanh thu</p>
        )}
      </div>
    </div>
  );
}
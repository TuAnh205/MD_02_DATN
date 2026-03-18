import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminRevenue() {
  const [viewType, setViewType] = useState('month'); // 'month' or 'year'
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [revenueData, setRevenueData] = useState([]);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRevenueData();
  }, [viewType, selectedDate]);

  const fetchRevenueData = async () => {
    try {
      setLoading(true);
      const year = selectedDate.getFullYear();
      const month = selectedDate.getMonth() + 1;

      const params = new URLSearchParams({
        ...(viewType === 'month' && { year, month }),
        ...(viewType === 'year' && { year })
      });

      const response = await api.get(`/admin/revenue?${params}`);
      setRevenueData(response.data.data || []);
      setTotalRevenue(response.data.total || 0);
    } catch (error) {
      console.error('Error fetching revenue data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePrevious = () => {
    const newDate = new Date(selectedDate);
    if (viewType === 'month') {
      newDate.setMonth(newDate.getMonth() - 1);
    } else {
      newDate.setFullYear(newDate.getFullYear() - 1);
    }
    setSelectedDate(newDate);
  };

  const handleNext = () => {
    const newDate = new Date(selectedDate);
    if (viewType === 'month') {
      newDate.setMonth(newDate.getMonth() + 1);
    } else {
      newDate.setFullYear(newDate.getFullYear() + 1);
    }
    setSelectedDate(newDate);
  };

  const getMaxRevenue = () => {
    return Math.max(...revenueData.map(d => d.revenue), 0);
  };

  const formatDate = () => {
    if (viewType === 'month') {
      return selectedDate.toLocaleDateString('vi-VN', { year: 'numeric', month: 'long' });
    } else {
      return selectedDate.getFullYear().toString();
    }
  };

  const getChartLabel = (item) => {
    if (viewType === 'month') {
      return `Ngày ${item.day}`;
    } else {
      const months = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
      return months[item.month - 1] || '';
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
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Doanh Thu</h1>
        <p className="text-gray-600 mt-1">Xem và phân tích doanh thu theo thời gian</p>
      </div>

      {/* Controls */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex gap-2">
            <button
              onClick={() => setViewType('month')}
              className={`px-4 py-2 rounded font-medium transition ${
                viewType === 'month'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Xem theo tháng
            </button>
            <button
              onClick={() => setViewType('year')}
              className={`px-4 py-2 rounded font-medium transition ${
                viewType === 'year'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Xem theo năm
            </button>
          </div>

          <div className="flex items-center gap-4">
            <button
              onClick={handlePrevious}
              className="px-3 py-2 bg-gray-200 rounded hover:bg-gray-300 transition"
            >
              ← Trước
            </button>
            <span className="font-semibold text-lg min-w-[150px] text-center">
              {formatDate()}
            </span>
            <button
              onClick={handleNext}
              className="px-3 py-2 bg-gray-200 rounded hover:bg-gray-300 transition"
            >
              Sau →
            </button>
          </div>
        </div>
      </div>

      {/* Total Revenue Card */}
      <div className="bg-gradient-to-r from-green-500 to-green-600 rounded-lg shadow p-6 text-white">
        <p className="text-green-100 text-sm font-medium">Tổng Doanh Thu ({formatDate()})</p>
        <p className="text-4xl font-bold mt-2">
          ₫{totalRevenue.toLocaleString('vi-VN')}
        </p>
        <p className="text-green-100 text-sm mt-2">Từ các đơn hàng thanh toán thành công</p>
      </div>

      {/* Chart */}
      {revenueData.length > 0 ? (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-6">Biểu đồ doanh thu</h2>

          {/* Bar Chart */}
          <div className="overflow-x-auto">
            <div className="min-w-max" style={{ minHeight: '400px' }}>
              <div className="flex items-end gap-2 h-80 border-l-2 border-b-2 border-gray-300 pl-4 pb-4">
                {revenueData.map((item, index) => {
                  const maxRevenue = getMaxRevenue();
                  const percentage = maxRevenue > 0 ? (item.revenue / maxRevenue) * 100 : 0;

                  return (
                    <div
                      key={index}
                      className="flex-1 flex flex-col items-center group"
                    >
                      <div
                        className="w-full bg-gradient-to-t from-blue-500 to-blue-400 rounded-t transition-all hover:from-blue-600 hover:to-blue-500 cursor-pointer"
                        style={{ height: `${percentage || 5}%` }}
                        title={`${getChartLabel(item)}: ₫${item.revenue.toLocaleString('vi-VN')}`}
                      >
                        <div className="h-full flex items-start justify-center pt-1 opacity-0 group-hover:opacity-100 transition">
                          <span className="text-white text-xs font-bold bg-blue-600 px-1 rounded">
                            {Math.round((item.revenue / 1000000) * 10) / 10}M
                          </span>
                        </div>
                      </div>
                      <p className="text-xs text-gray-600 mt-2 font-medium">
                        {getChartLabel(item)}
                      </p>
                      <p className="text-xs text-gray-500 mt-1">
                        ₫{Math.round(item.revenue / 1000)}K
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Legend */}
          <div className="mt-8 p-4 bg-gray-50 rounded border border-gray-200">
            <p className="text-sm font-semibold text-gray-700 mb-3">Thống kê chi tiết:</p>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div>
                <p className="text-xs text-gray-500">Tổng doanh thu</p>
                <p className="text-lg font-bold text-gray-900">
                  ₫{totalRevenue.toLocaleString('vi-VN')}
                </p>
              </div>
              <div>
                <p className="text-xs text-gray-500">Trung bình/ngày</p>
                <p className="text-lg font-bold text-gray-900">
                  ₫{Math.round(totalRevenue / revenueData.length).toLocaleString('vi-VN')}
                </p>
              </div>
              <div>
                <p className="text-xs text-gray-500">Cao nhất</p>
                <p className="text-lg font-bold text-green-600">
                  ₫{Math.max(...revenueData.map(d => d.revenue)).toLocaleString('vi-VN')}
                </p>
              </div>
              <div>
                <p className="text-xs text-gray-500">Thấp nhất</p>
                <p className="text-lg font-bold text-orange-600">
                  ₫{Math.min(...revenueData.map(d => d.revenue)).toLocaleString('vi-VN')}
                </p>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <p className="text-gray-400 text-lg">📊 Chưa có dữ liệu doanh thu</p>
        </div>
      )}

      {/* Table View */}
      {revenueData.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-6 border-b">
            <h2 className="text-lg font-bold text-gray-900">Chi tiết doanh thu</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    {viewType === 'month' ? 'Ngày' : 'Tháng'}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Doanh thu
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Tỷ lệ
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {revenueData.map((item, index) => {
                  const percentage = totalRevenue > 0 ? ((item.revenue / totalRevenue) * 100).toFixed(1) : 0;
                  return (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">
                        {getChartLabel(item)}
                      </td>
                      <td className="px-6 py-4 text-sm font-semibold text-gray-900">
                        ₫{item.revenue.toLocaleString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <div className="flex items-center gap-2">
                          <div className="w-24 h-2 bg-gray-200 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-blue-500 rounded-full"
                              style={{ width: `${percentage}%` }}
                            ></div>
                          </div>
                          <span className="text-gray-600">{percentage}%</span>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

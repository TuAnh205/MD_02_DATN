import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminRevenue() {
  const [activeTab, setActiveTab] = useState('total');
  const [viewType, setViewType] = useState('month'); // 'month' or 'year'
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [revenueData, setRevenueData] = useState([]);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [shopRevenue, setShopRevenue] = useState([]);
  const [shopTotal, setShopTotal] = useState(0);
  const [platformProducts, setPlatformProducts] = useState([]);
  const [platformProductTotal, setPlatformProductTotal] = useState(0);
  const [platformProductCount, setPlatformProductCount] = useState(0);
  const [platformActiveCount, setPlatformActiveCount] = useState(0);
  const [platformShops, setPlatformShops] = useState([]);
  const [platformShopTotal, setPlatformShopTotal] = useState(0);
  const [policyInfo, setPolicyInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  const formatCurrency = (value) => `₫${Number(value || 0).toLocaleString('vi-VN')}`;

  useEffect(() => {
    fetchRevenueData();
  }, [viewType, selectedDate]);

  const fetchRevenueData = async () => {
    try {
      setLoading(true);
      const year = selectedDate.getFullYear();
      const month = selectedDate.getMonth() + 1;

      const params = new URLSearchParams({
        year,
        ...(viewType === 'month' && { month })
      });

      const [totalRes, shopRes, platformRes, platformShopsRes] = await Promise.all([
        api.get(`/admin/revenue?${params}`),
        api.get(`/admin/revenue/shops?${params}`),
        api.get(`/admin/revenue/platform?${params}`),
        api.get(`/admin/revenue/platform/shops?${params}`),
      ]);

      setRevenueData(totalRes.data.data || []);
      setTotalRevenue(totalRes.data.total || 0);
      setShopRevenue(shopRes.data.shops || []);
      setShopTotal(shopRes.data.total || 0);
      setPlatformProducts(platformRes.data.products || []);
      setPlatformProductTotal(platformRes.data.totalFeeRevenue || 0);
      setPlatformProductCount(platformRes.data.productCount || 0);
      setPlatformActiveCount(platformRes.data.activeCount || 0);
      setPlatformShops(platformShopsRes.data.shops || []);
      setPlatformShopTotal(platformShopsRes.data.totalFeeRevenue || 0);
      setPolicyInfo(platformRes.data.policy || platformShopsRes.data.policy || null);
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
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Báo cáo Doanh Thu</h1>
        <p className="text-slate-600 mt-2">Xem doanh thu bán hàng và phí nền tảng chi tiết theo shop, sản phẩm và thời gian</p>
      </div>

      {policyInfo && (
        <div className="rounded-2xl border-2 border-blue-200 bg-gradient-to-r from-blue-50 to-blue-100 px-6 py-4 shadow-sm">
          <p className="text-sm font-bold text-blue-900 flex items-center gap-2">
            <span>ℹ️</span> Chính sách phí nền tảng
          </p>
          <p className="mt-2 text-sm text-blue-800">
            Từ <span className="font-semibold">{new Date(policyInfo.feeStartDate).toLocaleString('vi-VN')}</span>, hệ thống tính phí <span className="font-bold text-blue-700">{Math.round(policyInfo.commissionRate * 100)}%</span> trên giá bán sản phẩm khi đơn hàng thanh toán thành công.
          </p>
        </div>
      )}

      {/* Controls & Tabs */}
      <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-6">
          {/* View Type Buttons */}
          <div className="flex gap-3">
            <button
              onClick={() => setViewType('month')}
              className={`px-5 py-2 rounded-lg font-bold text-sm transition-all duration-200 ${
                viewType === 'month'
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-md'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              📅 Xem theo Tháng
            </button>
            <button
              onClick={() => setViewType('year')}
              className={`px-5 py-2 rounded-lg font-bold text-sm transition-all duration-200 ${
                viewType === 'year'
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white shadow-md'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              📊 Xem theo Năm
            </button>
          </div>

          {/* Date Navigation */}
          <div className="flex items-center gap-4 bg-gradient-to-r from-slate-50 to-blue-50 rounded-xl px-6 py-3 border border-slate-200">
            <button
              onClick={handlePrevious}
              className="p-2 rounded-lg hover:bg-white transition-colors text-slate-600 hover:text-blue-600 font-bold"
            >
              ◀
            </button>
            <span className="font-bold text-lg min-w-[180px] text-center text-slate-900">
              {formatDate()}
            </span>
            <button
              onClick={handleNext}
              className="p-2 rounded-lg hover:bg-white transition-colors text-slate-600 hover:text-blue-600 font-bold"
            >
              ▶
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
        <div className="flex flex-wrap border-b-2 border-slate-200">
          <button
            onClick={() => setActiveTab('total')}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all duration-200 ${
              activeTab === 'total'
                ? 'border-b-4 border-blue-600 text-blue-600 bg-blue-50'
                : 'text-slate-600 hover:text-slate-900 border-b-4 border-transparent'
            }`}
          >
            💰 Tổng Doanh Thu
          </button>
          <button
            onClick={() => setActiveTab('shops')}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all duration-200 ${
              activeTab === 'shops'
                ? 'border-b-4 border-purple-600 text-purple-600 bg-purple-50'
                : 'text-slate-600 hover:text-slate-900 border-b-4 border-transparent'
            }`}
          >
            🏪 Doanh Thu Shop
          </button>
          <button
            onClick={() => setActiveTab('platform-products')}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all duration-200 ${
              activeTab === 'platform-products'
                ? 'border-b-4 border-amber-600 text-amber-600 bg-amber-50'
                : 'text-slate-600 hover:text-slate-900 border-b-4 border-transparent'
            }`}
          >
            📦 Phí Theo Sản Phẩm
          </button>
          <button
            onClick={() => setActiveTab('platform-shops')}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all duration-200 ${
              activeTab === 'platform-shops'
                ? 'border-b-4 border-rose-600 text-rose-600 bg-rose-50'
                : 'text-slate-600 hover:text-slate-900 border-b-4 border-transparent'
            }`}
          >
            🛍️ Phí Theo Shop
          </button>
        </div>
      </div>

      {/* ===== TAB: TỔNG DOANH THU ===== */}
      {activeTab === 'total' && (
        <>
          {/* Total Revenue Card */}
          <div className="bg-gradient-to-br from-green-600 to-emerald-700 rounded-3xl shadow-lg p-8 text-white overflow-hidden relative">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-16 -mt-16"></div>
            <div className="relative z-10">
              <p className="text-green-100 text-sm font-bold uppercase tracking-widest">Tổng Doanh Thu ({formatDate()})</p>
              <p className="text-5xl font-bold mt-4">{formatCurrency(totalRevenue)}</p>
              <p className="text-green-100 text-sm mt-3">📊 Từ các đơn hàng thanh toán thành công</p>
            </div>
          </div>

          {/* Chart & Stats */}
          {revenueData.length > 0 ? (
            <>
              {/* Chart */}
              <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-8">
                <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
                  <span>📈</span> Biểu đồ doanh thu
                </h2>
                <div className="overflow-x-auto">
                  <div className="min-w-max" style={{ minHeight: '450px' }}>
                    <div className="flex items-end gap-2 h-96 border-l-4 border-b-4 border-slate-300 pl-6 pb-6">
                      {revenueData.map((item, index) => {
                        const maxRevenue = getMaxRevenue();
                        const percentage = maxRevenue > 0 ? (item.revenue / maxRevenue) * 100 : 0;
                        return (
                          <div key={index} className="flex-1 flex flex-col items-center group">
                            <div className="relative h-full w-full flex items-end justify-center">
                              <div
                                className="w-3/4 bg-gradient-to-t from-blue-600 to-blue-400 rounded-t-lg transition-all duration-200 hover:from-blue-700 hover:to-blue-500 cursor-pointer shadow-md"
                                style={{ height: `${Math.max(percentage, 8)}%` }}
                                title={`${getChartLabel(item)}: ₫${item.revenue.toLocaleString('vi-VN')}`}
                              >
                                <div className="h-full flex items-start justify-center pt-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                  <span className="text-white text-xs font-bold bg-blue-700 px-2 py-1 rounded-full">
                                    {Math.round((item.revenue / 1000000) * 10) / 10}M
                                  </span>
                                </div>
                              </div>
                            </div>
                            <p className="text-xs text-slate-700 font-bold mt-3">{getChartLabel(item)}</p>
                            <p className="text-xs text-slate-500 mt-1">₫{Math.round(item.revenue / 1000)}K</p>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>

              {/* Statistics */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-shadow">
                  <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Tổng doanh thu</p>
                  <p className="text-3xl font-bold text-slate-900 mt-3">₫{totalRevenue.toLocaleString('vi-VN')}</p>
                  <p className="text-xs text-slate-500 mt-2">Toàn kỳ</p>
                </div>
                <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-shadow">
                  <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Trung bình/ngày</p>
                  <p className="text-3xl font-bold text-blue-600 mt-3">₫{Math.round(totalRevenue / revenueData.length).toLocaleString('vi-VN')}</p>
                  <p className="text-xs text-slate-500 mt-2">{revenueData.length} ngày</p>
                </div>
                <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-shadow">
                  <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Cao nhất</p>
                  <p className="text-3xl font-bold text-green-600 mt-3">
                    ₫{Math.max(...revenueData.map(d => d.revenue)).toLocaleString('vi-VN')}
                  </p>
                  <p className="text-xs text-slate-500 mt-2">Ngày đỉnh điểm</p>
                </div>
                <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-shadow">
                  <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Thấp nhất</p>
                  <p className="text-3xl font-bold text-amber-600 mt-3">
                    ₫{Math.min(...revenueData.map(d => d.revenue)).toLocaleString('vi-VN')}
                  </p>
                  <p className="text-xs text-slate-500 mt-2">Ngày thấp nhất</p>
                </div>
              </div>

              {/* Detailed Table */}
              {revenueData.length > 0 && (
                <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
                  <div className="p-6 border-b-2 border-slate-200">
                    <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                      <span>📋</span> Chi tiết doanh thu
                    </h2>
                  </div>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-blue-600">
                        <tr>
                          <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                            {viewType === 'month' ? 'Ngày' : 'Tháng'}
                          </th>
                          <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Doanh Thu</th>
                          <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tỷ Lệ</th>
                          <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Thứ Hạng</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-200">
                        {revenueData.map((item, index) => {
                          const percentage = totalRevenue > 0 ? ((item.revenue / totalRevenue) * 100).toFixed(1) : 0;
                          const isMax = item.revenue === Math.max(...revenueData.map(d => d.revenue));
                          const isMin = item.revenue === Math.min(...revenueData.map(d => d.revenue));
                          return (
                            <tr key={index} className={`hover:bg-blue-50 transition-colors ${isMax ? 'bg-green-50' : isMin ? 'bg-amber-50' : ''}`}>
                              <td className="px-6 py-4 text-sm font-bold text-slate-900">{getChartLabel(item)}</td>
                              <td className="px-6 py-4 text-sm font-bold text-blue-600">{formatCurrency(item.revenue)}</td>
                              <td className="px-6 py-4 text-sm">
                                <div className="flex items-center gap-3">
                                  <div className="w-32 h-2 bg-slate-200 rounded-full overflow-hidden">
                                    <div className="h-full bg-gradient-to-r from-blue-500 to-blue-600 rounded-full" style={{ width: `${percentage}%` }}></div>
                                  </div>
                                  <span className="text-slate-700 font-semibold min-w-[45px]">{percentage}%</span>
                                </div>
                              </td>
                              <td className="px-6 py-4 text-sm">
                                {isMax && <span className="inline-flex items-center gap-1 px-3 py-1 bg-green-200 text-green-900 rounded-lg font-bold text-xs">🏆 Cao nhất</span>}
                                {isMin && <span className="inline-flex items-center gap-1 px-3 py-1 bg-amber-200 text-amber-900 rounded-lg font-bold text-xs">📉 Thấp nhất</span>}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-12 text-center">
              <p className="text-slate-400 text-lg">📊 Chưa có dữ liệu doanh thu</p>
            </div>
          )}
        </>
      )}

      {/* ===== TAB: DOANH THU THEO SHOP ===== */}
      {activeTab === 'shops' && (
        <>
          {/* Shop Total Card */}
          <div className="bg-gradient-to-br from-purple-600 to-indigo-700 rounded-3xl shadow-lg p-8 text-white overflow-hidden relative">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-16 -mt-16"></div>
            <div className="relative z-10">
              <p className="text-purple-100 text-sm font-bold uppercase tracking-widest">Tổng Doanh Thu - Shop ({formatDate()})</p>
              <p className="text-5xl font-bold mt-4">{formatCurrency(shopTotal)}</p>
              <p className="text-purple-100 text-sm mt-3">📊 Từ {shopRevenue.length} shop có doanh thu</p>
            </div>
          </div>

          {shopRevenue.length > 0 ? (
            <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
              <div className="p-6 border-b-2 border-slate-200">
                <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                  <span>🏪</span> Doanh thu từng shop
                </h2>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-purple-600">
                    <tr>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Xếp Hạng</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tên Shop</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Doanh Thu</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Đơn Hàng</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Sản Phẩm</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tỷ Lệ</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200">
                    {shopRevenue.map((shop, index) => {
                      const percentage = shopTotal > 0 ? ((shop.revenue / shopTotal) * 100).toFixed(1) : 0;
                      return (
                        <tr key={shop.shopId} className="hover:bg-purple-50 transition-colors">
                          <td className="px-6 py-4 text-sm font-bold">
                            {index === 0 ? <span className="text-2xl">🥇</span> : index === 1 ? <span className="text-2xl">🥈</span> : index === 2 ? <span className="text-2xl">🥉</span> : <span className="text-slate-600 font-bold">#{index + 1}</span>}
                          </td>
                          <td className="px-6 py-4 text-sm">
                            <div className="font-bold text-slate-900">{shop.shopName}</div>
                            <div className="text-xs text-slate-500">{shop.shopEmail}</div>
                          </td>
                          <td className="px-6 py-4 text-sm font-bold text-green-600">{formatCurrency(shop.revenue)}</td>
                          <td className="px-6 py-4 text-sm font-semibold text-slate-900">{shop.orderCount}</td>
                          <td className="px-6 py-4 text-sm font-semibold text-slate-900">{shop.itemsSold}</td>
                          <td className="px-6 py-4 text-sm">
                            <div className="flex items-center gap-3">
                              <div className="w-32 h-2 bg-slate-200 rounded-full overflow-hidden">
                                <div className="h-full bg-gradient-to-r from-purple-500 to-purple-600 rounded-full" style={{ width: `${percentage}%` }}></div>
                              </div>
                              <span className="text-slate-700 font-semibold min-w-[45px]">{percentage}%</span>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-12 text-center">
              <p className="text-slate-400 text-lg">📊 Chưa có dữ liệu doanh thu theo shop</p>
            </div>
          )}
        </>
      )}

      {activeTab === 'platform-products' && (
        <>
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-gradient-to-br from-amber-600 to-orange-700 rounded-3xl shadow-lg p-8 text-white overflow-hidden relative">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-16 -mt-16"></div>
              <div className="relative z-10">
                <p className="text-amber-100 text-sm font-bold uppercase tracking-widest">Tổng Phí Nền Tảng</p>
                <p className="text-4xl font-bold mt-3">{formatCurrency(platformProductTotal)}</p>
                <p className="text-amber-100 text-sm mt-2">({formatDate()})</p>
              </div>
            </div>
            <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-8">
              <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Dòng Bán Hàng Phát Sinh</p>
              <p className="text-4xl font-bold text-slate-900 mt-3">{platformProductCount}</p>
              <p className="text-sm text-slate-500 mt-2">Sản phẩm</p>
            </div>
            <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-8">
              <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">Đã Thu Phí</p>
              <p className="text-4xl font-bold text-rose-600 mt-3">{platformActiveCount}</p>
              <p className="text-sm text-slate-500 mt-2">Dòng đã thanh toán</p>
            </div>
          </div>

          {/* Products Table */}
          <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
            <div className="p-6 border-b-2 border-slate-200">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span>📦</span> Chi tiết phí theo sản phẩm
              </h2>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-amber-600">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Sản Phẩm</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Shop</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Đơn Hàng</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Giá Trị Tính Phí</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Phí 5%</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Trạng Thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {platformProducts.map((product) => (
                    <tr key={product.productId} className="hover:bg-amber-50 transition-colors">
                      <td className="px-6 py-4 text-sm font-bold text-slate-900">{product.productName}</td>
                      <td className="px-6 py-4 text-sm">
                        <div className="font-semibold text-slate-900">{product.shopName}</div>
                        <div className="text-xs text-slate-500">{product.shopEmail}</div>
                      </td>
                      <td className="px-6 py-4 text-sm font-semibold text-slate-900">{product.orderNumber}</td>
                      <td className="px-6 py-4 text-sm font-semibold text-slate-900">{formatCurrency(product.baseAmount)}</td>
                      <td className="px-6 py-4 text-sm font-bold text-amber-700">{formatCurrency(product.feeAmount)}</td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${product.feeStatus === 'paid' ? 'bg-green-200 text-green-900' : 'bg-rose-200 text-rose-900'}`}>
                          {product.feeStatus === 'paid' ? '✓ Đã thu phí' : '⏳ Chưa TT'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {platformProducts.length === 0 && (
              <div className="p-10 text-center text-slate-500">
                <p className="text-lg">📊 Chưa có sản phẩm nào phát sinh phí</p>
              </div>
            )}
          </div>
        </>
      )}

      {activeTab === 'platform-shops' && (
        <>
          <div className="bg-gradient-to-br from-rose-600 to-pink-700 rounded-3xl shadow-lg p-8 text-white overflow-hidden relative">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-16 -mt-16"></div>
            <div className="relative z-10">
              <p className="text-rose-100 text-sm font-bold uppercase tracking-widest">Tổng Phí Nền Tảng - Shop ({formatDate()})</p>
              <p className="text-5xl font-bold mt-4">{formatCurrency(platformShopTotal)}</p>
              <p className="text-rose-100 text-sm mt-3">📊 Từ {platformShops.length} shop phát sinh phí</p>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
            <div className="p-6 border-b-2 border-slate-200">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <span>🛍️</span> Phí nền tảng theo từng shop
              </h2>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-rose-600">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Xếp Hạng</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Shop</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tổng Phí</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Sản Phẩm</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Đã Thu Phí</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Sản Phẩm Tiêu Biểu</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {platformShops.map((shop, index) => (
                    <tr key={shop.shopId} className="hover:bg-rose-50 transition-colors align-top">
                      <td className="px-6 py-4 text-sm font-bold">
                        {index === 0 ? <span className="text-2xl">🥇</span> : index === 1 ? <span className="text-2xl">🥈</span> : index === 2 ? <span className="text-2xl">🥉</span> : <span className="text-slate-600 font-bold">#{index + 1}</span>}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <div className="font-bold text-slate-900">{shop.shopName}</div>
                        <div className="text-xs text-slate-500">{shop.shopEmail}</div>
                      </td>
                      <td className="px-6 py-4 text-sm font-bold text-rose-600">{formatCurrency(shop.totalFeeRevenue)}</td>
                      <td className="px-6 py-4 text-sm font-semibold text-slate-900">{shop.productCount}</td>
                      <td className="px-6 py-4 text-sm font-semibold text-slate-900">{shop.activeFeeProducts}</td>
                      <td className="px-6 py-4 text-sm">
                        <div className="space-y-2">
                          {shop.products.slice(0, 2).map((product) => (
                            <div key={product.productId} className="rounded-lg bg-slate-50 px-3 py-2 border border-slate-200">
                              <div className="font-semibold text-slate-900 text-xs truncate">{product.productName}</div>
                              <div className="text-xs text-slate-500 mt-1">
                                <span className="font-bold text-rose-600">{formatCurrency(product.feeAmount)}</span> • {new Date(product.feeStartAt).toLocaleDateString('vi-VN')}
                              </div>
                            </div>
                          ))}
                          {shop.products.length > 2 && (
                            <div className="text-xs text-slate-600 font-semibold px-3 py-1">
                              +{shop.products.length - 2} sản phẩm khác
                            </div>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {platformShops.length === 0 && (
              <div className="p-10 text-center text-slate-500">
                <p className="text-lg">📊 Chưa có shop nào phát sinh phí nền tảng</p>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

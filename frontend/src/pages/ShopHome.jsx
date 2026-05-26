import React, { useState, useEffect } from "react";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

export default function ShopHome() {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalRevenue: 0,
    totalOrders: 0,
    totalProducts: 0,
  });
  const [loading, setLoading] = useState(true);
  const [billingSummary, setBillingSummary] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [revenueRes, ordersRes, productsRes, billingsRes] =
        await Promise.all([
          api.get("/shop/revenue?period=month"),
          api.get("/shop/orders"),
          api.get("/shop/products"),
          api.get("/shop/billing-summary"),
        ]);

      setStats({
        totalRevenue: revenueRes.data.summary?.totalGrossRevenue || revenueRes.data.totalRevenue || 0,
        totalOrders: revenueRes.data.summary?.totalOrders || revenueRes.data.totalOrders || 0,
        totalProducts: Array.isArray(productsRes.data) ? productsRes.data.length : 0,
      });

      setBillingSummary(billingsRes.data?.summary || null);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (value) =>
    `₫${Number(value || 0).toLocaleString("vi-VN")}`;

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
      {/* Welcome Banner */}
      <div className="rounded-3xl overflow-hidden shadow-md bg-white border-2 border-slate-200">
        <div className="bg-gradient-to-r from-blue-600 via-blue-500 to-orange-500 px-8 py-12 text-white relative overflow-hidden">
          <div className="absolute top-0 right-0 w-40 h-40 bg-white opacity-10 rounded-full -mr-20 -mt-20"></div>
          <div className="relative z-10">
            <h2 className="text-4xl font-bold">Chào mừng, {user?.name}! 👋</h2>
            <p className="text-blue-100 mt-3 text-lg font-semibold">
              Đây là tổng quan hoạt động của shop trong tháng này. Hãy tiếp tục phát triển business!
            </p>
          </div>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Revenue Card */}
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">💰 Doanh Thu Tháng Này</p>
              <p className="text-3xl font-bold text-slate-900 mt-3">{formatCurrency(stats.totalRevenue)}</p>
              <p className="text-xs text-slate-500 mt-2 font-semibold">Trước khi trừ phí 5%</p>
            </div>
            <div className="text-5xl opacity-20">💰</div>
          </div>
        </div>

        {/* Orders Card */}
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">📋 Đơn Hàng Tháng Này</p>
              <p className="text-3xl font-bold text-slate-900 mt-3">{stats.totalOrders}</p>
              <p className="text-xs text-slate-500 mt-2 font-semibold">Tất cả trạng thái</p>
            </div>
            <div className="text-5xl opacity-20">📋</div>
          </div>
        </div>

        {/* Products Card */}
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <p className="text-xs font-bold text-slate-600 uppercase tracking-widest">📦 Tổng Sản Phẩm</p>
              <p className="text-3xl font-bold text-slate-900 mt-3">{stats.totalProducts}</p>
              <p className="text-xs text-slate-500 mt-2 font-semibold">Đang bán</p>
            </div>
            <div className="text-5xl opacity-20">📦</div>
          </div>
        </div>
      </div>

      {/* Billing Status */}
      {billingSummary && (
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm">
          <h3 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
            <span>💳</span> Trạng Thái Ví & Công Nợ
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Wallet Balance */}
            <div className="rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 p-6 border-2 border-blue-200">
              <p className="text-xs font-bold text-blue-700 uppercase tracking-widest">Số Dư Ví</p>
              <p className="text-4xl font-bold text-blue-900 mt-3">
                {formatCurrency(billingSummary.walletBalance || 0)}
              </p>
              <p className="text-sm text-blue-700 mt-2 font-semibold">
                ✓ Đủ để tiếp tục bán hàng
              </p>
            </div>

            {/* Outstanding Debt */}
            <div className={`rounded-2xl p-6 border-2 ${
              (billingSummary.outstandingAmount || 0) > 0
                ? "bg-gradient-to-br from-amber-50 to-orange-50 border-amber-200"
                : "bg-gradient-to-br from-green-50 to-emerald-50 border-green-200"
            }`}>
              <p className={`text-xs font-bold uppercase tracking-widest ${
                (billingSummary.outstandingAmount || 0) > 0
                  ? "text-amber-700"
                  : "text-green-700"
              }`}>
                Công Nợ Phí Sàn
              </p>
              <p className={`text-4xl font-bold mt-3 ${
                (billingSummary.outstandingAmount || 0) > 0
                  ? "text-amber-900"
                  : "text-green-900"
              }`}>
                {formatCurrency(billingSummary.outstandingAmount || 0)}
              </p>
              <p className={`text-sm mt-2 font-semibold ${
                (billingSummary.outstandingAmount || 0) > 0
                  ? "text-amber-700"
                  : "text-green-700"
              }`}>
                {(billingSummary.outstandingAmount || 0) > 0
                  ? "⚠️ Cần thanh toán sớm"
                  : "✓ Không có công nợ"}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm">
        <h3 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
          <span>⚡</span> Các Thao Tác Nhanh
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <a
            href="/shop/products"
            className="rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4 text-center text-white font-bold hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 border-2 border-blue-600"
          >
            <span>➕</span> Thêm Sản Phẩm
          </a>
          <a
            href="/shop/orders"
            className="rounded-xl bg-gradient-to-r from-green-600 to-emerald-600 px-6 py-4 text-center text-white font-bold hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 border-2 border-green-600"
          >
            <span>📋</span> Xem Đơn Hàng
          </a>
          <a
            href="/shop/revenue"
            className="rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 px-6 py-4 text-center text-white font-bold hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2 border-2 border-purple-600"
          >
            <span>📊</span> Xem Doanh Thu
          </a>
        </div>
      </div>
    </div>
  );
}

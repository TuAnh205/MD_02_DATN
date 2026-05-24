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
        totalProducts: productsRes.data?.meta?.total || 0,
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
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="rounded-lg bg-gradient-to-r from-blue-500 to-cyan-500 p-6 text-white">
        <h2 className="text-2xl font-bold">Chào mừng, {user?.name}!</h2>
        <p className="mt-2 text-blue-100">
          Đây là tổng quan về hoạt động shop của bạn trong tháng này
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Total Revenue */}
        <div className="rounded-lg bg-white border border-gray-200 p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">
                Doanh Thu Tháng Này
              </p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                {formatCurrency(stats.totalRevenue)}
              </p>
            </div>
            <div className="text-4xl">💰</div>
          </div>
        </div>

        {/* Total Orders */}
        <div className="rounded-lg bg-white border border-gray-200 p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">
                Đơn Hàng Tháng Này
              </p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                {stats.totalOrders}
              </p>
            </div>
            <div className="text-4xl">📋</div>
          </div>
        </div>

        {/* Total Products */}
        <div className="rounded-lg bg-white border border-gray-200 p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Tổng Sản Phẩm</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">
                {stats.totalProducts}
              </p>
            </div>
            <div className="text-4xl">📦</div>
          </div>
        </div>
      </div>

      {/* Billing Status */}
      {billingSummary && (
        <div className="rounded-lg bg-white border border-gray-200 p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Trạng Thái Phí
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="rounded-md bg-blue-50 p-4 border border-blue-200">
              <p className="text-sm text-blue-700">Số Dư Ví</p>
              <p className="text-xl font-bold text-blue-900 mt-1">
                {formatCurrency(billingSummary.walletBalance || 0)}
              </p>
            </div>
            <div className="rounded-md bg-amber-50 p-4 border border-amber-200">
              <p className="text-sm text-amber-700">Công Nợ</p>
              <p className="text-xl font-bold text-amber-900 mt-1">
                {formatCurrency(billingSummary.outstandingAmount || 0)}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="rounded-lg bg-white border border-gray-200 p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          Các Thao Tác Nhanh
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <a
            href="/shop/products"
            className="rounded-md bg-blue-600 px-4 py-2 text-center text-white font-medium hover:bg-blue-700 transition"
          >
            ➕ Thêm Sản Phẩm
          </a>
          <a
            href="/shop/orders"
            className="rounded-md bg-green-600 px-4 py-2 text-center text-white font-medium hover:bg-green-700 transition"
          >
            📋 Xem Đơn Hàng
          </a>
          <a
            href="/shop/revenue"
            className="rounded-md bg-purple-600 px-4 py-2 text-center text-white font-medium hover:bg-purple-700 transition"
          >
            💹 Xem Doanh Thu
          </a>
          <a
            href="/shop/reviews"
            className="rounded-md bg-orange-600 px-4 py-2 text-center text-white font-medium hover:bg-orange-700 transition"
          >
            ⭐ Xem Đánh Giá
          </a>
          <a
            href="/shop/vouchers"
            className="rounded-md bg-red-600 px-4 py-2 text-center text-white font-medium hover:bg-red-700 transition"
          >
            🎫 Quản Lý Voucher
          </a>
        </div>
      </div>
    </div>
  );
}

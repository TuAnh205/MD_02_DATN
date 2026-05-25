import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";

export default function AdminHome() {
  const { user } = useAuth();
  const RECENT_ORDERS_LIMIT = 5;

  const [stats, setStats] = useState({
    users: 0,
    products: 0,
    orders: 0,
    reviews: 0,
    revenue: 0,
    pendingOrders: 0,
  });
  const [loading, setLoading] = useState(true);
  const [recentOrders, setRecentOrders] = useState([]);
  const [recentUsers, setRecentUsers] = useState([]);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [usersRes, productsRes, ordersRes, reviewsRes] = await Promise.all([
        api.get("/admin/users/count"),
        api.get("/admin/products/count"),
        api.get("/admin/orders/count"),
        api.get("/admin/reviews/count"),
      ]);

      // Fetch recent data
      const [ordersData, usersData] = await Promise.all([
        api.get("/admin/orders?page=1&limit=100"),
        api.get("/admin/users?page=1&limit=5"),
      ]);

      // Calculate revenue from paid orders
      const paidOrders = ordersData.data.orders.filter(
        (order) => order.payment?.status === "paid",
      );
      const totalRevenue = paidOrders.reduce(
        (sum, order) => sum + (order.total || 0),
        0,
      );

      setStats({
        users: usersRes.data.count,
        products: productsRes.data.count,
        orders: ordersRes.data.count,
        reviews: reviewsRes.data.count,
        revenue: totalRevenue,
        pendingOrders: ordersData.data.orders.filter(
          (order) => order.status === "pending",
        ).length,
      });

      const sortedOrders = [...ordersData.data.orders].sort((a, b) => {
        const dateA = new Date(a.createdAt || a.updatedAt || 0).getTime();
        const dateB = new Date(b.createdAt || b.updatedAt || 0).getTime();
        return dateB - dateA;
      });

      setRecentOrders(sortedOrders.slice(0, RECENT_ORDERS_LIMIT));
      setRecentUsers(usersData.data.users.slice(0, 5));
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      title: "Tổng Người dùng",
      value: stats.users,
      icon: "👥",
      color: "from-slate-600 to-slate-700",
      bgColor: "bg-slate-50",
      borderColor: "border-slate-200"
    },
    {
      title: "Tổng Sản phẩm",
      value: stats.products,
      icon: "📦",
      color: "from-emerald-600 to-emerald-700",
      bgColor: "bg-emerald-50",
      borderColor: "border-emerald-200"
    },
    {
      title: "Tổng Đơn hàng",
      value: stats.orders,
      icon: "📋",
      color: "from-blue-600 to-blue-700",
      bgColor: "bg-blue-50",
      borderColor: "border-blue-200"
    },
    {
      title: "Đơn chờ xử lý",
      value: stats.pendingOrders,
      icon: "⏳",
      color: "from-amber-600 to-amber-700",
      bgColor: "bg-amber-50",
      borderColor: "border-amber-200"
    },
    {
      title: "Tổng Đánh giá",
      value: stats.reviews,
      icon: "⭐",
      color: "from-rose-600 to-rose-700",
      bgColor: "bg-rose-50",
      borderColor: "border-rose-200"
    },
    {
      title: "Doanh thu",
      value: `₫${stats.revenue.toLocaleString()}`,
      icon: "💰",
      color: "from-violet-600 to-violet-700",
      bgColor: "bg-violet-50",
      borderColor: "border-violet-200"
    },
  ];

  const getStatusColor = (status) => {
    switch (status) {
      case "pending":
      case "chờ xác nhận":
        return "bg-amber-100 text-amber-700";
      case "confirmed":
      case "đã xác nhận":
        return "bg-blue-100 text-blue-700";
      case "shipped":
      case "đang giao":
        return "bg-sky-100 text-sky-700";
      case "delivered":
      case "đã nhận":
        return "bg-emerald-100 text-emerald-700";
      case "cancelled":
      case "đã hủy":
        return "bg-red-100 text-red-700";
      default:
        return "bg-slate-100 text-slate-700";
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
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-blue-600 via-blue-500 to-orange-500 rounded-3xl p-8 md:p-12 text-white shadow-lg overflow-hidden relative">
        <div className="absolute top-0 right-0 w-40 h-40 bg-white opacity-10 rounded-full -mr-20 -mt-20"></div>
        <div className="relative z-10">
          <h1 className="text-3xl md:text-4xl font-bold mb-3">
            Chào mừng trở lại, {user?.name?.split(' ')[0]}! 👋
          </h1>
          <p className="text-blue-100 text-base md:text-lg opacity-90">
            Quản lý và theo dõi toàn bộ hoạt động hệ thống bán hàng
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {statCards.map((stat, index) => (
          <div
            key={index}
            className={`${stat.bgColor} rounded-2xl p-6 border ${stat.borderColor} shadow-sm hover:shadow-xl hover:scale-105 transition-all duration-300 cursor-pointer`}
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <p className="text-xs font-bold text-gray-600 uppercase tracking-widest mb-3 letter-spacing">
                  {stat.title}
                </p>
                <p className="text-4xl font-bold text-gray-900">{stat.value}</p>
              </div>
              <div
                className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${stat.color} flex items-center justify-center text-white text-3xl shadow-lg flex-shrink-0`}
              >
                {stat.icon}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Orders */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between mb-6 pb-4 border-b-2 border-blue-100">
            <h3 className="text-lg font-bold text-slate-900 flex items-center">
              <span className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center text-white text-sm mr-3">📋</span>
              Đơn hàng gần đây
            </h3>
            <Link
              to="/admin/orders"
              className="text-blue-600 hover:text-blue-700 text-sm font-semibold hover:underline transition"
            >
              Xem tất cả →
            </Link>
          </div>
          <div className="space-y-3">
            {recentOrders.length > 0 ? (
              recentOrders.map((order) => (
                <div
                  key={order._id}
                  className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-orange-50 rounded-xl hover:from-blue-100 hover:to-orange-100 transition duration-150 border border-blue-100"
                >
                  <div className="flex items-center space-x-4 flex-1 min-w-0">
                    <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl flex items-center justify-center shadow-md flex-shrink-0">
                      <span className="text-white text-lg">📦</span>
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-bold text-slate-900 truncate">
                        Đơn #{order._id.slice(-6)}
                      </p>
                      <p className="text-xs text-slate-500 mt-1 truncate">
                        {order.user?.name || "N/A"}
                      </p>
                    </div>
                  </div>
                  <div className="text-right ml-4 flex-shrink-0">
                    <span
                      className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${getStatusColor(order.status)}`}
                    >
                      {order.status}
                    </span>
                    <p className="text-sm font-bold text-blue-600 mt-2">
                      {order.total?.toLocaleString()}₫
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-slate-500 text-center py-8">
                Chưa có đơn hàng nào
              </p>
            )}
          </div>
        </div>

        {/* Recent Users */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between mb-6 pb-4 border-b-2 border-orange-100">
            <h3 className="text-lg font-bold text-slate-900 flex items-center">
              <span className="w-8 h-8 bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg flex items-center justify-center text-white text-sm mr-3">👥</span>
              Thành viên mới
            </h3>
            <Link
              to="/admin/users"
              className="text-orange-600 hover:text-orange-700 text-sm font-semibold hover:underline transition"
            >
              Xem tất cả →
            </Link>
          </div>
          <div className="space-y-3">
            {recentUsers.length > 0 ? (
              recentUsers.map((user) => (
                <div
                  key={user._id}
                  className="flex items-center justify-between p-4 bg-gradient-to-r from-orange-50 to-yellow-50 rounded-xl hover:from-orange-100 hover:to-yellow-100 transition duration-150 border border-orange-100"
                >
                  <div className="flex items-center space-x-4 flex-1 min-w-0">
                    <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center shadow-md flex-shrink-0 text-white font-bold">
                      {user.name?.charAt(0)?.toUpperCase()}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-bold text-slate-900 truncate">
                        {user.name}
                      </p>
                      <p className="text-xs text-slate-500 mt-1 truncate">{user.email}</p>
                    </div>
                  </div>
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ml-4 flex-shrink-0 ${
                      user.role === "admin"
                        ? "bg-purple-200 text-purple-800"
                        : user.role === "shop"
                        ? "bg-green-200 text-green-800"
                        : "bg-blue-200 text-blue-800"
                    }`}
                  >
                    {user.role}
                  </span>
                </div>
              ))
            ) : (
              <p className="text-slate-500 text-center py-8">
                Chưa có người dùng mới
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 md:p-8">
        <h3 className="text-xl font-bold text-slate-900 mb-6 flex items-center">
          <span className="w-8 h-8 bg-gradient-to-br from-blue-500 to-orange-500 rounded-lg flex items-center justify-center text-white text-sm mr-3">⚡</span>
          Hành động nhanh
        </h3>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          <Link
            to="/admin/orders"
            className="flex flex-col items-center p-6 bg-gradient-to-br from-emerald-50 to-emerald-100 rounded-2xl hover:from-emerald-100 hover:to-emerald-200 transition-all duration-200 border border-emerald-200 hover:border-emerald-400 group shadow-sm hover:shadow-md"
          >
            <span className="text-4xl mb-3 group-hover:scale-110 transition-transform duration-200">📋</span>
            <span className="text-sm font-bold text-emerald-800 text-center">
              Xử lý đơn hàng
            </span>
          </Link>
          <Link
            to="/admin/users"
            className="flex flex-col items-center p-6 bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl hover:from-blue-100 hover:to-blue-200 transition-all duration-200 border border-blue-200 hover:border-blue-400 group shadow-sm hover:shadow-md"
          >
            <span className="text-4xl mb-3 group-hover:scale-110 transition-transform duration-200">👥</span>
            <span className="text-sm font-bold text-blue-800 text-center">
              Quản lý tài khoản
            </span>
          </Link>
          <Link
            to="/admin/revenue"
            className="flex flex-col items-center p-6 bg-gradient-to-br from-violet-50 to-violet-100 rounded-2xl hover:from-violet-100 hover:to-violet-200 transition-all duration-200 border border-violet-200 hover:border-violet-400 group shadow-sm hover:shadow-md"
          >
            <span className="text-4xl mb-3 group-hover:scale-110 transition-transform duration-200">💰</span>
            <span className="text-sm font-bold text-violet-800 text-center">
              Xem doanh thu
            </span>
          </Link>
        </div>
      </div>
    </div>
  );
}

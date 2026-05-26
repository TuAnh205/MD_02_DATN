import React, { useState, useEffect } from "react";
import api from "../services/api";
import orderStatusEmitter from "../utils/orderStatusEmitter";

export default function ShopOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("all");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await api.get("/shop/orders");
      setOrders(response.data);
    } catch (error) {
      console.error("Error fetching orders:", error);
    } finally {
      setLoading(false);
    }
  };

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      await api.put(`/shop/orders/${orderId}/status`, { status: newStatus });
      fetchOrders();
      // Emit event to notify other components to refresh their data
      orderStatusEmitter.emit({ orderId, newStatus, timestamp: new Date() });
    } catch (error) {
      console.error("Error updating order status:", error);
      alert("Có lỗi xảy ra khi cập nhật trạng thái đơn hàng");
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "chờ xác nhận":
        return "bg-yellow-100 text-yellow-800";
      case "đã xác nhận":
        return "bg-blue-100 text-blue-800";
      case "đang giao":
        return "bg-purple-100 text-purple-800";
      case "đã nhận":
        return "bg-green-100 text-green-800";
      case "đã hủy":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case "chờ xác nhận":
        return "Chờ xác nhận";
      case "đã xác nhận":
        return "Đã xác nhận";
      case "đang giao":
        return "Đang giao";
      case "đã nhận":
        return "Đã giao";
      case "đã hủy":
        return "Đã hủy";
      default:
        return status;
    }
  };

  const getShopOrderTotal = (order) => {
    // Sử dụng total từ backend thay vì tính lại từ items
    return order.total || 0;
  };

  // Map filter value (English) sang status TV trong DB
  const statusFilterMap = {
    pending: "chờ xác nhận",
    confirmed: "đã xác nhận",
    shipped: "đang giao",
    delivered: "đã nhận",
    cancelled: "đã hủy",
  };

  const filteredOrders =
    statusFilter === "all"
      ? orders
      : orders.filter(
          (o) => o.status === (statusFilterMap[statusFilter] || statusFilter),
        );

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
      {/* Header */}
      <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">📋 Đơn Hàng</h1>
            <p className="text-slate-600 mt-2 font-semibold">
              Quản lý và cập nhật trạng thái các đơn hàng của shop
            </p>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 font-bold text-sm focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors"
          >
            <option value="all">📊 Tất cả trạng thái</option>
            <option value="pending">🔔 Chờ xác nhận ({orders.filter(o => o.status === "chờ xác nhận").length})</option>
            <option value="confirmed">✓ Đã xác nhận ({orders.filter(o => o.status === "đã xác nhận").length})</option>
            <option value="shipped">🚚 Đang giao ({orders.filter(o => o.status === "đang giao").length})</option>
            <option value="delivered">📦 Đã giao ({orders.filter(o => o.status === "đã nhận").length})</option>
            <option value="cancelled">❌ Đã hủy ({orders.filter(o => o.status === "đã hủy").length})</option>
          </select>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-blue-600">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Mã Đơn Hàng</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Khách Hàng</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Sản Phẩm</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Tổng Tiền</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Thanh Toán</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Trạng Thái</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Ngày Đặt</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {filteredOrders.map((order) => (
                <tr key={order._id} className="hover:bg-blue-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-bold text-slate-900 text-sm">#{order._id.slice(-8)}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-bold text-slate-900">{order.user?.name}</div>
                    <div className="text-xs text-slate-500 mt-1">{order.user?.email}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="font-bold text-slate-900 text-sm">{order.items?.length} sản phẩm</div>
                    <div className="text-xs text-slate-500 mt-1 line-clamp-2">
                      {order.items
                        ?.slice(0, 2)
                        .map((item) => item.product?.name || item.name)
                        .join(", ")}
                      {order.items?.length > 2 &&
                        ` +${order.items.length - 2}`}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-bold text-blue-600 text-sm">
                      ₫{getShopOrderTotal(order).toLocaleString("vi-VN")}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${
                        order.payment?.status === "paid"
                          ? "bg-green-200 text-green-900"
                          : "bg-amber-200 text-amber-900"
                      }`}
                    >
                      {order.payment?.status === "paid"
                        ? "✓ Đã TT"
                        : "⏳ Chưa TT"}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${
                        order.status === "chờ xác nhận"
                          ? "bg-amber-200 text-amber-900"
                          : order.status === "đã xác nhận"
                          ? "bg-blue-200 text-blue-900"
                          : order.status === "đang giao"
                          ? "bg-sky-200 text-sky-900"
                          : order.status === "đã nhận"
                          ? "bg-emerald-200 text-emerald-900"
                          : "bg-rose-200 text-rose-900"
                      }`}
                    >
                      {getStatusText(order.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-slate-900">
                      {new Date(order.createdAt).toLocaleDateString("vi-VN")}
                    </div>
                    <div className="text-xs text-slate-500 mt-1">
                      {new Date(order.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <select
                      value={order.status}
                      onChange={(e) =>
                        updateOrderStatus(order._id, e.target.value)
                      }
                      className="px-3 py-2 text-xs font-bold rounded-lg border-2 border-slate-200 bg-white text-slate-900 hover:border-blue-500 focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-all cursor-pointer"
                    >
                      <option value="chờ xác nhận">⏳ Chờ</option>
                      <option value="đã xác nhận">✓ Xác nhận</option>
                      <option value="đang giao">🚚 Giao</option>
                      <option value="đã nhận">📦 Nhận</option>
                      <option value="đã hủy">❌ Hủy</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredOrders.length === 0 && (
          <div className="text-center py-16">
            <div className="text-6xl mb-4 opacity-30">📦</div>
            <p className="text-slate-500 font-semibold text-lg">Chưa có đơn hàng nào</p>
            <p className="text-slate-400 text-sm mt-1">Các đơn hàng của khách hàng sẽ hiển thị ở đây</p>
          </div>
        )}
      </div>
    </div>
  );
}

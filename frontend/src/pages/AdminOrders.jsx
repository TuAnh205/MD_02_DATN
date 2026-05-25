import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState('all');
  const [totalRevenue, setTotalRevenue] = useState(0);

  useEffect(() => {
    fetchOrders();
  }, [page, statusFilter]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        page: page.toString(),
        limit: '10',
        ...(statusFilter !== 'all' && { status: statusFilter })
      });
      console.log('Fetching orders with params:', params.toString());
      const response = await api.get(`/admin/orders?${params}`);
      console.log('Orders response:', response.data);
      setOrders(response.data.orders);
      setTotalPages(response.data.pagination.pages);
      setTotalRevenue(response.data.totalRevenue || 0);
    } catch (error) {
      console.error('Error fetching orders:', error);
      console.error('Error response:', error.response);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'chờ xác nhận': return 'bg-amber-100 text-amber-700';
      case 'đã xác nhận': return 'bg-blue-100 text-blue-700';
      case 'đang giao': return 'bg-sky-100 text-sky-700';
      case 'đã nhận': return 'bg-emerald-100 text-emerald-700';
      case 'đã hủy': return 'bg-red-100 text-red-700';
      default: return 'bg-slate-100 text-slate-700';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'ch\u1edd x\u00e1c nh\u1eadn': return 'Ch\u1edd x\u00e1c nh\u1eadn';
      case '\u0111\u00e3 x\u00e1c nh\u1eadn': return '\u0110\u00e3 x\u00e1c nh\u1eadn';
      case '\u0111ang giao': return '\u0110ang giao';
      case '\u0111\u00e3 nh\u1eadn': return '\u0110\u00e3 giao';
      case '\u0111\u00e3 h\u1ee7y': return '\u0110\u00e3 h\u1ee7y';
      default: return status;
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
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-3xl shadow-lg p-8 text-white hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="text-sm font-bold opacity-80 uppercase tracking-widest">Tổng doanh thu</div>
              <div className="text-4xl font-bold mt-3">₫{totalRevenue.toLocaleString('vi-VN')}</div>
              <div className="text-sm opacity-75 mt-3">Từ tất cả đơn hàng</div>
            </div>
            <div className="text-5xl opacity-20">💰</div>
          </div>
        </div>
        <div className="bg-gradient-to-br from-emerald-600 to-emerald-700 rounded-3xl shadow-lg p-8 text-white hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="text-sm font-bold opacity-80 uppercase tracking-widest">Tổng đơn hàng</div>
              <div className="text-4xl font-bold mt-3">{orders.length}</div>
              <div className="text-sm opacity-75 mt-3">Trên trang hiện tại</div>
            </div>
            <div className="text-5xl opacity-20">📦</div>
          </div>
        </div>
        <div className="bg-gradient-to-br from-violet-600 to-violet-700 rounded-3xl shadow-lg p-8 text-white hover:shadow-xl transition-shadow duration-300">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="text-sm font-bold opacity-80 uppercase tracking-widest">Trung bình</div>
              <div className="text-4xl font-bold mt-3">₫{orders.length > 0 ? Math.round(orders.reduce((sum, o) => sum + (o.total || 0), 0) / orders.length).toLocaleString('vi-VN') : '0'}</div>
              <div className="text-sm opacity-75 mt-3">Giá trị trung bình / đơn</div>
            </div>
            <div className="text-5xl opacity-20">📊</div>
          </div>
        </div>
      </div>

      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Quản lý Đơn hàng</h1>
          <p className="text-slate-600 mt-2">Theo dõi và quản lý tất cả đơn hàng trong hệ thống</p>
        </div>
        <div className="flex items-center space-x-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border-2 border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-medium text-slate-700 hover:border-blue-400 transition-colors"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="pending">Chờ xác nhận</option>
            <option value="confirmed">Đã xác nhận</option>
            <option value="shipped">Đang giao</option>
            <option value="delivered">Đã giao</option>
            <option value="cancelled">Đã hủy</option>
          </select>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden hover:shadow-md transition-shadow">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-blue-600">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Mã đơn hàng
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Khách hàng
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Sản phẩm
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Tổng tiền
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Thanh toán
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Trạng thái
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Ngày đặt
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {orders.map((order, index) => (
                <tr key={order._id} className="hover:bg-blue-50 transition-colors duration-150">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-blue-600">#{order._id.slice(-8)}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-slate-900">
                      {order.user?.name || order.shipping?.address?.name || 'N/A'}
                    </div>
                    <div className="text-xs text-slate-500">
                      {order.user?.email || ''}
                    </div>
                    {!order.user && (
                      <div className="text-xs text-red-600 mt-1 font-semibold">⚠ Tài khoản bị xóa</div>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-semibold text-slate-900">
                      {order.items?.length} sản phẩm
                    </div>
                    <div className="text-xs text-slate-600 mt-1 line-clamp-2">
                      {order.items?.slice(0, 2).map(item => item.name).join(', ')}
                      {order.items?.length > 2 && ` +${order.items.length - 2}`}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-blue-600">
                      {order.total?.toLocaleString()}₫
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${
                      order.payment?.status === 'paid' 
                        ? 'bg-green-200 text-green-900' 
                        : 'bg-yellow-200 text-yellow-900'
                    }`}>
                      {order.payment?.status === 'paid' ? '✓ Thanh toán' : '⏳ Chưa TT'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${getStatusColor(order.status)}`}>
                      {getStatusText(order.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-semibold text-slate-900">
                      {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                    </div>
                    <div className="text-xs text-slate-500">
                      {new Date(order.createdAt).toLocaleTimeString('vi-VN')}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {orders.length === 0 && (
          <div className="text-center py-16">
            <div className="text-slate-300 text-7xl mb-4">📦</div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">Không có đơn hàng nào</h3>
            <p className="text-slate-600">Các đơn hàng sẽ xuất hiện ở đây khi khách hàng đặt hàng</p>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6">
          <button
            onClick={() => setPage(page - 1)}
            disabled={page === 1}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            ← Trước
          </button>
          <span className="text-sm font-bold text-slate-800">
            Trang <span className="text-blue-600">{page}</span> / <span className="text-slate-600">{totalPages}</span>
          </span>
          <button
            onClick={() => setPage(page + 1)}
            disabled={page === totalPages}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            Sau →
          </button>
        </div>
      )}
    </div>
  );
}
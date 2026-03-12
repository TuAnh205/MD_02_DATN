import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderService } from '../services/orderService';

export default function Orders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const data = await orderService.getOrders();
      setOrders(data);
    } catch (err) {
      setError('Không thể tải danh sách đơn hàng');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      'pending': 'bg-yellow-100 text-yellow-800',
      'confirmed': 'bg-blue-100 text-blue-800',
      'preparing': 'bg-purple-100 text-purple-800',
      'shipped': 'bg-cyan-100 text-cyan-800',
      'delivered': 'bg-green-100 text-green-800',
      'cancelled': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  const getStatusText = (status) => {
    const text = {
      'pending': 'Chờ xác nhận',
      'confirmed': 'Đã xác nhận',
      'preparing': 'Đang chuẩn bị',
      'shipped': 'Đã giao cho vận chuyển',
      'delivered': 'Đã giao',
      'cancelled': 'Đã hủy'
    };
    return text[status] || status;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-dark mb-2">Đơn Hàng Của Tôi</h1>
          <button
            onClick={() => navigate('/')}
            className="text-blue-600 hover:underline text-sm"
          >
            ← Quay lại trang chủ
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        {orders.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <p className="text-gray-500 text-lg mb-4">Chưa có đơn hàng nào</p>
            <button
              onClick={() => navigate('/')}
              className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90"
            >
              Quay về cửa hàng
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order._id} className="bg-white rounded-lg shadow p-6">
                <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Mã đơn hàng</p>
                    <p className="font-semibold text-dark">{order.orderNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Ngày đặt</p>
                    <p className="font-semibold text-dark">
                      {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tổng tiền</p>
                    <p className="font-semibold text-primary">
                      ₫{order.total?.toLocaleString('vi-VN')}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Trạng thái</p>
                    <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${getStatusColor(order.status)}`}>
                      {getStatusText(order.status)}
                    </span>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Thanh toán</p>
                    <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${order.payment.status === 'paid' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                      {order.payment.status === 'paid' ? 'Đã thanh toán' : 'Chưa thanh toán'}
                    </span>
                  </div>
                </div>

                {/* Items Preview */}
                <div className="border-t pt-4 mb-4">
                  <p className="text-sm font-semibold text-gray-600 mb-2">Sản phẩm:</p>
                  <div className="flex flex-wrap gap-2">
                    {order.items.map((item, idx) => (
                      <span key={idx} className="text-sm bg-gray-100 px-3 py-1 rounded">
                        {item.name} x{item.qty}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Actions */}
                <div className="flex gap-2">
                  <button
                    onClick={() => navigate(`/order-success/${order._id}`)}
                    className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                  >
                    Xem chi tiết
                  </button>
                  {order.status === 'pending' && (
                    <button className="text-red-600 hover:text-red-800 font-semibold text-sm">
                      Hủy đơn hàng
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

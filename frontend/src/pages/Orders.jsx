import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderService } from '../services/orderService';

const STATUS_TABS = [
  { label: 'Tất cả', value: '' },
  { label: 'Chờ xác nhận', value: 'chờ xác nhận' },
  { label: 'Đã xác nhận', value: 'đã xác nhận' },
  { label: 'Đang giao', value: 'đang giao' },
  { label: 'Đã nhận', value: 'đã nhận' },
  { label: 'Đã hủy', value: 'đã hủy' },
];

const STATUS_COLOR = {
  'chờ xác nhận': 'bg-yellow-100 text-yellow-800',
  'đã xác nhận': 'bg-blue-100 text-blue-800',
  'đang giao': 'bg-cyan-100 text-cyan-800',
  'đã nhận': 'bg-green-100 text-green-800',
  'đã hủy': 'bg-red-100 text-red-800',
  'trả hàng': 'bg-orange-100 text-orange-800',
  'hoàn tiền': 'bg-purple-100 text-purple-800',
};

export default function Orders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('');
  const [notification, setNotification] = useState(null);
  const prevStatusesRef = useRef({});

  useEffect(() => {
    fetchOrders();
  }, []);

  // Poll every 30s — show toast when any order status changes
  useEffect(() => {
    const id = setInterval(() => {
      orderService.getOrders().then(data => {
        data.forEach(order => {
          const prev = prevStatusesRef.current[order._id];
          if (prev && prev !== order.status) {
            setNotification(`Đơn hàng ${order.orderNumber} đã cập nhật: ${order.status}`);
            setTimeout(() => setNotification(null), 6000);
          }
        });
        prevStatusesRef.current = Object.fromEntries(data.map(o => [o._id, o.status]));
        setOrders(data);
      }).catch(() => {});
    }, 30000);
    return () => clearInterval(id);
  }, []);

  const fetchOrders = async () => {
    try {
      const data = await orderService.getOrders();
      setOrders(data);
      prevStatusesRef.current = Object.fromEntries(data.map(o => [o._id, o.status]));
    } catch (err) {
      setError('Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const filteredOrders = activeTab ? orders.filter(o => o.status === activeTab) : orders;

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        {notification && (
          <div className="mb-4 p-3 bg-blue-100 text-blue-800 rounded-lg flex items-center gap-2 shadow">
            <span>🔔</span>
            <span className="font-medium">{notification}</span>
            <button onClick={() => setNotification(null)} className="ml-auto text-blue-600 hover:text-blue-900 font-bold">✕</button>
          </div>
        )}

        <div className="mb-6">
          <h1 className="text-3xl font-bold text-dark mb-2">Đơn Hàng Của Tôi</h1>
          <button onClick={() => navigate('/')} className="text-blue-600 hover:underline text-sm">
            ← Quay lại trang chủ
          </button>
        </div>

        {/* Status filter tabs */}
        <div className="flex flex-wrap gap-2 mb-6">
          {STATUS_TABS.map(tab => (
            <button
              key={tab.value}
              onClick={() => setActiveTab(tab.value)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
                activeTab === tab.value
                  ? 'bg-blue-600 text-white shadow'
                  : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
              }`}
            >
              {tab.label}
              {tab.value && (
                <span className="ml-1.5 text-xs opacity-80">
                  ({orders.filter(o => o.status === tab.value).length})
                </span>
              )}
            </button>
          ))}
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">{error}</div>
        )}

        {filteredOrders.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <p className="text-gray-500 text-lg mb-4">
              {activeTab ? `Không có đơn hàng "${activeTab}"` : 'Chưa có đơn hàng nào'}
            </p>
            <button onClick={() => navigate('/')} className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90">
              Quay về cửa hàng
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.map((order) => (
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
                    <p className="font-semibold text-primary">₫{order.total?.toLocaleString('vi-VN')}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Trạng thái</p>
                    <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${STATUS_COLOR[order.status] || 'bg-gray-100 text-gray-800'}`}>
                      {order.status}
                    </span>
                    {order.status === 'đã hủy' && order.cancellationReason && (
                      <p className="text-xs text-red-600 mt-1">Lý do: {order.cancellationReason}</p>
                    )}
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Thanh toán</p>
                    <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${order.payment?.status === 'paid' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                      {order.payment?.status === 'paid' ? 'Đã thanh toán' : 'Chưa thanh toán'}
                    </span>
                  </div>
                </div>

                {/* Items Preview with thumbnails */}
                <div className="border-t pt-4 mb-4">
                  <p className="text-sm font-semibold text-gray-600 mb-2">Sản phẩm:</p>
                  <div className="flex flex-wrap gap-3">
                    {order.items.map((item, idx) => (
                      <div key={idx} className="flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
                        {item.image && (
                          <img
                            src={item.image}
                            alt={item.name}
                            className="w-10 h-10 object-cover rounded"
                            onError={e => { e.target.style.display = 'none'; }}
                          />
                        )}
                        <div>
                          <p className="text-sm font-medium text-gray-800">{item.name}</p>
                          <p className="text-xs text-gray-500">x{item.qty} · ₫{item.price?.toLocaleString('vi-VN')}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Actions */}
                <div className="flex gap-4">
                  <button
                    onClick={() => navigate(`/order-success/${order._id}`)}
                    className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                  >
                    Xem chi tiết →
                  </button>
                  {order.status === 'chờ xác nhận' && (
                    <button
                      onClick={async () => {
                        const ok = window.confirm('Bạn có chắc muốn hủy đơn hàng này?');
                        if (!ok) return;
                        const reason = window.prompt('Lý do hủy (tùy chọn):');
                        try {
                          await orderService.cancelOrder(order._id, reason || '');
                          fetchOrders();
                        } catch (err) {
                          setError('Không thể hủy đơn hàng');
                        }
                      }}
                      className="text-red-600 hover:text-red-800 font-semibold text-sm"
                    >
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

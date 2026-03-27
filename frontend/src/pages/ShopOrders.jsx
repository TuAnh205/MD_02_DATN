import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function ShopOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await api.get('/shop/orders');
      setOrders(response.data);
    } catch (error) {
      console.error('Error fetching orders:', error);
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
      <h1 className="text-2xl font-bold text-gray-900">Đơn hàng của Shop</h1>

      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        <ul className="divide-y divide-gray-200">
          {orders.map((order) => (
            <li key={order._id} className="px-6 py-4">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="text-sm font-medium text-gray-900">
                    Đơn hàng #{order.orderNumber}
                  </div>
                  <div className="text-sm text-gray-500">
                    Khách hàng: {order.user?.name || 'N/A'} • Tổng: {order.total?.toLocaleString('vi-VN')} VND
                  </div>
                  <div className="text-sm text-gray-500">
                    Trạng thái: <span className={`font-medium ${
                      order.status === 'delivered' ? 'text-green-600' :
                      order.status === 'shipped' ? 'text-blue-600' :
                      order.status === 'pending' ? 'text-yellow-600' :
                      'text-red-600'
                    }`}>{order.status}</span> • Ngày: {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                  </div>
                  <div className="text-sm text-gray-500 mt-1">
                    {order.items?.length} sản phẩm • Địa chỉ: {order.shipping?.address?.address}, {order.shipping?.address?.city}
                  </div>
                </div>
              </div>

              <div className="mt-4 bg-gray-50 p-3 rounded border border-gray-200">
                <h4 className="font-semibold mb-2">Chi tiết sản phẩm</h4>
                <ul className="space-y-2">
                  {order.items.map((item) => (
                    <li key={item.product?._id || item.product} className="flex justify-between items-start text-sm p-2 bg-white rounded">
                      <div className="w-2/3">
                        <p className="font-medium">{item.product?.name || item.name || 'Sản phẩm'}</p>
                        <p className="text-gray-500">SL: {item.qty} • Giá: {item.price?.toLocaleString('vi-VN')} VND</p>
                        <p className="text-gray-500">SKU: {item.sku || '---'}</p>
                      </div>
                      <div className="text-right text-gray-500">
                        <p>{item.product?.shopId ? `Shop: ${item.product.shopId.name || ''}` : ''}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </li>
          ))}
        </ul>
        {orders.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            Chưa có đơn hàng nào
          </div>
        )}
      </div>
    </div>
  );
}
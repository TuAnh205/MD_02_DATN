import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';

export default function Cart() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      setLoading(true);
      const data = await cartService.getCart();
      setCart(data);
    } catch (err) {
      setError('Không thể tải giỏ hàng');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateQuantity = async (itemId, newQuantity) => {
    if (newQuantity < 1) return;
    try {
      const updatedCart = await cartService.updateCartItem(itemId, newQuantity);
      setCart(updatedCart);
    } catch (err) {
      setError('Không thể cập nhật số lượng');
    }
  };

  const handleRemoveItem = async (itemId) => {
    try {
      const updatedCart = await cartService.removeFromCart(itemId);
      setCart(updatedCart);
    } catch (err) {
      setError('Không thể xóa sản phẩm');
    }
  };

  const handleClearCart = async () => {
    if (window.confirm('Bạn chắc chắn muốn xóa hết giỏ hàng?')) {
      try {
        const clearedCart = await cartService.clearCart();
        setCart(clearedCart);
      } catch (err) {
        setError('Không thể xóa giỏ hàng');
      }
    }
  };

  const calculateTotal = () => {
    if (!cart?.items) return 0;
    return cart.items.reduce((total, item) => total + (item.price * item.qty), 0);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải giỏ hàng...</div>
      </div>
    );
  }

  const total = calculateTotal();

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-dark mb-2">Giỏ Hàng</h1>
          <button
            onClick={() => navigate('/')}
            className="text-blue-600 hover:underline text-sm"
          >
            ← Tiếp tục mua sắm
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        {!cart || cart.items.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <p className="text-gray-500 text-lg mb-4">Giỏ hàng trống</p>
            <button
              onClick={() => navigate('/')}
              className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90"
            >
              Quay về cửa hàng
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Products List */}
            <div className="lg:col-span-2">
              <div className="bg-white rounded-lg shadow overflow-hidden">
                <table className="w-full">
                  <thead className="bg-gray-100 border-b">
                    <tr>
                      <th className="text-left px-6 py-3">Sản phẩm</th>
                      <th className="text-center px-6 py-3">Giá</th>
                      <th className="text-center px-6 py-3">Số lượng</th>
                      <th className="text-right px-6 py-3">Thành tiền</th>
                      <th className="text-center px-6 py-3">Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cart.items.map((item) => (
                      <tr key={item._id} className="border-b hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-4">
                            {(item.image) && (
                              <img
                                src={item.image}
                                alt={item.name}
                                className="w-16 h-16 object-cover rounded"
                              />
                            )}
                            <div>
                              <p className="font-semibold text-dark">{item.name}</p>
                              {item.variant && (
                                <p className="text-sm text-gray-500">{item.variant}</p>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center">
                          ₫{item.price?.toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center justify-center gap-2">
                            <button
                              onClick={() => handleUpdateQuantity(item._id, item.qty - 1)}
                              className="px-2 py-1 border rounded hover:bg-gray-100"
                            >
                              −
                            </button>
                            <input
                              type="number"
                              value={item.qty}
                              onChange={(e) => {
                                const newQty = parseInt(e.target.value) || 1;
                                handleUpdateQuantity(item._id, newQty);
                              }}
                              className="w-12 text-center border rounded py-1"
                              min="1"
                            />
                            <button
                              onClick={() => handleUpdateQuantity(item._id, item.qty + 1)}
                              className="px-2 py-1 border rounded hover:bg-gray-100"
                            >
                              +
                            </button>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-right font-semibold">
                          ₫{(item.price * item.qty)?.toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 text-center">
                          <button
                            onClick={() => handleRemoveItem(item._id)}
                            className="text-red-600 hover:text-red-800 text-sm"
                          >
                            Xóa
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {cart.items.length > 0 && (
                <button
                  onClick={handleClearCart}
                  className="mt-4 text-red-600 hover:text-red-800 font-semibold"
                >
                  Xóa hết giỏ hàng
                </button>
              )}
            </div>

            {/* Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow p-6 sticky top-4">
                <h2 className="text-xl font-bold text-dark mb-6">Tóm tắt đơn hàng</h2>

                <div className="space-y-4 mb-6">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Tạm tính:</span>
                    <span className="font-semibold">₫{total?.toLocaleString('vi-VN')}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Giao hàng:</span>
                    <span className="font-semibold text-green-600">Miễn phí</span>
                  </div>
                  <div className="border-t pt-4 flex justify-between">
                    <span className="text-lg font-bold">Tổng cộng:</span>
                    <span className="text-lg font-bold text-primary">
                      ₫{total?.toLocaleString('vi-VN')}
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => navigate('/checkout')}
                  className="w-full bg-primary text-white py-3 rounded font-semibold hover:bg-primary/90 mb-2"
                >
                  Thanh toán
                </button>
                <button
                  onClick={() => navigate('/')}
                  className="w-full border border-primary text-primary py-3 rounded font-semibold hover:bg-gray-50"
                >
                  Tiếp tục mua sắm
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

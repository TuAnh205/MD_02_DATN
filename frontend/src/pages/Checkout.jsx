import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { orderService } from '../services/orderService';

export default function Checkout() {
  const navigate = useNavigate();
  const [cartData, setCartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
    city: '',
    district: '',
    ward: '',
    paymentMethod: 'cod'
  });

  React.useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const cart = await cartService.getCart();
      if (!cart.items || cart.items.length === 0) {
        setError('Giỏ hàng trống, vui lòng thêm sản phẩm');
        return;
      }
      setCartData(cart);
    } catch (err) {
      setError('Không thể tải giỏ hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const calculateTotal = () => {
    if (!cartData?.items) return 0;
    return cartData.items.reduce((total, item) => total + (item.price * item.qty), 0);
  };

  const handleSubmitOrder = async (e) => {
    e.preventDefault();

    // Validation
    if (!formData.name || !formData.phone || !formData.address) {
      setError('Vui lòng điền đầy đủ thông tin giao hàng');
      return;
    }

    try {
      setLoading(true);

      const orderData = {
        items: cartData.items.map(item => {
          // Extract product ID whether it's an object or string
          const productId = typeof item.product === 'object' ? item.product._id : item.product;
          return {
            product: productId,
            name: item.name,
            price: item.price,
            qty: item.qty,
            image: item.image,
            sku: item.sku || ''
          };
        }),
        subtotal: calculateTotal(),
        total: calculateTotal(),
        payment: {
          method: formData.paymentMethod || 'cod',
          status: 'pending'
        },
        shipping: {
          address: {
            name: formData.name || '',
            phone: formData.phone || '',
            address: formData.address || '',
            city: formData.city || 'N/A',
            district: formData.district || 'N/A',
            ward: formData.ward || 'N/A'
          },
          method: 'standard',
          fee: 0
        }
      };

      const order = await orderService.createOrder(orderData);
      
      // Clear cart after successful order
      await cartService.clearCart();

      // Navigate to success page
      navigate(`/order-success/${order._id}`, { 
        state: { order } 
      });
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message;
      const errorDetails = err.response?.data?.details || [];
      setError(`Không thể tạo đơn hàng: ${errorMsg}${errorDetails.length > 0 ? '\n' + errorDetails.join('\n') : ''}`);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !cartData) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  if (error && !cartData) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl font-bold text-red-600 mb-4">{error}</p>
          <button
            onClick={() => navigate('/')}
            className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90"
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    );
  }

  const total = calculateTotal();

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-dark mb-2">Thanh Toán</h1>
          <button
            onClick={() => navigate('/cart')}
            className="text-blue-600 hover:underline text-sm"
          >
            ← Quay lại giỏ hàng
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Checkout Form */}
          <div className="lg:col-span-2">
            <form onSubmit={handleSubmitOrder}>
              {/* Shipping Information */}
              <div className="bg-white rounded-lg shadow p-6 mb-6">
                <h2 className="text-xl font-bold text-dark mb-6">Thông tin giao hàng</h2>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Họ và tên *</label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      required
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="Nguyễn Văn A"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Số điện thoại *</label>
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      required
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="0123456789"
                    />
                  </div>
                </div>

                <div className="mb-4">
                  <label className="block text-sm font-semibold mb-2">Địa chỉ *</label>
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    required
                    className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                    placeholder="123 Đường ABC, ..."
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Tỉnh/Thành phố</label>
                    <input
                      type="text"
                      name="city"
                      value={formData.city}
                      onChange={handleInputChange}
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="Hà Nội"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Quận/Huyện</label>
                    <input
                      type="text"
                      name="district"
                      value={formData.district}
                      onChange={handleInputChange}
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="Cầu Giấy"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Phường/Xã</label>
                    <input
                      type="text"
                      name="ward"
                      value={formData.ward}
                      onChange={handleInputChange}
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="Dịch Vọng"
                    />
                  </div>
                </div>
              </div>

              {/* Payment Method */}
              <div className="bg-white rounded-lg shadow p-6 mb-6">
                <h2 className="text-xl font-bold text-dark mb-6">Phương thức thanh toán</h2>

                <div className="space-y-3">
                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="cod"
                      checked={formData.paymentMethod === 'cod'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">Thanh toán khi nhận hàng (COD)</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50 opacity-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="momo"
                      disabled
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">MoMo (Sắp có)</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50 opacity-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="vnpay"
                      disabled
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">VNPay (Sắp có)</span>
                  </label>
                </div>
              </div>

              {/* Order Items */}
              <div className="bg-white rounded-lg shadow overflow-hidden">
                <div className="p-6 border-b">
                  <h2 className="text-xl font-bold text-dark">Chi tiết đơn hàng</h2>
                </div>
                <table className="w-full">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="text-left px-6 py-3">Sản phẩm</th>
                      <th className="text-center px-6 py-3">Giá</th>
                      <th className="text-center px-6 py-3">SL</th>
                      <th className="text-right px-6 py-3">Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cartData.items.map((item) => (
                      <tr key={item._id} className="border-b">
                        <td className="px-6 py-4">
                          <p className="font-semibold text-dark">{item.name}</p>
                        </td>
                        <td className="px-6 py-4 text-center">
                          ₫{item.price?.toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 text-center">{item.qty}</td>
                        <td className="px-6 py-4 text-right font-semibold">
                          ₫{(item.price * item.qty)?.toLocaleString('vi-VN')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </form>
          </div>

          {/* Order Summary */}
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
                onClick={handleSubmitOrder}
                disabled={loading}
                className="w-full bg-primary text-white py-3 rounded font-semibold hover:bg-primary/90 disabled:bg-gray-400"
              >
                {loading ? 'Đang xử lý...' : 'Đặt hàng'}
              </button>
              <button
                onClick={() => navigate('/cart')}
                className="w-full border border-primary text-primary py-3 rounded font-semibold hover:bg-gray-50 mt-2"
              >
                Quay lại giỏ hàng
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

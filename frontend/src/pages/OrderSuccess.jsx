import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderService } from '../services/orderService';

export default function OrderSuccess() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrder();
  }, [orderId]);

  const fetchOrder = async () => {
    try {
      const order = await orderService.getOrderById(orderId);
      setOrder(order);
    } catch (err) {
      setError('Không thể tải thông tin đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl font-bold text-red-600 mb-4">{error || 'Lỗi'}</p>
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

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        {/* Success / cancellation Message */}
        <div className="bg-white rounded-lg shadow p-8 text-center mb-8">
          <div className="mb-4">
            {order.status === 'cancelled' ? (
              <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 rounded-full mb-4">
                <svg className="w-8 h-8 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm-3-9l6 6m0-6l-6 6" clipRule="evenodd" />
                </svg>
              </div>
            ) : (
              <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
                <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
            )}
          </div>
          {order.status === 'cancelled' ? (
            <>
              <h1 className="text-3xl font-bold text-red-600 mb-2">Đơn hàng đã bị hủy</h1>
              {order.cancellationReason && (
                <p className="text-gray-600 mb-4">Lý do: {order.cancellationReason}</p>
              )}
            </>
          ) : (
            <>
              <h1 className="text-3xl font-bold text-dark mb-2">Đặt hàng thành công!</h1>
              <p className="text-gray-600 mb-4">Cảm ơn bạn đã mua hàng</p>
            </>
          )}
          <p className="text-lg font-semibold text-primary mb-6">
            Mã đơn hàng: {order.orderNumber}
          </p>
        </div>

        {/* Payment Confirmation Section */}
        {order.status !== 'cancelled' && (
          <div className={`rounded-lg shadow p-8 mb-8 ${order.payment.method === 'cod' ? 'bg-blue-50 border border-blue-200' : 'bg-amber-50 border border-amber-200'}`}>
            <h2 className="text-lg font-bold text-dark mb-4">Xác nhận thanh toán</h2>
            
            {order.payment.method === 'cod' ? (
              <>
                <div className="flex items-start gap-4">
                  <div className="text-2xl">📦</div>
                  <div>
                    <p className="font-semibold text-dark mb-2">Thanh toán khi nhận hàng (COD)</p>
                    <p className="text-gray-600 mb-2">
                      Bạn vui lòng thanh toán <span className="font-bold text-primary">₫{order.total?.toLocaleString('vi-VN')}</span> cho nhân viên giao hàng
                    </p>
                    <p className="text-sm text-gray-600">✓ Đơn hàng sẽ được giao trong vòng 3-5 ngày làm việc</p>
                  </div>
                </div>
              </>
            ) : order.payment.method === 'card' ? (
              <>
                <div className="flex items-start gap-4">
                  <div className="text-2xl">💳</div>
                  <div>
                    <p className="font-semibold text-dark mb-2">Thanh toán bằng Thẻ Tín Dụng/Ghi Nợ</p>
                    <div className="bg-white rounded p-4 mb-3 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Người thanh toán:</span>
                        <span className="font-semibold">{order.payment.cardholderName}</span>
                      </div>
                      {order.payment.cardLastFour && (
                        <div className="flex justify-between">
                          <span className="text-gray-600">Thẻ thanh toán:</span>
                          <span className="font-semibold">**** **** **** {order.payment.cardLastFour}</span>
                        </div>
                      )}
                      <div className="flex justify-between border-t pt-2 font-bold">
                        <span>Số tiền:</span>
                        <span className="text-primary">₫{order.total?.toLocaleString('vi-VN')}</span>
                      </div>
                    </div>
                    {order.payment.status === 'paid' ? (
                      <p className="text-sm text-green-700 bg-green-50 rounded p-2 border border-green-200">
                        ✅ Thanh toán thành công! Đơn hàng của bạn đã được xác nhận
                      </p>
                    ) : (
                      <p className="text-sm text-amber-700">
                        ⚠️ Vui lòng kiểm tra email hoặc sms để xác nhận thanh toán. Nếu không nhận được xác nhận, liên hệ ngay với chúng tôi
                      </p>
                    )}
                  </div>
                </div>
              </>
            ) : order.payment.method === 'bank' ? (
              <>
                <div className="flex items-start gap-4">
                  <div className="text-2xl">🏦</div>
                  <div>
                    <p className="font-semibold text-dark mb-2">Thanh toán bằng Chuyển Khoản Ngân Hàng</p>
                    <div className="bg-white rounded p-4 mb-3 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Ngân hàng:</span>
                        <span className="font-semibold">{order.payment.bankName}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Số tài khoản:</span>
                        <span className="font-semibold">{order.payment.accountNumber}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Chủ tài khoản:</span>
                        <span className="font-semibold">{order.payment.accountHolder}</span>
                      </div>
                      <div className="flex justify-between border-t pt-2 font-bold">
                        <span>Số tiền:</span>
                        <span className="text-primary">₫{order.total?.toLocaleString('vi-VN')}</span>
                      </div>
                    </div>
                    <p className="text-sm text-amber-700">
                      ⚠️ Vui lòng chuyển khoản trong vòng 24 giờ. Ghi nội dung: "ĐH-{order.orderNumber}". Sau khi nhận được chuyển khoản, hệ thống sẽ tự động xác nhận đơn hàng
                    </p>
                  </div>
                </div>
              </>
            ) : (
              <p className="text-gray-600">Phương thức thanh toán: {order.payment.method}</p>
            )}
          </div>
        )}

        {/* Order Details */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <h2 className="text-xl font-bold text-dark mb-6">Thông tin đơn hàng</h2>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div>
              <p className="text-sm text-gray-600">Trạng thái</p>
              <p className="font-semibold text-dark">
                {order.status === 'pending' ? 'Chờ xác nhận' : order.status}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Phương thức thanh toán</p>
              <p className="font-semibold text-dark">
                {order.payment.method === 'cod' ? 'Thanh toán khi nhận' : 
                 order.payment.method === 'card' ? 'Thẻ Tín Dụng' :
                 order.payment.method === 'bank' ? 'Chuyển Khoản' :
                 order.payment.method}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Trạng thái thanh toán</p>
              <p className={`font-semibold ${order.payment.status === 'paid' ? 'text-green-600' : 'text-yellow-600'}`}>
                {order.payment.status === 'paid' ? 'Đã thanh toán' : 'Chờ xác nhận'}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Ngày đặt</p>
              <p className="font-semibold text-dark">
                {new Date(order.createdAt).toLocaleDateString('vi-VN')}
              </p>
            </div>
          </div>

          {/* Shipping Info */}
          <div className="border-t pt-6">
            <h3 className="font-semibold text-dark mb-4">Địa chỉ giao hàng</h3>
            <p className="text-gray-600">
              {order.shipping.address.name} - {order.shipping.address.phone}
            </p>
            <p className="text-gray-600">
              {order.shipping.address.address}, {order.shipping.address.ward}, {order.shipping.address.district}, {order.shipping.address.city}
            </p>
          </div>
        </div>

        {/* Order Items */}
        <div className="bg-white rounded-lg shadow overflow-hidden mb-8">
          <div className="p-6 border-b">
            <h2 className="text-xl font-bold text-dark">Chi tiết sản phẩm</h2>
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
              {order.items.map((item, index) => (
                <tr key={index} className="border-b">
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

          <div className="p-6 bg-gray-50 flex justify-end">
            <div className="w-full md:w-1/3">
              <div className="flex justify-between mb-2">
                <span>Tạm tính:</span>
                <span>₫{order.subtotal?.toLocaleString('vi-VN')}</span>
              </div>
              <div className="flex justify-between mb-2">
                <span>Giao hàng:</span>
                <span>Miễn phí</span>
              </div>
              <div className="border-t pt-2 flex justify-between font-bold">
                <span>Tổng cộng:</span>
                <span className="text-primary text-lg">₫{order.total?.toLocaleString('vi-VN')}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-4">
          <button
            onClick={() => navigate('/')}
            className="flex-1 bg-primary text-white py-3 rounded font-semibold hover:bg-primary/90"
          >
            Tiếp tục mua sắm
          </button>
          <button
            onClick={() => navigate('/orders')}
            className="flex-1 border border-primary text-primary py-3 rounded font-semibold hover:bg-gray-50"
          >
            Xem đơn hàng của tôi
          </button>
          {(order.status === 'pending' || order.status === 'confirmed') && (
            <button
              onClick={async () => {
                const ok = window.confirm('Bạn có chắc muốn hủy đơn hàng này?');
                if (!ok) return;
                const reason = window.prompt('Lý do hủy (tùy chọn):');
                try {
                  await orderService.cancelOrder(order._id, reason || '');
                  // reload order so UI updates
                  fetchOrder();
                } catch (err) {
                  alert('Không thể hủy đơn hàng');
                  console.error(err);
                }
              }}
              className="flex-1 border border-red-600 text-red-600 py-3 rounded font-semibold hover:bg-red-50"
            >
              Hủy đơn hàng
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function ShopRevenue() {
  const { fetchProfile } = useAuth();
  const [revenueData, setRevenueData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('month');
  const [billingSummary, setBillingSummary] = useState(null);
  const [topUpAmount, setTopUpAmount] = useState('');
  const [processingPayment, setProcessingPayment] = useState(false);

  const formatCurrency = (value) => `₫${Number(value || 0).toLocaleString('vi-VN')}`;

  useEffect(() => {
    fetchRevenue();
    fetchBillingSummary();
  }, [period]);

  const fetchRevenue = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/shop/revenue?period=${period}`);
      setRevenueData(response.data);
    } catch (error) {
      console.error('Error fetching revenue:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchBillingSummary = async () => {
    try {
      const response = await api.get('/shop/billing-summary');
      setBillingSummary(response.data);
      await fetchProfile();
    } catch (error) {
      console.error('Error fetching billing summary:', error);
    }
  };

  const handleTopUp = async () => {
    try {
      setProcessingPayment(true);
      await api.post('/shop/wallet/top-up', { amount: Number(topUpAmount) });
      setTopUpAmount('');
      await Promise.all([fetchRevenue(), fetchBillingSummary()]);
    } catch (error) {
      alert(error.response?.data?.message || 'Không thể nạp ví');
    } finally {
      setProcessingPayment(false);
    }
  };

  const handleSettle = async () => {
    try {
      setProcessingPayment(true);
      await api.post('/shop/billing/settle');
      await Promise.all([fetchRevenue(), fetchBillingSummary()]);
    } catch (error) {
      alert(error.response?.data?.message || 'Không thể thanh toán công nợ');
    } finally {
      setProcessingPayment(false);
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
      {billingSummary?.summary && (
        <div className={`rounded-2xl border px-6 py-5 ${billingSummary.summary.isFrozen ? 'border-rose-200 bg-rose-50' : 'border-emerald-200 bg-emerald-50'}`}>
          <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <h2 className="text-xl font-bold text-gray-900">
                {billingSummary.summary.isFrozen ? 'Shop đang bị đóng băng bán hàng' : 'Ví shop và trạng thái thanh toán phí'}
              </h2>
              <p className="mt-2 text-sm text-gray-700">
                {billingSummary.summary.isFrozen
                  ? billingSummary.summary.message
                  : 'Hệ thống sẽ tự trừ phí 5% từ ví shop khi có đơn hàng thanh toán thành công từ 13/4/2026 18:02:24 trở đi.'}
              </p>
              <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
                <div className="rounded-xl bg-white/80 px-4 py-3 shadow-sm">
                  <p className="text-xs uppercase tracking-wide text-gray-500">Số dư ví</p>
                  <p className="mt-1 text-2xl font-bold text-gray-900">{formatCurrency(billingSummary.summary.walletBalance)}</p>
                </div>
                <div className="rounded-xl bg-white/80 px-4 py-3 shadow-sm">
                  <p className="text-xs uppercase tracking-wide text-gray-500">Công nợ hiện tại</p>
                  <p className="mt-1 text-2xl font-bold text-rose-600">{formatCurrency(billingSummary.summary.outstandingAmount)}</p>
                </div>
                <div className="rounded-xl bg-white/80 px-4 py-3 shadow-sm">
                  <p className="text-xs uppercase tracking-wide text-gray-500">Dòng đơn hàng chưa trả phí</p>
                  <p className="mt-1 text-2xl font-bold text-amber-600">{billingSummary.summary.dueProductCount}</p>
                </div>
              </div>
            </div>

            <div className="w-full max-w-md rounded-2xl bg-white p-4 shadow-sm">
              <p className="text-sm font-semibold text-gray-900">Nạp ví để hệ thống tự khấu trừ</p>
              <div className="mt-3 flex gap-2">
                <input
                  type="number"
                  min="0"
                  value={topUpAmount}
                  onChange={(e) => setTopUpAmount(e.target.value)}
                  placeholder="Nhập số tiền muốn nạp"
                  className="flex-1 rounded-md border border-gray-300 px-3 py-2"
                />
                <button
                  type="button"
                  onClick={handleTopUp}
                  disabled={processingPayment || !Number(topUpAmount)}
                  className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {processingPayment ? 'Đang xử lý...' : 'Nạp ví'}
                </button>
              </div>
              <button
                type="button"
                onClick={handleSettle}
                disabled={processingPayment}
                className="mt-3 w-full rounded-md border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-60"
              >
                Kiểm tra và tự thanh toán công nợ
              </button>
            </div>
          </div>

          {billingSummary.overdueItems?.length > 0 && (
            <div className="mt-5 rounded-xl bg-white p-4 shadow-sm">
              <p className="text-sm font-semibold text-gray-900">Các dòng đơn hàng đang nợ phí sàn</p>
              <div className="mt-3 space-y-2">
                {billingSummary.overdueItems.map((product) => (
                  <div key={product._id} className="flex flex-col gap-1 rounded-lg border border-gray-200 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="font-medium text-gray-900">{product.name} • Đơn {product.orderNumber}</p>
                      <p className="text-xs text-gray-500">Giá trị tính phí: {formatCurrency(product.baseAmount)} • Đã thanh toán lúc {new Date(product.paidAt).toLocaleString('vi-VN')}</p>
                    </div>
                    <div className="text-sm font-semibold text-rose-600">{formatCurrency(product.feeAmount)}</div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Thống kê Doanh thu</h1>
        <select
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
          className="border border-gray-300 rounded-md px-3 py-2"
        >
          <option value="day">Hôm nay</option>
          <option value="week">Tuần này</option>
          <option value="month">Tháng này</option>
          <option value="year">Năm này</option>
        </select>
      </div>

      {revenueData?.summary && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
            <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Tổng Tiền Bán</h3>
            <p className="text-3xl font-bold text-green-600">
              {formatCurrency(revenueData.summary.totalGrossRevenue)}
            </p>
            <p className="mt-2 text-xs text-gray-500">{revenueData.summary.totalOrders} đơn hàng</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow border-l-4 border-amber-500">
            <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Phí Sàn (5%)</h3>
            <p className="text-3xl font-bold text-amber-600">
              {formatCurrency(revenueData.summary.totalPlatformFees)}
            </p>
            <p className="mt-2 text-xs text-gray-500">{revenueData.summary.totalProducts} sản phẩm</p>
          </div>

          <div className="bg-white p-6 rounded-lg shadow border-l-4 border-emerald-500">
            <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Tổng Tiền Thực Nhận</h3>
            <p className="text-3xl font-bold text-emerald-600">
              {formatCurrency(revenueData.summary.totalNetRevenue)}
            </p>
            <p className="mt-2 text-xs text-gray-500">Sau khi trừ phí sàn</p>
          </div>

          <div className="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
            <h3 className="text-sm font-semibold text-gray-500 uppercase mb-2">Tỉ Lệ Phí</h3>
            <p className="text-3xl font-bold text-blue-600">
              {Math.round((revenueData.summary.platformFeeRate || 0.05) * 100)}%
            </p>
            <p className="mt-2 text-xs text-gray-500">Trên giá sản phẩm</p>
          </div>
        </div>
      )}

      {/* Chi tiết từng sản phẩm */}
      {revenueData?.productDetails && revenueData.productDetails.length > 0 ? (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">Chi tiết doanh thu từng sản phẩm</h3>
            <p className="text-sm text-gray-600 mt-1">Hiển thị chi tiết phí sàn 5% cho mỗi sản phẩm thanh toán thành công</p>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">Sản phẩm</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-700 uppercase tracking-wider">Giá</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-700 uppercase tracking-wider">Số lượng</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-700 uppercase tracking-wider">Tổng tiền</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-700 uppercase tracking-wider">Phí sàn (5%)</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-700 uppercase tracking-wider">Tiền thực nhận</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase tracking-wider">Đơn hàng</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {revenueData.productDetails.map((item, index) => (
                  <tr key={index} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        {item.productImage && (
                          <img src={item.productImage} alt={item.productName} className="w-10 h-10 rounded object-cover" />
                        )}
                        <div>
                          <p className="text-sm font-medium text-gray-900">{item.productName}</p>
                          <p className="text-xs text-gray-500">SKU: {item.sku || 'N/A'}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right text-sm text-gray-900 font-medium">{formatCurrency(item.price)}</td>
                    <td className="px-6 py-4 text-right text-sm text-gray-900">
                      <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-600 font-semibold">
                        {item.quantity}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm font-semibold text-gray-900">{formatCurrency(item.grossAmount)}</td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-block rounded-lg bg-amber-50 px-3 py-2 border border-amber-200">
                        <div className="text-sm font-semibold text-amber-700">{formatCurrency(item.platformFee)}</div>
                        <div className="text-xs text-amber-600">{Math.round(item.platformFeeRate * 100)}% phí sàn</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-block rounded-lg bg-emerald-50 px-3 py-2 border border-emerald-200">
                        <div className="text-sm font-bold text-emerald-700">{formatCurrency(item.netAmount)}</div>
                        <div className="text-xs text-emerald-600">Thực nhận</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-left">
                      <span className="inline-flex items-center rounded-full bg-blue-100 px-3 py-1 text-xs font-semibold text-blue-700">
                        {item.orderNumber}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gradient-to-r from-gray-50 to-gray-100 border-t-2 border-gray-300">
                <tr className="font-bold">
                  <td colSpan="3" className="px-6 py-4 text-right text-gray-900 text-base">
                    TỔNG CỘNG ({revenueData.summary.totalProducts} sản phẩm, {revenueData.summary.totalOrders} đơn):
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="text-lg text-gray-900">{formatCurrency(revenueData.summary.totalGrossRevenue)}</div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="inline-block rounded-lg bg-amber-100 px-3 py-2 border border-amber-300">
                      <div className="text-base text-amber-800">{formatCurrency(revenueData.summary.totalPlatformFees)}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="inline-block rounded-lg bg-emerald-100 px-3 py-2 border border-emerald-300">
                      <div className="text-base font-bold text-emerald-800">{formatCurrency(revenueData.summary.totalNetRevenue)}</div>
                    </div>
                  </td>
                  <td></td>
                </tr>
              </tfoot>
            </table>
          </div>

          {/* Ghi chú */}
          <div className="px-6 py-4 bg-blue-50 border-t border-gray-200">
            <p className="text-xs text-blue-700 flex items-start gap-2">
              <span className="text-sm mt-0.5">💡</span>
              <span>
                <strong>Lưu ý:</strong> Phí sàn <strong>5%</strong> được tính trên giá bán của sản phẩm khi khách hàng <strong>thanh toán thành công</strong>. 
                Hệ thống sẽ tự động trừ từ ví shop của bạn.
              </span>
            </p>
          </div>
        </div>
      ) : (
        <div className="bg-white p-12 rounded-lg shadow text-center">
          <span className="text-4xl mb-4">📊</span>
          <p className="text-lg text-gray-600 font-medium">Chưa có dữ liệu doanh thu</p>
          <p className="text-sm text-gray-500 mt-2">Dữ liệu sẽ hiển thị khi có sản phẩm thanh toán thành công</p>
        </div>
      )}
    </div>
  );
}
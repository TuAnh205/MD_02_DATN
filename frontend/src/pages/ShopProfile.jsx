import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const coretechVisuals = [
  {
    id: "ct-1",
    title: "CoreTech Retail Hub",
    image:
      "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-2",
    title: "CoreTech Device Lab",
    image:
      "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-3",
    title: "CoreTech Service Center",
    image:
      "https://images.unsplash.com/photo-1588702547923-7093a6c3ba33?auto=format&fit=crop&w=800&q=80",
  },
];

export default function ShopProfile() {
  const { user, fetchProfile } = useAuth();
  const [shopName, setShopName] = useState(user?.name || '');
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [billingSummary, setBillingSummary] = useState(null);
  const [revenueSummary, setRevenueSummary] = useState(null);
  const [showDepositModal, setShowDepositModal] = useState(false);
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);
  const [depositAmount, setDepositAmount] = useState('');
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [withdrawBank, setWithdrawBank] = useState('');
  const [withdrawAccountNumber, setWithdrawAccountNumber] = useState('');
  const [withdrawAccountName, setWithdrawAccountName] = useState('');
  const [transactionMessage, setTransactionMessage] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [billResponse, revenueResponse] = await Promise.all([
          api.get('/shop/billing-summary'),
          api.get('/shop/revenue?period=month')
        ]);
        setBillingSummary(billResponse.data.summary || null);
        setRevenueSummary(revenueResponse.data.summary || null);
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu:', err);
      }
    };
    loadData();
  }, []);

  const handleSaveName = async () => {
    try {
      setIsSaving(true);
      await api.put('/shop/profile', { name: shopName });
      setMessage('Cập nhật tên shop thành công!');
      setEditing(false);
      await fetchProfile();
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error updating shop name:', error);
      setMessage(error.response?.data?.message || 'Không thể cập nhật tên shop');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setShopName(user?.name || '');
    setEditing(false);
  };

  const handleDeposit = async () => {
    if (!depositAmount || isNaN(depositAmount) || depositAmount <= 0) {
      setTransactionMessage('Vui lòng nhập số tiền hợp lệ');
      return;
    }

    try {
      // Gọi API nạp tiền (cần setup endpoint trên backend)
      await api.post('/shop/wallet/deposit', {
        amount: parseFloat(depositAmount)
      });
      setTransactionMessage('✓ Nạp tiền thành công!');
      setDepositAmount('');
      setShowDepositModal(false);
      // Reload billing summary
      const response = await api.get('/shop/billing-summary');
      setBillingSummary(response.data.summary || null);
      setTimeout(() => setTransactionMessage(''), 3000);
    } catch (error) {
      console.error('Lỗi nạp tiền:', error);
      setTransactionMessage(error.response?.data?.message || 'Lỗi khi nạp tiền');
      setTimeout(() => setTransactionMessage(''), 3000);
    }
  };

  const handleWithdraw = async () => {
    if (!withdrawAmount || isNaN(withdrawAmount) || withdrawAmount <= 0) {
      setTransactionMessage('Vui lòng nhập số tiền hợp lệ');
      return;
    }

    if (!withdrawBank || !withdrawAccountNumber || !withdrawAccountName) {
      setTransactionMessage('Vui lòng điền đầy đủ thông tin ngân hàng');
      return;
    }

    if (withdrawAmount > billingSummary.walletBalance) {
      setTransactionMessage('Số tiền rút vượt quá số dư ví');
      return;
    }

    try {
      // Gọi API rút tiền (cần setup endpoint trên backend)
      await api.post('/shop/wallet/withdraw', {
        amount: parseFloat(withdrawAmount),
        bank: withdrawBank,
        accountNumber: withdrawAccountNumber,
        accountName: withdrawAccountName
      });
      setTransactionMessage('✓ Yêu cầu rút tiền thành công! Vui lòng chờ xác nhận.');
      setWithdrawAmount('');
      setWithdrawBank('');
      setWithdrawAccountNumber('');
      setWithdrawAccountName('');
      setShowWithdrawModal(false);
      // Reload billing summary
      const response = await api.get('/shop/billing-summary');
      setBillingSummary(response.data.summary || null);
      setTimeout(() => setTransactionMessage(''), 4000);
    } catch (error) {
      console.error('Lỗi rút tiền:', error);
      setTransactionMessage(error.response?.data?.message || 'Lỗi khi rút tiền');
      setTimeout(() => setTransactionMessage(''), 3000);
    }
  };

  const resetDepositForm = () => {
    setDepositAmount('');
    setShowDepositModal(false);
    setTransactionMessage('');
  };

  const resetWithdrawForm = () => {
    setWithdrawAmount('');
    setWithdrawBank('');
    setWithdrawAccountNumber('');
    setWithdrawAccountName('');
    setShowWithdrawModal(false);
    setTransactionMessage('');
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
      {/* CoreTech Demo Section */}
      <section className="mb-6 rounded-2xl overflow-hidden border border-blue-100 shadow-sm bg-white">
        <div className="relative">
          <div className="h-36 md:h-44 bg-gradient-to-r from-slate-900 via-blue-900 to-cyan-700" />
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.22),transparent_45%),radial-gradient(circle_at_80%_70%,rgba(255,255,255,0.15),transparent_40%)]" />

          <div className="absolute left-5 top-5 flex items-center gap-2">
            <span className="px-3 py-1 rounded-full bg-white/20 text-white text-xs font-semibold tracking-wide backdrop-blur">
              CORETECH VERIFIED SHOP
            </span>
            <span className="px-3 py-1 rounded-full bg-emerald-400/90 text-slate-900 text-xs font-bold">
              MANAGED BY CORETECH
            </span>
          </div>

          <div className="absolute left-5 bottom-5 right-5 text-white">
            <h2 className="text-xl md:text-2xl font-bold">
              {user?.name} thuộc hệ thống vận hành CORETECH
            </h2>
            <p className="text-sm text-white/90 mt-1">
              Không gian quản trị dành riêng cho đối tác shop chính thức
              trong mạng lưới CORETECH Commerce.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3 bg-slate-50">
          {coretechVisuals.map((item) => (
            <div
              key={item.id}
              className="relative rounded-xl overflow-hidden h-24 group"
            >
              <img
                src={item.image}
                alt={item.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
              <p className="absolute left-3 bottom-2 text-xs font-semibold text-white tracking-wide">
                {item.title}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Thông tin chi tiết Shop</h1>
        <p className="text-gray-600 mt-1">Quản lý thông tin cơ bản của shop</p>
      </div>

      {/* Message Alert */}
      {message && (
        <div className={`p-4 rounded-lg ${message.includes('thành công') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
          {message}
        </div>
      )}

      {/* Profile Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {/* Header Section */}
        <div className="h-32 bg-gradient-to-r from-blue-500 to-blue-600"></div>

        {/* Content Section */}
        <div className="px-6 pb-6">
          {/* Avatar and Name */}
          <div className="flex flex-col sm:flex-row sm:items-end sm:gap-4 -mt-16 mb-6">
            <div className="w-24 h-24 bg-blue-500 rounded-full flex items-center justify-center text-white font-bold text-4xl border-4 border-white shadow-md">
              {user?.name?.charAt(0)?.toUpperCase()}
            </div>
            <div className="flex-1">
              {editing ? (
                <div className="flex gap-2 items-end">
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-gray-500 mb-1">Tên Shop</label>
                    <input
                      type="text"
                      value={shopName}
                      onChange={(e) => setShopName(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <button
                    onClick={handleSaveName}
                    disabled={isSaving}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                  >
                    {isSaving ? 'Đang lưu...' : 'Lưu'}
                  </button>
                  <button
                    onClick={handleCancel}
                    disabled={isSaving}
                    className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:opacity-50"
                  >
                    Hủy
                  </button>
                </div>
              ) : (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">{shopName}</h2>
                  <p className="text-sm text-gray-500 mt-1">{user?.email}</p>
                  <button
                    onClick={() => setEditing(true)}
                    className="mt-2 text-sm text-blue-600 hover:text-blue-700 font-medium"
                  >
                    ✏️ Chỉnh sửa tên shop
                  </button>
                </div>
              )}
            </div>
          </div>

          <hr className="my-6" />

          {/* Account Info */}
          <div>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Thông tin tài khoản</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Email</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {user?.email}
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Vai trò</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  Shop
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Ngày tham gia</label>
                <div className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A'}
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Trạng thái</label>
                <div className="px-3 py-2 bg-green-50 border border-green-200 rounded-lg text-green-700 font-medium">
                  ✓ Hoạt động
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Wallet Information Section */}
      {billingSummary && (
        <div className="rounded-2xl overflow-hidden shadow-sm border-2 border-slate-200 bg-white">
          <div className="bg-gradient-to-r from-slate-100 to-slate-50 px-6 py-4 border-b-2 border-slate-200">
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <span>💰</span> Thông Tin Ví Shop
            </h2>
            <p className="text-sm text-slate-600 mt-1">Quản lý số dư ví và công nợ của shop</p>
          </div>

          <div className="p-6 space-y-6">
            {/* Main Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Wallet Balance */}
              <div className="rounded-xl bg-gradient-to-br from-blue-50 to-cyan-50 border-2 border-blue-200 p-5">
                <div className="flex items-center justify-between mb-3">
                  <p className="text-xs font-bold text-blue-700 uppercase tracking-widest">💳 Số Dư Ví</p>
                  <span className="text-2xl">🏦</span>
                </div>
                <p className="text-3xl font-bold text-blue-900">
                  {Number(billingSummary.walletBalance || 0).toLocaleString("vi-VN")}đ
                </p>
                <p className="text-xs text-blue-600 mt-2 font-medium">Số tiền hiện có trong ví shop</p>
              </div>

              {/* Monthly Revenue */}
              <div className="rounded-xl bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-200 p-5">
                <div className="flex items-center justify-between mb-3">
                  <p className="text-xs font-bold text-green-700 uppercase tracking-widest">📈 Doanh Thu Tháng</p>
                  <span className="text-2xl">📊</span>
                </div>
                <p className="text-3xl font-bold text-green-900">
                  {Number(revenueSummary?.totalRevenue || 0).toLocaleString("vi-VN")}đ
                </p>
                <p className="text-xs text-green-600 mt-2 font-medium">Tổng doanh thu tháng này</p>
              </div>

              {/* Outstanding Debt */}
              <div className={`rounded-xl bg-gradient-to-br border-2 p-5 ${
                billingSummary.outstandingAmount > 0
                  ? 'from-rose-50 to-pink-50 border-rose-200'
                  : 'from-emerald-50 to-green-50 border-emerald-200'
              }`}>
                <div className="flex items-center justify-between mb-3">
                  <p className={`text-xs font-bold uppercase tracking-widest ${
                    billingSummary.outstandingAmount > 0
                      ? 'text-rose-700'
                      : 'text-emerald-700'
                  }`}>
                    {billingSummary.outstandingAmount > 0 ? '⚠️ Công Nợ' : '✓ Không Có Công Nợ'}
                  </p>
                  <span className="text-2xl">{billingSummary.outstandingAmount > 0 ? '💸' : '✅'}</span>
                </div>
                <p className={`text-3xl font-bold ${
                  billingSummary.outstandingAmount > 0
                    ? 'text-rose-900'
                    : 'text-emerald-900'
                }`}>
                  {Number(billingSummary.outstandingAmount || 0).toLocaleString("vi-VN")}đ
                </p>
                <p className={`text-xs mt-2 font-medium ${
                  billingSummary.outstandingAmount > 0
                    ? 'text-rose-600'
                    : 'text-emerald-600'
                }`}>
                  {billingSummary.outstandingAmount > 0 ? 'Phí sàn chưa thanh toán' : 'Tài khoản thanh toán đầy đủ'}
                </p>
              </div>
            </div>

            {/* Status and Details */}
            <div className="border-t-2 border-slate-200 pt-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Account Status */}
                <div className="rounded-xl bg-slate-50 border-2 border-slate-200 p-4">
                  <p className="text-xs font-bold text-slate-700 uppercase tracking-widest mb-2">🔐 Trạng Thái Tài Khoản</p>
                  <div className="flex items-center gap-2">
                    <span className={`inline-flex items-center gap-1 px-3 py-2 rounded-lg font-bold text-sm ${
                      billingSummary.isFrozen
                        ? 'bg-rose-100 text-rose-900 border border-rose-300'
                        : 'bg-green-100 text-green-900 border border-green-300'
                    }`}>
                      <span>{billingSummary.isFrozen ? '🔒' : '✓'}</span>
                      {billingSummary.isFrozen ? 'Bị khóa' : 'Hoạt động bình thường'}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 mt-2">
                    {billingSummary.isFrozen ? 'Vui lòng thanh toán công nợ để mở khóa tài khoản' : 'Tài khoản đang hoạt động bình thường'}
                  </p>
                </div>

                {/* Payment Policy */}
                <div className="rounded-xl bg-blue-50 border-2 border-blue-200 p-4">
                  <p className="text-xs font-bold text-blue-700 uppercase tracking-widest mb-2">📋 Chính Sách Phí</p>
                  <div className="text-sm text-blue-900 space-y-1">
                    <p className="flex items-start gap-2">
                      <span className="font-bold">✓</span>
                      <span>Phí sàn: <span className="font-bold">5%</span> trên mỗi đơn hàng</span>
                    </p>
                    <p className="flex items-start gap-2">
                      <span className="font-bold">✓</span>
                      <span>Tính khi khách hàng <span className="font-bold">thanh toán thành công</span></span>
                    </p>
                    <p className="flex items-start gap-2">
                      <span className="font-bold">✓</span>
                      <span>Tự động trừ từ ví shop của bạn</span>
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Detailed Info Table */}
            <div className="border-t-2 border-slate-200 pt-6">
              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-widest mb-4">Chi Tiết Tài Chính</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div className="rounded-lg bg-slate-50 border-2 border-slate-200 p-3 text-center">
                  <p className="text-xs text-slate-600 font-semibold mb-1">Lần Thanh Toán Gần Nhất</p>
                  <p className="text-sm font-bold text-slate-900">
                    {billingSummary.lastPaymentDate ? new Date(billingSummary.lastPaymentDate).toLocaleDateString('vi-VN') : 'Chưa thanh toán'}
                  </p>
                </div>
                <div className="rounded-lg bg-slate-50 border-2 border-slate-200 p-3 text-center">
                  <p className="text-xs text-slate-600 font-semibold mb-1">Tổng Phí Đã Trừ</p>
                  <p className="text-sm font-bold text-slate-900">
                    {Number(billingSummary.totalFeesPaid || 0).toLocaleString("vi-VN")}đ
                  </p>
                </div>
                <div className="rounded-lg bg-slate-50 border-2 border-slate-200 p-3 text-center">
                  <p className="text-xs text-slate-600 font-semibold mb-1">Số Đơn Hàng Tháng Này</p>
                  <p className="text-sm font-bold text-slate-900">
                    {revenueSummary?.totalOrders || 0} đơn
                  </p>
                </div>
                <div className="rounded-lg bg-slate-50 border-2 border-slate-200 p-3 text-center">
                  <p className="text-xs text-slate-600 font-semibold mb-1">Phí Trung Bình/Đơn</p>
                  <p className="text-sm font-bold text-slate-900">
                    {Number((revenueSummary?.totalRevenue || 0) * 0.05 / Math.max(1, revenueSummary?.totalOrders || 1)).toLocaleString("vi-VN")}đ
                  </p>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="border-t-2 border-slate-200 pt-6 flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => setShowDepositModal(true)}
                className="flex-1 flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 text-white px-6 py-3 font-bold hover:shadow-lg transition-all duration-200"
              >
                <span>💳</span> Nạp Tiền
              </button>
              <button
                onClick={() => setShowWithdrawModal(true)}
                disabled={!billingSummary.walletBalance || billingSummary.walletBalance <= 0}
                className="flex-1 flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-emerald-600 to-emerald-700 text-white px-6 py-3 font-bold hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span>🏦</span> Rút Tiền
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Transaction Message Alert */}
      {transactionMessage && (
        <div className={`fixed bottom-4 right-4 rounded-xl px-6 py-4 font-bold shadow-lg border-2 z-50 animate-pulse ${
          transactionMessage.includes('✓') || transactionMessage.includes('thành công')
            ? 'bg-gradient-to-r from-green-50 to-emerald-50 border-green-300 text-green-900'
            : 'bg-gradient-to-r from-rose-50 to-pink-50 border-rose-300 text-rose-900'
        }`}>
          {transactionMessage}
        </div>
      )}

      {/* Deposit Modal */}
      {showDepositModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full border-2 border-blue-200">
            <div className="bg-gradient-to-r from-blue-100 to-cyan-100 px-6 py-4 border-b-2 border-blue-200">
              <h3 className="text-xl font-bold text-blue-900 flex items-center gap-2">
                <span>💳</span> Nạp Tiền Vào Ví
              </h3>
              <p className="text-sm text-blue-700 mt-1">Điền số tiền bạn muốn nạp</p>
            </div>

            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold text-slate-900 mb-2">Số Tiền *</label>
                <input
                  type="number"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                  placeholder="VD: 500000"
                  className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent"
                  min="0"
                  step="1000"
                />
                <p className="text-xs text-slate-500 mt-2">Số tiền tối thiểu: 10.000đ</p>
              </div>

              <div className="bg-blue-50 border-2 border-blue-200 rounded-xl p-4">
                <p className="text-sm text-blue-900">
                  <span className="font-bold">📌 Lưu ý:</span> Hệ thống sẽ chuyển hướng bạn tới cổng thanh toán an toàn
                </p>
              </div>

              <div className="flex gap-3 pt-4 border-t-2 border-slate-200">
                <button
                  onClick={resetDepositForm}
                  className="flex-1 rounded-xl bg-slate-100 text-slate-900 px-4 py-3 font-bold hover:bg-slate-200 transition-colors"
                >
                  ✕ Hủy
                </button>
                <button
                  onClick={handleDeposit}
                  className="flex-1 rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 text-white px-4 py-3 font-bold hover:shadow-lg transition-all duration-200"
                >
                  ✓ Nạp Tiền
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Withdraw Modal */}
      {showWithdrawModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full border-2 border-emerald-200">
            <div className="bg-gradient-to-r from-emerald-100 to-green-100 px-6 py-4 border-b-2 border-emerald-200">
              <h3 className="text-xl font-bold text-emerald-900 flex items-center gap-2">
                <span>🏦</span> Rút Tiền Từ Ví
              </h3>
              <p className="text-sm text-emerald-700 mt-1">Điền thông tin tài khoản ngân hàng</p>
            </div>

            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold text-slate-900 mb-2">Số Tiền *</label>
                <input
                  type="number"
                  value={withdrawAmount}
                  onChange={(e) => setWithdrawAmount(e.target.value)}
                  placeholder="VD: 500000"
                  max={billingSummary.walletBalance}
                  className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                  min="0"
                  step="1000"
                />
                <p className="text-xs text-slate-500 mt-2">Số dư hiện tại: {Number(billingSummary.walletBalance || 0).toLocaleString("vi-VN")}đ</p>
              </div>

              <div>
                <label className="block text-sm font-bold text-slate-900 mb-2">Tên Ngân Hàng *</label>
                <input
                  type="text"
                  value={withdrawBank}
                  onChange={(e) => setWithdrawBank(e.target.value)}
                  placeholder="VD: Techcombank"
                  className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-bold text-slate-900 mb-2">Số Tài Khoản *</label>
                <input
                  type="text"
                  value={withdrawAccountNumber}
                  onChange={(e) => setWithdrawAccountNumber(e.target.value)}
                  placeholder="VD: 1234567890"
                  className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-bold text-slate-900 mb-2">Tên Chủ Tài Khoản *</label>
                <input
                  type="text"
                  value={withdrawAccountName}
                  onChange={(e) => setWithdrawAccountName(e.target.value)}
                  placeholder="VD: Nguyễn Văn A"
                  className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                />
              </div>

              <div className="bg-amber-50 border-2 border-amber-200 rounded-xl p-4">
                <p className="text-sm text-amber-900">
                  <span className="font-bold">⏱️ Thời gian xử lý:</span> 1-3 ngày làm việc
                </p>
              </div>

              <div className="flex gap-3 pt-4 border-t-2 border-slate-200">
                <button
                  onClick={resetWithdrawForm}
                  className="flex-1 rounded-xl bg-slate-100 text-slate-900 px-4 py-3 font-bold hover:bg-slate-200 transition-colors"
                >
                  ✕ Hủy
                </button>
                <button
                  onClick={handleWithdraw}
                  className="flex-1 rounded-xl bg-gradient-to-r from-emerald-600 to-emerald-700 text-white px-4 py-3 font-bold hover:shadow-lg transition-all duration-200"
                >
                  ✓ Rút Tiền
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

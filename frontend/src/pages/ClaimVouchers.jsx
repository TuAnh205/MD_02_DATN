import React, { useEffect, useState } from 'react';
import { voucherService } from '../services/voucherService';
import { useAuth } from '../context/AuthContext';

export default function ClaimVouchers() {
  const { user } = useAuth();
  const [availableVouchers, setAvailableVouchers] = useState([]);
  const [myVouchers, setMyVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [claimCode, setClaimCode] = useState('');

  useEffect(() => {
    if (user) {
      fetchVouchers();
    }
  }, [user]);

  const fetchVouchers = async () => {
    try {
      setLoading(true);
      const [available, my] = await Promise.all([
        voucherService.getAvailableVouchers(),
        voucherService.getMyVouchers()
      ]);
      setAvailableVouchers(available.vouchers || []);
      setMyVouchers(my.vouchers || []);
    } catch (err) {
      setError('Lỗi tải voucher: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleClaimVoucher = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!claimCode.trim()) {
      setError('Vui lòng nhập mã voucher');
      return;
    }

    try {
      await voucherService.claimVoucher(claimCode);
      setSuccess('Nhận voucher thành công!');
      setClaimCode('');
      await fetchVouchers();
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể nhận voucher');
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Đang tải...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Claim Voucher Section */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <h1 className="text-3xl font-bold text-dark mb-6">Nhận Voucher Mới</h1>
          
          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
              {error}
            </div>
          )}

          {success && (
            <div className="mb-4 p-4 bg-green-100 text-green-700 rounded">
              {success}
            </div>
          )}

          <form onSubmit={handleClaimVoucher} className="flex gap-3 mb-6">
            <input
              type="text"
              value={claimCode}
              onChange={(e) => setClaimCode(e.target.value.toUpperCase())}
              placeholder="Nhập mã voucher..."
              className="flex-1 border rounded px-4 py-3 font-semibold text-lg"
            />
            <button
              type="submit"
              className="bg-primary text-white px-8 py-3 rounded font-semibold hover:bg-primary/90 whitespace-nowrap"
            >
              Nhận Voucher
            </button>
          </form>

          <p className="text-sm text-gray-600">
            💡 Nhập mã voucher được cung cấp bởi shop để nhận ưu đãi độc quyền
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* My Vouchers */}
          <div className="bg-white rounded-lg shadow p-8">
            <h2 className="text-2xl font-bold text-dark mb-6">Voucher Của Tôi ({myVouchers.length})</h2>
            
            {myVouchers.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <p>Bạn chưa nhận voucher nào</p>
                <p className="text-sm mt-2">Nhập mã voucher ở trên để nhận ưu đãi!</p>
              </div>
            ) : (
              <div className="space-y-4">
                {myVouchers.map((voucher) => {
                  const now = new Date();
                  const endDate = new Date(voucher.endDate);
                  const isExpired = endDate < now;

                  return (
                    <div
                      key={voucher._id}
                      className={`border-l-4 p-4 rounded ${
                        isExpired
                          ? 'border-gray-300 bg-gray-50'
                          : 'border-primary bg-blue-50'
                      }`}
                    >
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <h3 className="font-bold text-lg">{voucher.name}</h3>
                          <p className="text-xs text-blue-600 font-semibold mb-1">
                            🏪 {voucher.shop?.name || 'Shop'}
                          </p>
                          <p className="text-sm text-gray-600">{voucher.description}</p>
                        </div>
                        {isExpired && (
                          <span className="bg-red-100 text-red-700 px-3 py-1 rounded text-xs font-semibold">
                            Đã hết hạn
                          </span>
                        )}
                      </div>

                      <div className="bg-white rounded p-3 mb-3">
                        <p className="text-center font-mono font-bold text-lg text-primary">
                          {voucher.code}
                        </p>
                      </div>

                      <div className="grid grid-cols-2 gap-2 text-sm">
                        <div>
                          <span className="text-gray-600">Giảm:</span>
                          <p className="font-semibold">
                            {voucher.value}
                            {voucher.type === 'percentage' ? '%' : '₫'}
                          </p>
                        </div>
                        <div>
                          <span className="text-gray-600">Tối thiểu:</span>
                          <p className="font-semibold">
                            ₫{(voucher.minOrderValue || 0).toLocaleString('vi-VN')}
                          </p>
                        </div>
                        <div>
                          <span className="text-gray-600">Hạn dùng:</span>
                          <p className="font-semibold">
                            {new Date(voucher.endDate).toLocaleDateString('vi-VN')}
                          </p>
                        </div>
                        <div>
                          <span className="text-gray-600">Đã dùng:</span>
                          <p className="font-semibold">
                            {voucher.usedCount || 0}
                            {voucher.userLimit ? `/${voucher.userLimit}` : ''}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Available Shop Vouchers */}
          <div className="bg-white rounded-lg shadow p-8">
            <h2 className="text-2xl font-bold text-dark mb-6">Voucher Hiện Có</h2>
            
            {availableVouchers.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <p>Hiện không có voucher mới</p>
              </div>
            ) : (
              <div className="space-y-4">
                {availableVouchers.map((voucher) => (
                  <div
                    key={voucher._id}
                    className="border border-yellow-200 bg-yellow-50 rounded-lg p-4 hover:shadow-md transition"
                  >
                    <div className="flex justify-between items-start mb-2">
                      <div className="flex-1">
                        <h3 className="font-bold text-lg">{voucher.name}</h3>
                        <p className="text-xs text-blue-600 font-semibold mb-1">
                          🏪 {voucher.shop?.name || 'Shop'}
                        </p>
                        <p className="text-sm text-gray-600">{voucher.description}</p>
                      </div>
                      <span className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded text-xs font-semibold whitespace-nowrap ml-2">
                        {voucher.value}
                        {voucher.type === 'percentage' ? '%' : '₫'}
                      </span>
                    </div>

                    <div className="bg-white rounded p-2 mb-3">
                      <p className="text-center font-mono font-bold text-primary">
                        {voucher.code}
                      </p>
                    </div>

                    <div className="grid grid-cols-2 gap-2 text-xs text-gray-600">
                      <div>Tối thiểu: ₫{(voucher.minOrderValue || 0).toLocaleString('vi-VN')}</div>
                      <div>
                        Hạn: {new Date(voucher.endDate).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

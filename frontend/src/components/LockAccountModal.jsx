import React, { useState } from 'react';

export default function LockAccountModal({ isOpen, user, onClose, onConfirm, isLoading }) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = () => {
    if (!reason.trim()) {
      setError('Vui lòng nhập lý do khóa tài khoản');
      return;
    }

    if (reason.trim().length < 10) {
      setError('Lý do khóa phải có ít nhất 10 ký tự');
      return;
    }

    onConfirm(reason.trim());
  };

  const handleClose = () => {
    setReason('');
    setError('');
    onClose();
  };

  if (!isOpen || !user) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-red-500 to-red-600 px-6 py-5">
          <h2 className="text-xl font-bold text-white">🔒 Khóa Tài Khoản</h2>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4">
          {/* User info */}
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-sm text-gray-600">Khóa tài khoản của</p>
            <p className="font-bold text-gray-900 mt-1">{user.name}</p>
            <p className="text-sm text-gray-500">{user.email}</p>
          </div>

          {/* Lock reason input */}
          <div className="space-y-2">
            <label className="block text-sm font-semibold text-gray-700">
              Lý do khóa tài khoản *
            </label>
            <textarea
              value={reason}
              onChange={(e) => {
                setReason(e.target.value);
                setError('');
              }}
              placeholder="Ví dụ: Vi phạm chính sách điều khoản sử dụng, hành vi gian lận, spam..."
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 resize-none h-24 font-medium text-gray-700"
            />
            {error && <p className="text-sm text-red-600 font-semibold">{error}</p>}
            <p className="text-xs text-gray-500">
              Tối thiểu 10 ký tự - Lý do này sẽ được hiển thị cho người dùng
            </p>
          </div>

          {/* Warning */}
          <div className="bg-yellow-50 border-2 border-yellow-200 rounded-lg p-4">
            <p className="text-sm text-yellow-800">
              <span className="font-bold">⚠️ Lưu ý:</span> Tài khoản bị khóa sẽ không thể sử dụng bất kỳ tính năng nào.
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="bg-gray-50 px-6 py-4 flex gap-3 justify-end">
          <button
            onClick={handleClose}
            disabled={isLoading}
            className="px-4 py-2 rounded-lg font-semibold text-gray-700 bg-gray-200 hover:bg-gray-300 transition-all disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            onClick={handleSubmit}
            disabled={isLoading || !reason.trim()}
            className="px-4 py-2 rounded-lg font-semibold text-white bg-red-600 hover:bg-red-700 transition-all disabled:opacity-50 flex items-center gap-2"
          >
            {isLoading ? (
              <>
                <span className="animate-spin">⏳</span>
                Đang khóa...
              </>
            ) : (
              <>
                🔒 Khóa Tài Khoản
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

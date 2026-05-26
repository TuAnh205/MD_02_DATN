import React from 'react';

export default function AccountLockedModal({ lockReason }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-[9999]">
      <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full mx-4 overflow-hidden border-4 border-red-500">
        {/* Header */}
        <div className="bg-gradient-to-r from-red-600 to-red-700 px-8 py-6">
          <div className="text-center">
            <h2 className="text-3xl mb-2">🔒</h2>
            <h1 className="text-2xl font-bold text-white">Tài Khoản Bị Khóa</h1>
          </div>
        </div>

        {/* Content */}
        <div className="p-8 space-y-6">
          {/* Main Message */}
          <div className="space-y-3">
            <p className="text-lg font-bold text-slate-900">
              Tài khoản của bạn hiện đang bị khóa
            </p>
            <p className="text-sm text-slate-600 leading-relaxed">
              Bạn không thể sử dụng bất kỳ tính năng nào trong hệ thống cho đến khi tài khoản được mở khóa.
            </p>
          </div>

          {/* Lock Reason */}
          {lockReason && (
            <div className="bg-red-50 border-2 border-red-200 rounded-2xl p-5">
              <p className="text-xs font-bold text-red-700 uppercase tracking-widest mb-2">
                📌 Lý Do Khóa Tài Khoản
              </p>
              <p className="text-sm text-slate-800 font-medium leading-relaxed">
                {lockReason}
              </p>
            </div>
          )}

          {/* Contact Information */}
          <div className="bg-blue-50 border-2 border-blue-200 rounded-2xl p-5">
            <p className="text-xs font-bold text-blue-700 uppercase tracking-widest mb-3">
              📧 Liên Hệ Quản Trị Viên
            </p>
            <div className="space-y-2">
              <p className="text-sm text-slate-700">
                Vui lòng liên hệ với đội hỗ trợ của chúng tôi để được giải quyết:
              </p>
              <a
                href="mailto:CoretechVN@gmail.com"
                className="inline-flex items-center gap-2 font-bold text-blue-600 hover:text-blue-700 text-base break-all"
              >
                <span>✉️</span>
                CoretechVN@gmail.com
              </a>
            </div>
          </div>

          {/* Warning */}
          <div className="bg-yellow-50 border-2 border-yellow-300 rounded-2xl p-4">
            <p className="text-xs text-yellow-800 font-semibold">
              <span className="font-bold">⚠️ Lưu Ý:</span> Popup này sẽ tồn tại cho đến khi admin mở khóa tài khoản của bạn. Không thể đóng hoặc bỏ qua popup này.
            </p>
          </div>

          {/* Action Button - Only Open Email */}
          <a
            href="mailto:CoretechVN@gmail.com"
            className="w-full px-6 py-4 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-bold rounded-xl text-center transition-all duration-200 shadow-md hover:shadow-lg block"
          >
            📧 Gửi Email Liên Hệ
          </a>
        </div>

        {/* Footer */}
        <div className="bg-slate-50 px-8 py-4 border-t-2 border-slate-200">
          <p className="text-xs text-slate-600 text-center font-medium">
            🔐 CORE-TECH | Hệ Thống Quản Lý Tài Khoản An Toàn
          </p>
        </div>
      </div>
    </div>
  );
}

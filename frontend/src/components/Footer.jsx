import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const companyLinks = [
  'Giới thiệu về CORETECH',
  'Tin công nghệ mỗi ngày',
  'Tuyển dụng nội bộ',
  'Hệ thống showroom',
  'Dịch vụ bảo trì tận nơi',
  'Hướng dẫn mua hàng online',
  'Chính sách thành viên',
  'Liên hệ hợp tác đối tác'
];

const policyLinks = [
  'Chính sách bảo hành',
  'Chính sách đổi trả',
  'Chính sách bảo mật',
  'Chính sách vận chuyển',
  'Chính sách trả góp',
  'Điều khoản sử dụng',
  'Cam kết hàng chính hãng',
  'Quy trình giải quyết khiếu nại'
];

const paymentMethods = ['VISA', 'Mastercard', 'JCB', 'ZaloPay', 'MoMo', 'VNPay', 'PayPal', 'Apple Pay'];
const certifications = ['DMCA', 'BCT', 'PCI DSS', 'ISO 27001'];

export default function Footer() {
  const location = useLocation();

  if (location.pathname.startsWith('/admin') || location.pathname.startsWith('/shop')) {
    return null;
  }

  return (
    <footer className="bg-[#020b1a] text-slate-200 mt-16 border-t border-slate-800">
      <div className="max-w-6xl mx-auto px-4 pt-10 pb-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 pb-6 border-b border-slate-700/60">
          <div>
            <h3 className="text-white font-semibold text-lg">Hệ thống CORETECH Shop trên toàn quốc</h3>
            <p className="text-slate-300/80 text-sm mt-1">
              Bao gồm cửa hàng flagship CORETECH, trung tâm trải nghiệm và điểm nhận hàng nhanh tại 63 tỉnh thành.
            </p>
          </div>
          <button className="self-start lg:self-auto bg-red-500 hover:bg-red-600 text-white text-sm font-semibold px-5 py-2 rounded-full transition">
            Xem danh sách cửa hàng
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 py-8">
          <div>
            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-3">Tải ứng dụng CORETECH</h4>
            <div className="flex gap-3 mb-4">
              <div className="w-16 h-16 rounded bg-white text-slate-800 flex items-center justify-center text-[10px] font-bold text-center leading-tight">
                QR
                <br />
                CORETECH
              </div>
              <div className="space-y-2">
                <button className="block w-full border border-slate-500 rounded px-3 py-1 text-xs hover:border-white transition">App Store</button>
                <button className="block w-full border border-slate-500 rounded px-3 py-1 text-xs hover:border-white transition">Google Play</button>
              </div>
            </div>

            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-2">Kết nối với CORETECH</h4>
            <div className="flex gap-2 mb-4">
              {['f', 'z', 'yt', 'tt'].map((social) => (
                <span
                  key={social}
                  className="w-7 h-7 rounded-full bg-slate-700 hover:bg-slate-600 text-[11px] font-semibold flex items-center justify-center"
                >
                  {social}
                </span>
              ))}
            </div>

            <div className="text-sm leading-6 text-slate-300/90">
              <p className="font-semibold text-white">Tổng đài miễn phí</p>
              <p>Tư vấn mua hàng: 1800.6601 (8:00 - 22:00)</p>
              <p>Hỗ trợ kỹ thuật: 1800.6611 (24/7)</p>
              <p>CSKH doanh nghiệp: 1800.6622 (8:00 - 20:30)</p>
            </div>
          </div>

          <div>
            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-3">Về chúng tôi</h4>
            <ul className="space-y-2 text-sm text-slate-300/90">
              {companyLinks.map((item) => (
                <li key={item} className="hover:text-white transition cursor-pointer">{item}</li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-3">Chính sách</h4>
            <ul className="space-y-2 text-sm text-slate-300/90">
              {policyLinks.map((item) => (
                <li key={item} className="hover:text-white transition cursor-pointer">{item}</li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-3">Hỗ trợ thanh toán</h4>
            <div className="grid grid-cols-4 gap-2 mb-6">
              {paymentMethods.map((method) => (
                <span
                  key={method}
                  className="text-[10px] text-center bg-white text-slate-800 rounded px-1 py-1.5 font-semibold"
                >
                  {method}
                </span>
              ))}
            </div>

            <h4 className="text-white text-sm font-bold uppercase tracking-wide mb-3">Chứng nhận</h4>
            <div className="flex flex-wrap gap-2 mb-4">
              {certifications.map((item) => (
                <span
                  key={item}
                  className="text-[10px] bg-slate-100 text-slate-800 rounded px-2 py-1 font-semibold"
                >
                  {item}
                </span>
              ))}
            </div>

            <p className="text-xs text-slate-400 leading-5">
              Giao hàng thần tốc 2 giờ tại nội thành và hỗ trợ lắp đặt theo lịch hẹn ở các tỉnh thành lớn.
            </p>
          </div>
        </div>

        <div className="border-t border-slate-700/60 pt-5">
          <div className="flex flex-wrap items-center justify-between gap-4 text-xs text-slate-400">
            <p>
              © 2017 - 2026 CORETECH Retail JSC. Địa chỉ: 99 Đường Công Nghệ, P. Sáng Tạo, Q. Đổi Mới, TP.HCM.
            </p>
            <div className="flex items-center gap-3">
              <Link to="/" className="hover:text-white transition">coretech.vn</Link>
              <span>|</span>
              <span>MST: 0316 999 888 (thông tin mô phỏng)</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}

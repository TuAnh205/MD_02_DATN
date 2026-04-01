import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = React.useState(false);
  const [isRegisterMenuOpen, setIsRegisterMenuOpen] = React.useState(false);
  const dropdownRef = React.useRef(null);
  const registerMenuRef = React.useRef(null);

  // Hide header on admin and shop routes
  if (location.pathname.startsWith('/admin') || location.pathname.startsWith('/shop')) {
    return null;
  }

  // Close dropdown when clicking outside
  React.useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }

      if (registerMenuRef.current && !registerMenuRef.current.contains(event.target)) {
        setIsRegisterMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setIsDropdownOpen(false);
  };

  return (
    <header className="bg-white shadow-sm">
      <div className="bg-slate-900 text-slate-100 text-xs px-4 py-2">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-2">
          <div className="flex flex-wrap items-center gap-4">
            <span className="flex items-center gap-2">
              <span className="text-primary">🚚</span>
              Freeship toàn quốc từ 1.000.000₫
            </span>
            <span className="flex items-center gap-2">
              <span className="text-accent">💳</span>
              Trả góp 0%
            </span>
            <span className="flex items-center gap-2">
              <span className="text-white">📞</span>
              Hotline: 1900 1000
            </span>
          </div>
          <div className="flex flex-wrap items-center gap-4 text-gray-300">
            <span>📦 7 ngày đổi trả</span>
            <span>✅ Bảo hành chính hãng</span>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-4 flex flex-col md:flex-row items-center justify-between gap-4">
        <Link to="/" className="text-2xl font-bold text-primary tracking-tight">
          CORETECH
        </Link>


        <nav className="flex flex-wrap items-center gap-3">
          <Link to="/" className="text-dark hover:text-primary transition">
            Trang Chủ
          </Link>
          <Link to="/cart" className="text-dark hover:text-primary transition">
            🛒 Giỏ Hàng
          </Link>
          <Link to="/orders" className="text-dark hover:text-primary transition">
            📦 Đơn Hàng
          </Link>

          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button 
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                className="flex items-center gap-2 hover:text-primary transition"
              >
                <div className="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white font-medium text-sm">
                  {user.name?.charAt(0)?.toUpperCase()}
                </div>
                <span className="text-gray-600 hidden md:block">{user.name}</span>
              </button>

              {/* Dropdown Menu */}
              {isDropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
                  <Link
                    to="/profile"
                    onClick={() => setIsDropdownOpen(false)}
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-t-lg"
                  >
                    Thông tin cá nhân
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-b-lg border-t"
                  >
                    Đăng Xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="relative flex items-center gap-3" ref={registerMenuRef}>
              <Link to="/login" className="btn-secondary text-sm font-medium">
                Đăng Nhập
              </Link>

              <button
                onClick={() => setIsRegisterMenuOpen(!isRegisterMenuOpen)}
                className="btn-primary text-sm font-medium inline-flex items-center gap-2"
                aria-haspopup="menu"
                aria-expanded={isRegisterMenuOpen}
              >
                Đăng Ký
                <svg
                  className={`w-4 h-4 transition-transform ${isRegisterMenuOpen ? 'rotate-180' : ''}`}
                  viewBox="0 0 20 20"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.51a.75.75 0 01-1.08 0l-4.25-4.51a.75.75 0 01.02-1.06z"
                    clipRule="evenodd"
                  />
                </svg>
              </button>

              {isRegisterMenuOpen && (
                <div className="absolute left-0 right-0 md:left-auto md:right-0 top-full mt-2 w-full md:w-56 bg-white rounded-xl shadow-lg border border-gray-200 z-50 overflow-hidden">
                  <Link
                    to="/register/user"
                    onClick={() => setIsRegisterMenuOpen(false)}
                    className="block px-4 py-3 text-sm text-gray-700 hover:bg-slate-50 transition"
                  >
                    Đăng ký Người dùng
                  </Link>
                  <Link
                    to="/register/shop"
                    onClick={() => setIsRegisterMenuOpen(false)}
                    className="block px-4 py-3 text-sm text-gray-700 hover:bg-slate-50 transition border-t border-gray-100"
                  >
                    Đăng ký Shop
                  </Link>
                </div>
              )}
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

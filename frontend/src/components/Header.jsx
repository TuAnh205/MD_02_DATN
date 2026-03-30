import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = React.useState(false);
  const dropdownRef = React.useRef(null);

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
            <div className="flex gap-3">
              <Link to="/login" className="btn-secondary text-sm">
                Đăng Nhập
              </Link>
              <Link to="/register" className="btn-primary text-sm">
                ĐK Người Mua
              </Link>
              <Link to="/register?role=shop" className="btn-primary text-sm">
                ĐK Shop
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

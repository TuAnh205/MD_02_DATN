import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white shadow-sm">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-primary">
          MD_02_DATN
        </Link>

        <nav className="flex items-center gap-8">
          <Link to="/products" className="text-dark hover:text-primary transition">
            Sản Phẩm
          </Link>

          {user ? (
            <div className="flex items-center gap-4">
              <span className="text-gray-600">{user.name}</span>
              <button
                onClick={handleLogout}
                className="btn-primary text-sm"
              >
                Đăng Xuất
              </button>
            </div>
          ) : (
            <div className="flex gap-4">
              <Link to="/login" className="btn-secondary text-sm">
                Đăng Nhập
              </Link>
              <Link to="/register" className="btn-primary text-sm">
                Đăng Ký
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}

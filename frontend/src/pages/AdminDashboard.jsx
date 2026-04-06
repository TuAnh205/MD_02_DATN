import React, { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const menuItems = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      path: '/admin',
      icon: '📊',
      description: 'Tổng quan hệ thống'
    },
    {
      id: 'revenue',
      label: 'Doanh Thu',
      path: '/admin/revenue',
      icon: '💰',
      description: 'Xem biểu đồ doanh thu'
    },
    {
      id: 'users',
      label: 'Quản lý Người dùng',
      path: '/admin/users',
      icon: '👥',
      description: 'Quản lý tài khoản khách hàng'
    },
    {
      id: 'products',
      label: 'Quản lý Sản phẩm',
      path: '/admin/products',
      icon: '📦',
      description: 'Xem sản phẩm từ các shop'
    },
    {
      id: 'orders',
      label: 'Quản lý Đơn hàng',
      path: '/admin/orders',
      icon: '📋',
      description: 'Theo dõi đơn hàng'
    },
    {
      id: 'reviews',
      label: 'Quản lý Đánh giá',
      path: '/admin/reviews',
      icon: '⭐',
      description: 'Quản lý đánh giá sản phẩm'
    },
    {
      id: 'feedbacks',
      label: 'Phản hồi Khách hàng',
      path: '/admin/feedbacks',
      icon: '💬',
      description: 'Xem phản hồi từ khách hàng'
    },
    {
      id: 'posts',
      label: 'Quản lý Bài viết',
      path: '/admin/posts',
      icon: '📝',
      description: 'Quản lý nội dung website'
    },
    {
      id: 'vouchers',
      label: 'Quản lý Voucher',
      path: '/admin/vouchers',
      icon: '🎫',
      description: 'Tạo và quản lý voucher'
    },
  ];

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-gradient-to-r from-blue-600 to-blue-800 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden mr-4 p-2 rounded-md hover:bg-blue-700 transition"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
              <Link to="/admin" className="flex items-center space-x-3">
                <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                  <span className="text-blue-600 font-bold text-lg">F</span>
                </div>
                <div>
                  <h1 className="text-xl font-bold">FPT Shop Admin</h1>
                  <p className="text-xs text-blue-100">Quản lý hệ thống</p>
                </div>
              </Link>
            </div>

            <div className="flex items-center space-x-4">
              <div className="hidden md:flex items-center space-x-2">
                <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                  <span className="text-sm font-medium">{user?.name?.charAt(0)?.toUpperCase()}</span>
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium">{user?.name}</p>
                  <p className="text-xs text-blue-100">Administrator</p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                className="p-2 rounded-md hover:bg-blue-700 transition"
                title="Đăng xuất"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-xl transform transition-transform duration-300 ease-in-out md:translate-x-0 md:static md:inset-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}>
          <div className="flex flex-col h-full pt-16 md:pt-0">
            <nav className="flex-1 px-4 py-6 space-y-2">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.id}
                    to={item.path}
                    onClick={() => setSidebarOpen(false)}
                    className={`group flex items-center px-4 py-3 rounded-lg transition-all duration-200 ${
                      isActive
                        ? 'bg-blue-50 border-r-4 border-blue-600 text-blue-700'
                        : 'text-gray-700 hover:bg-gray-50 hover:text-blue-600'
                    }`}
                  >
                    <span className="text-xl mr-3">{item.icon}</span>
                    <div className="flex-1">
                      <div className="font-medium">{item.label}</div>
                      <div className="text-xs text-gray-500 mt-0.5">{item.description}</div>
                    </div>
                    {isActive && (
                      <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                      </svg>
                    )}
                  </Link>
                );
              })}
            </nav>

            {/* Footer */}
            <div className="p-4 border-t border-gray-200">
              <div className="text-center">
                <p className="text-xs text-gray-500">FPT Shop Admin v1.0</p>
                <p className="text-xs text-gray-400 mt-1">© 2024 FPT Corporation</p>
              </div>
            </div>
          </div>
        </aside>

        {/* Overlay for mobile */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-40 bg-black bg-opacity-50 md:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}

        {/* Main Content */}
        <main className="flex-1 min-h-screen">
          <div className="p-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
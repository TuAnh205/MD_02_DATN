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
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      {/* Header */}
      <header className="bg-white border-b-2 border-blue-600 shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden mr-4 p-2 rounded-lg hover:bg-slate-100 transition duration-200 text-slate-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
              <Link to="/admin" className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-blue-700 rounded-xl flex items-center justify-center shadow-md">
                  <span className="text-white font-bold text-lg">A</span>
                </div>
                <div>
                  <h1 className="text-lg font-bold text-slate-900">Admin</h1>
                  <p className="text-xs text-slate-500">Bảng điều khiển</p>
                </div>
              </Link>
            </div>

            <div className="flex items-center space-x-6">
              <div className="hidden md:flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-br from-orange-400 to-orange-500 rounded-xl flex items-center justify-center font-bold text-white">
                  {user?.name?.charAt(0)?.toUpperCase()}
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-slate-900">{user?.name}</p>
                  <p className="text-xs text-slate-500">Quản trị viên</p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                className="p-2 rounded-lg hover:bg-red-50 transition duration-200 text-slate-600 hover:text-red-600"
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
        <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-gradient-to-b from-slate-800 to-slate-900 shadow-xl transform transition-transform duration-300 ease-in-out md:translate-x-0 md:static md:inset-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}>
          <div className="flex flex-col h-full pt-16 md:pt-6">
            <nav className="flex-1 px-3 py-6 space-y-2 overflow-y-auto">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.id}
                    to={item.path}
                    onClick={() => setSidebarOpen(false)}
                    className={`group flex items-center px-4 py-3 rounded-lg transition-all duration-200 ${
                      isActive
                        ? 'bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-lg'
                        : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                    }`}
                  >
                    <span className="text-xl mr-3 flex-shrink-0">{item.icon}</span>
                    <div className="flex-1 min-w-0">
                      <div className="font-semibold text-sm truncate">{item.label}</div>
                      <div className="text-xs opacity-75 mt-0.5 truncate">{item.description}</div>
                    </div>
                    {isActive && (
                      <svg className="w-5 h-5 flex-shrink-0 ml-2" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                      </svg>
                    )}
                  </Link>
                );
              })}
            </nav>

            {/* Footer */}
            <div className="p-4 border-t border-slate-700 bg-slate-800">
              <div className="text-center">
                <p className="text-xs text-slate-400">FPT Shop Admin</p>
                <p className="text-xs text-slate-500 mt-1">v1.0</p>
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
          <div className="p-6 md:p-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
import React, { useState, useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import notificationService from '../services/notificationService';

export default function ShopDashboard() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);

  useEffect(() => {
    const loadNotifications = async () => {
      try {
        const items = await notificationService.getNotifications();
        setNotifications(items);
        setUnreadCount(items.filter((n) => !n.isRead).length);
      } catch (err) {
        console.error('Không thể tải thông báo:', err);
      }
    };
    loadNotifications();
  }, []);

  const menuItems = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      path: '/shop',
      icon: '📊',
      description: 'Tổng quan shop'
    },
    {
      id: 'products',
      label: 'Quản lý Sản phẩm',
      path: '/shop/products',
      icon: '📦',
      description: 'Thêm, sửa, xóa sản phẩm'
    },
    {
      id: 'revenue',
      label: 'Doanh Thu',
      path: '/shop/revenue',
      icon: '💰',
      description: 'Xem thống kê doanh thu'
    },
    {
      id: 'orders',
      label: 'Đơn hàng',
      path: '/shop/orders',
      icon: '📋',
      description: 'Xem đơn hàng của shop'
    },
    {
      id: 'reviews',
      label: 'Đánh giá',
      path: '/shop/reviews',
      icon: '📝',
      description: 'Xem và trả lời đánh giá'
    }
  ];

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
              >
                <span className="sr-only">Open sidebar</span>
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
              <h1 className="ml-2 md:ml-0 text-xl font-semibold text-gray-900">
                Shop Dashboard - {user?.name}
              </h1>
            </div>
            <div className="flex items-center space-x-4 relative">
              <button
                onClick={() => {
                  setShowNotifications(!showNotifications);
                }}
                className="relative text-gray-600 hover:text-gray-900"
              >
                <span role="img" aria-label="notification" className="text-2xl">🔔</span>
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 min-w-[18px] h-4 bg-red-600 text-white text-xs rounded-full flex items-center justify-center">{unreadCount}</span>
                )}
              </button>

              {showNotifications && (
                <div className="absolute right-0 top-12 z-50 w-80 bg-white border border-gray-200 rounded shadow-lg p-2">
                  <div className="flex justify-between items-center mb-2">
                    <strong>Thông báo</strong>
                    {unreadCount > 0 && (
                      <button
                        className="text-xs text-blue-600"
                        onClick={async () => {
                          await notificationService.markAllRead();
                          setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
                          setUnreadCount(0);
                        }}
                      >
                        Đánh dấu đã đọc
                      </button>
                    )}
                  </div>
                  <div className="max-h-72 overflow-y-auto">
                    {notifications.length === 0 ? (
                      <p className="text-sm text-gray-500">Không có thông báo</p>
                    ) : (
                      notifications.map((item) => (
                        <div key={item._id} className={`p-2 rounded mb-1 ${item.isRead ? 'bg-gray-50' : 'bg-blue-50'} border ${item.isRead ? 'border-gray-200' : 'border-blue-200'}`}>
                          <div className="flex justify-between items-start">
                            <p className="text-xs text-gray-500">{new Date(item.createdAt).toLocaleString('vi-VN')}</p>
                            {!item.isRead && (
                              <button
                                className="text-xs text-blue-600"
                                onClick={async () => {
                                  await notificationService.markRead(item._id);
                                  setNotifications((prev) => prev.map((n) => n._id === item._id ? { ...n, isRead: true } : n));
                                  setUnreadCount((c) => Math.max(0, c - 1));
                                }}
                              >
                                Đã đọc
                              </button>
                            )}
                          </div>
                          <p className="font-medium text-sm">{item.title}</p>
                          <p className="text-sm text-gray-700">{item.message}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}

              <Link
                to="/"
                className="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
              >
                Về trang chủ
              </Link>
              <button
                onClick={handleLogout}
                className="bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700"
              >
                Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="flex">
        {/* Sidebar */}
        <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} transition-transform duration-300 ease-in-out md:translate-x-0 md:static md:inset-0`}>
          <div className="flex flex-col h-full pt-16 md:pt-0">
            <nav className="flex-1 px-4 py-6 space-y-2">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.id}
                    to={item.path}
                    className={`group flex items-center px-3 py-3 text-sm font-medium rounded-md transition-colors ${
                      isActive
                        ? 'bg-primary text-white'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                    onClick={() => setSidebarOpen(false)}
                  >
                    <span className="mr-3 text-lg">{item.icon}</span>
                    <div>
                      <div>{item.label}</div>
                      <div className={`text-xs ${isActive ? 'text-white/80' : 'text-gray-500'}`}>
                        {item.description}
                      </div>
                    </div>
                  </Link>
                );
              })}
            </nav>
          </div>
        </div>

        {/* Main content */}
        <div className="flex-1 md:ml-0">
          <main className="p-6">
            <Outlet />
          </main>
        </div>
      </div>

      {/* Overlay for mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black bg-opacity-50 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  );
}
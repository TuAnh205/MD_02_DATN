import React, { useState, useEffect } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import notificationService from "../services/notificationService";
import api from "../services/api";
import orderStatusEmitter from "../utils/orderStatusEmitter";

const coretechVisuals = [
  {
    id: "ct-1",
    title: "CoreTech Retail Hub",
    image:
      "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-2",
    title: "CoreTech Device Lab",
    image:
      "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: "ct-3",
    title: "CoreTech Service Center",
    image:
      "https://images.unsplash.com/photo-1588702547923-7093a6c3ba33?auto=format&fit=crop&w=800&q=80",
  },
];

export default function ShopDashboard() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [billingSummary, setBillingSummary] = useState(null);
  const [revenueSummary, setRevenueSummary] = useState(null);
  const [showPolicyNotice, setShowPolicyNotice] = useState(true);
  const [showProfileDropdown, setShowProfileDropdown] = useState(false);
  const [isAccountLocked, setIsAccountLocked] = useState(false);
  const [lockReason, setLockReason] = useState('');
  const profileDropdownRef = React.useRef(null);

  useEffect(() => {
    const checkAccountStatus = async () => {
      try {
        const profile = await api.get('/auth/profile');
        if (profile.data.user.isLocked) {
          setIsAccountLocked(true);
          setLockReason(profile.data.user.lockReason);
        }
      } catch (err) {
        console.error('Error checking account status:', err);
      }
    };

    checkAccountStatus();

    const loadNotifications = async () => {
      try {
        const items = await notificationService.getNotifications();
        setNotifications(items);
        setUnreadCount(items.filter((n) => !n.isRead).length);
      } catch (err) {
        console.error("Không thể tải thông báo:", err);
      }
    };
    loadNotifications();

    const loadBillingSummary = async () => {
      try {
        const response = await api.get("/shop/billing-summary");
        setBillingSummary(response.data.summary || null);
      } catch (err) {
        console.error("Không thể tải trạng thái phí shop:", err);
      }
    };

    loadBillingSummary();
    const loadRevenueSummary = async () => {
      try {
        const res = await api.get("/shop/revenue?period=month");
        // API returns an object with `summary` inside
        setRevenueSummary(res.data.summary || null);
      } catch (err) {
        console.error("Không thể tải dữ liệu doanh thu:", err);
      }
    };

    loadRevenueSummary();

    // Listen for order status changes and refresh revenue summary
    const unsubscribe = orderStatusEmitter.on(() => {
      loadRevenueSummary();
    });

    // Close dropdown when clicking outside
    const handleClickOutside = (event) => {
      if (profileDropdownRef.current && !profileDropdownRef.current.contains(event.target)) {
        setShowProfileDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      unsubscribe();
    };
  }, []);

  const menuItems = [
    {
      id: "dashboard",
      label: "Dashboard",
      path: "/shop",
      icon: "📊",
      description: "Tổng quan shop",
    },
    {
      id: "products",
      label: "Quản lý Sản phẩm",
      path: "/shop/products",
      icon: "📦",
      description: "Thêm, sửa, xóa sản phẩm",
    },
    {
      id: "revenue",
      label: "Doanh Thu",
      path: "/shop/revenue",
      icon: "💰",
      description: "Xem thống kê doanh thu",
    },
    {
      id: "orders",
      label: "Đơn hàng",
      path: "/shop/orders",
      icon: "📋",
      description: "Xem đơn hàng của shop",
    },
    {
      id: "reviews",
      label: "Đánh giá",
      path: "/shop/reviews",
      icon: "📝",
      description: "Xem và trả lời đánh giá",
    },
    {
      id: "vouchers",
      label: "Quản Lý Voucher",
      path: "/shop/vouchers",
      icon: "🎫",
      description: "Tạo, sửa, xóa voucher",
    },
  ];

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b-2 border-blue-600 sticky top-0 z-40">
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-4">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden p-2 rounded-lg text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors"
              >
                <span className="sr-only">Open sidebar</span>
                <svg
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                </svg>
              </button>
              <div className="hidden md:flex items-center gap-2">
                <span className="text-3xl">🏪</span>
                <div>
                  <h1 className="text-lg font-bold text-slate-900">{user?.name}</h1>
                  <p className="text-xs text-slate-500 font-semibold">CORE-TECH SHOP</p>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3 sm:gap-6">
              {isAccountLocked && (
                <span className="hidden sm:inline-flex rounded-full bg-red-100 px-4 py-2 text-xs font-bold text-red-700 border border-red-300">
                  🔒 Tài khoản bị khóa
                </span>
              )}

              {billingSummary?.isFrozen && (
                <span className="hidden sm:inline-flex rounded-full bg-rose-100 px-4 py-2 text-xs font-bold text-rose-700 border border-rose-300">
                  ⚠️ Đóng băng bán hàng
                </span>
              )}

              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded-lg text-slate-600 hover:text-blue-600 hover:bg-blue-50 transition-colors"
              >
                <span className="text-2xl">🔔</span>
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 min-w-[20px] h-5 bg-rose-600 text-white text-xs font-bold rounded-full flex items-center justify-center">
                    {unreadCount}
                  </span>
                )}
              </button>

              {showNotifications && (
                <div className="absolute right-4 top-16 z-50 w-96 bg-white border-2 border-slate-200 rounded-2xl shadow-lg p-4">
                  <div className="flex justify-between items-center mb-4 pb-4 border-b-2 border-slate-200">
                    <h3 className="font-bold text-slate-900">📬 Thông báo</h3>
                    {unreadCount > 0 && (
                      <button
                        className="text-xs font-bold text-blue-600 hover:text-blue-700 px-3 py-1 bg-blue-50 rounded-lg transition-colors"
                        onClick={async () => {
                          await notificationService.markAllRead();
                          setNotifications((prev) =>
                            prev.map((n) => ({ ...n, isRead: true })),
                          );
                          setUnreadCount(0);
                        }}
                      >
                        Đánh dấu đã đọc
                      </button>
                    )}
                  </div>
                  <div className="max-h-80 overflow-y-auto space-y-2">
                    {notifications.length === 0 ? (
                      <p className="text-sm text-slate-500 text-center py-8">
                        ✨ Không có thông báo nào
                      </p>
                    ) : (
                      notifications.map((item) => (
                        <div
                          key={item._id}
                          className={`p-3 rounded-lg border-2 transition-all ${
                            item.isRead
                              ? "bg-slate-50 border-slate-200"
                              : "bg-blue-50 border-blue-300 shadow-sm"
                          }`}
                        >
                          <div className="flex justify-between items-start gap-2">
                            <div className="flex-1">
                              <p className={`text-xs font-bold ${item.isRead ? "text-slate-500" : "text-blue-700"}`}>
                                {new Date(item.createdAt).toLocaleString("vi-VN")}
                              </p>
                              <p className="font-bold text-sm text-slate-900 mt-1">{item.title}</p>
                              <p className="text-sm text-slate-700 mt-1">
                                {item.message}
                              </p>
                            </div>
                            {!item.isRead && (
                              <button
                                className="text-xs font-bold text-blue-600 hover:text-blue-700 whitespace-nowrap"
                                onClick={async () => {
                                  await notificationService.markRead(item._id);
                                  setNotifications((prev) =>
                                    prev.map((n) =>
                                      n._id === item._id
                                        ? { ...n, isRead: true }
                                        : n,
                                    ),
                                  );
                                  setUnreadCount((c) => Math.max(0, c - 1));
                                }}
                              >
                                ✓
                              </button>
                            )}
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}

              <div className="relative" ref={profileDropdownRef}>
                <button
                  onClick={() => setShowProfileDropdown(!showProfileDropdown)}
                  className="flex items-center gap-2 p-2 rounded-lg hover:bg-blue-50 transition-colors"
                >
                  <div className="w-8 h-8 bg-gradient-to-br from-blue-600 to-blue-700 rounded-full flex items-center justify-center text-white font-bold text-sm">
                    {user?.name?.charAt(0)?.toUpperCase()}
                  </div>
                  <svg className="hidden sm:block w-4 h-4 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                  </svg>
                </button>

                {showProfileDropdown && (
                  <div className="absolute right-0 top-full mt-2 w-56 bg-white rounded-2xl shadow-lg border-2 border-slate-200 z-50 overflow-hidden">
                    <Link
                      to="/shop/profile"
                      onClick={() => setShowProfileDropdown(false)}
                      className="block px-4 py-3 text-sm font-bold text-slate-900 hover:bg-blue-50 hover:text-blue-600 border-b-2 border-slate-200 transition-colors"
                    >
                      👤 Thông tin chi tiết Shop
                    </Link>
                    <button
                      onClick={() => {
                        handleLogout();
                        setShowProfileDropdown(false);
                      }}
                      className="block w-full text-left px-4 py-3 text-sm font-bold text-slate-900 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                    >
                      🚪 Đăng xuất
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="flex h-[calc(100vh-64px)]">
        {/* Sidebar */}
        <div
          className={`fixed inset-y-16 left-0 z-50 w-64 bg-gradient-to-b from-slate-800 to-slate-900 shadow-lg transform ${
            sidebarOpen ? "translate-x-0" : "-translate-x-full"
          } transition-transform duration-300 ease-in-out md:translate-x-0 md:relative md:inset-0 md:z-0`}
        >
          <div className="flex flex-col h-full">
            {/* Sidebar Header */}
            <div className="md:hidden px-6 py-4 border-b-2 border-slate-700">
              <div className="flex items-center gap-2">
                <span className="text-2xl">🏪</span>
                <div>
                  <p className="font-bold text-white text-sm">{user?.name}</p>
                  <p className="text-xs text-blue-300 font-semibold">CORE-TECH SHOP</p>
                </div>
              </div>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.id}
                    to={item.path}
                    className={`group flex items-center px-4 py-3 rounded-xl font-bold text-sm transition-all duration-200 ${
                      isActive
                        ? "bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md"
                        : "text-slate-300 hover:bg-slate-700 hover:text-white"
                    }`}
                    onClick={() => setSidebarOpen(false)}
                  >
                    <span className="text-lg mr-3">{item.icon}</span>
                    <div className="flex-1">
                      <div>{item.label}</div>
                      <div className={`text-xs font-normal ${isActive ? "text-blue-100" : "text-slate-400"}`}>
                        {item.description}
                      </div>
                    </div>
                    {isActive && <span className="ml-2">▶</span>}
                  </Link>
                );
              })}
            </nav>

            {/* Sidebar Footer - Branding */}
            <div className="px-4 py-4 border-t-2 border-slate-700 bg-slate-900">
              <div className="text-center">
                <p className="text-xs font-bold text-blue-400 uppercase tracking-widest">Powered by</p>
                <p className="text-lg font-bold text-white">CORE-TECH</p>
                <p className="text-xs text-slate-400 mt-1">Commerce Platform</p>
              </div>
            </div>
          </div>
        </div>

        {/* Main content */}
        <div className="flex-1 overflow-y-auto">
          <main className="p-6 lg:p-8 space-y-8">
            {/* CORE-TECH Branding Banner */}
            {location.pathname !== '/shop/profile' && (
              <section className="rounded-3xl overflow-hidden border-2 border-slate-200 shadow-md bg-white">
                <div className="relative h-48 md:h-56">
                  <div className="absolute inset-0 bg-gradient-to-r from-slate-900 via-blue-900 to-cyan-800" />
                  <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.25),transparent_45%),radial-gradient(circle_at_80%_70%,rgba(255,255,255,0.15),transparent_40%)]" />
                  
                  {/* Badge Area */}
                  <div className="absolute left-6 top-6 flex flex-wrap items-center gap-3">
                    <span className="inline-flex px-4 py-2 rounded-full bg-white/15 text-white text-xs font-bold tracking-wide backdrop-blur-sm border border-white/30">
                      ✓ CORE-TECH VERIFIED
                    </span>
                    <span className="inline-flex px-4 py-2 rounded-full bg-gradient-to-r from-emerald-400 to-green-500 text-slate-900 text-xs font-bold tracking-wide shadow-md">
                      🏆 OFFICIAL SHOP
                    </span>
                  </div>

                  {/* Text Area */}
                  <div className="absolute left-6 bottom-6 right-6 text-white">
                    <h2 className="text-2xl md:text-3xl font-bold leading-tight">
                      🏪 {user?.name}
                    </h2>
                    <p className="text-sm md:text-base text-blue-100 mt-2 font-semibold">
                      Đối tác chính thức trong hệ thống CORE-TECH Commerce Platform
                    </p>
                  </div>
                </div>

                {/* Visual Gallery */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 p-6 bg-gradient-to-r from-slate-50 to-blue-50">
                  {coretechVisuals.map((item) => (
                    <div
                      key={item.id}
                      className="relative rounded-2xl overflow-hidden h-28 group shadow-sm border-2 border-slate-200 hover:shadow-md transition-all duration-300"
                    >
                      <img
                        src={item.image}
                        alt={item.title}
                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/30 to-transparent" />
                      <p className="absolute left-4 bottom-3 text-xs font-bold text-white tracking-wide">
                        {item.title}
                      </p>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* Policy Notice */}
            {location.pathname !== '/shop/profile' && location.pathname !== '/shop/revenue' && showPolicyNotice && (
              <section className="rounded-2xl border-2 border-blue-300 bg-gradient-to-br from-blue-50 to-cyan-50 p-6 shadow-sm">
                <div className="flex justify-between items-start gap-6">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-4">
                      <span className="text-3xl">📋</span>
                      <div>
                        <h3 className="text-lg font-bold text-blue-900">Chính sách phí nền tảng</h3>
                        <p className="text-xs text-blue-700 font-semibold mt-1">Thông tin quan trọng</p>
                      </div>
                    </div>
                    <p className="text-sm font-bold text-blue-900 mb-4">
                      Tất cả sản phẩm thanh toán thành công sẽ tính <span className="text-blue-700 bg-blue-100 px-2 py-1 rounded">phí sàn 5%</span>
                    </p>
                    <ul className="text-sm text-blue-800 space-y-2 ml-4">
                      <li className="flex items-start gap-2">
                        <span className="text-green-600 font-bold mt-0.5">✓</span>
                        <span>Phí <span className="font-bold">5%</span> được tính trên giá bán sản phẩm</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-green-600 font-bold mt-0.5">✓</span>
                        <span>Phí chỉ tính khi khách hàng <span className="font-bold">thanh toán thành công</span></span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-green-600 font-bold mt-0.5">✓</span>
                        <span>Hệ thống <span className="font-bold">tự động trừ</span> từ ví shop của bạn</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-green-600 font-bold mt-0.5">✓</span>
                        <span>Xem chi tiết từng sản phẩm bị trừ bao nhiêu phí tại mục <span className="font-bold">Doanh Thu</span></span>
                      </li>
                    </ul>
                    <div className="mt-4 p-4 bg-white rounded-xl border-2 border-blue-200">
                      <p className="text-xs text-blue-700 font-semibold italic">
                        📌 <span className="font-bold">Ví dụ:</span> Sản phẩm bán được 100.000đ → Phí sàn 5.000đ → Bạn thực nhận 95.000đ
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => setShowPolicyNotice(false)}
                    className="flex-shrink-0 p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-100 rounded-lg transition-colors"
                  >
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </section>
            )}

            {/* Locked Account Alert */}
            {isAccountLocked && (
              <section className="rounded-2xl border-2 border-red-300 bg-gradient-to-br from-red-50 to-red-100 p-6 shadow-sm">
                <div className="flex items-start gap-4">
                  <span className="text-3xl mt-1">🔒</span>
                  <div className="flex-1">
                    <h3 className="text-lg font-bold text-red-900 mb-2">
                      Tài khoản của bạn đã bị khóa
                    </h3>
                    <p className="text-sm text-red-800 mb-3">
                      Tài khoản này không thể sử dụng bất kỳ tính năng nào trong hệ thống.
                    </p>
                    {lockReason && (
                      <div className="bg-white rounded-xl p-4 border-2 border-red-200 mb-4">
                        <p className="text-xs font-bold text-red-700 uppercase tracking-widest">Lý do khóa tài khoản</p>
                        <p className="text-sm text-slate-900 mt-2 font-medium">{lockReason}</p>
                      </div>
                    )}
                    <p className="text-xs text-red-700 font-semibold italic">
                      📌 Vui lòng liên hệ với đội hỗ trợ của chúng tôi để giải quyết vấn đề này.
                    </p>
                  </div>
                </div>
              </section>
            )}

            {/* Debt Alert */}
            {location.pathname !== '/shop/profile' && billingSummary?.isFrozen && (
              <section className="rounded-2xl border-2 border-rose-300 bg-gradient-to-br from-rose-50 to-pink-50 p-6 shadow-sm">
                <div className="flex items-start gap-4">
                  <span className="text-3xl mt-1">⚠️</span>
                  <div className="flex-1">
                    <h3 className="text-lg font-bold text-rose-900 mb-2">
                      Cảnh báo công nợ phí nền tảng
                    </h3>
                    <p className="text-sm text-rose-800 mb-3">
                      {billingSummary.message}
                    </p>
                    <div className="bg-white rounded-xl p-4 border-2 border-rose-200 mb-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-xs font-bold text-rose-700 uppercase tracking-widest">Số dư ví</p>
                          <p className="text-lg font-bold text-slate-900 mt-1">
                            {Number(billingSummary.walletBalance || 0).toLocaleString("vi-VN")}đ
                          </p>
                        </div>
                        <div>
                          <p className="text-xs font-bold text-rose-700 uppercase tracking-widest">Công nợ</p>
                          <p className="text-lg font-bold text-rose-700 mt-1">
                            {Number(billingSummary.outstandingAmount || 0).toLocaleString("vi-VN")}đ
                          </p>
                        </div>
                      </div>
                    </div>
                    <Link
                      to="/shop/revenue"
                      className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-rose-600 to-rose-700 px-6 py-3 text-sm font-bold text-white hover:shadow-md transition-shadow"
                    >
                      💳 Đi tới thanh toán công nợ
                    </Link>
                  </div>
                </div>
              </section>
            )}

            {/* Page Content */}
            <Outlet />
          </main>
        </div>
      </div>

      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black bg-opacity-50 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  );
}

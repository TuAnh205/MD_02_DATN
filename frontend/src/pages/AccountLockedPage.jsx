import React, { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import AccountLockedModal from '../components/AccountLockedModal';

export default function AccountLockedPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // If account is unlocked, redirect to home
    if (user && !user.isLocked) {
      if (user.role === 'admin') {
        navigate('/admin', { replace: true });
      } else if (user.role === 'shop') {
        navigate('/shop', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    }

    // Prevent back navigation
    const handlePopState = () => {
      window.history.pushState(null, '', window.location.href);
    };

    window.addEventListener('popstate', handlePopState);
    window.history.pushState(null, '', window.location.href);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [user, navigate]);

  // If not locked, don't render anything
  if (!user || !user.isLocked) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-100 via-blue-50 to-slate-100 flex items-center justify-center p-4">
      <AccountLockedModal lockReason={user.lockReason} />
      
      {/* Logout & Back to Login Button */}
      <button
        onClick={() => {
          logout();
          navigate('/login', { replace: true });
        }}
        className="fixed bottom-4 left-4 z-[99999] text-sm text-white font-bold px-4 py-2 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 rounded-lg border-2 border-blue-500 hover:shadow-lg transition-all duration-200"
      >
        ↩️ Quay Trở Về Đăng Nhập
      </button>

      {/* Hidden logout button for accessibility */}
      <button
        onClick={() => {
          logout();
          navigate('/login', { replace: true });
        }}
        className="fixed bottom-4 right-4 z-[99999] text-xs text-slate-600 hover:text-slate-900 font-semibold px-3 py-2 bg-white rounded-lg border border-slate-300 hover:bg-slate-50 transition-all"
      >
        Đăng Xuất
      </button>
    </div>
  );
}

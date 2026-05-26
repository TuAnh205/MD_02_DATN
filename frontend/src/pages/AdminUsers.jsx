import React, { useState, useEffect } from 'react';
import api from '../services/api';
import LockAccountModal from '../components/LockAccountModal';

const COLUMNS = [
  {
    role: 'user',
    label: 'Người dùng',
    icon: '👤',
    headerBg: 'bg-blue-600',
    badgeBg: 'bg-blue-100 text-blue-800',
    avatarBg: 'bg-gradient-to-br from-blue-400 to-blue-600',
    border: 'border-blue-200',
  },
  {
    role: 'shop',
    label: 'Shop',
    icon: '🏪',
    headerBg: 'bg-green-600',
    badgeBg: 'bg-green-100 text-green-800',
    avatarBg: 'bg-gradient-to-br from-green-400 to-green-600',
    border: 'border-green-200',
  },
  {
    role: 'admin',
    label: 'Quản trị viên',
    icon: '🛡️',
    headerBg: 'bg-purple-600',
    badgeBg: 'bg-purple-100 text-purple-800',
    avatarBg: 'bg-gradient-to-br from-purple-400 to-purple-600',
    border: 'border-purple-200',
  },
];

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [lockModalOpen, setLockModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isLocking, setIsLocking] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await api.get('/admin/users?limit=200');
      setUsers(response.data.users);
    } catch (error) {
      console.error('Error fetching users:', error);
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('Bạn có chắc muốn xóa người dùng này?')) return;
    try {
      await api.delete(`/admin/users/${userId}`);
      fetchUsers();
      alert('Xóa người dùng thành công');
    } catch (error) {
      console.error('Error deleting user:', error);
      alert('Có lỗi xảy ra khi xóa người dùng');
    }
  };

  const openLockModal = (user) => {
    setSelectedUser(user);
    setLockModalOpen(true);
  };

  const closeLockModal = () => {
    setSelectedUser(null);
    setLockModalOpen(false);
  };

  const lockUser = async (reason) => {
    if (!selectedUser) return;
    try {
      setIsLocking(true);
      await api.put(`/admin/users/${selectedUser._id}/lock`, { reason });
      fetchUsers();
      closeLockModal();
      alert('Khóa tài khoản thành công');
    } catch (error) {
      console.error('Error locking user:', error);
      alert('Có lỗi xảy ra khi khóa tài khoản');
    } finally {
      setIsLocking(false);
    }
  };

  const unlockUser = async (userId) => {
    if (!window.confirm('Bạn có chắc muốn mở khóa tài khoản này?')) return;
    try {
      await api.delete(`/admin/users/${userId}/unlock`);
      fetchUsers();
      alert('Mở khóa tài khoản thành công');
    } catch (error) {
      console.error('Error unlocking user:', error);
      alert('Có lỗi xảy ra khi mở khóa tài khoản');
    }
  };

  const filtered = users.filter(
    (u) =>
      !searchTerm ||
      u.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Quản lý Tài khoản</h1>
          <p className="text-slate-600 mt-2">Tổng cộng: <span className="font-bold text-blue-600">{users.length}</span> tài khoản</p>
        </div>
        <input
          type="text"
          placeholder="🔍 Tìm theo tên hoặc email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full sm:w-80 px-4 py-3 border-2 border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-medium text-slate-700 hover:border-blue-400 transition-all duration-200"
        />
      </div>

      {/* 3-column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {COLUMNS.map((col) => {
          const colUsers = filtered.filter((u) => u.role === col.role);
          return (
            <div key={col.role} className={`rounded-2xl border-2 ${col.border} overflow-hidden shadow-sm hover:shadow-lg transition-all duration-200 bg-white`}>
              {/* Column Header */}
              <div className={`${col.headerBg} text-white px-6 py-5 flex items-center justify-between bg-gradient-to-r`}>
                <div className="flex items-center gap-3 font-bold text-lg">
                  <span className="text-3xl">{col.icon}</span>
                  <span className="text-black">{col.label}</span>
                </div>
                <span className="bg-white bg-opacity-30 text-white text-sm font-bold px-3 py-1 rounded-full border-2 border-white border-opacity-40">
                  {colUsers.length}
                </span>
              </div>

              {/* User Cards */}
              <div className="divide-y divide-slate-200 max-h-[60vh] overflow-y-auto">
                {colUsers.length === 0 ? (
                  <div className="text-center py-12 bg-gradient-to-b from-slate-50 to-white">
                    <div className="text-5xl mb-3 opacity-30">{col.icon}</div>
                    <p className="text-slate-500 font-semibold">Không có tài khoản nào</p>
                  </div>
                ) : (
                  colUsers.map((user) => (
                    <div key={user._id} className={`px-5 py-4 transition-all duration-200 border-b border-slate-200 last:border-b-0 ${user.isLocked ? 'bg-red-50' : 'bg-white hover:bg-gradient-to-r hover:from-blue-50 hover:to-slate-50'}`}>
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-center gap-3 flex-1 min-w-0">
                          <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-sm flex-shrink-0 ${col.avatarBg} shadow-md border-2 ${col.border}`}>
                            {col.role === 'shop' ? '🏪' : user.name?.charAt(0)?.toUpperCase()}
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="text-sm font-bold text-slate-900 truncate">
                              {user.name}
                              {user.isLocked && <span className="ml-2 text-xs bg-red-100 text-red-700 px-2 py-1 rounded font-bold">🔒 Đã khóa</span>}
                            </div>
                            <div className="text-xs text-slate-500 truncate">{user.email}</div>
                            {user.isLocked && (
                              <div className="text-xs text-red-600 mt-1 font-semibold">
                                Lý do: {user.lockReason}
                              </div>
                            )}
                            <div className="text-xs text-slate-400 mt-1 font-medium">
                              📅 {new Date(user.createdAt).toLocaleDateString('vi-VN')}
                            </div>
                          </div>
                        </div>
                        <div className="flex gap-2 flex-shrink-0">
                          {user.isLocked ? (
                            <button
                              onClick={() => unlockUser(user._id)}
                              className="text-xs text-white font-bold bg-green-500 hover:bg-green-600 px-3 py-2 rounded-lg transition-all duration-200 hover:shadow-md"
                            >
                              Mở khóa
                            </button>
                          ) : (
                            <button
                              onClick={() => openLockModal(user)}
                              className="text-xs text-white font-bold bg-yellow-500 hover:bg-yellow-600 px-3 py-2 rounded-lg transition-all duration-200 hover:shadow-md"
                            >
                              Khóa
                            </button>
                          )}
                          <button
                            onClick={() => deleteUser(user._id)}
                            className="text-xs text-white font-bold bg-red-500 hover:bg-red-600 px-3 py-2 rounded-lg transition-all duration-200 hover:shadow-md"
                          >
                            Xóa
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Lock Account Modal */}
      <LockAccountModal
        isOpen={lockModalOpen}
        user={selectedUser}
        onClose={closeLockModal}
        onConfirm={lockUser}
        isLoading={isLocking}
      />
    </div>
  );
}
import React, { useState, useEffect } from 'react';
import api from '../services/api';

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

  const updateUserRole = async (userId, newRole) => {
    try {
      await api.put(`/admin/users/${userId}/role`, { role: newRole });
      fetchUsers();
    } catch (error) {
      console.error('Error updating user role:', error);
      alert('Có lỗi xảy ra khi cập nhật vai trò');
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('Bạn có chắc muốn xóa người dùng này?')) return;
    try {
      await api.delete(`/admin/users/${userId}`);
      fetchUsers();
    } catch (error) {
      console.error('Error deleting user:', error);
      alert('Có lỗi xảy ra khi xóa người dùng');
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
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Người dùng</h1>
          <p className="text-gray-600 mt-1">Tổng: {users.length} tài khoản</p>
        </div>
        <input
          type="text"
          placeholder="Tìm theo tên hoặc email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full sm:w-72 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* 3-column layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {COLUMNS.map((col) => {
          const colUsers = filtered.filter((u) => u.role === col.role);
          return (
            <div key={col.role} className={`rounded-xl border-2 ${col.border} overflow-hidden`}>
              {/* Column Header */}
              <div className={`${col.headerBg} text-white px-4 py-3 flex items-center justify-between`}>
                <div className="flex items-center gap-2 font-semibold text-base">
                  <span>{col.icon}</span>
                  <span>{col.label}</span>
                </div>
                <span className="bg-white bg-opacity-25 text-white text-xs font-bold px-2 py-1 rounded-full">
                  {colUsers.length}
                </span>
              </div>

              {/* User Cards */}
              <div className="bg-gray-50 divide-y divide-gray-200 max-h-[60vh] overflow-y-auto">
                {colUsers.length === 0 ? (
                  <div className="text-center py-10 text-gray-400">
                    <div className="text-4xl mb-2">{col.icon}</div>
                    <p className="text-sm">Không có tài khoản nào</p>
                  </div>
                ) : (
                  colUsers.map((user) => (
                    <div key={user._id} className="bg-white px-4 py-3 hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className={`w-9 h-9 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0 ${col.avatarBg}`}>
                          {col.role === 'shop' ? '🏪' : user.name?.charAt(0)?.toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="text-sm font-semibold text-gray-900 truncate">{user.name}</div>
                          <div className="text-xs text-gray-500 truncate">{user.email}</div>
                          <div className="text-xs text-gray-400">
                            {new Date(user.createdAt).toLocaleDateString('vi-VN')}
                          </div>
                        </div>
                      </div>
                      <div className="mt-2 flex items-center justify-between gap-2">
                        <select
                          value={user.role}
                          onChange={(e) => updateUserRole(user._id, e.target.value)}
                          className={`text-xs px-2 py-1 rounded-full font-medium border ${col.badgeBg} border-current flex-1`}
                        >
                          <option value="user">👤 Người dùng</option>
                          <option value="shop">🏪 Shop</option>
                          <option value="admin">🛡️ Admin</option>
                        </select>
                        <button
                          onClick={() => deleteUser(user._id)}
                          className="text-xs text-red-500 hover:text-red-700 font-medium flex-shrink-0"
                        >
                          Xóa
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
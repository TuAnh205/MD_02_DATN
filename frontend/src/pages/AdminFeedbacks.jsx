import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminFeedbacks() {
  const [feedbacks, setFeedbacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchFeedbacks();
  }, [page]);

  const fetchFeedbacks = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/feedbacks?page=${page}&limit=10`);
      setFeedbacks(response.data.feedbacks);
      setTotalPages(response.data.pagination.pages);
    } catch (error) {
      console.error('Error fetching feedbacks:', error);
    } finally {
      setLoading(false);
    }
  };

  const deleteFeedback = async (feedbackId) => {
    if (!window.confirm('Bạn có chắc muốn xóa phản hồi này?')) return;

    try {
      await api.delete(`/admin/feedbacks/${feedbackId}`);
      fetchFeedbacks();
    } catch (error) {
      console.error('Error deleting feedback:', error);
      alert('Có lỗi xảy ra khi xóa phản hồi');
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'complaint': return 'bg-red-200 text-red-900';
      case 'suggestion': return 'bg-blue-200 text-blue-900';
      case 'question': return 'bg-amber-200 text-amber-900';
      case 'praise': return 'bg-green-200 text-green-900';
      default: return 'bg-slate-200 text-slate-900';
    }
  };

  const getTypeText = (type) => {
    switch (type) {
      case 'complaint': return '⚠️ Khiếu nại';
      case 'suggestion': return '💡 Góp ý';
      case 'question': return '❓ Câu hỏi';
      case 'praise': return '👍 Khen ngợi';
      default: return type;
    }
  };

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
          <h1 className="text-3xl font-bold text-slate-900">Quản lý Phản hồi</h1>
          <p className="text-slate-600 mt-2">Xem và xử lý phản hồi từ khách hàng ({feedbacks.length})</p>
        </div>
      </div>

      {/* Feedbacks List */}
      <div className="space-y-5">
        {feedbacks.map((feedback) => (
          <div key={feedback._id} className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-all duration-200">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-4">
                  <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl flex items-center justify-center text-white font-bold shadow-md">
                    {feedback.user?.name?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <div>
                        <div className="font-bold text-slate-900 truncate">{feedback.user?.name}</div>
                        <div className="text-sm text-slate-500 truncate">{feedback.user?.email}</div>
                      </div>
                      <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg flex-shrink-0 ${getTypeColor(feedback.type)}`}>
                        {getTypeText(feedback.type)}
                      </span>
                    </div>
                    <div className="text-sm text-slate-500 mt-2 flex items-center gap-2">
                      📅 {new Date(feedback.createdAt).toLocaleDateString('vi-VN')}
                      <span className="text-slate-400">•</span>
                      🕐 {new Date(feedback.createdAt).toLocaleTimeString('vi-VN')}
                    </div>
                  </div>
                </div>

                <div className="mb-4 p-4 bg-gradient-to-r from-slate-50 to-blue-50 rounded-xl border-2 border-blue-200">
                  <h4 className="font-bold text-slate-900 mb-2">{feedback.subject}</h4>
                  <div className="text-slate-800 leading-relaxed">
                    {feedback.message}
                  </div>
                </div>

                {feedback.order && (
                  <div className="mt-4 p-3 bg-gradient-to-r from-orange-50 to-yellow-50 rounded-lg border-l-4 border-orange-600">
                    <div className="text-sm font-semibold text-orange-800">
                      📦 Liên quan đến đơn hàng: #{feedback.order.slice(-8)}
                    </div>
                  </div>
                )}
              </div>

              <div className="ml-4 flex-shrink-0">
                <button
                  onClick={() => deleteFeedback(feedback._id)}
                  className="px-4 py-2 text-sm text-white font-bold bg-red-500 hover:bg-red-600 rounded-lg transition-all duration-200 hover:shadow-md"
                >
                  Xóa
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {feedbacks.length === 0 && (
        <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-12 text-center">
          <div className="text-slate-300 text-7xl mb-4">💬</div>
          <h3 className="text-lg font-bold text-slate-900 mb-2">Chưa có phản hồi nào</h3>
          <p className="text-slate-600">Phản hồi từ khách hàng sẽ xuất hiện ở đây</p>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6">
          <button
            onClick={() => setPage(page - 1)}
            disabled={page === 1}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            ← Trước
          </button>
          <span className="text-sm font-bold text-slate-800">
            Trang <span className="text-blue-600">{page}</span> / <span className="text-slate-600">{totalPages}</span>
          </span>
          <button
            onClick={() => setPage(page + 1)}
            disabled={page === totalPages}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            Sau →
          </button>
        </div>
      )}
    </div>
  );
}
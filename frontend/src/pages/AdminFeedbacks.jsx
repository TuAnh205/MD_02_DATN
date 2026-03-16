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
      case 'complaint': return 'bg-red-100 text-red-800';
      case 'suggestion': return 'bg-blue-100 text-blue-800';
      case 'question': return 'bg-yellow-100 text-yellow-800';
      case 'praise': return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeText = (type) => {
    switch (type) {
      case 'complaint': return 'Khiếu nại';
      case 'suggestion': return 'Góp ý';
      case 'question': return 'Câu hỏi';
      case 'praise': return 'Khen ngợi';
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
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Phản hồi</h1>
          <p className="text-gray-600 mt-1">Xem và xử lý phản hồi từ khách hàng</p>
        </div>
      </div>

      {/* Feedbacks List */}
      <div className="space-y-4">
        {feedbacks.map((feedback) => (
          <div key={feedback._id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <span className="text-blue-600 font-medium">
                      {feedback.user?.name?.charAt(0)?.toUpperCase() || 'U'}
                    </span>
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="font-medium text-gray-900">{feedback.user?.name}</div>
                        <div className="text-sm text-gray-500">{feedback.user?.email}</div>
                      </div>
                      <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getTypeColor(feedback.type)}`}>
                        {getTypeText(feedback.type)}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500 mt-1">
                      {new Date(feedback.createdAt).toLocaleDateString('vi-VN')} lúc{' '}
                      {new Date(feedback.createdAt).toLocaleTimeString('vi-VN')}
                    </div>
                  </div>
                </div>

                <div className="mb-3">
                  <h4 className="font-medium text-gray-900 mb-2">{feedback.subject}</h4>
                  <div className="text-gray-700 leading-relaxed">
                    {feedback.message}
                  </div>
                </div>

                {feedback.order && (
                  <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                    <div className="text-sm text-gray-600">
                      <span className="font-medium">Liên quan đến đơn hàng:</span> #{feedback.order.slice(-8)}
                    </div>
                  </div>
                )}
              </div>

              <div className="ml-4">
                <button
                  onClick={() => deleteFeedback(feedback._id)}
                  className="px-3 py-1 text-sm text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
                >
                  Xóa
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {feedbacks.length === 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <div className="text-gray-400 text-6xl mb-4">💬</div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Chưa có phản hồi nào</h3>
          <p className="text-gray-500">Phản hồi từ khách hàng sẽ xuất hiện ở đây</p>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center">
          <button
            onClick={() => setPage(page - 1)}
            disabled={page === 1}
            className="px-4 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            ← Trước
          </button>
          <span className="text-sm text-gray-700">
            Trang {page} / {totalPages}
          </span>
          <button
            onClick={() => setPage(page + 1)}
            disabled={page === totalPages}
            className="px-4 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau →
          </button>
        </div>
      )}
    </div>
  );
}
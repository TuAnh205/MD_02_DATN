import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [replyText, setReplyText] = useState('');
  const [editingReviewId, setEditingReviewId] = useState(null);

  useEffect(() => {
    fetchReviews();
  }, [page]);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/reviews?page=${page}&limit=10`);
      setReviews(response.data.reviews);
      setTotalPages(response.data.pagination.pages);
    } catch (error) {
      console.error('Error fetching reviews:', error);
    } finally {
      setLoading(false);
    }
  };

  const deleteReview = async (reviewId) => {
    if (!window.confirm('Bạn có chắc muốn xóa đánh giá này?')) return;

    try {
      await api.delete(`/admin/reviews/${reviewId}`);
      fetchReviews();
    } catch (error) {
      console.error('Error deleting review:', error);
      alert('Có lỗi xảy ra khi xóa đánh giá');
    }
  };

  const replyReview = async (reviewId) => {
    if (!replyText.trim()) {
      alert('Vui lòng nhập nội dung trả lời');
      return;
    }

    try {
      await api.put(`/admin/reviews/${reviewId}/reply`, { text: replyText });
      setReplyText('');
      setEditingReviewId(null);
      fetchReviews();
    } catch (error) {
      console.error('Error replying to review:', error);
      alert('Có lỗi xảy ra khi trả lời đánh giá');
    }
  };

  const renderStars = (rating) => {
    return (
      <div className="flex items-center">
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            className={`text-lg ${star <= rating ? 'text-yellow-400' : 'text-gray-300'}`}
          >
            ★
          </span>
        ))}
        <span className="ml-2 text-sm text-gray-600">({rating}/5)</span>
      </div>
    );
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
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Đánh giá</h1>
          <p className="text-gray-600 mt-1">Xem và quản lý đánh giá sản phẩm từ khách hàng</p>
        </div>
      </div>

      {/* Reviews List */}
      <div className="space-y-4">
        {reviews.map((review) => (
          <div key={review._id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <span className="text-blue-600 font-medium">
                      {review.user?.name?.charAt(0)?.toUpperCase() || 'U'}
                    </span>
                  </div>
                  <div>
                    <div className="font-medium text-gray-900">{review.user?.name}</div>
                    <div className="text-sm text-gray-500">
                      {new Date(review.createdAt).toLocaleDateString('vi-VN')} lúc{' '}
                      {new Date(review.createdAt).toLocaleTimeString('vi-VN')}
                    </div>
                  </div>
                </div>

                <div className="mb-3">
                  <div className="font-medium text-gray-900 mb-1">
                    {review.product?.name}
                  </div>
                  {renderStars(review.rating)}
                </div>

                <div className="text-gray-700 leading-relaxed">
                  {review.comment}
                </div>

                {review.images && review.images.length > 0 && (
                  <div className="mt-4 flex space-x-2">
                    {review.images.map((image, index) => (
                      <img
                        key={index}
                        src={image}
                        alt={`Review image ${index + 1}`}
                        className="w-20 h-20 object-cover rounded-lg border border-gray-200"
                      />
                    ))}
                  </div>
                )}

                {review.response && (
                  <div className="mt-4 p-4 bg-blue-50 rounded-lg border-l-4 border-blue-400">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="text-sm font-medium text-blue-800">
                        Phản hồi từ {review.response.respondedBy?.name || 'Admin'}
                      </span>
                      <span className="text-xs text-blue-600">
                        {new Date(review.response.respondedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                    <p className="text-blue-700">{review.response.text}</p>
                  </div>
                )}

                {editingReviewId === review._id && (
                  <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                    <textarea
                      value={replyText}
                      onChange={(e) => setReplyText(e.target.value)}
                      placeholder="Nhập nội dung trả lời..."
                      className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      rows={3}
                    />
                    <div className="flex space-x-2 mt-2">
                      <button
                        onClick={() => replyReview(review._id)}
                        className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        Gửi trả lời
                      </button>
                      <button
                        onClick={() => {
                          setEditingReviewId(null);
                          setReplyText('');
                        }}
                        className="px-4 py-2 bg-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-400 transition-colors"
                      >
                        Hủy
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="ml-4 flex flex-col space-y-2">
                {!review.response && editingReviewId !== review._id && (
                  <button
                    onClick={() => setEditingReviewId(review._id)}
                    className="px-3 py-1 text-sm text-blue-600 border border-blue-300 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    Trả lời
                  </button>
                )}
                <button
                  onClick={() => deleteReview(review._id)}
                  className="px-3 py-1 text-sm text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
                >
                  Xóa
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {reviews.length === 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <div className="text-gray-400 text-6xl mb-4">⭐</div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Chưa có đánh giá nào</h3>
          <p className="text-gray-500">Đánh giá sẽ xuất hiện ở đây khi khách hàng đánh giá sản phẩm</p>
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
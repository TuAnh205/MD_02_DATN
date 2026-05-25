import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

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
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Quản lý Đánh giá</h1>
          <p className="text-slate-600 mt-2">Xem và quản lý đánh giá sản phẩm từ khách hàng ({reviews.length})</p>
        </div>
      </div>

      {/* Reviews List */}
      <div className="space-y-5">
        {reviews.map((review) => (
          <div key={review._id} className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6 hover:shadow-md transition-all duration-200">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1">
                <div className="flex items-center space-x-4 mb-4">
                  <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl flex items-center justify-center text-white font-bold shadow-md">
                    {review.user?.name?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <div>
                    <div className="font-bold text-slate-900">{review.user?.name}</div>
                    <div className="text-sm text-slate-500 flex items-center gap-2">
                      📅 {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                      <span className="text-slate-400">•</span>
                      🕐 {new Date(review.createdAt).toLocaleTimeString('vi-VN')}
                    </div>
                  </div>
                </div>

                <div className="mb-4 p-4 bg-gradient-to-r from-blue-50 to-slate-50 rounded-xl border-2 border-blue-200">
                  <div className="font-bold text-slate-900 mb-2">
                    📦 {review.product?.name}
                  </div>
                  {renderStars(review.rating)}
                </div>

                <div className="text-slate-800 leading-relaxed mb-4 p-3 bg-slate-50 rounded-lg border-l-4 border-blue-600">
                  {review.comment}
                </div>

                {review.images && review.images.length > 0 && (
                  <div className="mt-4 flex space-x-3 flex-wrap">
                    {review.images.map((image, index) => (
                      <img
                        key={index}
                        src={image}
                        alt={`Review image ${index + 1}`}
                        className="w-24 h-24 object-cover rounded-lg border-2 border-slate-200 hover:scale-105 transition-transform duration-200"
                      />
                    ))}
                  </div>
                )}

                {review.response && (
                  <div className="mt-4 p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg border-l-4 border-green-600">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="text-sm font-bold text-green-800">
                        ✓ Phản hồi từ {review.response.respondedBy?.name || 'Shop'}
                      </span>
                      <span className="text-xs text-green-700">
                        {new Date(review.response.respondedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                    <p className="text-green-800">{review.response.text}</p>
                  </div>
                )}
              </div>

              <div className="ml-4 flex-shrink-0">
                <button
                  onClick={() => deleteReview(review._id)}
                  className="px-4 py-2 text-sm text-white font-bold bg-red-500 hover:bg-red-600 rounded-lg transition-all duration-200 hover:shadow-md"
                >
                  Xóa
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {reviews.length === 0 && (
        <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-12 text-center">
          <div className="text-slate-300 text-7xl mb-4">⭐</div>
          <h3 className="text-lg font-bold text-slate-900 mb-2">Chưa có đánh giá nào</h3>
          <p className="text-slate-600">Đánh giá sẽ xuất hiện ở đây khi khách hàng đánh giá sản phẩm</p>
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
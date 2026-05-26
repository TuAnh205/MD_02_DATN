import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function ShopReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [replyText, setReplyText] = useState('');
  const [selectedReviewId, setSelectedReviewId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchReviews();
  }, []);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      const response = await api.get('/shop/reviews');
      setReviews(response.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể tải đánh giá');
    } finally {
      setLoading(false);
    }
  };

  const handleReply = async (reviewId) => {
    if (!replyText.trim()) {
      setError('Vui lòng nhập nội dung trả lời');
      return;
    }
    try {
      await api.put(`/shop/reviews/${reviewId}/reply`, { text: replyText.trim() });
      setReplyText('');
      setSelectedReviewId(null);
      fetchReviews();
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể gửi phản hồi');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="space-y-4 text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-4 border-blue-200 border-t-blue-600 mx-auto"></div>
          <p className="text-slate-600 font-semibold">Đang tải dữ liệu...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm">
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
          <span>⭐</span> Đánh Giá Sản Phẩm
        </h1>
        <p className="text-slate-600 mt-2 font-semibold">
          Quản lý và trả lời các đánh giá từ khách hàng
        </p>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="rounded-2xl border-2 border-rose-300 bg-gradient-to-r from-rose-50 to-pink-50 p-6 shadow-sm">
          <div className="flex items-start gap-4">
            <span className="text-3xl">⚠️</span>
            <div className="flex-1">
              <p className="font-bold text-rose-900">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Reviews List */}
      {reviews.length === 0 ? (
        <div className="rounded-2xl bg-white border-2 border-slate-200 p-12 text-center shadow-sm">
          <div className="text-6xl mb-4 opacity-20">⭐</div>
          <p className="text-slate-600 font-semibold text-lg">Chưa có đánh giá nào</p>
          <p className="text-slate-400 text-sm mt-1">Các đánh giá từ khách hàng sẽ hiển thị ở đây</p>
        </div>
      ) : (
        <div className="space-y-6">
          {reviews.map((review) => (
            <div key={review._id} className="rounded-2xl bg-white border-2 border-slate-200 p-6 shadow-sm hover:shadow-md transition-shadow">
              {/* Review Header */}
              <div className="flex items-start justify-between mb-4 pb-4 border-b-2 border-slate-200">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm">
                      {review.user?.name?.charAt(0)?.toUpperCase() || '?'}
                    </div>
                    <div>
                      <p className="font-bold text-slate-900">{review.user?.name || 'Người dùng'}</p>
                      <p className="text-xs text-slate-500">{review.user?.email}</p>
                    </div>
                  </div>
                </div>
                <p className="text-xs font-bold text-slate-500">
                  {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                </p>
              </div>

              {/* Product & Rating Info */}
              <div className="mb-4 p-4 bg-slate-50 rounded-xl border-2 border-slate-200">
                <p className="text-xs font-bold text-slate-600 uppercase tracking-widest mb-2">Sản Phẩm</p>
                <p className="font-bold text-slate-900 mb-3">{review.product?.name || 'Không xác định'}</p>
                <div className="flex items-center gap-3">
                  <span className="text-lg">⭐</span>
                  <span className="text-2xl font-bold text-amber-500">
                    {'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}
                  </span>
                  <span className="text-sm font-bold text-slate-900">{review.rating}/5 sao</span>
                </div>
              </div>

              {/* Review Comment */}
              <div className="mb-4">
                <p className="text-sm text-slate-700 leading-relaxed">{review.comment}</p>
              </div>

              {/* Response Section */}
              {review.response ? (
                <div className="rounded-xl bg-gradient-to-br from-green-50 to-emerald-50 p-4 border-2 border-green-200">
                  <div className="flex items-start gap-2 mb-2">
                    <span className="text-lg">✓</span>
                    <p className="font-bold text-green-900">Phản hồi của bạn</p>
                  </div>
                  <p className="text-sm text-green-800 mb-3">{review.response.text}</p>
                  <p className="text-xs text-green-700 font-semibold">
                    {review.response.respondedBy?.name || 'Bạn'} • {new Date(review.response.respondedAt).toLocaleString('vi-VN')}
                  </p>
                </div>
              ) : (
                <div className="rounded-xl bg-slate-50 p-4 border-2 border-slate-200">
                  <p className="text-xs font-bold text-slate-600 uppercase tracking-widest mb-3">Trả Lời Đánh Giá</p>
                  <textarea
                    value={selectedReviewId === review._id ? replyText : ''}
                    onChange={(e) => {
                      setSelectedReviewId(review._id);
                      setReplyText(e.target.value);
                    }}
                    rows={3}
                    className="w-full rounded-lg border-2 border-slate-200 p-3 text-slate-900 font-medium focus:ring-2 focus:ring-blue-600 focus:border-blue-600 transition-colors resize-none"
                    placeholder="Nhập phản hồi của bạn..."
                  />
                  <button
                    onClick={() => handleReply(review._id)}
                    className="mt-3 inline-flex items-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-2 text-sm font-bold text-white hover:shadow-md transition-all duration-200"
                  >
                    <span>💬</span> Gửi Phản Hồi
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
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
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Đánh giá sản phẩm của Shop</h1>
      {error && <div className="bg-red-100 text-red-700 p-3 rounded">{error}</div>}

      {reviews.length === 0 ? (
        <div className="bg-white p-6 rounded-lg shadow text-gray-500">Chưa có đánh giá</div>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => (
            <div key={review._id} className="bg-white p-4 rounded-lg shadow">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs text-gray-500">Sản phẩm: {review.product?.name || 'Không có'}</p>
                  <p className="font-semibold">{review.product?.name || 'Sản phẩm'}</p>
                  <p className="text-xs text-gray-500">Mã SP: {review.product?._id || '---'}</p>
                  <p className="text-sm text-gray-500">Người đánh giá: {review.user?.name || 'Người dùng'}</p>
                  <p className="text-sm text-gray-500">Điểm: {review.rating} sao</p>
                  <p className="mt-2 text-gray-700">{review.comment}</p>
                </div>
                <span className="text-xs text-gray-400">{new Date(review.createdAt).toLocaleDateString('vi-VN')}</span>
              </div>

              {review.response ? (
                <div className="mt-4 p-3 bg-gray-50 rounded border border-gray-200">
                  <p className="text-sm text-gray-700 font-semibold">Phản hồi đã gửi</p>
                  <p className="text-sm text-gray-600">{review.response.text}</p>
                  <p className="text-xs text-gray-500">{review.response.respondedBy?.name || 'Bạn'} • {new Date(review.response.respondedAt).toLocaleString('vi-VN')}</p>
                </div>
              ) : (
                <div className="mt-4">
                  <textarea
                    value={selectedReviewId === review._id ? replyText : ''}
                    onChange={(e) => {
                      setSelectedReviewId(review._id);
                      setReplyText(e.target.value);
                    }}
                    rows={3}
                    className="w-full border border-gray-300 rounded p-2"
                    placeholder="Nhập phản hồi cho đánh giá này"
                  />
                  <button
                    onClick={() => handleReply(review._id)}
                    className="mt-2 bg-blue-600 text-white px-4 py-2 rounded"
                  >
                    Gửi phản hồi
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
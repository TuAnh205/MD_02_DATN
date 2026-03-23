import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { cartService } from '../services/cartService';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState(null);
  const [activeImageIndex, setActiveImageIndex] = useState(0);

  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(true);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewMessage, setReviewMessage] = useState(null);

  useEffect(() => {
    fetchProductDetail();
    fetchReviews();
  }, [id]);

  const fetchReviews = async () => {
    try {
      setLoadingReviews(true);
      const response = await api.get(`/reviews/product/${id}`);
      setReviews(response.data || []);
    } catch (err) {
      console.error('Error fetching reviews:', err);
    } finally {
      setLoadingReviews(false);
    }
  };

  const fetchProductDetail = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/products/${id}`);
      setProduct(response.data);
    } catch (err) {
      console.error('Error fetching product:', err);
      setError('Không tìm thấy sản phẩm');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl font-bold text-red-600 mb-4">{error || 'Sản phẩm không tồn tại'}</p>
          <button
            onClick={() => navigate('/')}
            className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90"
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    );
  }

  const handleAddToCart = async () => {
    try {
      await cartService.addToCart(id, quantity);
      alert(`Đã thêm ${quantity} sản phẩm vào giỏ hàng!`);
      setQuantity(1);
    } catch (err) {
      alert('Không thể thêm vào giỏ hàng: ' + err.message);
    }
  };

  const handleSubmitReview = async () => {
    if (!comment.trim()) {
      setReviewMessage({ type: 'error', text: 'Vui lòng nhập đánh giá.' });
      return;
    }

    try {
      setSubmittingReview(true);
      setReviewMessage(null);
      await api.post('/reviews', {
        productId: id,
        rating,
        comment: comment.trim(),
      });
      setReviewMessage({ type: 'success', text: 'Cảm ơn bạn đã đánh giá!' });
      setComment('');
      setRating(5);
      fetchReviews();
    } catch (err) {
      const message = err.response?.data?.message || err.message;
      setReviewMessage({ type: 'error', text: message });
    } finally {
      setSubmittingReview(false);
    }
  };

  const averageRating = reviews.length
    ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length
    : product.ratings?.average;
  const ratingCount = reviews.length || product.ratings?.count || 0;

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Breadcrumb */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/')}
            className="text-blue-600 hover:underline text-sm"
          >
            ← Quay lại
          </button>
        </div>

        {/* Product Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 bg-white p-8 rounded-lg shadow">
          {/* Image Section */}
          <div className="flex flex-col items-center">
            {(() => {
              const rawImages = product.images || [];
              const normalizedImages = Array.isArray(rawImages)
                ? rawImages
                : typeof rawImages === 'string'
                ? [rawImages]
                : [];

              const defaultImg = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60';
              const images = [product.image, ...normalizedImages, defaultImg].filter(Boolean);

              const activeImage = images[activeImageIndex] || images[0];

              return (
                <>
                  <img
                    src={activeImage}
                    alt={product.name}
                    className="w-full max-w-sm h-auto rounded-lg object-cover mb-4"
                  />

                  {images.length > 1 && (
                    <div className="flex gap-2 overflow-x-auto pb-2">
                      {images.map((img, idx) => (
                        <button
                          key={idx}
                          type="button"
                          onClick={() => setActiveImageIndex(idx)}
                          className={`w-20 h-20 rounded-lg overflow-hidden border ${
                            idx === activeImageIndex ? 'border-primary' : 'border-gray-200'
                          } focus:outline-none`}
                        >
                          <img
                            src={img}
                            alt={`${product.name} ${idx + 1}`}
                            className="w-full h-full object-cover"
                          />
                        </button>
                      ))}
                    </div>
                  )}
                </>
              );
            })()}
          </div>

          {/* Info Section */}
          <div>
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4">
              <h1 className="text-4xl font-bold text-dark">{product.name}</h1>
              {product.createdBy && (
                <p className="text-sm text-gray-500 mt-2 sm:mt-0">
                  Người tạo: <span className="font-semibold text-dark">{product.createdBy.name}</span>
                </p>
              )}
            </div>

            {/* Price */}
            <div className="mb-6">
              <p className="text-3xl font-bold text-primary">
                ₫{product.price?.toLocaleString('vi-VN')}
              </p>
              {product.originalPrice && product.originalPrice > product.price && (
                <p className="text-lg text-gray-400 line-through">
                  ₫{product.originalPrice?.toLocaleString('vi-VN')}
                </p>
              )}
            </div>

            {/* Category & Brand */}
            <div className="mb-6 space-y-2">
              {product.category && (
                <p className="text-sm">
                  <span className="font-semibold">Danh mục:</span> {product.category}
                </p>
              )}
              {product.brand && (
                <p className="text-sm">
                  <span className="font-semibold">Thương hiệu:</span> {product.brand}
                </p>
              )}
              {product.tags && product.tags.length > 0 && (
                <p className="text-sm">
                  <span className="font-semibold">Tags:</span> {product.tags.join(', ')}
                </p>
              )}
            </div>

            {/* Description */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold mb-2">Mô tả</h3>
              <p className="text-gray-600">
                {product.description || product.detailedDescription || 'Chưa có mô tả'}
              </p>
            </div>


            {/* Technical Details */}
            {(product.attributes || product.variants?.length || product.weight || product.dimensions) && (
              <div className="mb-6">
                <h3 className="text-lg font-semibold mb-2">Chi tiết sản phẩm</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {product.attributes && Object.keys(product.attributes).length > 0 && (
                    <div className="bg-gray-50 p-4 rounded">
                      <h4 className="text-sm font-semibold mb-2">Thông số kỹ thuật</h4>
                      <div className="space-y-2 text-sm text-gray-700">
                        {Object.entries(product.attributes).map(([key, value]) => (
                          <div key={key} className="flex justify-between">
                            <span className="text-gray-500">{key}</span>
                            <span className="font-medium">{value}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {(product.weight || product.dimensions) && (
                    <div className="bg-gray-50 p-4 rounded">
                      <h4 className="text-sm font-semibold mb-2">Kích thước & Trọng lượng</h4>
                      <div className="space-y-2 text-sm text-gray-700">
                        {product.weight && (
                          <div className="flex justify-between">
                            <span className="text-gray-500">Trọng lượng</span>
                            <span className="font-medium">{product.weight} kg</span>
                          </div>
                        )}
                        {product.dimensions && (
                          <div className="flex justify-between">
                            <span className="text-gray-500">Kích thước</span>
                            <span className="font-medium">
                              {product.dimensions.length || 0} x {product.dimensions.width || 0} x {product.dimensions.height || 0} cm
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {product.variants?.length > 0 && (
                    <div className="bg-gray-50 p-4 rounded">
                      <h4 className="text-sm font-semibold mb-2">Phiên bản</h4>
                      <div className="space-y-2 text-sm text-gray-700">
                        {product.variants.map((variant, idx) => (
                          <div key={idx} className="border-b last:border-b-0 pb-2">
                            <div className="flex justify-between">
                              <span className="font-medium">{variant.name}</span>
                              <span className="text-gray-500">{variant.value}</span>
                            </div>
                            {variant.priceModifier ? (
                              <div className="text-xs text-gray-500">
                                +₫{variant.priceModifier.toLocaleString('vi-VN')}
                              </div>
                            ) : null}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Rating Display */}
            <div className="mb-6">
              <div className="flex items-center gap-2 mb-2">
                <span className="font-semibold">Đánh giá:</span>
                {ratingCount > 0 ? (
                  <>
                    <div className="flex items-center">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span
                          key={star}
                          className={`text-lg ${
                            star <= Math.floor(averageRating)
                              ? 'text-yellow-500'
                              : star - 0.5 <= averageRating
                              ? 'text-yellow-500'
                              : 'text-gray-300'
                          }`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                    <span className="font-semibold text-gray-800">
                      {averageRating.toFixed(1)}
                    </span>
                    <span className="text-gray-500">({ratingCount} đánh giá)</span>
                  </>
                ) : (
                  <span className="text-gray-400">Chưa có đánh giá</span>
                )}
              </div>
            </div>

            {/* Quantity & Add to Cart */}
            <div className="flex items-center gap-4 mb-6">
              <div className="flex items-center border rounded">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  disabled={product.stock === 0}
                  className="px-4 py-2 hover:bg-gray-100 disabled:text-gray-400"
                >
                  −
                </button>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  disabled={product.stock === 0}
                  className="w-16 text-center border-l border-r py-2 disabled:bg-gray-100"
                  min="1"
                  max={product.stock}
                />
                <button
                  onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                  disabled={product.stock === 0}
                  className="px-4 py-2 hover:bg-gray-100 disabled:text-gray-400"
                >
                  +
                </button>
              </div>
              <button
                onClick={handleAddToCart}
                disabled={product.stock === 0}
                className="flex-1 bg-primary text-white py-3 rounded font-semibold hover:bg-primary/90 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                Thêm vào giỏ hàng
              </button>
            </div>

            {/* Additional Info */}
            <div className="pt-6 border-t">
              <p className="text-sm">
                <span className="font-semibold">Đánh giá:</span>{' '}
                {ratingCount > 0 ? (
                  <>
                    <div className="flex items-center gap-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span
                          key={star}
                          className={`text-sm ${
                            star <= Math.floor(averageRating)
                              ? 'text-yellow-500'
                              : star - 0.5 <= averageRating
                              ? 'text-yellow-500'
                              : 'text-gray-300'
                          }`}
                        >
                          ★
                        </span>
                      ))}
                      <span className="font-semibold text-gray-800">
                        {averageRating.toFixed(1)}
                      </span>
                      <span className="text-gray-500">({ratingCount} đánh giá)</span>
                    </div>
                  </>
                ) : (
                  'Chưa có đánh giá'
                )}
              </p>
            </div>
          </div>
        </div>

        {/* Reviews */}
        <div className="max-w-6xl mx-auto px-4 py-10">
          <h2 className="text-2xl font-bold text-dark mb-6">Đánh giá khách hàng</h2>

          <div className="bg-white p-6 rounded-lg shadow mb-8">
            <h3 className="text-lg font-semibold mb-3">Viết đánh giá của bạn</h3>
            {reviewMessage && (
              <div
                className={`mb-4 rounded px-4 py-3 text-sm ${
                  reviewMessage.type === 'success' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                }`}
              >
                {reviewMessage.text}
              </div>
            )}
            <div className="mb-4">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-sm font-medium">Đánh giá:</span>
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setRating(star)}
                      className={`text-2xl leading-none transition ${
                        star <= rating ? 'text-accent' : 'text-gray-300 hover:text-accent'
                      }`}
                    >
                      ★
                    </button>
                  ))}
                </div>
              </div>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={4}
                placeholder="Viết cảm nhận của bạn về sản phẩm..."
                className="w-full border border-gray-200 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
            <button
              onClick={handleSubmitReview}
              disabled={submittingReview}
              className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90 disabled:bg-gray-300"
            >
              {submittingReview ? 'Đang gửi...' : 'Gửi đánh giá'}
            </button>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold mb-4">Đánh giá gần đây</h3>
            {loadingReviews ? (
              <div className="text-gray-500">Đang tải đánh giá...</div>
            ) : reviews.length === 0 ? (
              <p className="text-gray-500">Chưa có đánh giá nào. Hãy là người đầu tiên nhận xét!</p>
            ) : (
              <div className="space-y-6">
                {reviews.map((review) => (
                  <div key={review._id} className="border border-gray-100 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="text-sm font-semibold text-dark">
                        {review.user?.name || 'Khách hàng'}
                      </div>
                      <div className="text-xs text-gray-500">
                        {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                    <div className="flex items-center gap-1 mb-2">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span
                          key={star}
                          className={star <= review.rating ? 'text-accent' : 'text-gray-200'}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                    {review.comment && <p className="text-gray-700">{review.comment}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

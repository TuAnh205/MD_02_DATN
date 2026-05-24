import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { favoriteService } from '../services/favoriteService';

export default function FeaturedProducts() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [favoriteIds, setFavoriteIds] = useState(new Set());
  const [favoriteLoading, setFavoriteLoading] = useState(true);

  useEffect(() => {
    fetchFeaturedProducts();
  }, []);

  useEffect(() => {
    const loadFavorites = async () => {
      if (!user) {
        setFavoriteIds(new Set());
        setFavoriteLoading(false);
        return;
      }

      try {
        setFavoriteLoading(true);
        const data = await favoriteService.listFavorites();
        const ids = new Set(data.map((favorite) => favorite.product?._id || favorite.product));
        setFavoriteIds(ids);
      } catch (err) {
        setFavoriteIds(new Set());
      } finally {
        setFavoriteLoading(false);
      }
    };

    loadFavorites();
  }, [user]);

  const fetchFeaturedProducts = async () => {
    try {
      setLoading(true);
      const response = await api.get('/products?isFeatured=true&limit=1000');
      setProducts(response.data.data || []);
    } catch (err) {
      console.error('Error fetching featured products:', err);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const getDiscountPercent = (product) => {
    const original = product.originalPrice || product.price;
    if (!original || original <= product.price) return 0;
    return Math.round((1 - product.price / original) * 100);
  };

  const isFavorite = (productId) => favoriteIds.has(productId);

  const toggleFavorite = async (event, product) => {
    event.preventDefault();
    event.stopPropagation();
    if (!user) {
      navigate('/login');
      return;
    }

    const productId = product._id;
    const alreadyFavorite = favoriteIds.has(productId);
    const nextFavorites = new Set(favoriteIds);
    if (alreadyFavorite) {
      nextFavorites.delete(productId);
    } else {
      nextFavorites.add(productId);
    }
    setFavoriteIds(nextFavorites);

    try {
      if (alreadyFavorite) {
        await favoriteService.removeFavorite(productId);
      } else {
        await favoriteService.addFavorite(productId);
      }
      window.dispatchEvent(new CustomEvent('favoritesChanged'));
    } catch (err) {
      setFavoriteIds(favoriteIds);
      console.error('Error toggling favorite:', err);
    }
  };

  return (
    <div className="min-h-screen bg-light">
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div>
          <h2 className="text-3xl font-bold text-dark mb-2">Sản phẩm nổi bật</h2>
          <p className="text-gray-500 mb-6">
            {products.length} sản phẩm được tìm thấy
          </p>
        </div>

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="text-gray-500">Đang tải sản phẩm...</div>
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm nổi bật nào</p>
            <Link to="/products" className="text-primary hover:underline mt-4 inline-block">
              ← Quay lại tất cả sản phẩm
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 auto-rows-fr">
            {products.map((product) => {
              const discount = getDiscountPercent(product);
              const favorite = isFavorite(product._id);

              return (
                <div key={product._id} className="h-full">
                  <div className="card hover:shadow-lg relative flex flex-col h-full min-h-96">
                    <button
                      type="button"
                      onClick={(event) => toggleFavorite(event, product)}
                      className={`absolute top-4 left-4 z-10 w-11 h-11 rounded-full border border-gray-200 flex items-center justify-center transition shadow-sm pointer-events-auto ${
                        favorite ? 'bg-red-100 text-red-600' : 'bg-white text-gray-400 hover:text-red-500'
                      }`}
                      title={favorite ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'}
                    >
                      <span className={`text-xl ${favorite ? 'text-red-600' : 'text-gray-400'}`}>
                        ♥
                      </span>
                    </button>

                    {discount > 0 && (
                      <span className="badge badge-discount absolute right-4 top-4">
                        -{discount}%
                      </span>
                    )}

                    <Link to={`/products/${product._id}`} className="flex-1 flex flex-col overflow-hidden">
                      <img
                        src={product.image || product.images?.[0] || 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=60'}
                        alt={product.name}
                        className="w-full aspect-square object-cover rounded-t mb-4"
                      />
                      <div className="px-4 flex-1 flex flex-col">
                        <h3 className="font-semibold text-dark mb-2 line-clamp-2 h-14 flex items-start">
                          {product.name}
                        </h3>
                        <div className="flex items-center gap-2 text-sm mb-2">
                          {product.ratings?.average ? (
                            <>
                              <div className="flex items-center">
                                {[1, 2, 3, 4, 5].map((star) => (
                                  <span
                                    key={star}
                                    className={`text-sm ${
                                      star <= Math.floor(product.ratings.average)
                                        ? 'text-yellow-500'
                                        : star - 0.5 <= product.ratings.average
                                        ? 'text-yellow-500'
                                        : 'text-gray-300'
                                    }`}
                                  >
                                    ★
                                  </span>
                                ))}
                              </div>
                              <span className="font-semibold text-gray-800">
                                {product.ratings.average.toFixed(1)}
                              </span>
                            </>
                          ) : (
                            <span className="text-gray-400">Chưa có đánh giá</span>
                          )}
                        </div>
                        <div className="mt-auto" />
                      </div>
                    </Link>

                    <div className="px-4 pb-4 pt-2">
                      <div className="flex items-baseline gap-3">
                        <p className="text-primary font-bold text-lg">
                          ₫{product.price?.toLocaleString('vi-VN')}
                        </p>
                        {discount > 0 && (
                          <p className="text-sm text-gray-400 line-through">
                            ₫{product.originalPrice?.toLocaleString('vi-VN')}
                          </p>
                        )}
                      </div>
                      <p className="text-sm text-gray-500 mt-2">{product.category}</p>
                      <p className={`text-sm font-semibold mt-1 ${product.stock > 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {product.stock > 0 ? `Còn ${product.stock}` : 'Hết hàng'}
                      </p>
                      {product.shopId && (
                        <p className="text-xs text-gray-600 mt-1">Shop: {product.shopId.name}</p>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

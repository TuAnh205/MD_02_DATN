import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { favoriteService } from '../services/favoriteService';

export default function Favorites() {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadFavorites = async () => {
    try {
      setLoading(true);
      const data = await favoriteService.listFavorites();
      setFavorites(data || []);
    } catch (err) {
      console.error('Error loading favorites:', err);
      setError(err.response?.data?.message || 'Không thể tải danh sách yêu thích');
      setFavorites([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFavorites();
  }, []);

  const removeFavorite = async (productId) => {
    try {
      await favoriteService.removeFavorite(productId);
      window.dispatchEvent(new CustomEvent('favoritesChanged'));
      loadFavorites();
    } catch (err) {
      console.error('Error removing favorite:', err);
      setError(err.response?.data?.message || 'Không thể xóa sản phẩm khỏi yêu thích');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8 flex items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-dark">Sản phẩm yêu thích</h1>
            <p className="text-gray-500 mt-2">Danh sách các sản phẩm bạn đã đánh dấu yêu thích.</p>
          </div>
          <button
            type="button"
            onClick={() => navigate('/products')}
            className="bg-white border border-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-100"
          >
            Tiếp tục mua sắm
          </button>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-lg bg-red-100 text-red-700">{error}</div>
        )}

        {loading ? (
          <div className="text-gray-500">Đang tải danh sách yêu thích...</div>
        ) : favorites.length === 0 ? (
          <div className="rounded-lg bg-white p-8 text-center">
            <p className="text-lg font-semibold text-gray-800">Bạn chưa có sản phẩm yêu thích nào.</p>
            <p className="text-sm text-gray-500 mt-2">Hãy quay lại trang sản phẩm và bấm vào trái tim để lưu sản phẩm yêu thích.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {favorites.map((favorite) => {
              const product = favorite.product || {};
              return (
                <div key={product._id || favorite._id} className="bg-white rounded-3xl shadow-sm overflow-hidden">
                  <Link to={`/products/${product._id}`} className="block">
                    <img
                      src={product.image || product.images?.[0] || 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=60'}
                      alt={product.name}
                      className="w-full h-48 object-cover"
                    />
                    <div className="p-4">
                      <h2 className="font-semibold text-lg text-dark line-clamp-2 min-h-14">{product.name}</h2>
                      <p className="text-sm text-gray-500 mt-2">{product.category}</p>
                      <div className="mt-4 flex items-center justify-between gap-3">
                        <p className="font-bold text-primary">₫{product.price?.toLocaleString('vi-VN')}</p>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.preventDefault();
                            removeFavorite(product._id);
                          }}
                          className="px-3 py-2 rounded-full bg-red-100 text-red-700 text-sm hover:bg-red-200"
                        >
                          Xóa
                        </button>
                      </div>
                    </div>
                  </Link>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

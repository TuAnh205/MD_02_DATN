import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const promotions = [
  {
    title: 'Flash sale mỗi ngày',
    description: 'Ưu đãi đến 50% cho sản phẩm chọn lọc',
    emoji: '🔥',
  },
  {
    title: 'Freeship toàn quốc',
    description: 'Miễn phí giao hàng cho đơn trên 1.000.000₫',
    emoji: '🚚',
  },
  {
    title: 'Trả góp 0%',
    description: 'Duyệt nhanh trong 1 phút',
    emoji: '💳',
  },
  {
    title: 'Quà tặng kèm',
    description: 'Tặng voucher đến 500.000₫',
    emoji: '🎁',
  },
];

export default function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [filterBrand, setFilterBrand] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [minRating, setMinRating] = useState(0);
  const [sortBy, setSortBy] = useState('-createdAt');
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const { user } = useAuth();

  useEffect(() => {
    fetchCategories();
    fetchBrands();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProducts(searchQuery, filterCategory);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery, filterCategory, filterBrand, minPrice, maxPrice, minRating, sortBy]);

  const fetchCategories = async () => {
    try {
      const response = await api.get('/products/categories');
      setCategories(response.data || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const fetchBrands = async () => {
    try {
      const response = await api.get('/products/brands');
      setBrands(response.data || []);
    } catch (err) {
      console.error('Error fetching brands:', err);
    }
  };

  const fetchProducts = async (query = '', category = '') => {
    try {
      setLoading(true);

      const params = new URLSearchParams();
      params.set('limit', '20');
      params.set('page', '1');
      params.set('sort', sortBy);

      if (query) params.set('q', query);
      if (category) params.set('category', category);
      if (filterBrand) params.set('brand', filterBrand);
      if (minPrice) params.set('minPrice', minPrice);
      if (maxPrice) params.set('maxPrice', maxPrice);
      if (minRating) params.set('minRating', minRating);

      const response = await api.get(`/products?${params.toString()}`);
      setProducts(response.data.data || []);
    } catch (err) {
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const getDiscountPercent = (product) => {
    const original = product.originalPrice || product.price;
    if (!original || original <= product.price) return 0;
    return Math.round((1 - product.price / original) * 100);
  };

  return (
    <div className="min-h-screen bg-light">
      <div className="hero-banner py-16 px-6">
        <div className="hero-banner-content max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
          <div>
            <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-4">
              CORETECH - Mua sắm công nghệ đỉnh cao
            </h1>
            <p className="text-lg text-white/90 mb-6">
              Khuyến mãi mỗi ngày - trả góp 0% - freeship toàn quốc. Tìm ngay sản phẩm ưng ý!
            </p>
            <div className="flex flex-wrap gap-3">
              <Link to="/" className="btn-primary">
                Xem ưu đãi ngay
              </Link>
              <Link to="/cart" className="btn-secondary">
                Giỏ hàng của tôi
              </Link>
            </div>
          </div>
          <div className="hidden lg:block">
            <img
              src="https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60"
              alt="Khuyến mãi"
              className="rounded-xl shadow-lg"
            />
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-10">
        <h2 className="text-2xl font-bold text-dark mb-4">Khuyến mãi hot</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {promotions.map((promo) => (
            <div key={promo.title} className="promo-card">
              <div className="mb-3 text-3xl">{promo.emoji}</div>
              <h3 className="text-lg font-semibold text-dark mb-1">{promo.title}</h3>
              <p className="text-sm text-gray-600">{promo.description}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 pb-16">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Filters */}
          <aside className="lg:col-span-3 bg-white rounded-xl shadow-card p-6">
            <h3 className="text-lg font-semibold text-dark mb-4">Bộ lọc</h3>

            <div className="space-y-5">
              <div>
                <p className="text-sm font-semibold text-gray-600 mb-2">Danh mục</p>
                <div className="flex flex-wrap gap-2">
                  <button
                    className={`px-3 py-1 rounded text-xs font-medium transition-all ${
                      filterCategory === '' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                    onClick={() => setFilterCategory('')}
                  >
                    Tất cả
                  </button>
                  {categories.map((cat) => (
                    <button
                      key={cat}
                      className={`px-3 py-1 rounded text-xs font-medium transition-all ${
                        filterCategory === cat ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                      onClick={() => setFilterCategory(cat)}
                    >
                      {cat}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-600 mb-2">Thương hiệu</p>
                <div className="flex flex-wrap gap-2">
                  <button
                    className={`px-3 py-1 rounded text-xs font-medium transition-all ${
                      filterBrand === '' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                    onClick={() => setFilterBrand('')}
                  >
                    Tất cả
                  </button>
                  {brands.map((brand) => (
                    <button
                      key={brand}
                      className={`px-3 py-1 rounded text-xs font-medium transition-all ${
                        filterBrand === brand ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                      onClick={() => setFilterBrand(brand)}
                    >
                      {brand}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-600 mb-2">Giá</p>
                <div className="grid grid-cols-2 gap-3">
                  <input
                    type="number"
                    min={0}
                    value={minPrice}
                    onChange={(e) => setMinPrice(e.target.value)}
                    placeholder="Từ"
                    className="input-field"
                  />
                  <input
                    type="number"
                    min={0}
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(e.target.value)}
                    placeholder="Đến"
                    className="input-field"
                  />
                </div>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-600 mb-2">Đánh giá</p>
                <div className="flex items-center gap-2">
                  {[5, 4, 3, 2, 1].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setMinRating(star)}
                      className={`flex items-center gap-1 px-2 py-1 rounded text-xs font-medium transition ${
                        minRating === star ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      <span className="text-accent">★</span>
                      {star}+
                    </button>
                  ))}
                </div>
              </div>

              <button
                onClick={() => {
                  setFilterCategory('');
                  setFilterBrand('');
                  setMinPrice('');
                  setMaxPrice('');
                  setMinRating(0);
                  setSearchQuery('');
                  setSortBy('-createdAt');
                }}
                className="w-full bg-gray-100 text-gray-700 py-2 rounded font-medium hover:bg-gray-200"
              >
                Xóa bộ lọc
              </button>
            </div>
          </aside>

          <section className="lg:col-span-9">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-bold text-dark">Sản phẩm nổi bật</h2>
                <p className="text-sm text-gray-500 mt-1">Tìm nhanh sản phẩm bằng bộ lọc nâng cao.</p>
              </div>
              <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto items-stretch md:items-center">
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm, mã, thương hiệu..."
                  className="input-field w-full"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="input-field w-full sm:w-56"
                >
                  <option value="-createdAt">Mới nhất</option>
                  <option value="price">Giá: thấp → cao</option>
                  <option value="-price">Giá: cao → thấp</option>
                  <option value="-salesCount">Bán chạy</option>
                  <option value="-ratings.average">Đánh giá cao</option>
                </select>
              </div>
            </div>

            {loading ? (
              <div className="flex justify-center py-12">
                <div className="text-gray-500">Đang tải sản phẩm...</div>
              </div>
            ) : products.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-gray-500 text-lg">Chưa có sản phẩm nào</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {products.map((product) => {
                  const discount = getDiscountPercent(product);

                  return (
                    <Link key={product._id} to={`/products/${product._id}`}>
                      <div className="card hover:shadow-lg cursor-pointer relative">
                        {discount > 0 && (
                          <span className="badge badge-discount absolute right-4 top-4">
                            -{discount}%
                          </span>
                        )}
                        {(() => {
                          const rawImages = product.images || [];
                          const normalizedImages = Array.isArray(rawImages)
                            ? rawImages
                            : typeof rawImages === 'string'
                            ? [rawImages]
                            : [];

                          const defaultImg = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60';
                          const imageSrc = product.image || normalizedImages[0] || defaultImg;

                          return (
                            <img
                              src={imageSrc}
                              alt={product.name}
                              className="w-full h-48 object-cover rounded mb-4"
                            />
                          );
                        })()}
                        <h3 className="font-semibold text-dark mb-2 line-clamp-2">
                          {product.name}
                        </h3>
                        <div className="flex items-center gap-2 text-sm mb-2">
                          <span className="font-semibold text-gray-800">
                            {product.ratings?.average ? product.ratings.average.toFixed(1) : '—'}
                          </span>
                          <span className="text-yellow-500">★</span>
                          <span className="text-gray-500">({product.ratings?.count || 0})</span>
                        </div>
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
                        <p className="text-sm mt-1">
                          <span className={`font-semibold ${product.stock > 0 ? 'text-green-600' : 'text-red-600'}`}>
                            {product.stock > 0 ? 'Còn hàng' : 'Hết hàng'}
                          </span>
                        </p>
                      </div>
                    </Link>
                  );
                })}
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}

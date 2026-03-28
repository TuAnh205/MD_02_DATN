import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../services/api';

export default function Products() {
  const [searchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);

  // Get initial filters from URL params
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [filterCategory, setFilterCategory] = useState(searchParams.get('category') || '');
  const [filterBrand, setFilterBrand] = useState(searchParams.get('brand') || '');
  const [minPrice, setMinPrice] = useState(searchParams.get('minPrice') || '');
  const [maxPrice, setMaxPrice] = useState(searchParams.get('maxPrice') || '');
  const [minRating, setMinRating] = useState(Number(searchParams.get('minRating')) || 0);
  const [sortBy, setSortBy] = useState(searchParams.get('sort') || '-createdAt');

  useEffect(() => {
    fetchCategories();
    fetchBrands();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProducts();
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

  const fetchProducts = async () => {
    try {
      setLoading(true);

      const params = new URLSearchParams();
      params.set('limit', '10000');
      params.set('page', '1');
      params.set('sort', sortBy);

      if (searchQuery) params.set('q', searchQuery);
      if (filterCategory) params.set('category', filterCategory);
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
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Filters */}
          <aside className="lg:col-span-3">
            <div className="bg-white rounded-xl shadow-card p-6 sticky top-4">
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
                <div className="flex flex-wrap items-center gap-1.5">
                  {[5, 4, 3, 2, 1].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setMinRating(star)}
                      className={`flex items-center gap-0.5 px-1.5 py-1 rounded text-xs font-medium transition whitespace-nowrap ${
                        minRating === star ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      <span className="text-sm">★</span>
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
            </div>
          </aside>

          <section className="lg:col-span-9">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
              <div>
                <h2 className="text-2xl font-bold text-dark">
                  {filterCategory ? `Sản phẩm ${filterCategory}` : 'Tất cả sản phẩm'}
                </h2>
                <p className="text-sm text-gray-500 mt-1">
                  {products.length} sản phẩm được tìm thấy
                </p>
              </div>
              <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto items-stretch md:items-center">
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm..."
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
                <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm nào</p>
                <p className="text-gray-400 text-sm mt-2">Hãy thử điều chỉnh bộ lọc</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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
                        <img
                          src={product.image || product.images?.[0] || 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=60'}
                          alt={product.name}
                          className="w-full h-48 object-cover rounded mb-4"
                        />
                        <h3 className="font-semibold text-dark mb-2 line-clamp-2">
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
                        {product.shopId && (
                          <p className="text-xs text-gray-600 mt-1">Shop: {product.shopId.name}</p>
                        )}
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
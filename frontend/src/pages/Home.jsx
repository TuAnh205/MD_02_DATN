import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const heroSlides = [
  {
    id: 1,
    title: 'CORETECH - Mua sắm công nghệ đỉnh cao',
    subtitle: 'Khuyến mãi mỗi ngày - trả góp 0% - freeship toàn quốc',
    description: 'Tìm ngay sản phẩm ưng ý với ưu đãi hấp dẫn',
    image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80',
    cta: 'Xem ưu đãi ngay',
    ctaLink: '/',
  },
  {
    id: 2,
    title: 'Siêu sale cuối tuần',
    subtitle: 'Giảm đến 70% cho laptop gaming',
    description: 'Gaming gear chất lượng cao với giá không thể tốt hơn',
    image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80',
    cta: 'Mua ngay',
    ctaLink: '/products?category=Máy tính',
  },
  {
    id: 3,
    title: 'Smartphone flagship',
    subtitle: 'iPhone 15 Pro - Camera đỉnh cao',
    description: 'Trải nghiệm nhiếp ảnh chuyên nghiệp với AI',
    image: 'https://images.unsplash.com/photo-1542751110-97427bbecf20?auto=format&fit=crop&w=1200&q=80',
    cta: 'Khám phá ngay',
    ctaLink: '/products?category=Điện thoại',
  },
];

const categories = [
  { name: 'Máy tính', icon: '💻', count: '25+', image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=300&q=60' },
  { name: 'Điện thoại', icon: '📱', count: '15+', image: 'https://images.unsplash.com/photo-1542751110-97427bbecf20?auto=format&fit=crop&w=300&q=60' },
  { name: 'Tai nghe', icon: '🎧', count: '20+', image: 'https://images.unsplash.com/photo-1580894894518-5d728c78d432?auto=format&fit=crop&w=300&q=60' },
  { name: 'Phụ kiện', icon: '🖱️', count: '30+', image: 'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=300&q=60' },
  { name: 'Màn hình', icon: '🖥️', count: '12+', image: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=300&q=60' },
  { name: 'Loa', icon: '🔊', count: '18+', image: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=300&q=60' },
];

const promotions = [
  {
    title: 'Flash sale mỗi ngày',
    description: 'Ưu đãi đến 50% cho sản phẩm chọn lọc',
    emoji: '🔥',
    bgColor: 'bg-red-50',
    textColor: 'text-red-600',
  },
  {
    title: 'Freeship toàn quốc',
    description: 'Miễn phí giao hàng cho đơn trên 1.000.000₫',
    emoji: '🚚',
    bgColor: 'bg-blue-50',
    textColor: 'text-blue-600',
  },
  {
    title: 'Trả góp 0%',
    description: 'Duyệt nhanh trong 1 phút',
    emoji: '💳',
    bgColor: 'bg-green-50',
    textColor: 'text-green-600',
  },
  {
    title: 'Quà tặng kèm',
    description: 'Tặng voucher đến 500.000₫',
    emoji: '🎁',
    bgColor: 'bg-purple-50',
    textColor: 'text-purple-600',
  },
];

const sideBanners = {
  left: {
    title: 'Siêu Sale Laptop',
    subtitle: 'Đồng giá ưu đãi mỗi ngày',
    cta: 'Xem deal ngay',
    link: '/products?category=Máy tính',
    image: 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=1000&q=80',
    accent: 'from-rose-700/80 via-orange-600/75 to-amber-500/70'
  },
  right: {
    title: 'Phụ Kiện Chính Hãng',
    subtitle: 'Giảm sâu tai nghe, loa, gear',
    cta: 'Mua ngay',
    link: '/products?category=Phụ kiện',
    image: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=1000&q=80',
    accent: 'from-indigo-800/80 via-blue-700/75 to-cyan-600/70'
  }
};

export default function Home() {
  const [products, setProducts] = useState([]);
  const [showAllProducts, setShowAllProducts] = useState(false);
  const [hotProducts, setHotProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [filterBrand, setFilterBrand] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [minRating, setMinRating] = useState(0);
  const [sortBy, setSortBy] = useState('-createdAt');
  const [productCategories, setProductCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [currentSlide, setCurrentSlide] = useState(0);
  const { user } = useAuth();

  useEffect(() => {
    fetchCategories();
    fetchBrands();
    fetchHotProducts();
  }, []);

  // Auto-slide carousel
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % heroSlides.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProducts(searchQuery, filterCategory);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery, filterCategory, filterBrand, minPrice, maxPrice, minRating, sortBy]);

  useEffect(() => {
    setShowAllProducts(false);
  }, [searchQuery, filterCategory, filterBrand, minPrice, maxPrice, minRating, sortBy]);

  const fetchCategories = async () => {
    try {
      const response = await api.get('/products/categories');
      setProductCategories(response.data || []);
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

  const fetchHotProducts = async () => {
    try {
      const response = await api.get('/products?hot=true&limit=8');
      setHotProducts(response.data.data || []);
    } catch (err) {
      console.error('Error fetching hot products:', err);
    }
  };

  const fetchProducts = async (query = '', category = '') => {
    try {
      setLoading(true);

      const params = new URLSearchParams();
      params.set('limit', '10000');
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

  const visibleProducts = showAllProducts ? products : products.slice(0, 12);

  return (
    <div className="relative min-h-screen bg-light overflow-hidden">
      <div className="hidden 2xl:block pointer-events-none absolute inset-0 z-0">
        <div className="absolute left-0 top-0 bottom-0 w-56 border-r border-white/30">
          <img src={sideBanners.left.image} alt={sideBanners.left.title} className="w-full h-full object-cover" />
          <div className={`absolute inset-0 bg-gradient-to-b ${sideBanners.left.accent}`} />
          <div className="absolute inset-0 bg-black/15" />
          <div className="absolute left-4 right-4 top-28">
            <p className="text-white/85 text-xs uppercase tracking-wider">Khuyến mãi nổi bật</p>
            <h3 className="text-white text-2xl font-bold leading-tight mt-2">{sideBanners.left.title}</h3>
            <p className="text-white/90 text-sm mt-2">{sideBanners.left.subtitle}</p>
          </div>
          <Link
            to={sideBanners.left.link}
            className="pointer-events-auto absolute left-4 right-4 bottom-10 text-center text-white font-semibold bg-white/20 hover:bg-white/30 backdrop-blur rounded-xl px-4 py-3 transition"
          >
            {sideBanners.left.cta}
          </Link>
        </div>

        <div className="absolute right-0 top-0 bottom-0 w-56 border-l border-white/30">
          <img src={sideBanners.right.image} alt={sideBanners.right.title} className="w-full h-full object-cover" />
          <div className={`absolute inset-0 bg-gradient-to-b ${sideBanners.right.accent}`} />
          <div className="absolute inset-0 bg-black/20" />
          <div className="absolute left-4 right-4 top-28">
            <p className="text-white/85 text-xs uppercase tracking-wider">Ưu đãi công nghệ</p>
            <h3 className="text-white text-2xl font-bold leading-tight mt-2">{sideBanners.right.title}</h3>
            <p className="text-white/90 text-sm mt-2">{sideBanners.right.subtitle}</p>
          </div>
          <Link
            to={sideBanners.right.link}
            className="pointer-events-auto absolute left-4 right-4 bottom-10 text-center text-white font-semibold bg-white/20 hover:bg-white/30 backdrop-blur rounded-xl px-4 py-3 transition"
          >
            {sideBanners.right.cta}
          </Link>
        </div>
      </div>

      {/* Hero Carousel */}
      <div className="relative h-96 md:h-[500px] overflow-hidden z-10">
        {heroSlides.map((slide, index) => (
          <div
            key={slide.id}
            className={`absolute inset-0 transition-opacity duration-1000 ${
              index === currentSlide ? 'opacity-100' : 'opacity-0'
            }`}
          >
            <div className="absolute inset-0 bg-black/40 z-10" />
            <img
              src={slide.image}
              alt={slide.title}
              className="w-full h-full object-cover"
            />
            <div className="absolute inset-0 z-20 flex items-center">
              <div className="max-w-6xl mx-auto px-4 w-full">
                <div className="max-w-lg">
                  <h1 className="text-3xl md:text-5xl font-bold text-white mb-4 leading-tight">
                    {slide.title}
                  </h1>
                  <p className="text-lg md:text-xl text-white/90 mb-2">
                    {slide.subtitle}
                  </p>
                  <p className="text-sm md:text-base text-white/80 mb-6">
                    {slide.description}
                  </p>
                  <Link
                    to={slide.ctaLink}
                    className="inline-block bg-primary hover:bg-primary-dark text-white px-8 py-3 rounded-lg font-semibold transition-colors"
                  >
                    {slide.cta}
                  </Link>
                </div>
              </div>
            </div>
          </div>
        ))}

        {/* Carousel Indicators */}
        <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 z-30 flex space-x-2">
          {heroSlides.map((_, index) => (
            <button
              key={index}
              onClick={() => setCurrentSlide(index)}
              className={`w-3 h-3 rounded-full transition-colors ${
                index === currentSlide ? 'bg-white' : 'bg-white/50'
              }`}
            />
          ))}
        </div>

        {/* Carousel Navigation */}
        <button
          onClick={() => setCurrentSlide((prev) => (prev - 1 + heroSlides.length) % heroSlides.length)}
          className="absolute left-4 top-1/2 transform -translate-y-1/2 z-30 bg-white/20 hover:bg-white/30 text-white p-2 rounded-full transition-colors"
        >
          ‹
        </button>
        <button
          onClick={() => setCurrentSlide((prev) => (prev + 1) % heroSlides.length)}
          className="absolute right-4 top-1/2 transform -translate-y-1/2 z-30 bg-white/20 hover:bg-white/30 text-white p-2 rounded-full transition-colors"
        >
          ›
        </button>
      </div>

      {/* Categories Section */}
      <div className="max-w-6xl mx-auto px-4 py-12">
        <h2 className="text-2xl font-bold text-dark mb-8 text-center">Danh mục nổi bật</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          {categories.map((category) => (
            <Link
              key={category.name}
              to={`/products?category=${category.name}`}
              className="group"
            >
              <div className="bg-white rounded-xl shadow-card p-4 hover:shadow-lg transition-shadow text-center">
                <div className="w-16 h-16 mx-auto mb-3 rounded-full bg-gradient-to-br from-primary/10 to-primary/20 flex items-center justify-center group-hover:scale-110 transition-transform">
                  <span className="text-2xl">{category.icon}</span>
                </div>
                <h3 className="font-semibold text-dark mb-1 group-hover:text-primary transition-colors">
                  {category.name}
                </h3>
                <p className="text-sm text-gray-500">{category.count} sản phẩm</p>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* Hot Products Section */}
      {hotProducts.length > 0 && (
        <div className="bg-gray-50 py-12">
          <div className="max-w-6xl mx-auto px-4">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-2xl font-bold text-dark">Sản phẩm hot 🔥</h2>
              <Link
                to="/products?hot=true"
                className="text-primary hover:text-primary-dark font-semibold flex items-center gap-2"
              >
                Xem tất cả
                <span>→</span>
              </Link>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {hotProducts.map((product) => {
                const discount = getDiscountPercent(product);

                return (
                  <Link key={product._id} to={`/products/${product._id}`}>
                    <div className="bg-white rounded-xl shadow-card hover:shadow-lg cursor-pointer relative overflow-hidden group">
                      {discount > 0 && (
                        <span className="badge badge-discount absolute right-4 top-4 z-10">
                          -{discount}%
                        </span>
                      )}
                      <div className="relative overflow-hidden">
                        <img
                          src={product.image || product.images?.[0] || 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&q=60'}
                          alt={product.name}
                          className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
                        />
                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors" />
                      </div>
                      <div className="p-4">
                        <h3 className="font-semibold text-dark mb-2 line-clamp-2 group-hover:text-primary transition-colors">
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
                        {product.shopId && (
                          <p className="text-xs text-gray-600 mt-2">Shop: {product.shopId.name}</p>
                        )}
                      </div>
                    </div>
                  </Link>
                );
              })}
            </div>
          </div>
        </div>
      )}

      <div className="max-w-6xl mx-auto px-4 py-10">
        <h2 className="text-2xl font-bold text-dark mb-4">Khuyến mãi hot</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {promotions.map((promo) => (
            <div key={promo.title} className={`promo-card ${promo.bgColor} border-0 hover:shadow-lg transition-shadow`}>
              <div className="mb-3 text-3xl">{promo.emoji}</div>
              <h3 className={`text-lg font-semibold ${promo.textColor} mb-1`}>{promo.title}</h3>
              <p className="text-sm text-gray-600">{promo.description}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 pb-16">
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
                  {productCategories.map((cat) => (
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
              <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {visibleProducts.map((product) => {
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
                        {product.shopId && (
                          <p className="text-xs text-gray-600">Shop: {product.shopId.name}</p>
                        )}
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

              {!showAllProducts && products.length > 12 && (
                <div className="mt-8 flex justify-center">
                  <button
                    type="button"
                    onClick={() => setShowAllProducts(true)}
                    className="px-6 py-3 rounded-lg bg-primary text-white font-semibold hover:bg-primary/90 transition"
                  >
                    Xem thêm sản phẩm
                  </button>
                </div>
              )}
              </>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}

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
  const [categories, setCategories] = useState([]);
  const { user } = useAuth();

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProducts(searchQuery, filterCategory);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery, filterCategory]);

  const fetchCategories = async () => {
    try {
      const response = await api.get('/products/categories');
      setCategories(response.data || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const fetchProducts = async (query = '', category = '') => {
    try {
      setLoading(true);
      let qs = '/products?limit=20&page=1';
      if (query) {
        qs += `&q=${encodeURIComponent(query)}`;
      }
      if (category) {
        qs += `&category=${encodeURIComponent(category)}`;
      }
      const response = await api.get(qs);
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
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">
          <h2 className="text-2xl font-bold text-dark">Sản phẩm nổi bật</h2>

          <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm (máy tính, điện thoại, tai nghe...)"
              className="input-field"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        {/* Category Filter */}
        <div className="mb-8">
          <p className="text-sm font-semibold text-gray-600 mb-3">Lọc theo danh mục:</p>
          <div className="flex flex-wrap gap-2">
            <button
              className={`px-4 py-2 rounded text-sm font-medium transition-all ${
                filterCategory === '' ? 'bg-primary text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
              onClick={() => setFilterCategory('')}
            >
              Tất cả
            </button>
            {categories.map((cat) => (
              <button
                key={cat}
                className={`px-4 py-2 rounded text-sm font-medium transition-all ${
                  filterCategory === cat ? 'bg-primary text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
                onClick={() => setFilterCategory(cat)}
              >
                {cat}
              </button>
            ))}
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
                    {(product.image || product.images?.[0]) && (
                      <img
                        src={product.image || product.images?.[0]}
                        alt={product.name}
                        className="w-full h-48 object-cover rounded mb-4"
                      />
                    )}
                    <h3 className="font-semibold text-dark mb-2 line-clamp-2">
                      {product.name}
                    </h3>
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
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

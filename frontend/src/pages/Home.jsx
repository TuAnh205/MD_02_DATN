import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

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

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-primary to-secondary text-white py-12 px-4">
        <div className="max-w-6xl mx-auto">
          <h1 className="text-4xl font-bold mb-4">
            Chào mừng {user?.name ? `, ${user.name}` : ''}!
          </h1>
          <p className="text-lg opacity-90">
            Khám phá những sản phẩm chất lượng hàng đầu
          </p>
        </div>
      </div>

      {/* Products Section */}
      <div className="max-w-6xl mx-auto px-4 py-12">
        <h2 className="text-2xl font-bold text-dark mb-4">Sản Phẩm Nổi Bật</h2>

        {/* Search Input */}
        <div className="mb-6">
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm (máy tính, điện thoại, tai nghe...)"
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        {/* Category Filter */}
        <div className="mb-6">
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
            {products.map((product) => (
              <Link key={product._id} to={`/products/${product._id}`}>
                <div className="card hover:shadow-lg cursor-pointer">
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
                  <p className="text-primary font-bold text-lg">
                    ${product.price?.toLocaleString()}
                  </p>
                  <p className="text-sm text-gray-500 mt-2">{product.category}</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

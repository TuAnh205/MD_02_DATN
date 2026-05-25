import React, { useState, useEffect } from 'react';
import api from '../services/api';

export default function AdminProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchProducts();
  }, [page]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/products?page=${page}&limit=10`);
      setProducts(response.data.products);
      setTotalPages(response.data.pagination.pages);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
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
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Danh sách Sản phẩm</h1>
          <p className="text-slate-600 mt-2">Xem tất cả sản phẩm từ các shop đã đăng ký ({products.length})</p>
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-2xl shadow-sm border-2 border-slate-200 overflow-hidden hover:shadow-md transition-shadow">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-gradient-to-r from-slate-100 to-slate-50 border-b-2 border-blue-600">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Sản phẩm
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Danh mục
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Người tạo
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Giá
                </th>
                <th className="px-6 py-4 text-left text-xs font-bold text-slate-800 uppercase tracking-widest">
                  Tồn kho
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {products.map((product) => (
                <tr key={product._id} className="hover:bg-blue-50 transition-colors duration-150">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center space-x-4">
                      <div className="w-14 h-14 bg-gradient-to-br from-blue-100 to-slate-100 rounded-xl flex items-center justify-center shadow-md flex-shrink-0 overflow-hidden border-2 border-blue-200">
                        {product.images && product.images[0] ? (
                          <img
                            src={product.images[0]}
                            alt={product.name}
                            className="w-14 h-14 object-cover"
                          />
                        ) : (
                          <span className="text-slate-400 text-xl">📦</span>
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="text-sm font-bold text-slate-900 truncate">{product.name}</div>
                        <div className="text-xs text-slate-500 truncate max-w-xs">
                          {product.description?.substring(0, 40)}...
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex px-3 py-1 text-xs font-bold bg-gradient-to-r from-blue-100 to-blue-50 text-blue-800 rounded-lg border border-blue-200">
                      {product.category || 'N/A'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-slate-900">
                      {product.createdBy?.name || 'N/A'}
                    </div>
                    <div className="text-xs text-slate-500">
                      {product.createdBy?.email || ''}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-bold text-blue-600 text-lg">
                      {product.price?.toLocaleString()}₫
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-3 py-1 text-xs font-bold rounded-lg ${
                      product.stock > 10
                        ? 'bg-emerald-200 text-emerald-900'
                        : product.stock > 0
                        ? 'bg-amber-200 text-amber-900'
                        : 'bg-red-200 text-red-900'
                    }`}>
                      {product.stock} sản phẩm
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {products.length === 0 && (
          <div className="text-center py-16">
            <div className="text-slate-300 text-7xl mb-4">📦</div>
            <h3 className="text-xl font-bold text-slate-900 mb-2">Chưa có sản phẩm nào</h3>
            <p className="text-slate-600">Các sản phẩm từ shop đã đăng ký sẽ hiển thị ở đây</p>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center bg-white rounded-2xl shadow-sm border-2 border-slate-200 p-6">
          <button
            onClick={() => setPage(page - 1)}
            disabled={page === 1}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            ← Trước
          </button>
          <span className="text-sm font-bold text-slate-800">
            Trang <span className="text-blue-600">{page}</span> / <span className="text-slate-600">{totalPages}</span>
          </span>
          <button
            onClick={() => setPage(page + 1)}
            disabled={page === totalPages}
            className="px-6 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-blue-700 rounded-lg hover:from-blue-700 hover:to-blue-800 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-200"
          >
            Sau →
          </button>
        </div>
      )}
      )}
    </div>
  );
}
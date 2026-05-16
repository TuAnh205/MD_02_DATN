import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function ShopProducts() {
  const { fetchProfile } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [policy, setPolicy] = useState(null);
  const [billingSummary, setBillingSummary] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    category: '',
    stock: '',
    image: '',
    images: [],
  });
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    fetchProducts();
    fetchPolicy();
    fetchBillingSummary();
  }, []);

  const formatCurrency = (value) => `₫${Number(value || 0).toLocaleString('vi-VN')}`;

  const formatDateTime = (value) => new Date(value).toLocaleString('vi-VN');

  const fetchPolicy = async () => {
    try {
      const response = await api.get('/shop/billing-policy');
      setPolicy(response.data);
    } catch (error) {
      console.error('Error fetching shop policy:', error);
    }
  };

  const fetchBillingSummary = async () => {
    try {
      const response = await api.get('/shop/billing-summary');
      setBillingSummary(response.data.summary || null);
    } catch (error) {
      console.error('Error fetching billing summary:', error);
    }
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await api.get('/shop/products');
      setProducts(response.data);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingProduct) {
        await api.put(`/shop/products/${editingProduct._id}`, formData);
      } else {
        await api.post('/shop/products', formData);
      }
      fetchProducts();
      fetchBillingSummary();
      resetForm();
    } catch (error) {
      console.error('Error saving product:', error);
      if (error.response?.data?.code === 'SHOP_FROZEN') {
        setBillingSummary(error.response.data.billingSummary || null);
        alert(error.response.data.message || 'Shop đang bị đóng băng bán hàng');
        return;
      }
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi lưu sản phẩm');
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      price: '',
      category: '',
      stock: '',
      image: '',
      images: [],
    });
    setImagePreview(null);
    setEditingProduct(null);
    setShowCreateForm(false);
  };

  const handleImageUrlChange = (e) => {
    const url = e.target.value;
    setFormData({ ...formData, image: url });
    setImagePreview(url);
  };

  const handleImageFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const base64 = event.target?.result;
        setFormData({ ...formData, image: base64 });
        setImagePreview(base64);
      };
      reader.readAsDataURL(file);
    }
  };

  const editProduct = (product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      stock: product.stock,
      image: product.image || '',
      images: product.images || [],
    });
    setImagePreview(product.image || null);
    setShowCreateForm(true);
  };

  const handleOpenCreateForm = () => {
    setShowCreateForm(true);
  };

  const deleteProduct = async (productId) => {
    if (!window.confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

    try {
      await api.delete(`/shop/products/${productId}`);
      fetchProducts();
      fetchBillingSummary();
    } catch (error) {
      console.error('Error deleting product:', error);
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi xóa sản phẩm');
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
      {billingSummary?.isFrozen && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 px-5 py-4">
          <p className="text-sm font-semibold text-rose-900">Tài khoản shop đang bị đóng băng bán hàng</p>
          <p className="mt-1 text-sm text-rose-800">{billingSummary.message}</p>
          <p className="mt-2 text-xs text-rose-700">
            Số dư ví hiện tại: {formatCurrency(billingSummary.walletBalance)}. Công nợ: {formatCurrency(billingSummary.outstandingAmount)}.
          </p>
        </div>
      )}

      <div className="rounded-xl border border-blue-200 bg-blue-50 px-5 py-4">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm font-semibold text-blue-900">Chính sách phí nền tảng</p>
            <p className="mt-1 text-sm text-blue-700">
              Tất cả đơn hàng thanh toán thành công từ {formatDateTime(policy?.feeStartDate)} trở đi sẽ bị tính phí sàn <strong>{Math.round((policy?.commissionRate || 0.05) * 100)}%</strong> trên giá sản phẩm.
            </p>
            <p className="mt-1 text-xs text-blue-600">Phí chỉ áp dụng khi khách hàng thanh toán thành công. Hệ thống tự động trừ phí từ ví shop.</p>
          </div>
        </div>
      </div>

      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Quản lý Sản phẩm</h1>
        <button
          onClick={handleOpenCreateForm}
          disabled={billingSummary?.isFrozen}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          Thêm sản phẩm mới
        </button>
      </div>


      {/* Create/Edit Form */}
      {showCreateForm && (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-semibold mb-4">
            {editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Tên sản phẩm</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Mô tả</label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
                rows={3}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Giá</label>
                <input
                  type="number"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Tồn kho</label>
                <input
                  type="number"
                  value={formData.stock}
                  onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
                  required
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Danh mục</label>
              <input
                type="text"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              />
            </div>

            {/* Image Section */}
            <div className="border-t pt-4">
              <label className="block text-sm font-medium text-gray-700 mb-3">Hình ảnh sản phẩm</label>
              <div className="space-y-3">
                {/* Image URL Input */}
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Từ đường dẫn URL</label>
                  <input
                    type="text"
                    placeholder="Nhập đường dẫn ảnh..."
                    value={formData.image}
                    onChange={handleImageUrlChange}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 text-sm"
                  />
                </div>

                {/* File Upload Input */}
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Hoặc tải ảnh từ thiết bị</label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageFileChange}
                    className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 text-sm"
                  />
                </div>

                {/* Image Preview */}
                {imagePreview && (
                  <div className="mt-4 border rounded-md p-4 bg-gray-50">
                    <p className="text-xs font-medium text-gray-600 mb-2">Xem trước ảnh:</p>
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="max-h-48 max-w-full rounded-md object-cover"
                      onError={() => {
                        setImagePreview(null);
                        setFormData({ ...formData, image: '' });
                        alert('Lỗi tải ảnh. Vui lòng kiểm tra đường dẫn hoặc thử file khác.');
                      }}
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="flex space-x-4">
              <button
                type="submit"
                className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
              >
                {editingProduct ? 'Cập nhật' : 'Thêm'}
              </button>
              <button
                type="button"
                onClick={resetForm}
                className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700"
              >
                Hủy
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Products List */}
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        <ul className="divide-y divide-gray-200">
          {products.map((product) => (
            <li key={product._id} className="px-6 py-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="flex-shrink-0 h-12 w-12">
                    <img
                      className="h-12 w-12 rounded-lg object-cover"
                      src={product.image || 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60'}
                      alt={product.name}
                    />
                  </div>
                  <div className="ml-4">
                    <div className="text-sm font-medium text-gray-900">{product.name}</div>
                    <div className="text-sm text-gray-500">
                      {product.price.toLocaleString('vi-VN')} VND • Tồn kho: {product.stock}
                    </div>
                    <div className="mt-1 text-xs">
                      <span className="rounded-full bg-blue-50 px-2 py-1 font-medium text-blue-700">
                        Các đơn phát sinh từ {formatDateTime(policy?.feeStartDate)} sẽ bị tính phí sàn 5%
                      </span>
                    </div>
                  </div>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => editProduct(product)}
                    className="bg-yellow-500 text-white px-3 py-1 rounded text-sm hover:bg-yellow-600"
                  >
                    Sửa
                  </button>
                  <button
                    onClick={() => deleteProduct(product._id)}
                    className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                  >
                    Xóa
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
        {products.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            Chưa có sản phẩm nào
          </div>
        )}
      </div>
    </div>
  );
}
import api from './api';

export const cartService = {
  getCart: async () => {
    try {
      const response = await api.get('/cart');
      return response.data;
    } catch (err) {
      console.error('Error getting cart:', err);
      throw err;
    }
  },

  addToCart: async (productId, qty = 1, variant = null) => {
    try {
      const response = await api.post('/cart', {
        productId,
        qty,
        variant
      });
      return response.data;
    } catch (err) {
      console.error('Error adding to cart:', err);
      throw err;
    }
  },

  updateCartItem: async (itemId, qty) => {
    try {
      const response = await api.put(`/cart/${itemId}`, { qty });
      return response.data;
    } catch (err) {
      console.error('Error updating cart item:', err);
      throw err;
    }
  },

  removeFromCart: async (itemId) => {
    try {
      const response = await api.delete(`/cart/${itemId}`);
      return response.data;
    } catch (err) {
      console.error('Error removing from cart:', err);
      throw err;
    }
  },

  clearCart: async () => {
    try {
      const response = await api.post('/cart/clear');
      return response.data;
    } catch (err) {
      console.error('Error clearing cart:', err);
      throw err;
    }
  },

  applyVoucher: async (code) => {
    try {
      const response = await api.post('/cart/apply-voucher', { code });
      return response.data;
    } catch (err) {
      console.error('Error applying voucher:', err);
      throw err;
    }
  },

  removeVoucher: async () => {
    try {
      const response = await api.post('/cart/remove-voucher');
      return response.data;
    } catch (err) {
      console.error('Error removing voucher:', err);
      throw err;
    }
  }
};

import api from './api';

export const promotionService = {
  // Apply discount code
  applyDiscountCode: async (code, cartItems, userId) => {
    const response = await api.post('/promotions/apply', {
      code,
      cartItems,
      userId
    });
    return response.data;
  },

  // Get all promotions (admin only)
  getPromotions: async (params = {}) => {
    const response = await api.get('/promotions', { params });
    return response.data;
  },

  // Create promotion (admin only)
  createPromotion: async (promotionData) => {
    const response = await api.post('/promotions', promotionData);
    return response.data;
  },

  // Update promotion (admin only)
  updatePromotion: async (id, promotionData) => {
    const response = await api.put(`/promotions/${id}`, promotionData);
    return response.data;
  },

  // Delete promotion (admin only)
  deletePromotion: async (id) => {
    const response = await api.delete(`/promotions/${id}`);
    return response.data;
  }
};
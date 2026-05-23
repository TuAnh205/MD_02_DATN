import api from './api';

export const voucherService = {
  getAvailableVouchers: async () => {
    try {
      const response = await api.get('/vouchers');
      return response.data;
    } catch (err) {
      console.error('Error getting available vouchers:', err);
      throw err;
    }
  },

  getMyVouchers: async () => {
    try {
      const response = await api.get('/vouchers/my');
      return response.data;
    } catch (err) {
      console.error('Error getting my vouchers:', err);
      throw err;
    }
  },

  claimVoucher: async (code) => {
    try {
      const response = await api.post('/vouchers/claim', { code });
      return response.data;
    } catch (err) {
      console.error('Error claiming voucher:', err);
      throw err;
    }
  },

  // Shop voucher methods
  getShopVouchers: async (shopId) => {
    try {
      const response = await api.get(`/vouchers/${shopId}/shop`);
      return response.data;
    } catch (err) {
      console.error('Error getting shop vouchers:', err);
      throw err;
    }
  },

  createShopVoucher: async (voucherData) => {
    try {
      const response = await api.post('/vouchers/shop', voucherData);
      return response.data;
    } catch (err) {
      console.error('Error creating shop voucher:', err);
      throw err;
    }
  },

  getMyCreatedVouchers: async () => {
    try {
      const response = await api.get('/vouchers/shop/my');
      return response.data;
    } catch (err) {
      console.error('Error getting my created vouchers:', err);
      throw err;
    }
  },

  updateShopVoucher: async (voucherId, updateData) => {
    try {
      const response = await api.put(`/vouchers/shop/${voucherId}`, updateData);
      return response.data;
    } catch (err) {
      console.error('Error updating shop voucher:', err);
      throw err;
    }
  },

  deleteShopVoucher: async (voucherId) => {
    try {
      const response = await api.delete(`/vouchers/shop/${voucherId}`);
      return response.data;
    } catch (err) {
      console.error('Error deleting shop voucher:', err);
      throw err;
    }
  }
};

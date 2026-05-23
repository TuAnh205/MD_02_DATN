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
  }
};

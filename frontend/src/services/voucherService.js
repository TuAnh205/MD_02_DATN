import api from './api';

export const voucherService = {
  getAvailableVouchers: async () => {
    const response = await api.get('/vouchers');
    return response.data;
  },

  getMyVouchers: async () => {
    const response = await api.get('/vouchers/my');
    return response.data;
  },

  claimVoucher: async (code) => {
    const response = await api.post('/vouchers/claim', { code });
    return response.data;
  }
};

import api from './api';

export const orderService = {
  createOrder: async (orderData) => {
    try {
      const response = await api.post('/orders', orderData);
      return response.data;
    } catch (err) {
      console.error('Error creating order:', err);
      throw err;
    }
  },

  getOrders: async () => {
    try {
      const response = await api.get('/orders');
      return response.data;
    } catch (err) {
      console.error('Error getting orders:', err);
      throw err;
    }
  },

  getOrderById: async (orderId) => {
    try {
      const response = await api.get(`/orders/${orderId}`);
      return response.data;
    } catch (err) {
      console.error('Error getting order:', err);
      throw err;
    }
  },

  markPaid: async (orderId) => {
    try {
      const response = await api.patch(`/orders/${orderId}/mark-paid`);
      return response.data;
    } catch (err) {
      console.error('Error marking order as paid:', err);
      throw err;
    }
  },

  processCardPayment: async (orderId, cardData) => {
    try {
      const response = await api.post(`/orders/${orderId}/process-payment`, {
        method: 'card',
        cardData
      });
      return response.data;
    } catch (err) {
      console.error('Error processing card payment:', err);
      throw err;
    }
  },

  cancelOrder: async (orderId, reason = '') => {
    try {
      const response = await api.patch(`/orders/${orderId}/cancel`, { reason });
      return response.data;
    } catch (err) {
      console.error('Error cancelling order:', err);
      throw err;
    }
  }
};

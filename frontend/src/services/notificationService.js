import api from './api';

const notificationService = {
  getNotifications: async () => {
    const response = await api.get('/shop/notifications');
    return response.data;
  },

  markRead: async (id) => {
    const response = await api.put(`/shop/notifications/${id}/read`);
    return response.data;
  },

  markAllRead: async () => {
    const response = await api.put('/shop/notifications/read-all');
    return response.data;
  }
};

export default notificationService;
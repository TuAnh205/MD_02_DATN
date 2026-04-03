import api from './api';

export const favoriteService = {
  addFavorite: async (productId) => {
    const res = await api.post('/favorites', { productId });
    return res.data;
  },
  removeFavorite: async (productId) => {
    const res = await api.delete(`/favorites/${productId}`);
    return res.data;
  },
  listFavorites: async () => {
    const res = await api.get('/favorites');
    return res.data;
  }
};

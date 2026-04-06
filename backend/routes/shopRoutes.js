const express = require('express');
const router = express.Router();
const { auth, shopAuth } = require('../middleware/auth');
const shopController = require('../controllers/shopController');

// Apply auth and shopAuth to all routes
router.use(auth);
router.use(shopAuth);

// Product management routes
router.get('/products', shopController.getShopProducts);
router.post('/products', shopController.createProduct);
router.put('/products/:id', shopController.updateProduct);
router.delete('/products/:id', shopController.deleteProduct);

// Order management routes
router.get('/orders', shopController.getShopOrders);
router.put('/orders/:id/status', shopController.updateShopOrderStatus);

// Review management routes
router.get('/reviews', shopController.getShopReviews);
router.put('/reviews/:id/reply', shopController.replyShopReview);

// Notification routes
router.get('/notifications', shopController.getShopNotifications);
router.put('/notifications/:id/read', shopController.markNotificationRead);
router.put('/notifications/read-all', shopController.markAllNotificationsRead);

// Revenue analytics routes
router.get('/revenue', shopController.getShopRevenue);

module.exports = router;
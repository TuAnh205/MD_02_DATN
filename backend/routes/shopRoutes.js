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

// Review management routes
router.get('/reviews', shopController.getShopReviews);
router.put('/reviews/:id/reply', shopController.replyShopReview);

// Revenue analytics routes
router.get('/revenue', shopController.getShopRevenue);

module.exports = router;
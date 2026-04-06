const express = require('express');
const router = express.Router();
const { auth, adminAuth } = require('../middleware/auth');
const adminController = require('../controllers/adminController');

// Apply auth and adminAuth to all routes
router.use(auth);
router.use(adminAuth);

// User management routes
router.get('/users', adminController.getUsers);
router.get('/users/count', adminController.getUserCount);
router.put('/users/:id/role', adminController.updateUserRole);
router.delete('/users/:id', adminController.deleteUser);

// Shop management routes
router.get('/shops', adminController.getShops);
router.get('/shops/count', adminController.getShopCount);
router.put('/shops/:id/status', adminController.updateShopStatus);

// Product management routes (view only)
router.get('/products', adminController.getProducts);
router.get('/products/count', adminController.getProductCount);

// Order management routes
router.get('/orders', adminController.getOrders);
router.get('/orders/count', adminController.getOrderCount);
router.put('/orders/:id/status', adminController.updateOrderStatus);
router.patch('/orders/:id/status', adminController.updateOrderStatus);

// Review management routes
router.get('/reviews', adminController.getReviews);
router.get('/reviews/count', adminController.getReviewCount);
router.put('/reviews/:id/reply', adminController.replyReview);
router.delete('/reviews/:id', adminController.deleteReview);

// Feedback management routes
router.get('/feedbacks', adminController.getFeedbacks);
router.delete('/feedbacks/:id', adminController.deleteFeedback);

// Post management routes
router.get('/posts', adminController.getPosts);
router.post('/posts', adminController.createPost);
router.put('/posts/:id', adminController.updatePost);
router.delete('/posts/:id', adminController.deletePost);

// Voucher management routes
router.get('/vouchers', adminController.getVouchers);
router.post('/vouchers', adminController.createVoucher);
router.put('/vouchers/:id', adminController.updateVoucher);
router.delete('/vouchers/:id', adminController.deleteVoucher);

// Revenue analytics routes
router.get('/revenue', adminController.getRevenue);
router.get('/revenue/shops', adminController.getRevenueByShop);

module.exports = router;
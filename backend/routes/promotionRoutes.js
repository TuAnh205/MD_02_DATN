const express = require('express');
const router = express.Router();
const promotionController = require('../controllers/promotionController');
const auth = require('../middleware/auth');
const { adminAuth } = require('../middleware/auth');

// Public routes
router.post('/apply', promotionController.applyDiscountCode);

// Admin routes
router.get('/', auth, adminAuth, promotionController.getPromotions);
router.get('/:id', auth, adminAuth, promotionController.getPromotionById);
router.post('/', auth, adminAuth, promotionController.createPromotion);
router.put('/:id', auth, adminAuth, promotionController.updatePromotion);
router.delete('/:id', auth, adminAuth, promotionController.deletePromotion);

module.exports = router;
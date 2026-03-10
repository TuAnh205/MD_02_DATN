const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/cartController');
const auth = require('../middleware/auth');

// Protect all cart routes with auth middleware
router.use(auth);

router.get('/', ctrl.getCart);
router.post('/', ctrl.addToCart);
router.put('/:itemId', ctrl.updateCartItem);
router.delete('/:itemId', ctrl.removeFromCart);
router.post('/clear', ctrl.clearCart);
router.post('/apply-voucher', ctrl.applyVoucher);

module.exports = router;

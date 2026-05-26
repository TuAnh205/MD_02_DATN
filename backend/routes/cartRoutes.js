const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/cartController');
const { auth, checkAccountLocked } = require('../middleware/auth');

// Protect all cart routes with auth middleware and account lock check
router.use(auth);
router.use(checkAccountLocked);

router.get('/', ctrl.getCart);
router.post('/', ctrl.addToCart);
router.put('/:itemId', ctrl.updateCartItem);
router.delete('/:itemId', ctrl.removeFromCart);
router.post('/clear', ctrl.clearCart);
router.post('/apply-voucher', ctrl.applyVoucher);
router.post('/remove-voucher', ctrl.removeVoucher);

module.exports = router;

const express = require('express');
const router = express.Router();
const voucherController = require('../controllers/voucherController');
const { auth } = require('../middleware/auth');

// Authenticated user routes (must be before wildcard routes)
router.get('/my', auth, voucherController.getMyVouchers);
router.post('/claim', auth, voucherController.claimVoucher);

// Shop routes (must be before wildcard routes)
router.post('/shop', auth, voucherController.createShopVoucher);
router.get('/shop/my', auth, voucherController.getMyCreatedVouchers);
router.put('/shop/:voucherId', auth, voucherController.updateShopVoucher);
router.delete('/shop/:voucherId', auth, voucherController.deleteShopVoucher);

// Public routes (wildcard should be last)
router.get('/', voucherController.getActiveVouchers);
router.get('/:shopId/shop', voucherController.getShopVouchers);

module.exports = router;

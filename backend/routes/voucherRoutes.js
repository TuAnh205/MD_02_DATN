const express = require('express');
const router = express.Router();
const voucherController = require('../controllers/voucherController');
const { auth, checkAccountLocked } = require('../middleware/auth');

// Authenticated user routes (must be before wildcard routes)
router.get('/my', auth, voucherController.getMyVouchers);
router.post('/claim', auth, checkAccountLocked, voucherController.claimVoucher);

// Shop routes (must be before wildcard routes)
router.post('/shop', auth, checkAccountLocked, voucherController.createShopVoucher);
router.get('/shop/my', auth, voucherController.getMyCreatedVouchers);
router.put('/shop/:voucherId', auth, checkAccountLocked, voucherController.updateShopVoucher);
router.delete('/shop/:voucherId', auth, checkAccountLocked, voucherController.deleteShopVoucher);

// Public routes (wildcard should be last)
router.get('/', voucherController.getActiveVouchers);
router.get('/:shopId/shop', voucherController.getShopVouchers);

module.exports = router;

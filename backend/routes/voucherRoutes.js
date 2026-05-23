const express = require('express');
const router = express.Router();
const voucherController = require('../controllers/voucherController');
const { auth } = require('../middleware/auth');

// Public route: Get all active vouchers for users
router.get('/', voucherController.getActiveVouchers);
router.get('/my', auth, voucherController.getMyVouchers);
router.post('/claim', auth, voucherController.claimVoucher);

module.exports = router;

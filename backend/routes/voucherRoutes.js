const express = require('express');
const router = express.Router();
const voucherController = require('../controllers/voucherController');

// Public route: Get all active vouchers for users
router.get('/', voucherController.getActiveVouchers);

module.exports = router;

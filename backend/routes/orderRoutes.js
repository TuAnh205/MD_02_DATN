const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/orderController');
const auth = require('../middleware/auth');

router.use(auth);
router.post('/', ctrl.createOrder);
router.get('/', ctrl.getOrders);
router.get('/:id', ctrl.getOrderById);
router.patch('/:id/status', ctrl.updateStatus); // admin
router.patch('/:id/mark-paid', ctrl.markPaid);

module.exports = router;

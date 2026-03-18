const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/orderController');
const auth = require('../middleware/auth');

router.use(auth);
router.post('/', ctrl.createOrder);
router.get('/', ctrl.getOrders);
router.get('/:id', ctrl.getOrderById);
router.post('/:id/process-payment', ctrl.processPayment);
router.patch('/:id/status', ctrl.updateStatus); // admin
router.patch('/:id/mark-paid', ctrl.markPaid);
// allow user to cancel their own order when not yet shipped/delivered
router.patch('/:id/cancel', ctrl.cancelOrder);

module.exports = router;

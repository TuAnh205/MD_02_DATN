const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/reviewController');
const { auth, checkAccountLocked } = require('../middleware/auth');

router.post('/', auth, checkAccountLocked, ctrl.createReview);
router.put('/:id', auth, checkAccountLocked, ctrl.updateReview);
router.delete('/:id', auth, checkAccountLocked, ctrl.deleteReview);
router.get('/product/:productId', ctrl.listByProduct);

module.exports = router;

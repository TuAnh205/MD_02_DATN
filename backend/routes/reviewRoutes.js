const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/reviewController');
const auth = require('../middleware/auth');

router.post('/', auth, ctrl.createReview);
router.put('/:id', auth, ctrl.updateReview);
router.delete('/:id', auth, ctrl.deleteReview);
router.get('/product/:productId', ctrl.listByProduct);

module.exports = router;

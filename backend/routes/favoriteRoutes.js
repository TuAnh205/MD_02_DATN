const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/favoriteController');
const { auth, checkAccountLocked } = require('../middleware/auth');

router.use(auth);
router.use(checkAccountLocked);
router.post('/', ctrl.addFavorite);
router.get('/', ctrl.listFavorites);
router.delete('/:productId', ctrl.removeFavorite);

module.exports = router;

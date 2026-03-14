const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/productController');

router.get('/', ctrl.getProducts);
router.get('/categories', ctrl.getCategories);
router.get('/brands', ctrl.getBrands);
router.get('/:id', ctrl.getProductById);
router.post('/', ctrl.createProduct);
router.put('/:id', ctrl.updateProduct);
router.delete('/:id', ctrl.deleteProduct);
router.patch('/:id/stock', ctrl.updateStock);
router.post('/bulk-delete', ctrl.bulkDelete);

module.exports = router;

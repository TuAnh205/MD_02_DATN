const express = require('express');
const router = express.Router();
const addressController = require('../controllers/addressController');
const auth = require('../middleware/auth');

router.get('/', auth, addressController.getAddresses);
router.post('/', auth, addressController.addAddress);
router.put('/:addressId', auth, addressController.updateAddress);
router.delete('/:addressId', auth, addressController.deleteAddress);

module.exports = router;

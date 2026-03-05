const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/postController');
const auth = require('../middleware/auth');

router.get('/', ctrl.listPosts);
router.get('/:id', ctrl.getPost);
router.post('/', auth, ctrl.createPost); // admin can create (auth check only)

module.exports = router;

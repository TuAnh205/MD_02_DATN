const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/feedbackController');
const auth = require('../middleware/auth');

router.post('/', auth, ctrl.createFeedback); // allow authenticated users to post feedback
router.get('/', auth, ctrl.listFeedback); // admin only

module.exports = router;

const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');
const auth = require('../middleware/auth'); // 👈 đúng

router.get('/', auth, async (req, res) => {
    try {
        const userId = req.user.id; // 👈 lấy từ token

        const notifications = await Notification.find({ user: userId })
            .sort({ createdAt: -1 });

        res.json(notifications);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

module.exports = router;
const Feedback = require('../models/Feedback');

exports.createFeedback = async (req, res) => {
    try {
        const userId = req.user ? req.user.id : null;
        const { message, contact } = req.body;
        if (!message) return res.status(400).json({ message: 'message required' });
        const fb = new Feedback({ user: userId, message, contact });
        await fb.save();
        res.status(201).json(fb);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.listFeedback = async (req, res) => {
    try {
        if (req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        const all = await Feedback.find().populate('user', 'name email');
        res.json(all);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

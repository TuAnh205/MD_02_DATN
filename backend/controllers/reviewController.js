const Review = require('../models/Review');

exports.createReview = async (req, res) => {
    try {
        const userId = req.user.id;
        const { productId, rating, comment } = req.body;
        if (!productId || !rating) return res.status(400).json({ message: 'productId and rating required' });

        const review = new Review({ user: userId, product: productId, rating, comment });
        await review.save();
        res.status(201).json(review);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'You have already reviewed this product' });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.updateReview = async (req, res) => {
    try {
        const userId = req.user.id;
        const review = await Review.findById(req.params.id);
        if (!review) return res.status(404).json({ message: 'Review not found' });
        if (review.user.toString() !== userId && req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        const { rating, comment } = req.body;
        if (rating) review.rating = rating;
        if (comment) review.comment = comment;
        await review.save();
        res.json(review);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.deleteReview = async (req, res) => {
    try {
        const userId = req.user.id;
        const review = await Review.findById(req.params.id);
        if (!review) return res.status(404).json({ message: 'Review not found' });
        if (review.user.toString() !== userId && req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        await review.remove();
        res.json({ message: 'Deleted' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.listByProduct = async (req, res) => {
    try {
        const productId = req.params.productId;
        const items = await Review.find({ product: productId }).populate('user', 'name');
        res.json(items);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

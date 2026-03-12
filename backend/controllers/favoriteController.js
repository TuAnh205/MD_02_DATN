const Favorite = require('../models/Favorite');

exports.addFavorite = async (req, res) => {
    try {
        const userId = req.user.id;
        const { productId } = req.body;
        if (!productId) return res.status(400).json({ message: 'productId required' });

        const fav = new Favorite({ user: userId, product: productId });
        await fav.save();
        res.status(201).json(fav);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Already in favorites' });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.removeFavorite = async (req, res) => {
    try {
        const userId = req.user.id;
        const { productId } = req.params;
        const removed = await Favorite.findOneAndDelete({ user: userId, product: productId });
        if (!removed) return res.status(404).json({ message: 'Favorite not found' });
        res.json({ message: 'Removed' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.listFavorites = async (req, res) => {
    try {
        const userId = req.user.id;
        const items = await Favorite.find({ user: userId }).populate('product');
        res.json(items);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

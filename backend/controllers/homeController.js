const Product = require('../models/Product');
const Banner = require('../models/Banner');

exports.getHomeData = async (req, res) => {
    try {
        // Get active banners
        const banners = await Banner.find({ isActive: true })
            .sort({ position: 1 })
            .limit(5);

        // Get featured products
        const featuredProducts = await Product.find({
            isFeatured: true
        })
        .select('name price originalPrice images ratings category')
        .sort({ createdAt: -1 })
        .limit(100);

        // Get best-selling products
        const bestSellingProducts = await Product.find({
            salesCount: { $gt: 0 }
        })
        .select('name price originalPrice images ratings category salesCount')
        .sort({ salesCount: -1 })
        .limit(100);

        // Get new products
        const newProducts = await Product.find({
            createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } // Last 30 days
        })
        .select('name price originalPrice images ratings category')
        .sort({ createdAt: -1 })
        .limit(100);

        // Get categories with product counts
        const categories = await Product.aggregate([
            { $group: { _id: '$category', count: { $sum: 1 } } },
            { $sort: { count: -1 } },
            { $limit: 10 }
        ]);

        res.json({
            banners,
            featuredProducts,
            bestSellingProducts,
            newProducts,
            categories
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getBanners = async (req, res) => {
    try {
        const banners = await Banner.find({ isActive: true })
            .sort({ position: 1 });
        res.json(banners);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getFeaturedProducts = async (req, res) => {
    try {
        const { limit = 10000, page = 1 } = req.query;
        const skip = (page - 1) * limit;

        const products = await Product.find({
            isFeatured: true
        })
        .select('name price originalPrice images ratings category brand')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const total = await Product.countDocuments({
            isFeatured: true
        });

        res.json({
            products,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total,
                pages: Math.ceil(total / limit)
            }
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getBestSellingProducts = async (req, res) => {
    try {
        const { limit = 10000, page = 1 } = req.query;
        const skip = (page - 1) * limit;

        const products = await Product.find({
            salesCount: { $gt: 0 }
        })
        .select('name price originalPrice images ratings category brand salesCount')
        .sort({ salesCount: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const total = await Product.countDocuments({
            salesCount: { $gt: 0 }
        });

        res.json({
            products,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total,
                pages: Math.ceil(total / limit)
            }
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getNewProducts = async (req, res) => {
    try {
        const { limit = 10000, page = 1 } = req.query;
        const skip = (page - 1) * limit;

        const products = await Product.find({
            createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) }
        })
        .select('name price originalPrice images ratings category brand')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const total = await Product.countDocuments({
            createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) }
        });

        res.json({
            products,
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total,
                pages: Math.ceil(total / limit)
            }
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getCategories = async (req, res) => {
    try {
        const categories = await Product.aggregate([
            {
                $group: {
                    _id: '$category',
                    count: { $sum: 1 },
                    image: { $first: '$images' }
                }
            },
            { $sort: { count: -1 } }
        ]);

        res.json(categories);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
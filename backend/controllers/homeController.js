const Product = require('../models/Product');
const Banner = require('../models/Banner');
const User = require('../models/User');

const getPublicProductFilter = async (extraFilter = {}) => {
    const frozenShops = await User.find({ role: 'shop', shopStatus: 'frozen' }).select('_id');
    if (frozenShops.length === 0) {
        return extraFilter;
    }

    return {
        ...extraFilter,
        shopId: { $nin: frozenShops.map((shop) => shop._id) },
    };
};

exports.getHomeData = async (req, res) => {
    try {
        // Get active banners
        const banners = await Banner.find({ isActive: true })
            .sort({ position: 1 })
            .limit(5);

        // Get featured products
        const featuredProducts = await Product.find(await getPublicProductFilter({
            isFeatured: true
        }))
        .select('name price originalPrice images ratings category')
        .sort({ createdAt: -1 })
        .limit(100);

        // Get best-selling products
        const bestSellingProducts = await Product.find(await getPublicProductFilter({
            salesCount: { $gt: 0 }
        }))
        .select('name price originalPrice images ratings category salesCount')
        .sort({ salesCount: -1 })
        .limit(100);

        // Get new products
        const newProducts = await Product.find(await getPublicProductFilter({
            createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) } // Last 30 days
        }))
        .select('name price originalPrice images ratings category')
        .sort({ createdAt: -1 })
        .limit(100);

        // Get categories with product counts
        const frozenShops = await User.find({ role: 'shop', shopStatus: 'frozen' }).select('_id');
        const categoryMatch = frozenShops.length > 0
            ? [{ $match: { shopId: { $nin: frozenShops.map((shop) => shop._id) } } }]
            : [];
        const categories = await Product.aggregate([
            ...categoryMatch,
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

        const products = await Product.find(await getPublicProductFilter({
            isFeatured: true
        }))
        .select('name price originalPrice images ratings category brand')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const totalFilter = await getPublicProductFilter({ isFeatured: true });
        const total = await Product.countDocuments(totalFilter);

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

        const products = await Product.find(await getPublicProductFilter({
            salesCount: { $gt: 0 }
        }))
        .select('name price originalPrice images ratings category brand salesCount')
        .sort({ salesCount: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const totalFilter = await getPublicProductFilter({ salesCount: { $gt: 0 } });
        const total = await Product.countDocuments(totalFilter);

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

        const newFilter = await getPublicProductFilter({
            createdAt: { $gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) }
        });
        const products = await Product.find(newFilter)
        .select('name price originalPrice images ratings category brand')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(parseInt(limit));

        const total = await Product.countDocuments(newFilter);

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
        const frozenShops = await User.find({ role: 'shop', shopStatus: 'frozen' }).select('_id');
        const matchStage = frozenShops.length > 0
            ? [{ $match: { shopId: { $nin: frozenShops.map((shop) => shop._id) } } }]
            : [];
        const categories = await Product.aggregate([
            ...matchStage,
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
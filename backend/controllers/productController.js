const Product = require('../models/Product');
const Order = require('../models/Order');

// GET /api/products
exports.getProducts = async (req, res) => {
    try {
        const {
            q,
            category,
            brand,
            minPrice,
            maxPrice,
            minRating,
            featured,
            hot,
            sort = '-createdAt',
            page = 1,
            limit = 20,
        } = req.query;

        const filter = {};
        if (q) {
            const re = new RegExp(q, 'i');
            filter.$or = [{ name: re }, { description: re }, { sku: re }];
        }
        if (category) filter.category = category;
        if (brand) filter.brand = brand;
        if (minPrice) filter.price = { ...(filter.price || {}), $gte: Number(minPrice) };
        if (maxPrice) filter.price = { ...(filter.price || {}), $lte: Number(maxPrice) };
        if (minRating) filter['ratings.average'] = { $gte: Number(minRating) };
        if (featured === 'true') filter.isFeatured = true;
        if (hot === 'true') filter.hot = true;

        const skip = (Math.max(1, Number(page)) - 1) * Number(limit);
        const [items, total] = await Promise.all([
            Product.find(filter).populate('createdBy', 'name email').sort(sort).skip(skip).limit(Number(limit)),
            Product.countDocuments(filter),
        ]);

        res.json({ data: items, meta: { total, page: Number(page), limit: Number(limit) } });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getBrands = async (req, res) => {
    try {
        const brands = await Product.distinct('brand');
        res.json(brands.filter(Boolean).sort());
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// GET /api/products/categories (get unique categories)
exports.getCategories = async (req, res) => {
    try {
        const categories = await Product.distinct('category');
        res.json(categories.sort());
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// GET /api/products/:id
exports.getProductById = async (req, res) => {
    try {
        const prod = await Product.findById(req.params.id).populate('createdBy', 'name email').populate('shopId', 'name email');
        if (!prod) return res.status(404).json({ message: 'Product not found' });
        res.json(prod);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/products
exports.createProduct = async (req, res) => {
    try {
        const { name, price } = req.body;
        if (!name || price === undefined) return res.status(400).json({ message: 'name and price are required' });

        const productData = {
            ...req.body,
            createdBy: req.user.id,
            shopId: req.user.role === 'shop' ? req.user.id : req.body.shopId // For admin, can specify shopId
        };

        const newProd = new Product(productData);
        await newProd.save();
        res.status(201).json(newProd);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Duplicate key', error: err.keyValue });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/products/:id
exports.updateProduct = async (req, res) => {
    try {
        const filter = req.user.role === 'shop' ? { _id: req.params.id, shopId: req.user.id } : { _id: req.params.id };
        const updated = await Product.findOneAndUpdate(filter, req.body, { new: true, runValidators: true });
        if (!updated) return res.status(404).json({ message: 'Product not found or not authorized' });
        res.json(updated);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Duplicate key', error: err.keyValue });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// DELETE /api/products/:id
exports.deleteProduct = async (req, res) => {
    try {
        const filter = req.user.role === 'shop' ? { _id: req.params.id, shopId: req.user.id } : { _id: req.params.id };
        const removed = await Product.findOneAndDelete(filter);
        if (!removed) return res.status(404).json({ message: 'Product not found or not authorized' });
        res.json({ message: 'Deleted', id: req.params.id });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PATCH /api/products/:id/stock  body: { stock: number }
exports.updateStock = async (req, res) => {
    try {
        const { stock } = req.body;
        if (stock === undefined) return res.status(400).json({ message: 'stock is required' });
        const updated = await Product.findByIdAndUpdate(req.params.id, { stock }, { new: true, runValidators: true });
        if (!updated) return res.status(404).json({ message: 'Product not found' });
        res.json(updated);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/products/bulk-delete  body: { ids: [id1,id2,...] }
exports.bulkDelete = async (req, res) => {
    try {
        const { ids } = req.body;
        if (!Array.isArray(ids) || ids.length === 0) return res.status(400).json({ message: 'ids array required' });
        const result = await Product.deleteMany({ _id: { $in: ids } });
        res.json({ deletedCount: result.deletedCount });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// ================= SHOP FUNCTIONS =================

// GET /api/shop/products - Get products for current shop
exports.getMyProducts = async (req, res) => {
    try {
        const {
            q,
            category,
            sort = '-createdAt',
            page = 1,
            limit = 20,
        } = req.query;

        const filter = { shopId: req.user.id };
        if (q) {
            const re = new RegExp(q, 'i');
            filter.$or = [{ name: re }, { description: re }, { sku: re }];
        }
        if (category) filter.category = category;

        const skip = (Math.max(1, Number(page)) - 1) * Number(limit);
        const [items, total] = await Promise.all([
            Product.find(filter).sort(sort).skip(skip).limit(Number(limit)),
            Product.countDocuments(filter),
        ]);

        res.json({ data: items, meta: { total, page: Number(page), limit: Number(limit) } });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST /api/shop/products - Create product for shop
exports.createMyProduct = async (req, res) => {
    try {
        const { name, price } = req.body;
        if (!name || price === undefined) return res.status(400).json({ message: 'name and price are required' });

        const productData = {
            ...req.body,
            createdBy: req.user.id,
            shopId: req.user.id
        };

        const newProd = new Product(productData);
        await newProd.save();
        res.status(201).json(newProd);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Duplicate key', error: err.keyValue });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PUT /api/shop/products/:id - Update product for shop
exports.updateMyProduct = async (req, res) => {
    try {
        const updated = await Product.findOneAndUpdate(
            { _id: req.params.id, shopId: req.user.id },
            req.body,
            { new: true, runValidators: true }
        );
        if (!updated) return res.status(404).json({ message: 'Product not found' });
        res.json(updated);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Duplicate key', error: err.keyValue });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// DELETE /api/shop/products/:id - Delete product for shop
exports.deleteMyProduct = async (req, res) => {
    try {
        const removed = await Product.findOneAndDelete({ _id: req.params.id, shopId: req.user.id });
        if (!removed) return res.status(404).json({ message: 'Product not found' });
        res.json({ message: 'Deleted', id: req.params.id });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// GET /api/shop/revenue - Get revenue stats for shop
exports.getMyRevenue = async (req, res) => {
    try {
        const { period = 'month' } = req.query; // 'day', 'week', 'month', 'year'

        const now = new Date();
        let startDate;

        switch (period) {
            case 'day':
                startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                break;
            case 'week':
                const weekStart = now.getDate() - now.getDay();
                startDate = new Date(now.getFullYear(), now.getMonth(), weekStart);
                break;
            case 'month':
                startDate = new Date(now.getFullYear(), now.getMonth(), 1);
                break;
            case 'year':
                startDate = new Date(now.getFullYear(), 0, 1);
                break;
            default:
                startDate = new Date(now.getFullYear(), now.getMonth(), 1);
        }

        // Aggregate revenue from delivered orders
        const revenueData = await Order.aggregate([
            {
                $match: {
                    status: 'delivered',
                    'items.product': { $exists: true },
                    createdAt: { $gte: startDate }
                }
            },
            {
                $unwind: '$items'
            },
            {
                $lookup: {
                    from: 'products',
                    localField: 'items.product',
                    foreignField: '_id',
                    as: 'productInfo'
                }
            },
            {
                $unwind: '$productInfo'
            },
            {
                $match: {
                    'productInfo.shopId': req.user._id
                }
            },
            {
                $group: {
                    _id: null,
                    totalRevenue: { $sum: { $multiply: ['$items.price', '$items.qty'] } },
                    totalOrders: { $addToSet: '$_id' },
                    totalProducts: { $sum: '$items.qty' }
                }
            }
        ]);

        const stats = revenueData[0] || { totalRevenue: 0, totalOrders: [], totalProducts: 0 };

        res.json({
            period,
            startDate,
            totalRevenue: stats.totalRevenue,
            totalOrders: stats.totalOrders.length,
            totalProducts: stats.totalProducts
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

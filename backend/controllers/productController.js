const Product = require('../models/Product');

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
        const prod = await Product.findById(req.params.id);
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

        const newProd = new Product(req.body);
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
        const updated = await Product.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
        if (!updated) return res.status(404).json({ message: 'Product not found' });
        res.json(updated);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Duplicate key', error: err.keyValue });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// DELETE /api/products/:id
exports.deleteProduct = async (req, res) => {
    try {
        const removed = await Product.findByIdAndDelete(req.params.id);
        if (!removed) return res.status(404).json({ message: 'Product not found' });
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

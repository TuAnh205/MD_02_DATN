const Promotion = require('../models/Promotion');
const Product = require('../models/Product');

// Get all promotions
exports.getPromotions = async (req, res) => {
    try {
        const { page = 1, limit = 20, isActive } = req.query;
        const filter = {};
        if (isActive !== undefined) filter.isActive = isActive === 'true';

        const skip = (Math.max(1, Number(page)) - 1) * Number(limit);
        const [promotions, total] = await Promise.all([
            Promotion.find(filter).populate('createdBy', 'name').sort('-createdAt').skip(skip).limit(Number(limit)),
            Promotion.countDocuments(filter)
        ]);

        res.json({ data: promotions, meta: { total, page: Number(page), limit: Number(limit) } });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Get promotion by ID
exports.getPromotionById = async (req, res) => {
    try {
        const promotion = await Promotion.findById(req.params.id).populate('createdBy', 'name');
        if (!promotion) return res.status(404).json({ message: 'Promotion not found' });
        res.json(promotion);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Create promotion
exports.createPromotion = async (req, res) => {
    try {
        const promotion = new Promotion({
            ...req.body,
            createdBy: req.user.id
        });
        await promotion.save();
        res.status(201).json(promotion);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Promotion code already exists' });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Update promotion
exports.updatePromotion = async (req, res) => {
    try {
        const updated = await Promotion.findByIdAndUpdate(req.params.id, req.body, { new: true });
        if (!updated) return res.status(404).json({ message: 'Promotion not found' });
        res.json(updated);
    } catch (err) {
        if (err.code === 11000) return res.status(400).json({ message: 'Promotion code already exists' });
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Delete promotion
exports.deletePromotion = async (req, res) => {
    try {
        const deleted = await Promotion.findByIdAndDelete(req.params.id);
        if (!deleted) return res.status(404).json({ message: 'Promotion not found' });
        res.json({ message: 'Deleted successfully' });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Apply discount code
exports.applyDiscountCode = async (req, res) => {
    try {
        const { code, cartItems, userId } = req.body;
        if (!code) return res.status(400).json({ message: 'Discount code is required' });

        const promotion = await Promotion.findOne({
            code: code.toUpperCase(),
            isActive: true,
            $or: [
                { endDate: { $exists: false } },
                { endDate: { $gte: new Date() } }
            ]
        });

        if (!promotion) return res.status(404).json({ message: 'Invalid or expired discount code' });

        // Check usage limits
        if (promotion.conditions.usageLimit) {
            // This would need tracking in a separate collection for used codes
            // For now, we'll skip this check
        }

        // Calculate discount
        let discountAmount = 0;
        let applicableItems = [];

        for (const item of cartItems) {
            const product = await Product.findById(item.productId);
            if (!product) continue;

            let itemDiscount = 0;

            // Check if product is applicable
            const isApplicable = (
                promotion.conditions.applicableProducts.length === 0 ||
                promotion.conditions.applicableProducts.includes(product._id)
            ) && (
                promotion.conditions.applicableCategories.length === 0 ||
                promotion.conditions.applicableCategories.includes(product.category)
            ) && (
                promotion.conditions.applicableBrands.length === 0 ||
                promotion.conditions.applicableBrands.includes(product.brand)
            );

            if (isApplicable) {
                if (promotion.type === 'percentage') {
                    itemDiscount = (product.price * promotion.value / 100) * item.quantity;
                } else if (promotion.type === 'fixed') {
                    itemDiscount = Math.min(promotion.value, product.price * item.quantity);
                }

                if (promotion.conditions.maxDiscount) {
                    itemDiscount = Math.min(itemDiscount, promotion.conditions.maxDiscount);
                }

                discountAmount += itemDiscount;
                applicableItems.push({
                    productId: item.productId,
                    discount: itemDiscount
                });
            }
        }

        // Check minimum order value
        const subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        if (subtotal < promotion.conditions.minOrderValue) {
            return res.status(400).json({
                message: `Minimum order value is ₫${promotion.conditions.minOrderValue.toLocaleString('vi-VN')}`
            });
        }

        res.json({
            promotion: {
                id: promotion._id,
                name: promotion.name,
                type: promotion.type,
                value: promotion.value
            },
            discountAmount,
            applicableItems
        });

    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
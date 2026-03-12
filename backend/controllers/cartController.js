const Cart = require('../models/Cart');
const Product = require('../models/Product');
const Voucher = require('../models/Voucher');

exports.getCart = async (req, res) => {
    try {
        let cart = await Cart.findOne({ user: req.user.id }).populate('items.product');
        if (!cart) {
            cart = new Cart({ user: req.user.id, items: [] });
            await cart.save();
        }
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.addToCart = async (req, res) => {
    try {
        const { productId, qty = 1, variant } = req.body;

        const product = await Product.findById(productId);
        if (!product || !product.isActive) {
            return res.status(404).json({ message: 'Product not found' });
        }

        if (product.stock < qty) {
            return res.status(400).json({ message: 'Insufficient stock' });
        }

        let cart = await Cart.findOne({ user: req.user.id });
        if (!cart) {
            cart = new Cart({ user: req.user.id, items: [] });
        }

        const existingItem = cart.items.find(item =>
            item.product.toString() === productId && item.variant === variant
        );

        if (existingItem) {
            existingItem.qty += qty;
        } else {
            cart.items.push({
                product: productId,
                name: product.name,
                price: product.price,
                qty,
                variant,
                image: product.images[0],
                attributes: product.attributes
            });
        }

        await cart.save();
        await cart.populate('items.product');
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.updateCartItem = async (req, res) => {
    try {
        const { itemId } = req.params;
        const { qty } = req.body;

        if (qty < 1) {
            return res.status(400).json({ message: 'Quantity must be at least 1' });
        }

        const cart = await Cart.findOne({ user: req.user.id });
        if (!cart) {
            return res.status(404).json({ message: 'Cart not found' });
        }

        const item = cart.items.id(itemId);
        if (!item) {
            return res.status(404).json({ message: 'Cart item not found' });
        }

        const product = await Product.findById(item.product);
        if (product.stock < qty) {
            return res.status(400).json({ message: 'Insufficient stock' });
        }

        item.qty = qty;
        await cart.save();
        await cart.populate('items.product');
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.removeFromCart = async (req, res) => {
    try {
        const { itemId } = req.params;

        const cart = await Cart.findOne({ user: req.user.id });
        if (!cart) {
            return res.status(404).json({ message: 'Cart not found' });
        }

        cart.items.pull(itemId);
        await cart.save();
        await cart.populate('items.product');
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.clearCart = async (req, res) => {
    try {
        const cart = await Cart.findOne({ user: req.user.id });
        if (!cart) {
            return res.status(404).json({ message: 'Cart not found' });
        }

        cart.items = [];
        cart.voucher = undefined;
        await cart.save();
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.applyVoucher = async (req, res) => {
    try {
        const { code } = req.body;

        const voucher = await Voucher.findOne({
            code: code.toUpperCase(),
            isActive: true,
            startDate: { $lte: new Date() },
            endDate: { $gte: new Date() }
        });

        if (!voucher) {
            return res.status(400).json({ message: 'Invalid or expired voucher' });
        }

        if (voucher.usageLimit && voucher.usedCount >= voucher.usageLimit) {
            return res.status(400).json({ message: 'Voucher usage limit exceeded' });
        }

        const cart = await Cart.findOne({ user: req.user.id }).populate('items.product');
        if (!cart || cart.items.length === 0) {
            return res.status(400).json({ message: 'Cart is empty' });
        }

        // Calculate subtotal
        let subtotal = cart.items.reduce((sum, item) => sum + (item.price * item.qty), 0);

        if (subtotal < voucher.minOrderValue) {
            return res.status(400).json({ message: `Minimum order value is ${voucher.minOrderValue}` });
        }

        let discount = 0;
        if (voucher.type === 'percentage') {
            discount = (subtotal * voucher.value) / 100;
            if (voucher.maxDiscount && discount > voucher.maxDiscount) {
                discount = voucher.maxDiscount;
            }
        } else {
            discount = voucher.value;
        }

        cart.voucher = {
            code: voucher.code,
            discount,
            type: voucher.type
        };

        await cart.save();
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.removeVoucher = async (req, res) => {
    try {
        const cart = await Cart.findOne({ user: req.user.id });
        if (!cart) {
            return res.status(404).json({ message: 'Cart not found' });
        }

        cart.voucher = undefined;
        await cart.save();
        await cart.populate('items.product');
        res.json(cart);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getCartSummary = async (req, res) => {
    try {
        const cart = await Cart.findOne({ user: req.user.id }).populate('items.product');
        if (!cart) {
            return res.json({ items: [], subtotal: 0, discount: 0, total: 0 });
        }

        let subtotal = cart.items.reduce((sum, item) => sum + (item.price * item.qty), 0);
        let discount = cart.voucher ? cart.voucher.discount : 0;
        let total = subtotal - discount;

        // Estimate shipping (simplified)
        let shipping = total > 500000 ? 0 : 30000; // Free shipping over 500k VND
        total += shipping;

        res.json({
            items: cart.items,
            subtotal,
            discount,
            shipping,
            total,
            voucher: cart.voucher
        });
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
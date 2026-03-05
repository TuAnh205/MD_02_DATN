const Order = require('../models/Order');

exports.createOrder = async (req, res) => {
    try {
        const userId = req.user.id;
        const { items, total, address, payment } = req.body;
        if (!Array.isArray(items) || items.length === 0) return res.status(400).json({ message: 'items required' });
        if (total === undefined) return res.status(400).json({ message: 'total required' });

        const order = new Order({ user: userId, items, total, address, payment });
        // If payment.paid true, set paidAt
        if (payment && payment.paid) order.payment.paidAt = new Date();
        await order.save();
        res.status(201).json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getOrders = async (req, res) => {
    try {
        const userId = req.user.id;
        // admin can get all
        if (req.user.role === 'admin') {
            const all = await Order.find().populate('user items.product');
            return res.json(all);
        }
        const orders = await Order.find({ user: userId }).populate('items.product');
        res.json(orders);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getOrderById = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id).populate('items.product');
        if (!order) return res.status(404).json({ message: 'Order not found' });
        // allow access if owner or admin
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        res.json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.updateStatus = async (req, res) => {
    try {
        // Only admin can update status
        if (req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        const { status } = req.body;
        const order = await Order.findByIdAndUpdate(req.params.id, { status }, { new: true });
        if (!order) return res.status(404).json({ message: 'Order not found' });
        res.json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.markPaid = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id);
        if (!order) return res.status(404).json({ message: 'Order not found' });
        // allow owner or admin
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        order.payment.paid = true;
        order.payment.paidAt = new Date();
        await order.save();
        res.json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

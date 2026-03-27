const Order = require('../models/Order');

exports.createOrder = async (req, res) => {
    try {
        const userId = req.user.id;
        const { items, subtotal, total, payment, shipping } = req.body;
        
        console.log('Creating order with data:', { userId, items, subtotal, total, payment, shipping });
        
        if (!Array.isArray(items) || items.length === 0) {
            return res.status(400).json({ message: 'items required and must be non-empty array' });
        }
        
        if (total === undefined || total === null) {
            return res.status(400).json({ message: 'total is required' });
        }
        
        if (!shipping || !shipping.address) {
            return res.status(400).json({ message: 'shipping address is required' });
        }

        const productIds = items.map(item => item.product);
        const productsInDb = await require('../models/Product').find({ _id: { $in: productIds } }).select('_id shopId');

        if (productsInDb.length !== productIds.length) {
            return res.status(400).json({ message: 'Một hoặc nhiều sản phẩm không hợp lệ' });
        }

        const productMap = productsInDb.reduce((acc, p) => {
            acc[p._id.toString()] = p;
            return acc;
        }, {});

        const orderData = {
            orderNumber: 'ORD-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5).toUpperCase(),
            user: userId,
            items: items.map(item => {
                const p = productMap[item.product];
                return {
                    product: item.product,
                    shopId: p.shopId,
                    name: item.name || 'Unknown Product',
                    price: item.price || 0,
                    qty: item.qty || 1,
                    image: item.image || '',
                    sku: item.sku || ''
                };
            }),
            subtotal: subtotal || total || 0,
            total: total || 0,
            shipping: {
                address: {
                    name: shipping.address.name || '',
                    phone: shipping.address.phone || '',
                    address: shipping.address.address || '',
                    city: shipping.address.city || 'N/A',
                    district: shipping.address.district || 'N/A',
                    ward: shipping.address.ward || 'N/A'
                },
                method: shipping.method || 'standard',
                fee: shipping.fee || 0
            },
            payment: {
                method: payment?.method || 'cod',
                status: payment?.status || 'pending',
                // Store card payment info if provided
                ...(payment?.cardholderName && { cardholderName: payment.cardholderName }),
                ...(payment?.cardLastFour && { cardLastFour: payment.cardLastFour }),
                // Store bank transfer info if provided
                ...(payment?.bankName && { bankName: payment.bankName }),
                ...(payment?.accountNumber && { accountNumber: payment.accountNumber }),
                ...(payment?.accountHolder && { accountHolder: payment.accountHolder })
            }
        };

        console.log('Order data prepared:', orderData);

        const order = new Order(orderData);
        await order.save();
        
        console.log('Order created successfully:', order._id);
        
        res.status(201).json(order);
    } catch (err) {
        console.error('Error creating order:', err.message);
        console.error('Full error:', err);
        res.status(500).json({ 
            message: 'Server error', 
            error: err.message,
            details: err.errors ? Object.keys(err.errors).map(k => `${k}: ${err.errors[k].message}`) : []
        });
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

// POST /api/orders/:id/process-payment
// Process card payment
exports.processPayment = async (req, res) => {
    try {
        const { method, cardData } = req.body;
        const order = await Order.findById(req.params.id);
        
        if (!order) {
            return res.status(404).json({ message: 'Order not found' });
        }
        
        // Allow owner or admin
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Forbidden' });
        }
        
        if (method === 'card') {
            // Validate card - test card is 4242424242424242
            const testCard = '4242424242424242';
            if (cardData.cardNumber !== testCard) {
                return res.status(400).json({ error: 'Mã thẻ không hợp lệ! Vui lòng sử dụng thẻ test: 4242 4242 4242 4242' });
            }
            
            // Payment successful - update order
            order.payment.status = 'paid';
            order.payment.paidAt = new Date();
            await order.save();
            
            return res.json({ 
                success: true, 
                message: '✅ Thanh toán thành công!',
                order 
            });
        }
        
        res.status(400).json({ error: 'Invalid payment method' });
    } catch (err) {
        console.error('Error processing payment:', err);
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// PATCH /api/orders/:id/cancel
// body: { reason: string }
exports.cancelOrder = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id);
        if (!order) return res.status(404).json({ message: 'Order not found' });
        // only owner or admin can cancel
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Forbidden' });
        }

        // cannot cancel once shipped/delivered/returned/refunded
        const nonCancelable = ['shipped', 'delivered', 'returned', 'refunded'];
        if (nonCancelable.includes(order.status)) {
            return res.status(400).json({ message: `Cannot cancel order with status '${order.status}'` });
        }

        order.status = 'cancelled';
        const reason = req.body.reason || '';
        if (reason) order.cancellationReason = reason;
        order.statusHistory.push({ status: 'cancelled', note: reason });
        await order.save();
        res.json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

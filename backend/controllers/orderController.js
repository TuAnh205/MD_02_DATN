const Order = require('../models/Order');
const Notification = require('../models/Notification');
const Product = require('../models/Product');
const Voucher = require('../models/Voucher');

// ================= CREATE ORDER =================
exports.createOrder = async (req, res) => {
    try {
        const userId = req.user.id;
        const { items, subtotal, payment, shipping, voucherCode } = req.body;

        if (!Array.isArray(items) || items.length === 0) {
            return res.status(400).json({ message: 'items required' });
        }

        if (!shipping || !shipping.address) {
            return res.status(400).json({ message: 'shipping address is required' });
        }

        // ===== VALIDATE ADDRESS =====
        const name = String(shipping.address.name || '').trim();
        const phone = String(shipping.address.phone || '').trim();
        const address = String(shipping.address.address || '').trim();
        const city = String(shipping.address.city || '').trim();
        const district = String(shipping.address.district || '').trim();
        const ward = String(shipping.address.ward || '').trim();

        if (!name || !phone || !address || !city || !district || !ward) {
            return res.status(400).json({ message: 'Thông tin giao hàng không hợp lệ' });
        }

        if (!/^0\d{9,10}$/.test(phone)) {
            return res.status(400).json({ message: 'SĐT không hợp lệ' });
        }

        // ===== LOAD PRODUCT =====
        const productIds = items.map(i => i.product);
        const products = await Product.find({ _id: { $in: productIds } }).select('_id shopId');

        if (products.length !== productIds.length) {
            return res.status(400).json({ message: 'Sản phẩm không hợp lệ' });
        }

        const productMap = {};
        products.forEach(p => productMap[p._id] = p);

        // ===== DISCOUNT FIX =====
        let discount = {
            code: '',
            amount: 0,
            type: 'fixed' // 🔥 FIX
        };

        if (voucherCode) {
            const voucher = await Voucher.findOne({ code: voucherCode.toUpperCase() });

            if (!voucher) {
                return res.status(400).json({ message: 'Voucher không tồn tại' });
            }

            const now = new Date();

            if (!voucher.isActive || now < voucher.startDate || now > voucher.endDate) {
                return res.status(400).json({ message: 'Voucher hết hạn' });
            }

            if (subtotal < voucher.minOrderValue) {
                return res.status(400).json({ message: 'Không đủ điều kiện dùng voucher' });
            }

            let amount = 0;

            if (voucher.type === 'percentage') {
                amount = Math.round(subtotal * voucher.value / 100);
                if (voucher.maxDiscount && amount > voucher.maxDiscount) {
                    amount = voucher.maxDiscount;
                }
            } else {
                amount = voucher.value;
            }

            discount = {
                code: voucher.code,
                amount,
                type: voucher.type || 'fixed'
            };
        }

        // ===== BUILD ORDER =====
        const orderData = {
            orderNumber: 'ORD-' + Date.now(),
            user: userId,

            items: items.map(item => ({
                product: item.product,
                shopId: productMap[item.product].shopId,
                name: item.name || '',
                price: item.price || 0,
                qty: item.qty || 1,
                image: item.image || ''
            })),

            subtotal: subtotal || 0,
            discount,
            total: (subtotal || 0) - discount.amount,

            shipping: {
                address: { name, phone, address, city, district, ward },
                method: shipping.method || 'standard',
                fee: shipping.fee || 0
            },

            payment: {
                method: payment?.method || 'cod',
                status: payment?.status || 'pending'
            }
        };

        const order = new Order(orderData);
        await order.save();

        // ===== NOTIFICATION =====
        const shopIds = [...new Set(order.items.map(i => i.shopId.toString()))];

        for (const shopId of shopIds) {
            await Notification.create({
                user: shopId,
                type: 'shop_order',
                title: 'Đơn hàng mới',
                message: `Bạn có đơn hàng mới`,
                data: { orderId: order._id }
            });
        }

        res.status(201).json(order);

    } catch (err) {
        console.error("CREATE ORDER ERROR:", err);
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// ================= GET ALL =================
exports.getOrders = async (req, res) => {
    try {
        const userId = req.user.id;

        if (req.user.role === 'admin') {
            const orders = await Order.find().populate('items.product');
            return res.json(orders);
        }

        const orders = await Order.find({ user: userId }).populate('items.product');
        res.json(orders);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

// ================= GET ONE =================
exports.getOrderById = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id).populate('items.product');

        if (!order) return res.status(404).json({ message: 'Order not found' });

        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Forbidden' });
        }

        res.json(order);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

// ================= UPDATE STATUS (ADMIN) =================
exports.updateStatus = async (req, res) => {
    try {
        if (req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Forbidden' });
        }

        const { status } = req.body;
        const order = await Order.findById(req.params.id);

        if (!order) return res.status(404).json({ message: 'Not found' });

        order.status = status;
        await order.save();

        res.json(order);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

// ================= MARK PAID =================
exports.markPaid = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id);

        if (!order) return res.status(404).json({ message: 'Not found' });

        order.payment.status = 'paid';
        order.payment.paidAt = new Date();

        await order.save();
        res.json(order);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

// ================= PROCESS PAYMENT =================
exports.processPayment = async (req, res) => {
    try {
        const { method, cardData } = req.body;
        const order = await Order.findById(req.params.id);

        if (!order) return res.status(404).json({ message: 'Not found' });

        if (method === 'card') {
            if (cardData.cardNumber !== '4242424242424242') {
                return res.status(400).json({ message: 'Sai thẻ test' });
            }

            order.payment.status = 'paid';
            order.payment.paidAt = new Date();

            await order.save();

            return res.json({ success: true, order });
        }

        res.status(400).json({ message: 'Invalid payment' });

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};

// ================= CANCEL =================
exports.cancelOrder = async (req, res) => {
    try {
        const order = await Order.findById(req.params.id);

        if (!order) return res.status(404).json({ message: 'Not found' });

        order.status = 'đã hủy';
        await order.save();

        res.json(order);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};
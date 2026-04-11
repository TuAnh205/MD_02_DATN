const Order = require('../models/Order');
const Notification = require('../models/Notification');

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

        const shippingName = String(shipping.address.name || '').trim().replace(/\s+/g, ' ');
        const shippingPhone = String(shipping.address.phone || '').trim();
        const shippingAddress = String(shipping.address.address || '').trim();
        const shippingCity = String(shipping.address.city || '').trim().replace(/\s+/g, ' ');
        const shippingDistrict = String(shipping.address.district || '').trim().replace(/\s+/g, ' ');
        const shippingWard = String(shipping.address.ward || '').trim().replace(/\s+/g, ' ');

        if (!shippingName || !shippingPhone || !shippingAddress || !shippingCity || !shippingDistrict || !shippingWard) {
            return res.status(400).json({ message: 'Thông tin giao hàng không hợp lệ' });
        }

        if (/\d/.test(shippingName)) {
            return res.status(400).json({ message: 'Họ và tên người nhận không được chứa số' });
        }

        if (!/^0\d{9,10}$/.test(shippingPhone)) {
            return res.status(400).json({ message: 'Số điện thoại giao hàng không hợp lệ' });
        }

        if (/\d/.test(shippingCity)) {
            return res.status(400).json({ message: 'Tỉnh/Thành phố không được chứa số' });
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
                    name: shippingName,
                    phone: shippingPhone,
                    address: shippingAddress,
                    city: shippingCity,
                    district: shippingDistrict,
                    ward: shippingWard
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

        // Tạo thông báo cho shop (mỗi shop một thông báo)
        const shopIds = [...new Set(order.items.map((item) => item.shopId.toString()))];
        for (const shopId of shopIds) {
            const relatedCount = order.items.filter((item) => item.shopId.toString() === shopId)
                .reduce((sum, i) => sum + i.qty, 0);

            await Notification.create({
                user: shopId,
                type: 'shop_order',
                title: 'Đơn hàng mới',
                message: `Bạn có ${relatedCount} sản phẩm trong đơn #${order.orderNumber}`,
                data: { orderId: order._id, orderNumber: order.orderNumber },
                isRead: false
            });
        }
        
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
        console.log('[ADMIN UPDATE ORDER STATUS] status nhận được:', status);
        // Nếu admin duyệt đơn (status là 'đã xác nhận'), trừ số lượng kho
        let order = await Order.findById(req.params.id);
        if (!order) return res.status(404).json({ message: 'Order not found' });

        // Chỉ trừ kho khi chuyển sang trạng thái 'đã xác nhận'
        const shouldReduceStock = (status === 'đã xác nhận') && order.status !== status;
        if (shouldReduceStock) {
            const Product = require('../models/Product');
            // Kiểm tra tồn kho trước khi trừ
            for (const item of order.items) {
                const product = await Product.findById(item.product);
                if (!product) return res.status(400).json({ message: `Sản phẩm không tồn tại: ${item.name}` });
                if (product.stock < item.qty) {
                    return res.status(400).json({ message: `Sản phẩm '${product.name}' không đủ hàng trong kho. Hiện còn: ${product.stock}` });
                }
            }
            // Trừ kho
            for (const item of order.items) {
                await Product.findByIdAndUpdate(item.product, { $inc: { stock: -item.qty } });
            }
        }
        // Cập nhật trạng thái đơn hàng
        order.status = status;
        await order.save();
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
        const nonCancelable = ['đang giao', 'đã nhận', 'trả hàng', 'hoàn tiền'];
        if (nonCancelable.includes(order.status)) {
            return res.status(400).json({ message: `Cannot cancel order with status '${order.status}'` });
        }

        order.status = 'đã hủy';
        const reason = req.body.reason || '';
        if (reason) order.cancellationReason = reason;
        order.statusHistory.push({ status: 'đã hủy', note: reason });
        await order.save();

        // Gửi thông báo cho user
        try {
            await Notification.create({
                user: order.user,
                type: 'order_status',
                title: 'Đơn hàng đã bị hủy',
                message: reason ? `Đơn hàng của bạn đã bị hủy. Lý do: ${reason}` : 'Đơn hàng của bạn đã bị hủy.',
                data: { orderId: order._id, status: 'đã hủy' }
            });
        } catch (notifyErr) {
            console.error('Lỗi gửi thông báo:', notifyErr);
        }

        res.json(order);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

const Order = require('../models/Order');
const Notification = require('../models/Notification');
const User = require('../models/User');
const { SHOP_BILLING_POLICY, roundCurrency, isOrderInFeeablePeriod } = require('../config/shopBillingPolicy');

/**
 * Cập nhật platformFee khi order được thanh toán thành công
 * Gọi function này khi payment.status = 'paid'
 * Chỉ tính phí nếu order thanh toán từ 13/4/2026 18:02:24 trở đi
 */
const updatePlatformFeeOnPayment = async (orderId) => {
  try {
    const order = await Order.findById(orderId);
    if (!order) return null;

    // Chỉ update nếu payment đã paid
    if (order.payment?.status !== 'paid') return order;

    // Kiểm tra xem order có phát sinh từ thời điểm tính phí không
    const paidDate = order.payment?.paidAt || new Date();
    const isFeeable = isOrderInFeeablePeriod(paidDate);

    // Cập nhật platformFee cho mỗi item
    order.items.forEach((item) => {
      if (!item.platformFee) {
        item.platformFee = {};
      }

      if (isFeeable) {
        // Tính phí: 5% trên giá bán
        const baseAmount = roundCurrency((item.price || 0) * (item.qty || 1));
        const feeAmount = roundCurrency(baseAmount * SHOP_BILLING_POLICY.commissionRate);

        item.platformFee.eligible = true;
        item.platformFee.rate = SHOP_BILLING_POLICY.commissionRate;
        item.platformFee.baseAmount = baseAmount;
        item.platformFee.feeAmount = feeAmount;
        item.platformFee.status = 'unpaid'; // Sẽ được shopBillingService tính và trừ từ ví
        item.platformFee.chargedAt = new Date();
      } else {
        // Đơn trước 13/4/2026 18:02:24 - không tính phí
        item.platformFee.eligible = false;
        item.platformFee.rate = 0;
        item.platformFee.baseAmount = roundCurrency((item.price || 0) * (item.qty || 1));
        item.platformFee.feeAmount = 0;
        item.platformFee.status = 'exempt'; // Miễn phí
      }
    });

    await order.save();
    return order;
  } catch (error) {
    console.error('Error updating platform fee on payment:', error);
    return null;
  }
};

exports.updatePlatformFeeOnPayment = updatePlatformFeeOnPayment;

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

        if (!/^0\d{9,10}$/.test(shippingPhone)) {
            return res.status(400).json({ message: 'Số điện thoại giao hàng không hợp lệ' });
        }

        if (/\d/.test(shippingCity)) {
            return res.status(400).json({ message: 'Tỉnh/Thành phố không được chứa số' });
        }

        const productIds = items.map(item => item.product);
        const productsInDb = await require('../models/Product').find({ _id: { $in: productIds } }).select('_id shopId createdAt billing name');

        if (productsInDb.length !== productIds.length) {
            return res.status(400).json({ message: 'Một hoặc nhiều sản phẩm không hợp lệ' });
        }

        const productMap = productsInDb.reduce((acc, p) => {
            acc[p._id.toString()] = p;
            return acc;
        }, {});

        const shopIds = [...new Set(productsInDb.map((product) => product.shopId.toString()))];
        const shops = await User.find({ _id: { $in: shopIds } }).select('_id shopStatus');
        const frozenShops = new Set(shops.filter((shop) => shop.shopStatus === 'frozen').map((shop) => shop._id.toString()));
        const blockedProduct = productsInDb.find((product) => frozenShops.has(product.shopId.toString()));
        if (blockedProduct) {
            return res.status(400).json({ message: `Sản phẩm ${blockedProduct.name} hiện không thể bán do shop đang bị đóng băng` });
        }

        const now = new Date();

        const orderData = {
            orderNumber: 'ORD-' + Date.now(),
            user: userId,
            items: items.map(item => {
                const p = productMap[item.product];
                const qty = item.qty || 1;
                const lineAmount = (item.price || 0) * qty;
                // Phí chỉ tính khi thanh toán thành công, nên khi tạo order, eligible = false
                return {
                    product: item.product,
                    shopId: p.shopId,
                    name: item.name || 'Unknown Product',
                    price: item.price || 0,
                    qty,
                    image: item.image || '',
                    sku: item.sku || '',
                    platformFee: {
                        eligible: false,  // Chưa tính vì chưa thanh toán
                        rate: SHOP_BILLING_POLICY.commissionRate,
                        baseAmount: roundCurrency(lineAmount),
                        feeAmount: 0,  // Sẽ tính khi thanh toán thành công
                        status: 'pending',  // Chuyển sang 'paid' khi payment.status = 'paid'
                    }
                };
            }),
            subtotal: subtotal || total || 0,
            total: total || 0,
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

        // Tạo thông báo cho shop (mỗi shop một thông báo)
        for (const shopId of shopIds) {
            const relatedCount = order.items.filter((item) => item.shopId.toString() === shopId)
                .reduce((sum, i) => sum + i.qty, 0);

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
        if (!order) return res.status(404).json({ message: 'Order not found' });
        // allow owner or admin
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') return res.status(403).json({ message: 'Forbidden' });
        
        order.payment.status = 'paid';
        order.payment.paidAt = new Date();

        await order.save();
        
        // Cập nhật phí sàn khi thanh toán thành công
        await updatePlatformFeeOnPayment(req.params.id);
        
        // Lấy order đã được update
        const updatedOrder = await Order.findById(req.params.id);
        res.json(updatedOrder);
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
            
            // Cập nhật phí sàn khi thanh toán thành công
            await updatePlatformFeeOnPayment(req.params.id);
            
            // Lấy order đã được update
            const updatedOrder = await Order.findById(req.params.id);
            
            return res.json({ 
                success: true, 
                message: '✅ Thanh toán thành công!',
                order: updatedOrder
            });
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
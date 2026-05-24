const Order = require('../models/Order');
const Product = require('../models/Product');
const Notification = require('../models/Notification');
const User = require('../models/User');
const Voucher = require('../models/Voucher');
const { SHOP_BILLING_POLICY, roundCurrency, isOrderInFeeablePeriod } = require('../config/shopBillingPolicy');

const restoreOrderStock = async (order) => {
    if (!order || !Array.isArray(order.items)) return;

    await Promise.all(order.items.map(async (item) => {
        const qty = item.qty || 1;
        if (!item.product || qty <= 0) return;
        await Product.findByIdAndUpdate(item.product, { $inc: { stock: qty } }, { new: true });
    }));
};

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

// ================= SETTLE PAYMENT AND CREDIT SHOPS =================
const settlePaymentAndCreditShops = async (orderId) => {
    try {
        const order = await Order.findById(orderId);
        if (!order) return null;

        const now = new Date();
        const shopMap = new Map();

        // Aggregate gross and fee per shop, mark fee as paid on items
        for (const item of order.items) {
            const shopId = String(item.shopId);
            const qty = item.qty || 1;
            const gross = roundCurrency((item.price || 0) * qty);
            const feeAmount = roundCurrency(item.platformFee?.feeAmount || ((item.price || 0) * qty * SHOP_BILLING_POLICY.commissionRate));

            // ensure platformFee object exists
            item.platformFee = item.platformFee || {};
            item.platformFee.feeAmount = feeAmount;
            item.platformFee.eligible = true;
            item.platformFee.status = 'paid';
            item.platformFee.chargedAt = now;

            if (!shopMap.has(shopId)) shopMap.set(shopId, { gross: 0, fee: 0 });
            const agg = shopMap.get(shopId);
            agg.gross = roundCurrency(agg.gross + gross);
            agg.fee = roundCurrency(agg.fee + feeAmount);
        }

        // Update voucher usage if applied
        if (order.discount?.code) {
            const voucher = await Voucher.findOne({ code: order.discount.code });
            if (voucher) {
                voucher.usedCount = (voucher.usedCount || 0) + 1;
                await voucher.save();
            }

            // Update user's voucher usage count
            const user = await User.findById(order.user).populate('userVouchers.voucher');
            if (user) {
                const userVoucherEntry = user.userVouchers.find(
                    entry => entry.voucher && entry.voucher.code === order.discount.code
                );
                if (userVoucherEntry) {
                    userVoucherEntry.usedCount = (userVoucherEntry.usedCount || 0) + 1;
                    if (voucher.userLimit && userVoucherEntry.usedCount >= voucher.userLimit) {
                        userVoucherEntry.isConsumed = true;
                    }
                    await user.save();
                }
            }
        }

        // save order with updated platformFee statuses
        await order.save();

        // Credit each shop's wallet with net amount (gross - fee)
        for (const [shopId, agg] of shopMap.entries()) {
            const shop = await User.findById(shopId);
            if (!shop) continue;

            if (!shop.shopWallet) shop.shopWallet = { balance: 0 };
            if (typeof shop.shopWallet.balance !== 'number') shop.shopWallet.balance = Number(shop.shopWallet.balance) || 0;

            const net = roundCurrency(agg.gross - agg.fee);
            shop.shopWallet.balance = roundCurrency((shop.shopWallet.balance || 0) + net);
            shop.shopWallet.updatedAt = now;
            await shop.save();

            // notify shop
            await Notification.create({
                user: shop._id,
                type: 'system',
                title: 'Đã cộng tiền bán hàng',
                message: `Đơn ${order.orderNumber} đã được thanh toán. Số tiền ${net.toLocaleString('vi-VN')}đ đã được cộng vào ví sau khi trừ phí sàn ${agg.fee.toLocaleString('vi-VN')}đ.`,
                data: { orderId: order._id, amount: net, feeAmount: agg.fee, type: 'shop_income' },
            });
        }

        return order;
    } catch (err) {
        console.error('SETTLE PAYMENT ERROR:', err);
        return null;
    }
};

exports.settlePaymentAndCreditShops = settlePaymentAndCreditShops;

const buildOrderResponse = (order) => {
    const orderObj = order.toObject ? order.toObject() : order;
    const discountAmount = orderObj.discount?.amount || orderObj.discount?.value || 0;

    if (!orderObj.discount) {
        orderObj.discount = { amount: discountAmount, value: discountAmount };
    }

    // Normalize discount response so Android can read either amount or value
    orderObj.discount.amount = discountAmount;
    orderObj.discount.value = discountAmount;

    orderObj.voucherDiscount = discountAmount;
    orderObj.voucher = {
        amount: discountAmount,
        discount: discountAmount,
        value: discountAmount,
        code: orderObj.discount?.code || ''
    };

    const cancellationReason = orderObj.cancellationReason || orderObj.reason || '';
    orderObj.cancellationReason = cancellationReason;
    if (!orderObj.reason && cancellationReason) {
        orderObj.reason = cancellationReason;
    }

    return orderObj;
};

exports.createOrder = async (req, res) => {
    try {
        const userId = req.user.id;
        const { items, subtotal, total, payment, shipping, voucherCode, discountCode, discount: discountBody } = req.body;
        const voucherCodeValue = voucherCode || discountCode || (discountBody && discountBody.code) || null;

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

        if (!/^0\d{9}$/.test(phone)) {
            return res.status(400).json({ message: 'SĐT không hợp lệ' });
        }

        if (/\d/.test(city)) {
            return res.status(400).json({ message: 'Tỉnh/Thành phố không được chứa số' });
        }

        const productIds = items.map(item => item.product);
        const productsInDb = await Product.find({ _id: { $in: productIds } }).select('_id shopId createdAt billing name stock');

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

        let finalDiscountAmount = typeof discountBody === 'number'
            ? discountBody
            : (discountBody && typeof discountBody === 'object')
                ? discountBody.amount || discountBody.value || 0
                : 0;
        let finalDiscountType = discountBody && typeof discountBody === 'object' ? discountBody.type || discountBody?.type : undefined;

        let voucherFromDb = null;
        let claimedVoucher = null;
        let voucherUser = null;

        if (voucherCodeValue) {
            voucherFromDb = await Voucher.findOne({
                code: voucherCodeValue.toUpperCase(),
                isActive: true,
                startDate: { $lte: new Date() },
                endDate: { $gte: new Date() }
            });

            if (voucherFromDb) {
                voucherUser = await User.findById(userId);
                if (!voucherUser) {
                    return res.status(404).json({ message: 'User not found' });
                }

                claimedVoucher = (voucherUser.userVouchers || []).find((entry) => {
                    if (!entry || !entry.voucher) return false;
                    try {
                        return String(entry.voucher) === String(voucherFromDb._id);
                    } catch (e) {
                        return false;
                    }
                });
                if (!claimedVoucher) {
                    return res.status(400).json({ message: 'Bạn chưa nhận voucher này' });
                }

                if (voucherFromDb.userLimit && (claimedVoucher.usedCount || 0) >= voucherFromDb.userLimit) {
                    return res.status(400).json({ message: 'Bạn đã dùng voucher này tối đa số lần' });
                }

                const subtotalAmount = subtotal || 0;
                if (subtotalAmount < voucherFromDb.minOrderValue) {
                    return res.status(400).json({ message: `Giá trị đơn hàng phải lớn hơn hoặc bằng ${voucherFromDb.minOrderValue}` });
                }

                if (finalDiscountAmount === 0) {
                    let applicableSubtotal = 0;

                    for (const item of items) {
                        const product = productMap[item.product];
                        if (!product) continue;

                        const itemApplicable = (
                            !voucherFromDb.applicableProducts || voucherFromDb.applicableProducts.length === 0 ||
                            voucherFromDb.applicableProducts.map(id => id.toString()).includes(product._id.toString())
                        ) && (
                            !voucherFromDb.applicableCategories || voucherFromDb.applicableCategories.length === 0 ||
                            voucherFromDb.applicableCategories.includes(product.category)
                        );

                        if (!itemApplicable) continue;

                        const lineTotal = (item.price || 0) * (item.qty || 1);
                        applicableSubtotal += lineTotal;
                    }

                    if (voucherFromDb.type === 'percentage') {
                        let computedDiscount = (applicableSubtotal * voucherFromDb.value) / 100;
                        if (voucherFromDb.maxDiscount && computedDiscount > voucherFromDb.maxDiscount) {
                            computedDiscount = voucherFromDb.maxDiscount;
                        }
                        finalDiscountAmount = roundCurrency(computedDiscount);
                    } else {
                        finalDiscountAmount = roundCurrency(voucherFromDb.value);
                    }
                }

                finalDiscountType = finalDiscountType || voucherFromDb.type;
            }
        }

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
            subtotal: subtotal || 0,
            total: typeof total === 'number' ? total : roundCurrency((subtotal || 0) - finalDiscountAmount + (shipping.fee || 0)),
            discount: {
                amount: finalDiscountAmount,
                code: voucherCodeValue,
                type: finalDiscountType
            },
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

        // Giảm tồn kho khi đơn được tạo thành công
        try {
            await Promise.all(order.items.map(async (item) => {
                const qty = item.qty || 1;
                const updatedProduct = await Product.findOneAndUpdate(
                    { _id: item.product, stock: { $gte: qty } },
                    { $inc: { stock: -qty } },
                    { new: true }
                );

                if (!updatedProduct) {
                    throw new Error(`Sản phẩm ${productMap[String(item.product)]?.name || 'không đủ'} không còn đủ hàng`);
                }
            }));
        } catch (stockErr) {
            await Order.findByIdAndDelete(order._id);
            return res.status(400).json({ message: stockErr.message || 'Không đủ tồn kho để tạo đơn hàng' });
        }

        if (voucherFromDb && claimedVoucher && voucherUser) {
            claimedVoucher.usedCount = (claimedVoucher.usedCount || 0) + 1;
            if (voucherFromDb.userLimit && claimedVoucher.usedCount >= voucherFromDb.userLimit) {
                claimedVoucher.isConsumed = true;
            }
            await voucherUser.save();
            voucherFromDb.usedCount = (voucherFromDb.usedCount || 0) + 1;
            await voucherFromDb.save();
        }

        // Tạo thông báo cho shop (mỗi shop một thông báo)
        for (const shopId of shopIds) {
            const relatedCount = order.items.filter((item) => item.shopId.toString() === shopId)
                .reduce((sum, i) => sum + i.qty, 0);

            await Notification.create({
                user: shopId,
                type: 'shop_order',
                title: 'Đơn hàng mới',
                message: `Bạn có đơn hàng mới${voucherCodeValue ? ` (áp dụng voucher ${voucherCodeValue})` : ''}`,
                data: { orderId: order._id }
            });
        }

        res.status(201).json(buildOrderResponse(order));

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
            return res.json(orders.map(buildOrderResponse));
        }

        const orders = await Order.find({ user: userId }).populate('items.product');
        res.json(orders.map(buildOrderResponse));

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

        res.json(buildOrderResponse(order));

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
        // Cộng tiền về ví shop và đánh dấu phí đã trừ
        await settlePaymentAndCreditShops(req.params.id);

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
            if (!cardData || !cardData.cardNumber || !/^\d{16}$/.test(cardData.cardNumber)) {
                return res.status(400).json({ message: 'Số thẻ không hợp lệ, cần 16 chữ số' });
            }

            order.payment.status = 'paid';
            order.payment.paidAt = new Date();

            await order.save();
            
            // Cập nhật phí sàn khi thanh toán thành công
            await updatePlatformFeeOnPayment(req.params.id);
            // Cộng tiền vào ví shop và cập nhật trạng thái phí
            await settlePaymentAndCreditShops(req.params.id);

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
        if (order.user.toString() !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Forbidden' });
        }

        if (order.status === 'đã hủy') {
            return res.status(400).json({ message: 'Đơn hàng đã bị hủy trước đó' });
        }

        const { reason } = req.body;

        await restoreOrderStock(order);

        // Hoàn lại voucher nếu được sử dụng trong đơn hàng này
        if (order.discount?.code) {
            try {
                const voucherFromDb = await Voucher.findOne({ code: order.discount.code });
                const user = await User.findById(order.user).populate('userVouchers.voucher');
                
                if (voucherFromDb && user) {
                    const userVoucherEntry = user.userVouchers.find(
                        entry => entry.voucher && entry.voucher._id.toString() === voucherFromDb._id.toString()
                    );

                    if (userVoucherEntry && userVoucherEntry.usedCount > 0) {
                        // Giảm usedCount và nếu chưa dùng hết thì mở lại voucher
                        userVoucherEntry.usedCount = Math.max(0, userVoucherEntry.usedCount - 1);
                        if (voucherFromDb.userLimit && userVoucherEntry.usedCount < voucherFromDb.userLimit) {
                            userVoucherEntry.isConsumed = false;
                        }
                        await user.save();
                    }

                    // Giảm usedCount của voucher chính
                    if (voucherFromDb.usedCount > 0) {
                        voucherFromDb.usedCount = Math.max(0, voucherFromDb.usedCount - 1);
                        await voucherFromDb.save();
                    }
                }
            } catch (voucherErr) {
                console.error('Error restoring voucher on cancel:', voucherErr);
                // Không dừng quá trình hủy nếu có lỗi hoàn voucher
            }
        }

        order.status = 'đã hủy';
        if (reason && typeof reason === 'string') {
            order.cancellationReason = reason.trim();
        }

        order.statusHistory = order.statusHistory || [];
        order.statusHistory.push({
            status: 'đã hủy',
            timestamp: new Date(),
            note: reason ? reason.trim() : 'Khách hàng hủy đơn'
        });

        await order.save();

        res.json(order);

    } catch (err) {
        res.status(500).json({ message: err.message });
    }
};
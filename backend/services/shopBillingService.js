const Order = require('../models/Order');
const User = require('../models/User');
const Notification = require('../models/Notification');
const { roundCurrency } = require('../config/shopBillingPolicy');

const createSystemNotification = async (userId, title, message, data = {}) => {
  await Notification.create({
    user: userId,
    type: 'system',
    title,
    message,
    data,
  });
};

const buildSellingRestrictionMessage = (summary) => {
  if (!summary?.outstandingAmount) {
    return 'Tài khoản shop đang bị đóng băng do công nợ phí nền tảng chưa được xử lý.';
  }

  return `Tài khoản shop đang bị đóng băng. Công nợ hiện tại là ${summary.outstandingAmount.toLocaleString('vi-VN')}đ. Vui lòng nạp ví để hệ thống tự khấu trừ.`;
};

const ensureWalletShape = (shop) => {
  if (!shop.shopWallet) {
    shop.shopWallet = { balance: 0 };
  }

  if (typeof shop.shopWallet.balance !== 'number') {
    shop.shopWallet.balance = Number(shop.shopWallet.balance) || 0;
  }
};

const syncShopBilling = async (shopId, options = {}) => {
  const { now = new Date(), createNotifications = true } = options;

  const shop = await User.findById(shopId);
  if (!shop || shop.role !== 'shop') {
    return null;
  }

  ensureWalletShape(shop);

  const orders = await Order.find({
    'payment.status': 'paid',
    'items.shopId': shopId,
  }).sort({ 'payment.paidAt': 1, createdAt: 1 });
  const dueItems = [];
  const unpaidItems = [];
  const paidItems = [];
  let walletBalance = Number(shop.shopWallet.balance) || 0;

  for (const order of orders) {
    let orderChanged = false;

    for (const item of order.items) {
      if (item.shopId.toString() !== shopId.toString()) continue;
      if (!item.platformFee?.eligible) continue;

      const feeAmount = roundCurrency(item.platformFee?.feeAmount || 0);
      if (feeAmount <= 0) continue;

      const currentStatus = item.platformFee?.status || 'pending';
      if (currentStatus === 'paid') {
        paidItems.push({ order, item, feeAmount });
        continue;
      }

      dueItems.push({ order, item, feeAmount });

      if (walletBalance >= feeAmount) {
        walletBalance = roundCurrency(walletBalance - feeAmount);
        item.platformFee.status = 'paid';
        item.platformFee.chargedAt = now;
        item.platformFee.notifiedAt = now;
        orderChanged = true;
        paidItems.push({ order, item, feeAmount });

        if (createNotifications) {
          await createSystemNotification(
            shop._id,
            'Đã trừ phí sàn tự động',
            `Đơn ${order.orderNumber} của sản phẩm ${item.name} đã bị trừ ${feeAmount.toLocaleString('vi-VN')}đ phí sàn từ ví shop.`,
            { orderId: order._id, orderNumber: order.orderNumber, productId: item.product, feeAmount, type: 'platform_fee_paid' }
          );
        }
      } else {
        const firstUnpaidNotice = !item.platformFee.notifiedAt;
        item.platformFee.status = 'unpaid';
        item.platformFee.notifiedAt = item.platformFee.notifiedAt || now;
        orderChanged = true;
        unpaidItems.push({ order, item, feeAmount });

        if (createNotifications && firstUnpaidNotice) {
          await createSystemNotification(
            shop._id,
            'Thiếu số dư để thanh toán phí sàn',
            `Đơn ${order.orderNumber} của sản phẩm ${item.name} phát sinh ${feeAmount.toLocaleString('vi-VN')}đ phí sàn chưa thanh toán. Shop sẽ bị đóng băng cho tới khi công nợ được xử lý.`,
            { orderId: order._id, orderNumber: order.orderNumber, productId: item.product, feeAmount, type: 'platform_fee_unpaid' }
          );
        }
      }
    }

    if (orderChanged) {
      await order.save();
    }
  }

  const outstandingAmount = roundCurrency(
    unpaidItems.reduce((sum, item) => sum + item.feeAmount, 0)
  );

  const shouldFreeze = outstandingAmount > 0;
  const wasFrozen = shop.shopStatus === 'frozen';

  shop.shopWallet.balance = walletBalance;
  shop.shopWallet.updatedAt = now;
  shop.shopWallet.lastAutoChargeAt = dueItems.length ? now : shop.shopWallet.lastAutoChargeAt;
  shop.shopBillingSummary = {
    outstandingAmount,
    dueProductCount: unpaidItems.length,
    paidProductCount: paidItems.length,
    lastSyncedAt: now,
  };

  if (shouldFreeze) {
    shop.shopStatus = 'frozen';
    shop.shopStatusReason = 'unpaid_platform_fee';
    shop.shopFrozenAt = shop.shopFrozenAt || now;
  } else if (wasFrozen && shop.shopStatusReason === 'unpaid_platform_fee') {
    shop.shopStatus = 'active';
    shop.shopStatusReason = '';
    shop.shopFrozenAt = null;
  }

  await shop.save();

  if (createNotifications && shouldFreeze && !wasFrozen) {
    await createSystemNotification(
      shop._id,
      'Tài khoản shop đã bị đóng băng bán hàng',
      `Shop bị đóng băng do còn nợ ${outstandingAmount.toLocaleString('vi-VN')}đ phí nền tảng. Sau khi nạp ví đủ số dư, hệ thống sẽ tự mở lại.`,
      { outstandingAmount, type: 'shop_frozen' }
    );
  }

  if (createNotifications && !shouldFreeze && wasFrozen && shop.shopStatus === 'active') {
    await createSystemNotification(
      shop._id,
      'Tài khoản shop đã được mở bán lại',
      'Tất cả phí nền tảng đã được thanh toán. Shop có thể tiếp tục đăng bán sản phẩm.',
      { type: 'shop_unfrozen' }
    );
  }

  return {
    shop,
    summary: {
      walletBalance: roundCurrency(walletBalance),
      outstandingAmount,
      dueProductCount: unpaidItems.length,
      paidProductCount: paidItems.length,
      chargeableProductCount: dueItems.length,
      status: shop.shopStatus,
      statusReason: shop.shopStatusReason || '',
      isFrozen: shop.shopStatus === 'frozen',
      message: shop.shopStatus === 'frozen' ? buildSellingRestrictionMessage({ outstandingAmount }) : '',
    },
  };
};

const topUpShopWallet = async (shopId, amount) => {
  const topUpAmount = roundCurrency(amount);
  if (topUpAmount <= 0) {
    throw new Error('Số tiền nạp phải lớn hơn 0');
  }

  const shop = await User.findById(shopId);
  if (!shop || shop.role !== 'shop') {
    throw new Error('Shop not found');
  }

  ensureWalletShape(shop);
  shop.shopWallet.balance = roundCurrency((shop.shopWallet.balance || 0) + topUpAmount);
  shop.shopWallet.lastTopUpAt = new Date();
  shop.shopWallet.updatedAt = new Date();
  await shop.save();

  await createSystemNotification(
    shop._id,
    'Ví shop vừa được nạp tiền',
    `Hệ thống đã cộng ${topUpAmount.toLocaleString('vi-VN')}đ vào ví shop của bạn.`,
    { amount: topUpAmount, type: 'wallet_topup' }
  );

  return syncShopBilling(shopId, { createNotifications: true });
};

const getFrozenShopIds = async () => {
  const shops = await User.find({ role: 'shop', shopStatus: 'frozen' }).select('_id');
  return shops.map((shop) => shop._id);
};

module.exports = {
  syncShopBilling,
  topUpShopWallet,
  getFrozenShopIds,
};
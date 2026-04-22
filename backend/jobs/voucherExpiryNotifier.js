// Job: Thông báo voucher sắp hết hạn cho user
const mongoose = require('mongoose');
const Voucher = require('../models/Voucher');
const Notification = require('../models/Notification');
const User = require('../models/User');

// Hàm kiểm tra và gửi notification cho user có voucher sắp hết hạn (trong 3 ngày tới)
async function notifyExpiringVouchers() {
  const now = new Date();
  const soon = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000); // 3 ngày tới
  // Lấy các voucher sắp hết hạn
  const expiringVouchers = await Voucher.find({ expiresAt: { $gte: now, $lte: soon } });
  for (const voucher of expiringVouchers) {
    // Lấy user đã nhận voucher này (giả sử có trường users hoặc logic phù hợp)
    if (!voucher.users || !Array.isArray(voucher.users)) continue;
    for (const userId of voucher.users) {
      // Kiểm tra đã có notification chưa (tránh gửi trùng)
      const exists = await Notification.findOne({ user: userId, 'data.voucherId': voucher._id, type: 'voucher', message: /sắp hết hạn/ });
      if (!exists) {
        await Notification.create({
          user: userId,
          type: 'voucher',
          title: 'Voucher sắp hết hạn',
          message: `Voucher "${voucher.code}" của bạn sẽ hết hạn vào ${voucher.expiresAt.toLocaleDateString('vi-VN')}. Nhanh tay sử dụng nhé!`,
          data: { voucherId: voucher._id, code: voucher.code },
          isRead: false,
          expiresAt: voucher.expiresAt
        });
      }
    }
  }
}

// Để chạy định kỳ, có thể dùng node-cron hoặc gọi hàm này từ script/server
module.exports = notifyExpiringVouchers;
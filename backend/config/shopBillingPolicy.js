const DAY_IN_MS = 24 * 60 * 60 * 1000;

// Ngày bắt đầu tính phí: 13/4/2026 18:02:24
const SHOP_BILLING_POLICY = {
  version: '2026-05-14',
  feeStartDate: new Date('2026-04-13T18:02:24Z'), // Từ ngày này trở đi tính phí 5%
  commissionRate: 0.05,
  title: 'Chính sách phí nền tảng',
  summary: 'Phí nền tảng: 5% trên giá bán sản phẩm khi khách hàng thanh toán thành công. Áp dụng từ 13/4/2026 18:02:24.',
  details: [
    'Phí sàn 5% được tính trên giá bán sản phẩm',
    'Phí chỉ được tính khi đơn hàng được thanh toán thành công',
    'Áp dụng cho tất cả đơn hàng từ 13/4/2026 18:02:24 trở đi',
    'Hệ thống tự động trừ phí từ ví shop của bạn',
  ],
};

const roundCurrency = (value) => Math.round((Number(value) || 0) * 100) / 100;

/**
 * Kiểm tra xem order có phát sinh từ ngày tính phí hay không
 */
const isOrderInFeeablePeriod = (paidAt) => {
  if (!paidAt) return false;
  const paidDate = new Date(paidAt);
  return paidDate >= SHOP_BILLING_POLICY.feeStartDate;
};

module.exports = {
  SHOP_BILLING_POLICY,
  roundCurrency,
  isOrderInFeeablePeriod,
};
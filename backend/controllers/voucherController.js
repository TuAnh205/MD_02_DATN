const Voucher = require('../models/Voucher');

// GET /api/vouchers - Public: Get all active vouchers for users
exports.getActiveVouchers = async (req, res) => {
  try {
    const now = new Date();
    const vouchers = await Voucher.find({
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    }).select('-createdBy -usageLimit -usedCount -userLimit -applicableProducts -applicableCategories -__v -updatedAt -createdAt');
    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

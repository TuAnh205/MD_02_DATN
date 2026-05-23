const Voucher = require('../models/Voucher');
const User = require('../models/User');

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

exports.getUserVouchers = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).populate('userVouchers.voucher');
    if (!user) return res.status(404).json({ message: 'User not found' });

    const vouchers = (user.userVouchers || []).map((entry) => ({
      _id: entry._id,
      voucherId: entry.voucher?._id,
      code: entry.code,
      name: entry.name,
      description: entry.description,
      claimedAt: entry.claimedAt,
      usedCount: entry.usedCount,
      isExpired: !!entry.voucher && entry.voucher.endDate && entry.voucher.endDate < new Date(),
      isActive: !!entry.voucher && entry.voucher.isActive,
      startDate: entry.voucher?.startDate,
      endDate: entry.voucher?.endDate,
      type: entry.voucher?.type,
      value: entry.voucher?.value,
      minOrderValue: entry.voucher?.minOrderValue,
      maxDiscount: entry.voucher?.maxDiscount
    }));

    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

exports.claimVoucher = async (req, res) => {
  try {
    const { code } = req.body;
    if (!code) {
      return res.status(400).json({ message: 'Voucher code is required' });
    }

    const now = new Date();
    const voucher = await Voucher.findOne({
      code: code.toUpperCase(),
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    });

    if (!voucher) {
      return res.status(404).json({ message: 'Voucher không hợp lệ hoặc đã hết hạn' });
    }

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    const existing = (user.userVouchers || []).find((entry) => entry.code === voucher.code);
    if (existing) {
      return res.status(400).json({ message: 'Bạn đã nhận voucher này rồi' });
    }

    user.userVouchers = user.userVouchers || [];
    user.userVouchers.push({
      voucher: voucher._id,
      code: voucher.code,
      name: voucher.name,
      description: voucher.description,
      claimedAt: now,
      usedCount: 0
    });

    await user.save();

    res.status(201).json({
      message: 'Đã nhận voucher thành công',
      voucher: {
        _id: voucher._id,
        code: voucher.code,
        name: voucher.name,
        description: voucher.description,
        type: voucher.type,
        value: voucher.value,
        minOrderValue: voucher.minOrderValue,
        maxDiscount: voucher.maxDiscount,
        startDate: voucher.startDate,
        endDate: voucher.endDate
      }
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

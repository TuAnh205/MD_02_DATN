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

// GET /api/vouchers/my - Authenticated: Get vouchers claimed by the current user
exports.getMyVouchers = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).populate({
      path: 'userVouchers.voucher',
      select: 'code name description type value minOrderValue maxDiscount startDate endDate endDate isActive'
    });
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    const vouchers = user.userVouchers
      .filter((entry) => entry.voucher)
      .map((entry) => ({
        ...entry.voucher.toObject(),
        claimedAt: entry.claimedAt,
        usedCount: entry.usedCount,
      }));

    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

// POST /api/vouchers/claim - Authenticated: Claim a voucher by code
exports.claimVoucher = async (req, res) => {
  try {
    const { code } = req.body;
    if (!code) {
      return res.status(400).json({ message: 'Voucher code is required' });
    }

    const upperCode = String(code).trim().toUpperCase();
    const now = new Date();
    const voucher = await Voucher.findOne({
      code: upperCode,
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now }
    });

    if (!voucher) {
      return res.status(404).json({ message: 'Voucher không tồn tại hoặc đã hết hạn' });
    }

    const user = await User.findById(req.user.id).populate('userVouchers.voucher');
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    const alreadyClaimed = user.userVouchers.some(
      (entry) => entry.voucher && entry.voucher._id.equals(voucher._id)
    );
    if (alreadyClaimed) {
      return res.status(400).json({ message: 'Bạn đã nhận voucher này rồi' });
    }

    const claimedCount = user.userVouchers.filter(
      (entry) => entry.voucher && entry.voucher._id.equals(voucher._id)
    ).length;
    if (voucher.userLimit && claimedCount >= voucher.userLimit) {
      return res.status(400).json({ message: 'Bạn không thể nhận voucher này thêm lần nữa' });
    }

    user.userVouchers.push({ voucher: voucher._id, claimedAt: new Date() });
    await user.save();

    res.json({ voucher });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

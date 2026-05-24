const Voucher = require("../models/Voucher");
const User = require("../models/User");

// GET /api/vouchers - Public: Get all active vouchers for users from all shops
exports.getActiveVouchers = async (req, res) => {
  try {
    const now = new Date();
    const vouchers = await Voucher.find({
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now },
    })
      .populate("shop", "name _id")
      .select(
        "-createdBy -usageLimit -usedCount -userLimit -applicableProducts -applicableCategories -__v -updatedAt -createdAt",
      );
    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// GET /api/vouchers/my - Authenticated: Get vouchers claimed by the current user
exports.getMyVouchers = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).populate({
      path: "userVouchers.voucher",
      populate: {
        path: "shop",
        select: "name _id",
      },
      select:
        "code name description type value minOrderValue maxDiscount startDate endDate isActive shop",
    });
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    // Deduplicate by voucher ID to prevent duplicate entries
    const voucherMap = new Map();
    user.userVouchers.forEach((entry) => {
      if (entry.voucher && entry.voucher._id) {
        const voucherId = entry.voucher._id.toString();
        if (!voucherMap.has(voucherId)) {
          voucherMap.set(voucherId, {
            ...entry.voucher.toObject(),
            claimedAt: entry.claimedAt,
            usedCount: entry.usedCount,
            isConsumed: entry.isConsumed || false,
          });
        }
      }
    });
    const vouchers = Array.from(voucherMap.values());

    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// POST /api/vouchers/claim - Authenticated: Claim a voucher by code
exports.claimVoucher = async (req, res) => {
  try {
    const { code } = req.body;
    if (!code) {
      return res.status(400).json({ message: "Voucher code is required" });
    }

    const upperCode = String(code).trim().toUpperCase();
    const now = new Date();
    const voucher = await Voucher.findOne({
      code: upperCode,
      isActive: true,
      startDate: { $lte: now },
      endDate: { $gte: now },
    });

    if (!voucher) {
      return res
        .status(404)
        .json({ message: "Voucher không tồn tại hoặc đã hết hạn" });
    }

    const user = await User.findById(req.user.id).populate(
      "userVouchers.voucher",
    );
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const alreadyClaimed = user.userVouchers.some(
      (entry) => entry.voucher && entry.voucher._id.equals(voucher._id),
    );
    if (alreadyClaimed) {
      return res.status(400).json({ message: "Bạn đã nhận voucher này rồi" });
    }

    const claimedCount = user.userVouchers.filter(
      (entry) => entry.voucher && entry.voucher._id.equals(voucher._id),
    ).length;
    if (voucher.userLimit && claimedCount >= voucher.userLimit) {
      return res
        .status(400)
        .json({ message: "Bạn không thể nhận voucher này thêm lần nữa" });
    }

    user.userVouchers.push({
      voucher: voucher._id,
      code: voucher.code,
      name: voucher.name,
      description: voucher.description,
      claimedAt: new Date(),
      usedCount: 0,
    });
    await user.save();

    res.json({ voucher });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// GET /api/vouchers/shop/:shopId - Get all vouchers created by a shop (with shop info)
exports.getShopVouchers = async (req, res) => {
  try {
    const { shopId } = req.params;
    const vouchers = await Voucher.find({ shop: shopId, isActive: true })
      .populate("shop", "name _id")
      .select(
        "code name description type value minOrderValue maxDiscount startDate endDate shop",
      );
    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// POST /api/vouchers/shop - Shop: Create a new voucher
exports.createShopVoucher = async (req, res) => {
  try {
    const {
      code,
      name,
      description,
      type,
      value,
      minOrderValue,
      maxDiscount,
      usageLimit,
      userLimit,
      startDate,
      endDate,
    } = req.body;

    if (!code || !name || !type || !value || !startDate || !endDate) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    const upperCode = String(code).trim().toUpperCase();
    const existingVoucher = await Voucher.findOne({ code: upperCode });
    if (existingVoucher) {
      return res.status(400).json({ message: "Mã voucher này đã tồn tại" });
    }

    const voucher = new Voucher({
      code: upperCode,
      name,
      description,
      type,
      value,
      minOrderValue: minOrderValue || 0,
      maxDiscount,
      usageLimit: usageLimit || null,
      userLimit: userLimit || 1,
      startDate: new Date(startDate),
      endDate: new Date(endDate),
      isActive: true,
      shop: req.user.id,
      createdBy: req.user.id,
    });

    await voucher.save();
    res.status(201).json({ message: "Voucher created successfully", voucher });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// GET /api/vouchers/shop/my - Shop: Get my created vouchers
exports.getMyCreatedVouchers = async (req, res) => {
  try {
    const vouchers = await Voucher.find({ shop: req.user.id })
      .populate("shop", "name _id")
      .select(
        "code name description type value minOrderValue maxDiscount usageLimit usedCount userLimit startDate endDate isActive shop",
      );
    res.json({ vouchers });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// PUT /api/vouchers/shop/:voucherId - Shop: Update a voucher
exports.updateShopVoucher = async (req, res) => {
  try {
    const { voucherId } = req.params;
    const {
      name,
      description,
      type,
      value,
      minOrderValue,
      maxDiscount,
      usageLimit,
      userLimit,
      startDate,
      endDate,
      isActive,
    } = req.body;

    const voucher = await Voucher.findById(voucherId);
    if (!voucher) {
      return res.status(404).json({ message: "Voucher not found" });
    }

    if (!voucher.shop.equals(req.user.id)) {
      return res.status(403).json({ message: "Unauthorized" });
    }

    if (name) voucher.name = name;
    if (description) voucher.description = description;
    if (type) voucher.type = type;
    if (value !== undefined) voucher.value = value;
    if (minOrderValue !== undefined) voucher.minOrderValue = minOrderValue;
    if (maxDiscount !== undefined) voucher.maxDiscount = maxDiscount;
    if (usageLimit !== undefined) voucher.usageLimit = usageLimit;
    if (userLimit !== undefined) voucher.userLimit = userLimit;
    if (startDate) voucher.startDate = new Date(startDate);
    if (endDate) voucher.endDate = new Date(endDate);
    if (isActive !== undefined) voucher.isActive = isActive;

    await voucher.save();
    res.json({ message: "Voucher updated successfully", voucher });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// DELETE /api/vouchers/shop/:voucherId - Shop: Delete a voucher
exports.deleteShopVoucher = async (req, res) => {
  try {
    const { voucherId } = req.params;
    const voucher = await Voucher.findById(voucherId);

    if (!voucher) {
      return res.status(404).json({ message: "Voucher not found" });
    }

    if (!voucher.shop.equals(req.user.id)) {
      return res.status(403).json({ message: "Unauthorized" });
    }

    await Voucher.findByIdAndDelete(voucherId);
    res.json({ message: "Voucher deleted successfully" });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

const User = require('../models/User');

// Lấy danh sách địa chỉ của user
exports.getAddresses = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json({ addresses: user.addresses });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Thêm địa chỉ mới
exports.addAddress = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ message: 'User not found' });

    // Nếu là mặc định thì bỏ mặc định các địa chỉ khác
    if (req.body.isDefault) {
      user.addresses.forEach(addr => addr.isDefault = false);
    }

    user.addresses.push(req.body);
    await user.save();
    res.json({ addresses: user.addresses });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Sửa địa chỉ
exports.updateAddress = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ message: 'User not found' });

    const addr = user.addresses.id(req.params.addressId);
    if (!addr) return res.status(404).json({ message: 'Address not found' });

    if (req.body.isDefault) {
      user.addresses.forEach(a => a.isDefault = false);
    }

    Object.assign(addr, req.body);
    await user.save();
    res.json({ addresses: user.addresses });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// Xóa địa chỉ
exports.deleteAddress = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ message: 'User not found' });

    user.addresses.id(req.params.addressId).remove();
    await user.save();
    res.json({ addresses: user.addresses });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};
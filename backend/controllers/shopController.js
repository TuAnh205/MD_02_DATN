const mongoose = require('mongoose');
const Product = require('../models/Product');
const User = require('../models/User');
const Order = require('../models/Order');
const Review = require('../models/Review');
const Notification = require('../models/Notification');

// ================= GET SHOP PRODUCTS =================
exports.getShopProducts = async (req, res) => {
  try {
    const shopId = req.user.id;
    const products = await Product.find({ shopId });
    res.json(products);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= CREATE PRODUCT =================
exports.createProduct = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productData = {
      ...req.body,
      shopId,
      createdBy: req.user.id // Phải có createdBy vì model bắt buộc
    };
    const product = new Product(productData);
    await product.save();
    res.status(201).json(product);
  } catch (err) {
    if (err.name === 'ValidationError') {
      return res.status(400).json({ message: 'Định dạng dữ liệu không hợp lệ', details: err.message });
    }
    if (err.code === 11000) {
      return res.status(400).json({ message: 'Sản phẩm đã tồn tại', details: err.keyValue });
    }
    res.status(500).json({ message: 'Lỗi khi tạo sản phẩm', error: err.message });
  }
};

// ================= UPDATE PRODUCT =================
exports.updateProduct = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productId = req.params.id;
    const product = await Product.findOneAndUpdate(
      { _id: productId, shopId },
      req.body,
      { new: true }
    );
    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }
    res.json(product);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= DELETE PRODUCT =================
exports.deleteProduct = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productId = req.params.id;
    const product = await Product.findOneAndDelete({ _id: productId, shopId });
    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }
    res.json({ message: 'Product deleted' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= GET SHOP ORDERS =================
exports.getShopOrders = async (req, res) => {
  try {
    const shopId = req.user.id;

    const orders = await Order.find({
      'items.shopId': shopId,
      status: { $ne: 'cancelled' }
    })
      .populate('user', 'name email')
      .populate('items.product', 'name price')
      .sort({ createdAt: -1 });

    res.json(orders);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= UPDATE SHOP ORDER STATUS =================
exports.updateShopOrderStatus = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { id } = req.params;
    const { status } = req.body;

    // Map English values from frontend → Vietnamese enum stored in DB
    const statusMap = {
      pending: 'chờ xác nhận',
      confirmed: 'đã xác nhận',
      shipped: 'đang giao',
      delivered: 'đã nhận',
      cancelled: 'đã hủy',
    };

    // Accept both English (legacy) and Vietnamese directly
    const mappedStatus = statusMap[status] || status;

    const validStatuses = ['chờ xác nhận', 'đã xác nhận', 'đang giao', 'đã nhận', 'đã hủy'];
    if (!validStatuses.includes(mappedStatus)) {
      return res.status(400).json({ message: 'Trạng thái không hợp lệ' });
    }

    // Chỉ cho phép shop cập nhật đơn hàng có sản phẩm của shop đó
    const order = await Order.findOne({ _id: id, 'items.shopId': new mongoose.Types.ObjectId(shopId) });
    if (!order) {
      return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    }

    order.status = mappedStatus;
    await order.save();

    // Nếu trạng thái là "đã xác nhận" thì tạo notification cho user
    if (mappedStatus === 'đã xác nhận') {
      try {
        await Notification.create({
          user: order.user, // user nhận thông báo
          type: 'order_status',
          title: 'Đơn hàng đã được xác nhận',
          message: `Đơn hàng #${order.orderNumber} của bạn đã được shop xác nhận.`,
          data: { orderId: order._id, orderNumber: order.orderNumber },
          isRead: false
        });
      } catch (notiErr) {
        console.error('Lỗi tạo notification cho user:', notiErr);
      }
    }

    res.json({ message: 'Cập nhật trạng thái thành công', order });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= SHOP NOTIFICATIONS =================
exports.getShopNotifications = async (req, res) => {
  try {
    const shopId = req.user.id;
    const notifications = await Notification.find({ user: shopId })
      .sort({ createdAt: -1 })
      .limit(50);
    res.json(notifications);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

exports.markNotificationRead = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { id } = req.params;

    const notification = await Notification.findOneAndUpdate(
      { _id: id, user: shopId },
      { isRead: true, readAt: new Date() },
      { new: true }
    );
    if (!notification) return res.status(404).json({ message: 'Notification not found' });
    res.json(notification);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

exports.markAllNotificationsRead = async (req, res) => {
  try {
    const shopId = req.user.id;
    await Notification.updateMany({ user: shopId, isRead: false }, { isRead: true, readAt: new Date() });
    res.json({ message: 'All notifications marked read' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= GET SHOP REVIEWS =================
exports.getShopReviews = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productId = req.query.productId;

    const productFilter = { shopId };
    if (productId) {
      productFilter._id = productId;
    }

    const shopProducts = await Product.find(productFilter).select('_id');
    const productIds = shopProducts.map((p) => p._id);

    const reviews = await Review.find({ product: { $in: productIds } })
      .populate('user', 'name email')
      .populate('product', 'name')
      .populate('response.respondedBy', 'name')
      .sort({ createdAt: -1 });

    res.json(reviews);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

// ================= REPLY SHOP REVIEW =================
exports.replyShopReview = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { id } = req.params;
    const { text } = req.body;

    if (!text || text.trim() === '') {
      return res.status(400).json({ message: 'Reply text is required' });
    }

    const review = await Review.findById(id).populate('product', 'shopId');
    if (!review) return res.status(404).json({ message: 'Review not found' });

    if (!review.product || review.product.shopId.toString() !== shopId) {
      return res.status(403).json({ message: 'Forbidden: review not belong to your shop' });
    }

    review.response = {
      text: text.trim(),
      respondedAt: new Date(),
      respondedBy: shopId
    };

    await review.save();
    await review.populate('response.respondedBy', 'name');

    res.json({ review });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

// ================= GET SHOP REVENUE =================
exports.getShopRevenue = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { period = 'month' } = req.query;

    const now = new Date();
    let startDate;

    switch (period) {
      case 'day':
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        break;
      case 'week':
        const weekStart = now.getDate() - now.getDay();
        startDate = new Date(now.getFullYear(), now.getMonth(), weekStart);
        break;
      case 'month':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1);
        break;
      case 'year':
        startDate = new Date(now.getFullYear(), 0, 1);
        break;
      default:
        startDate = new Date(now.getFullYear(), now.getMonth(), 1);
    }

    const shopObjectId = new mongoose.Types.ObjectId(shopId);

    const revenueData = await Order.aggregate([
      {
        $match: {
          'payment.status': 'paid',
          createdAt: { $gte: startDate }
        }
      },
      {
        $unwind: '$items'
      },
      {
        $match: {
          'items.shopId': shopObjectId
        }
      },
      {
        $group: {
          _id: null,
          totalRevenue: { $sum: { $multiply: ['$items.price', '$items.qty'] } },
          totalOrders: { $addToSet: '$_id' },
          totalProducts: { $sum: '$items.qty' }
        }
      }
    ]);

    const stats = revenueData[0] || { totalRevenue: 0, totalOrders: [], totalProducts: 0 };

    res.json({
      period,
      startDate,
      totalRevenue: stats.totalRevenue,
      totalOrders: stats.totalOrders.length,
      totalProducts: stats.totalProducts
    });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};
const User = require('../models/User');
const Product = require('../models/Product');
const Order = require('../models/Order');
const Review = require('../models/Review');
const Feedback = require('../models/Feedback');
const Post = require('../models/Post');
const Voucher = require('../models/Voucher');
const {
  SHOP_BILLING_POLICY,
  roundCurrency,
} = require('../config/shopBillingPolicy');

const buildDateRange = (yearNum, monthNum) => {
  const start = monthNum
    ? new Date(yearNum, monthNum - 1, 1)
    : new Date(yearNum, 0, 1);
  const end = monthNum
    ? new Date(yearNum, monthNum, 1)
    : new Date(yearNum + 1, 0, 1);

  return { start, end };
};

// User management
exports.getUsers = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const users = await User.find()
      .select('-password')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await User.countDocuments();

    res.json({
      users,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getUserCount = async (req, res) => {
  try {
    const count = await User.countDocuments();
    res.json({ count });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updateUserRole = async (req, res) => {
  try {
    const { id } = req.params;
    const { role } = req.body;

    if (!['user', 'shop', 'admin'].includes(role)) {
      return res.status(400).json({ message: 'Invalid role' });
    }

    const user = await User.findByIdAndUpdate(
      id,
      { role },
      { new: true }
    ).select('-password');

    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    res.json({ user });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deleteUser = async (req, res) => {
  try {
    const { id } = req.params;
    const user = await User.findByIdAndDelete(id);

    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    res.json({ message: 'User deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Shop management
exports.getShops = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const shops = await User.find({ role: 'shop' })
      .select('-password')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await User.countDocuments({ role: 'shop' });

    res.json({
      shops,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getShopCount = async (req, res) => {
  try {
    const count = await User.countDocuments({ role: 'shop' });
    res.json({ count });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updateShopStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { isActive } = req.body;

    const shop = await User.findByIdAndUpdate(
      id,
      { isActive },
      { new: true }
    ).select('-password');

    if (!shop) {
      return res.status(404).json({ message: 'Shop not found' });
    }

    res.json({ shop });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Product management
exports.getProducts = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const products = await Product.find()
      .populate('category')
      .populate('createdBy', 'name email')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Product.countDocuments();

    res.json({
      products,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getProductCount = async (req, res) => {
  try {
    const count = await Product.countDocuments();
    res.json({ count });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.createProduct = async (req, res) => {
  try {
    const productData = { ...req.body, createdBy: req.user.id };
    const product = new Product(productData);
    await product.save();
    res.status(201).json({ product });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updateProduct = async (req, res) => {
  try {
    const { id } = req.params;
    const product = await Product.findByIdAndUpdate(id, req.body, { new: true });
    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }
    res.json({ product });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deleteProduct = async (req, res) => {
  try {
    const { id } = req.params;
    const product = await Product.findByIdAndDelete(id);
    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }
    res.json({ message: 'Product deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Order management
exports.getOrders = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    // Map English filter values to Vietnamese status stored in DB
    const statusMap = {
      pending: 'ch\u1edd x\u00e1c nh\u1eadn',
      confirmed: '\u0111\u00e3 x\u00e1c nh\u1eadn',
      shipped: '\u0111ang giao',
      delivered: '\u0111\u00e3 nh\u1eadn',
      cancelled: '\u0111\u00e3 h\u1ee7y',
    };

    const filter = {};
    if (req.query.status && req.query.status !== 'all') {
      filter.status = statusMap[req.query.status] || req.query.status;
    }

    const orders = await Order.find(filter)
      .populate('user', 'name email role')
      .populate('items.product', 'name price')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Order.countDocuments(filter);

    res.json({
      orders,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getOrderCount = async (req, res) => {
  try {
    const count = await Order.countDocuments();
    res.json({ count });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updateOrderStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const validStatuses = ['pending', 'confirmed', 'shipped', 'delivered', 'cancelled'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ message: 'Invalid status' });
    }

    const order = await Order.findByIdAndUpdate(
      id,
      { status },
      { new: true }
    ).populate('user', 'name email');

    if (!order) {
      return res.status(404).json({ message: 'Order not found' });
    }

    res.json({ order });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Review management
exports.getReviews = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const reviews = await Review.find()
      .populate('user', 'name')
      .populate('product', 'name')
      .populate('response.respondedBy', 'name')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Review.countDocuments();

    res.json({
      reviews,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getReviewCount = async (req, res) => {
  try {
    const count = await Review.countDocuments();
    res.json({ count });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deleteReview = async (req, res) => {
  try {
    const { id } = req.params;
    const review = await Review.findByIdAndDelete(id);
    if (!review) {
      return res.status(404).json({ message: 'Review not found' });
    }
    res.json({ message: 'Review deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.replyReview = async (req, res) => {
  try {
    const { id } = req.params;
    const { text } = req.body;

    if (!text || text.trim() === '') {
      return res.status(400).json({ message: 'Reply text is required' });
    }

    const review = await Review.findById(id);
    if (!review) {
      return res.status(404).json({ message: 'Review not found' });
    }

    review.response = {
      text: text.trim(),
      respondedAt: new Date(),
      respondedBy: req.user.id
    };

    await review.save();
    await review.populate('response.respondedBy', 'name');

    res.json({ review });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Feedback management
exports.getFeedbacks = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const feedbacks = await Feedback.find()
      .populate('user', 'name email')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Feedback.countDocuments();

    res.json({
      feedbacks,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deleteFeedback = async (req, res) => {
  try {
    const { id } = req.params;
    const feedback = await Feedback.findByIdAndDelete(id);
    if (!feedback) {
      return res.status(404).json({ message: 'Feedback not found' });
    }
    res.json({ message: 'Feedback deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Post management
exports.getPosts = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const posts = await Post.find()
      .populate('author', 'name')
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Post.countDocuments();

    res.json({
      posts,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.createPost = async (req, res) => {
  try {
    const postData = {
      ...req.body,
      author: req.user.id,
      tags: req.body.tags ? req.body.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : []
    };
    const post = new Post(postData);
    await post.save();
    await post.populate('author', 'name');
    res.status(201).json({ post });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updatePost = async (req, res) => {
  try {
    const { id } = req.params;
    const postData = {
      ...req.body,
      tags: req.body.tags ? req.body.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : []
    };
    const post = await Post.findByIdAndUpdate(id, postData, { new: true }).populate('author', 'name');
    if (!post) {
      return res.status(404).json({ message: 'Post not found' });
    }
    res.json({ post });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deletePost = async (req, res) => {
  try {
    const { id } = req.params;
    const post = await Post.findByIdAndDelete(id);
    if (!post) {
      return res.status(404).json({ message: 'Post not found' });
    }
    res.json({ message: 'Post deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Voucher management
exports.getVouchers = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const vouchers = await Voucher.find()
      .skip(skip)
      .limit(limit)
      .sort({ createdAt: -1 });

    const total = await Voucher.countDocuments();

    res.json({
      vouchers,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.createVoucher = async (req, res) => {
  try {
    const voucher = new Voucher(req.body);
    await voucher.save();
    res.status(201).json({ voucher });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.updateVoucher = async (req, res) => {
  try {
    const { id } = req.params;
    const voucher = await Voucher.findByIdAndUpdate(id, req.body, { new: true });
    if (!voucher) {
      return res.status(404).json({ message: 'Voucher not found' });
    }
    res.json({ voucher });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.deleteVoucher = async (req, res) => {
  try {
    const { id } = req.params;
    const voucher = await Voucher.findByIdAndDelete(id);
    if (!voucher) {
      return res.status(404).json({ message: 'Voucher not found' });
    }
    res.json({ message: 'Voucher deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// Revenue analytics
exports.getRevenue = async (req, res) => {
  try {
    const { year, month } = req.query;
    
    if (!year) {
      return res.status(400).json({ message: 'Year is required' });
    }

    const yearNum = parseInt(year);
    const monthNum = month ? parseInt(month) : null;

    // Find all paid orders
    const query = {
      'payment.status': 'paid'
    };

    // Get orders
    const orders = await Order.find(query).populate('items.product');
    
    let revenueData = [];
    let total = 0;

    if (monthNum) {
      // Group by day for a specific month
      const dailyRevenue = {};
      
      orders.forEach(order => {
        const orderDate = new Date(order.createdAt);
        if (orderDate.getFullYear() === yearNum && orderDate.getMonth() + 1 === monthNum) {
          const day = orderDate.getDate();
          if (!dailyRevenue[day]) {
            dailyRevenue[day] = 0;
          }
          dailyRevenue[day] += order.total || 0;
        }
      });

      // Get the number of days in the month
      const daysInMonth = new Date(yearNum, monthNum, 0).getDate();
      for (let day = 1; day <= daysInMonth; day++) {
        revenueData.push({
          day,
          month: monthNum,
          revenue: dailyRevenue[day] || 0
        });
        total += dailyRevenue[day] || 0;
      }
    } else {
      // Group by month for the year
      const monthlyRevenue = {};
      
      orders.forEach(order => {
        const orderDate = new Date(order.createdAt);
        if (orderDate.getFullYear() === yearNum) {
          const month = orderDate.getMonth() + 1;
          if (!monthlyRevenue[month]) {
            monthlyRevenue[month] = 0;
          }
          monthlyRevenue[month] += order.total || 0;
        }
      });

      // Include all 12 months
      for (let month = 1; month <= 12; month++) {
        revenueData.push({
          month,
          revenue: monthlyRevenue[month] || 0
        });
        total += monthlyRevenue[month] || 0;
      }
    }

    res.json({
      data: revenueData,
      total,
      year: yearNum,
      ...(monthNum && { month: monthNum })
    });
  } catch (error) {
    console.error('Error fetching revenue:', error);
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getRevenueByShop = async (req, res) => {
  try {
    const { year, month } = req.query;

    if (!year) {
      return res.status(400).json({ message: 'Year is required' });
    }

    const yearNum = parseInt(year);
    const monthNum = month ? parseInt(month) : null;

    const orders = await Order.find({ 'payment.status': 'paid' })
      .populate('items.shopId', 'name email');

    // Aggregate revenue per shop
    const shopMap = {};

    orders.forEach(order => {
      const orderDate = new Date(order.createdAt);
      const matchYear = orderDate.getFullYear() === yearNum;
      const matchMonth = monthNum ? orderDate.getMonth() + 1 === monthNum : true;
      if (!matchYear || !matchMonth) return;

      order.items.forEach(item => {
        const shopId = item.shopId?._id?.toString() || item.shopId?.toString();
        if (!shopId) return;
        if (!shopMap[shopId]) {
          shopMap[shopId] = {
            shopId,
            shopName: item.shopId?.name || 'Shop không xác định',
            shopEmail: item.shopId?.email || '',
            revenue: 0,
            orders: new Set(),
            itemsSold: 0
          };
        }
        shopMap[shopId].revenue += (item.price || 0) * (item.qty || 1);
        shopMap[shopId].orders.add(order._id.toString());
        shopMap[shopId].itemsSold += item.qty || 1;
      });
    });

    const shops = Object.values(shopMap)
      .map(s => ({ ...s, orderCount: s.orders.size, orders: undefined }))
      .sort((a, b) => b.revenue - a.revenue);

    const total = shops.reduce((sum, s) => sum + s.revenue, 0);

    res.json({ shops, total, year: yearNum, ...(monthNum && { month: monthNum }) });
  } catch (error) {
    console.error('Error fetching shop revenue:', error);
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getPlatformRevenue = async (req, res) => {
  try {
    const { year, month } = req.query;

    if (!year) {
      return res.status(400).json({ message: 'Year is required' });
    }

    const yearNum = parseInt(year, 10);
    const monthNum = month ? parseInt(month, 10) : null;
    const { start, end } = buildDateRange(yearNum, monthNum);

    const orders = await Order.find({ 'payment.status': 'paid' }).populate('items.shopId', 'name email');
    const chargeableProducts = [];

    orders.forEach((order) => {
      const paidAt = order.payment?.paidAt || order.createdAt;
      if (paidAt < start || paidAt >= end) {
        return;
      }

      order.items.forEach((item) => {
        if (!item.platformFee?.eligible) {
          return;
        }

        chargeableProducts.push({
          orderId: order._id,
          orderNumber: order.orderNumber,
          productId: item.product,
          productName: item.name,
          shopId: item.shopId?._id || item.shopId,
          shopName: item.shopId?.name || 'Shop không xác định',
          shopEmail: item.shopId?.email || '',
          price: Number(item.price) || 0,
          qty: item.qty || 1,
          baseAmount: roundCurrency(item.platformFee?.baseAmount || ((item.price || 0) * (item.qty || 1))),
          feeAmount: roundCurrency(item.platformFee?.feeAmount || 0),
          feeStartAt: paidAt,
          commissionRate: item.platformFee?.rate || SHOP_BILLING_POLICY.commissionRate,
          isFeeActive: item.platformFee?.status === 'paid',
          feeStatus: item.platformFee?.status || 'pending',
        });
      });
    });

    chargeableProducts.sort((a, b) => b.feeAmount - a.feeAmount);

    const totalFeeRevenue = roundCurrency(
      chargeableProducts.reduce((sum, product) => sum + product.feeAmount, 0)
    );

    const activeCount = chargeableProducts.filter((product) => product.feeStatus === 'paid').length;

    res.json({
      products: chargeableProducts,
      totalFeeRevenue,
      productCount: chargeableProducts.length,
      activeCount,
      policy: {
        version: SHOP_BILLING_POLICY.version,
        freeTrialDays: SHOP_BILLING_POLICY.freeTrialDays,
        commissionRate: SHOP_BILLING_POLICY.commissionRate,
      },
      year: yearNum,
      ...(monthNum && { month: monthNum }),
    });
  } catch (error) {
    console.error('Error fetching platform revenue:', error);
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getPlatformRevenueByShop = async (req, res) => {
  try {
    const { year, month } = req.query;

    if (!year) {
      return res.status(400).json({ message: 'Year is required' });
    }

    const yearNum = parseInt(year, 10);
    const monthNum = month ? parseInt(month, 10) : null;
    const { start, end } = buildDateRange(yearNum, monthNum);

    const orders = await Order.find({ 'payment.status': 'paid' }).populate('items.shopId', 'name email');
    const shopMap = new Map();

    orders.forEach((order) => {
      const paidAt = order.payment?.paidAt || order.createdAt;
      if (paidAt < start || paidAt >= end) {
        return;
      }

      order.items.forEach((item) => {
        if (!item.platformFee?.eligible) {
          return;
        }

        const shopId = item.shopId?._id?.toString() || item.shopId?.toString();
        if (!shopId) {
          return;
        }

        if (!shopMap.has(shopId)) {
          shopMap.set(shopId, {
            shopId,
            shopName: item.shopId?.name || 'Shop không xác định',
            shopEmail: item.shopId?.email || '',
            totalFeeRevenue: 0,
            productCount: 0,
            activeFeeProducts: 0,
            products: [],
          });
        }

        const shopEntry = shopMap.get(shopId);
        const feeAmount = roundCurrency(item.platformFee?.feeAmount || 0);
        shopEntry.totalFeeRevenue += feeAmount;
        shopEntry.productCount += item.qty || 1;
        if (item.platformFee?.status === 'paid') {
          shopEntry.activeFeeProducts += 1;
        }
        shopEntry.products.push({
          productId: item.product,
          orderId: order._id,
          orderNumber: order.orderNumber,
          productName: item.name,
          price: Number(item.price) || 0,
          qty: item.qty || 1,
          feeAmount,
          feeStartAt: paidAt,
          commissionRate: item.platformFee?.rate || SHOP_BILLING_POLICY.commissionRate,
        });
      });
    });

    const shops = Array.from(shopMap.values())
      .map((shop) => ({
        ...shop,
        totalFeeRevenue: roundCurrency(shop.totalFeeRevenue),
        products: shop.products.sort((a, b) => b.feeAmount - a.feeAmount),
      }))
      .sort((a, b) => b.totalFeeRevenue - a.totalFeeRevenue);

    const totalFeeRevenue = roundCurrency(
      shops.reduce((sum, shop) => sum + shop.totalFeeRevenue, 0)
    );

    res.json({
      shops,
      totalFeeRevenue,
      totalShops: shops.length,
      policy: {
        version: SHOP_BILLING_POLICY.version,
        freeTrialDays: SHOP_BILLING_POLICY.freeTrialDays,
        commissionRate: SHOP_BILLING_POLICY.commissionRate,
      },
      year: yearNum,
      ...(monthNum && { month: monthNum }),
    });
  } catch (error) {
    console.error('Error fetching platform revenue by shop:', error);
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};
const mongoose = require("mongoose");
const Product = require("../models/Product");
const User = require("../models/User");
const Order = require("../models/Order");
const Review = require("../models/Review");
const Notification = require("../models/Notification");
const {
  SHOP_BILLING_POLICY,
  roundCurrency,
} = require("../config/shopBillingPolicy");
const {
  syncShopBilling,
  topUpShopWallet,
} = require("../services/shopBillingService");

const getShopPolicyPayload = (user) => ({
  ...SHOP_BILLING_POLICY,
  shopStatus: user?.shopStatus || "active",
  walletBalance: roundCurrency(user?.shopWallet?.balance || 0),
  outstandingAmount: roundCurrency(
    user?.shopBillingSummary?.outstandingAmount || 0,
  ),
});

// ================= UPDATE SHOP PROFILE =================
exports.updateShopProfile = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { name } = req.body;

    if (!name || name.trim() === "") {
      return res.status(400).json({ message: "Tên shop không được để trống" });
    }

    const user = await User.findByIdAndUpdate(
      shopId,
      { name: name.trim() },
      { new: true },
    ).select("-password");

    if (!user) {
      return res.status(404).json({ message: "Shop không tìm thấy" });
    }

    res.json({
      message: "Cập nhật tên shop thành công",
      user,
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= GET SHOP PRODUCTS =================
exports.getShopProducts = async (req, res) => {
  try {
    const shopId = req.user.id;
    const products = await Product.find({ shopId }).sort({ createdAt: -1 });
    const now = new Date();
    const feeActive = now >= SHOP_BILLING_POLICY.feeStartDate;

    res.json(
      products.map((product) => ({
        ...product.toObject(),
        billingStatus: {
          feeStartAt: SHOP_BILLING_POLICY.feeStartDate,
          isFeeActive: feeActive,
          commissionRate: SHOP_BILLING_POLICY.commissionRate,
          policyVersion: SHOP_BILLING_POLICY.version,
        },
        chargeStatus: product.billing?.chargeStatus || "pending",
      })),
    );
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

exports.getBillingSummary = async (req, res) => {
  try {
    const synced = await syncShopBilling(req.user.id, {
      createNotifications: true,
    });
    if (!synced?.shop) {
      return res.status(404).json({ message: "Shop not found" });
    }

    const overdueOrders = await Order.find({
      "payment.status": "paid",
      "items.shopId": req.user.id,
      "items.platformFee.status": "unpaid",
    }).select("orderNumber items payment createdAt");

    const overdueItems = [];
    overdueOrders.forEach((order) => {
      order.items.forEach((item) => {
        if (item.shopId.toString() !== req.user.id.toString()) return;
        if (item.platformFee?.status !== "unpaid") return;
        overdueItems.push({
          _id: `${order._id}-${item._id}`,
          orderId: order._id,
          orderNumber: order.orderNumber,
          name: item.name,
          price: item.price,
          qty: item.qty,
          baseAmount: roundCurrency(
            item.platformFee?.baseAmount || (item.price || 0) * (item.qty || 1),
          ),
          feeAmount: roundCurrency(item.platformFee?.feeAmount || 0),
          paidAt: order.payment?.paidAt || order.createdAt,
        });
      });
    });

    res.json({
      summary: synced.summary,
      policy: getShopPolicyPayload(synced.shop),
      overdueItems,
    });
  } catch (err) {
    res
      .status(500)
      .json({ message: "Lỗi khi lấy công nợ shop", error: err.message });
  }
};

exports.topUpWallet = async (req, res) => {
  try {
    const amount = Number(req.body.amount);
    const synced = await topUpShopWallet(req.user.id, amount);

    res.json({
      message:
        "Nạp ví thành công, hệ thống đã kiểm tra và tự khấu trừ phí đến hạn",
      summary: synced.summary,
      policy: getShopPolicyPayload(synced.shop),
    });
  } catch (err) {
    res.status(400).json({ message: err.message || "Không thể nạp ví" });
  }
};

exports.settleBilling = async (req, res) => {
  try {
    const synced = await syncShopBilling(req.user.id, {
      createNotifications: true,
    });
    if (!synced?.shop) {
      return res.status(404).json({ message: "Shop not found" });
    }

    res.json({
      message:
        synced.summary.outstandingAmount > 0
          ? "Ví chưa đủ số dư để thanh toán hết công nợ"
          : "Công nợ đã được thanh toán đầy đủ",
      summary: synced.summary,
      policy: getShopPolicyPayload(synced.shop),
    });
  } catch (err) {
    res
      .status(500)
      .json({ message: "Lỗi khi thanh toán công nợ", error: err.message });
  }
};

exports.getBillingPolicy = async (req, res) => {
  try {
    const shop = await User.findById(req.user.id).select("shopBillingPolicy");
    if (!shop) {
      return res.status(404).json({ message: "Shop not found" });
    }

    res.json(getShopPolicyPayload(shop));
  } catch (err) {
    res.status(500).json({
      message: "Lỗi khi lấy chính sách phí nền tảng",
      error: err.message,
    });
  }
};

exports.acceptBillingPolicy = async (req, res) => {
  try {
    const acceptedAt = new Date();
    const shop = await User.findByIdAndUpdate(
      req.user.id,
      {
        shopBillingPolicy: {
          acceptedAt,
          version: SHOP_BILLING_POLICY.version,
        },
      },
      { new: true },
    ).select("-password");

    if (!shop) {
      return res.status(404).json({ message: "Shop not found" });
    }

    res.json({
      message: "Đã cập nhật chính sách phí nền tảng cho shop",
      user: shop,
      policy: getShopPolicyPayload(shop),
    });
  } catch (err) {
    res.status(500).json({
      message: "Lỗi khi cập nhật chính sách phí nền tảng",
      error: err.message,
    });
  }
};

// ================= CREATE PRODUCT =================
exports.createProduct = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productData = {
      ...req.body,
      shopId,
      createdBy: req.user.id,
    };
    const product = new Product(productData);
    await product.save();
    res.status(201).json({
      ...product.toObject(),
      billingStatus: {
        feeStartAt: SHOP_BILLING_POLICY.feeStartDate,
        isFeeActive: new Date() >= SHOP_BILLING_POLICY.feeStartDate,
        commissionRate: SHOP_BILLING_POLICY.commissionRate,
        policyVersion: SHOP_BILLING_POLICY.version,
      },
      billingSummary: req.shopBillingSummary || null,
    });
  } catch (err) {
    if (err.name === "ValidationError") {
      return res.status(400).json({
        message: "Định dạng dữ liệu không hợp lệ",
        details: err.message,
      });
    }
    if (err.code === 11000) {
      return res
        .status(400)
        .json({ message: "Sản phẩm đã tồn tại", details: err.keyValue });
    }
    res
      .status(500)
      .json({ message: "Lỗi khi tạo sản phẩm", error: err.message });
  }
};

// ================= UPDATE PRODUCT =================
exports.updateProduct = async (req, res) => {
  try {
    const shopId = req.user.id;
    const productId = req.params.id;
    const existingProduct = await Product.findOne({ _id: productId, shopId });
    if (!existingProduct) {
      return res.status(404).json({ message: "Product not found" });
    }

    const updateData = { ...req.body };

    const product = await Product.findOneAndUpdate(
      { _id: productId, shopId },
      updateData,
      { new: true, runValidators: true },
    );

    res.json({
      ...product.toObject(),
      billingStatus: {
        feeStartAt: SHOP_BILLING_POLICY.feeStartDate,
        isFeeActive: new Date() >= SHOP_BILLING_POLICY.feeStartDate,
        commissionRate: SHOP_BILLING_POLICY.commissionRate,
        policyVersion: SHOP_BILLING_POLICY.version,
      },
      billingSummary: req.shopBillingSummary || null,
    });
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
      return res.status(404).json({ message: "Product not found" });
    }
    res.json({ message: "Product deleted" });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= GET SHOP ORDERS =================
exports.getShopOrders = async (req, res) => {
  try {
    const shopId = req.user.id;
    const mongoose = require("mongoose");
    const shopObjectId = new mongoose.Types.ObjectId(shopId);

    // Use aggregation to filter items by shopId and show all active orders
    // Pending orders must still be visible in the shop order list.
    const orders = await Order.aggregate([
      {
        $match: {
          status: { $nin: ["đã hủy", "trả hàng", "hoàn tiền"] },
          "items.shopId": shopObjectId,
        },
      },
      {
        $addFields: {
          items: {
            $filter: {
              input: "$items",
              as: "item",
              cond: { $eq: ["$$item.shopId", shopObjectId] },
            },
          },
        },
      },
      {
        $lookup: {
          from: "users",
          localField: "user",
          foreignField: "_id",
          as: "user",
        },
      },
      {
        $unwind: {
          path: "$user",
          preserveNullAndEmptyArrays: true,
        },
      },
      {
        $lookup: {
          from: "products",
          localField: "items.product",
          foreignField: "_id",
          as: "productDetails",
        },
      },
      {
        $addFields: {
          items: {
            $map: {
              input: "$items",
              as: "item",
              in: {
                $mergeObjects: [
                  "$$item",
                  {
                    product: {
                      $arrayElemAt: [
                        {
                          $filter: {
                            input: "$productDetails",
                            as: "prod",
                            cond: { $eq: ["$$prod._id", "$$item.product"] },
                          },
                        },
                        0,
                      ],
                    },
                  },
                ],
              },
            },
          },
        },
      },
      {
        $project: {
          productDetails: 0,
        },
      },
      {
        $sort: { createdAt: -1 },
      },
    ]);

    res.json(orders);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= GET SHOP ORDER DETAILS =================
exports.getShopOrderById = async (req, res) => {
  try {
    const shopId = req.user.id;
    const mongoose = require("mongoose");
    const shopObjectId = new mongoose.Types.ObjectId(shopId);

    // Use aggregation to filter items by shopId and keep pending orders visible
    const orders = await Order.aggregate([
      {
        $match: {
          _id: new mongoose.Types.ObjectId(req.params.id),
          status: { $nin: ["đã hủy", "trả hàng", "hoàn tiền"] },
          "items.shopId": shopObjectId,
        },
      },
      {
        $addFields: {
          items: {
            $filter: {
              input: "$items",
              as: "item",
              cond: { $eq: ["$$item.shopId", shopObjectId] },
            },
          },
        },
      },
      {
        $lookup: {
          from: "users",
          localField: "user",
          foreignField: "_id",
          as: "user",
        },
      },
      {
        $unwind: {
          path: "$user",
          preserveNullAndEmptyArrays: true,
        },
      },
      {
        $lookup: {
          from: "products",
          localField: "items.product",
          foreignField: "_id",
          as: "productDetails",
        },
      },
      {
        $addFields: {
          items: {
            $map: {
              input: "$items",
              as: "item",
              in: {
                $mergeObjects: [
                  "$$item",
                  {
                    product: {
                      $arrayElemAt: [
                        {
                          $filter: {
                            input: "$productDetails",
                            as: "prod",
                            cond: { $eq: ["$$prod._id", "$$item.product"] },
                          },
                        },
                        0,
                      ],
                    },
                  },
                ],
              },
            },
          },
        },
      },
      {
        $project: {
          productDetails: 0,
        },
      },
    ]);

    if (orders.length === 0) {
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });
    }

    res.json(orders[0]);
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
      pending: "chờ xác nhận",
      confirmed: "đã xác nhận",
      shipped: "đang giao",
      delivered: "đã nhận",
      cancelled: "đã hủy",
    };

    // Accept both English (legacy) and Vietnamese directly
    const mappedStatus = statusMap[status] || status;

    const validStatuses = [
      "chờ xác nhận",
      "đã xác nhận",
      "đang giao",
      "đã nhận",
      "đã hủy",
    ];
    if (!validStatuses.includes(mappedStatus)) {
      return res.status(400).json({ message: "Trạng thái không hợp lệ" });
    }

    // Chỉ cho phép shop cập nhật đơn hàng có sản phẩm của shop đó
    const order = await Order.findOne({
      _id: id,
      "items.shopId": new mongoose.Types.ObjectId(shopId),
    }).populate("items.product", "name price image images");
    if (!order) {
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });
    }

    order.status = mappedStatus;
    await order.save();
    await order.populate("items.product", "name price image images");

    // Nếu trạng thái là "đã xác nhận" thì tạo notification cho user
    if (mappedStatus === "đã xác nhận") {
      try {
        await Notification.create({
          user: order.user, // user nhận thông báo
          type: "order_status",
          title: "Đơn hàng đã được xác nhận",
          message: `Đơn hàng #${order.orderNumber} của bạn đã được shop xác nhận.`,
          data: { orderId: order._id, orderNumber: order.orderNumber },
          isRead: false,
        });
      } catch (notiErr) {
        console.error("Lỗi tạo notification cho user:", notiErr);
      }
    }

    res.json({ message: "Cập nhật trạng thái thành công", order });
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
      { new: true },
    );
    if (!notification)
      return res.status(404).json({ message: "Notification not found" });
    res.json(notification);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

exports.markAllNotificationsRead = async (req, res) => {
  try {
    const shopId = req.user.id;
    await Notification.updateMany(
      { user: shopId, isRead: false },
      { isRead: true, readAt: new Date() },
    );
    res.json({ message: "All notifications marked read" });
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

    const shopProducts = await Product.find(productFilter).select("_id");
    const productIds = shopProducts.map((p) => p._id);

    const reviews = await Review.find({ product: { $in: productIds } })
      .populate("user", "name email")
      .populate("product", "name image")
      .populate("response.respondedBy", "name")
      .sort({ createdAt: -1 });

    res.json(reviews);
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// ================= REPLY SHOP REVIEW =================
exports.replyShopReview = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { id } = req.params;
    const { text } = req.body;

    if (!text || text.trim() === "") {
      return res.status(400).json({ message: "Reply text is required" });
    }

    const review = await Review.findById(id).populate("product", "shopId");
    if (!review) return res.status(404).json({ message: "Review not found" });

    if (!review.product || review.product.shopId.toString() !== shopId) {
      return res
        .status(403)
        .json({ message: "Forbidden: review not belong to your shop" });
    }

    review.response = {
      text: text.trim(),
      respondedAt: new Date(),
      respondedBy: shopId,
    };

    await review.save();
    await review.populate("response.respondedBy", "name");

    res.json({ review });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

// ================= GET SHOP REVENUE =================
exports.getShopRevenue = async (req, res) => {
  try {
    const shopId = req.user.id;
    const { period = "month" } = req.query;

    const now = new Date();
    let startDate;

    switch (period) {
      case "day":
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        break;
      case "week":
        const weekStart = now.getDate() - now.getDay();
        startDate = new Date(now.getFullYear(), now.getMonth(), weekStart);
        break;
      case "month":
        startDate = new Date(now.getFullYear(), now.getMonth(), 1);
        break;
      case "year":
        startDate = new Date(now.getFullYear(), 0, 1);
        break;
      default:
        startDate = new Date(now.getFullYear(), now.getMonth(), 1);
    }

    const shopObjectId = new mongoose.Types.ObjectId(shopId);

    // Lấy tất cả orders đã thanh toán hoặc đã nhận, không tính đơn hủy/trả hàng/hoàn tiền
    // Include orders where any relevant timestamp falls into the period
    const timeFilter = {
      $or: [
        { updatedAt: { $gte: startDate } },
        { "payment.paidAt": { $gte: startDate } },
        { createdAt: { $gte: startDate } },
      ],
    };

    const orders = await Order.find({
      $and: [
        {
          $or: [{ status: "đã nhận" }, { "payment.status": "paid" }],
        },
        { status: { $nin: ["đã hủy", "trả hàng", "hoàn tiền"] } },
        { "items.shopId": shopId },
        timeFilter,
      ],
    }).select("user items payment createdAt updatedAt orderNumber status");

    // Chi tiết từng sản phẩm với phí sàn
    const productDetails = [];
    let totalGrossRevenue = 0;
    let totalPlatformFees = 0;
    let totalNetRevenue = 0;

    orders.forEach((order) => {
      order.items.forEach((item) => {
        if (item.shopId.toString() !== shopId.toString()) return;

        const grossAmount = roundCurrency(item.price * (item.qty || 1));
        const platformFee = roundCurrency(grossAmount * 0.05); // 5% phí sàn
        const netAmount = roundCurrency(grossAmount - platformFee);

        totalGrossRevenue += grossAmount;
        totalPlatformFees += platformFee;
        totalNetRevenue += netAmount;

        const revenueAt =
          order.updatedAt || order.payment?.paidAt || order.createdAt;

        productDetails.push({
          orderId: order._id,
          orderNumber: order.orderNumber,
          productId: item.product,
          productName: item.name,
          productImage: item.image,
          sku: item.sku,
          price: item.price,
          quantity: item.qty || 1,
          grossAmount: grossAmount,
          platformFeeRate: 0.05, // 5%
          platformFee: platformFee,
          netAmount: netAmount,
          paidAt: revenueAt,
          feeStatus: "charged",
        });
      });
    });

    // Tính toán thống kê
    const revenueData = await Order.aggregate([
      {
        $match: {
          $and: [
            {
              $or: [{ status: "đã nhận" }, { "payment.status": "paid" }],
            },
            { status: { $nin: ["đã hủy", "trả hàng", "hoàn tiền"] } },
            { "items.shopId": shopObjectId },
            {
              $or: [
                { updatedAt: { $gte: startDate } },
                { "payment.paidAt": { $gte: startDate } },
                { createdAt: { $gte: startDate } },
              ],
            },
          ],
        },
      },
      {
        $unwind: "$items",
      },
      {
        $match: {
          "items.shopId": shopObjectId,
        },
      },
      {
        $group: {
          _id: null,
          totalOrders: { $addToSet: "$_id" },
          totalProducts: { $sum: "$items.qty" },
        },
      },
    ]);

    const stats = revenueData[0] || { totalOrders: [], totalProducts: 0 };

    const uniqueCustomerIds = new Set(
      orders
        .map((order) => (order.user ? order.user.toString() : null))
        .filter(Boolean),
    );
    const chartBuckets = [0, 0, 0, 0, 0];
    const periodMs = Math.max(1, now.getTime() - startDate.getTime());
    const bucketMs = periodMs / chartBuckets.length;

    productDetails.forEach((item) => {
      const paidAt = item.paidAt ? new Date(item.paidAt) : now;
      const bucketIndex = Math.min(
        chartBuckets.length - 1,
        Math.max(
          0,
          Math.floor((paidAt.getTime() - startDate.getTime()) / bucketMs),
        ),
      );
      chartBuckets[bucketIndex] += item.grossAmount;
    });

    const firstBucket = chartBuckets[0];
    const lastBucket = chartBuckets[chartBuckets.length - 1];
    const percentChange =
      firstBucket > 0
        ? Math.round(((lastBucket - firstBucket) / firstBucket) * 100)
        : 0;
    const chartNote =
      percentChange >= 0
        ? `Tăng trưởng so với kỳ trước +${percentChange}%`
        : `Giảm so với kỳ trước ${Math.abs(percentChange)}%`;

    const shopUser = await User.findById(shopId).select(
      "shopBillingPolicy shopStatus shopWallet shopBillingSummary",
    );

    res.json({
      period,
      startDate,
      totalRevenue: roundCurrency(totalGrossRevenue),
      totalOrders: stats.totalOrders.length,
      totalProducts: stats.totalProducts,
      summary: {
        totalGrossRevenue: roundCurrency(totalGrossRevenue),
        totalPlatformFees: roundCurrency(totalPlatformFees),
        totalNetRevenue: roundCurrency(totalNetRevenue),
        totalOrders: stats.totalOrders.length,
        totalProducts: stats.totalProducts,
        newCustomers: uniqueCustomerIds.size,
        platformFeeRate: 0.05, // 5%
      },
      chart: {
        values: chartBuckets,
      },
      chartNote,
      productDetails: productDetails.sort(
        (a, b) => new Date(b.paidAt) - new Date(a.paidAt),
      ),
      policy: getShopPolicyPayload(shopUser),
    });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

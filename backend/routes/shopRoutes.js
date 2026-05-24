const express = require("express");
const router = express.Router();
const { auth, shopAuth, ensureShopCanSell } = require("../middleware/auth");
const shopController = require("../controllers/shopController");

// Apply auth and shopAuth to all routes
router.use(auth);
router.use(shopAuth);

// Profile management routes
router.put("/profile", shopController.updateShopProfile);

// Product management routes
router.get("/products", shopController.getShopProducts);
router.get("/billing-policy", shopController.getBillingPolicy);
router.post("/billing-policy/accept", shopController.acceptBillingPolicy);
router.get("/billing-summary", shopController.getBillingSummary);
router.post("/wallet/top-up", shopController.topUpWallet);
router.post("/billing/settle", shopController.settleBilling);
router.post("/products", ensureShopCanSell, shopController.createProduct);
router.put("/products/:id", ensureShopCanSell, shopController.updateProduct);
router.delete("/products/:id", ensureShopCanSell, shopController.deleteProduct);

// Order management routes
router.get("/orders", shopController.getShopOrders);
router.get("/orders/:id", shopController.getShopOrderById);
router.put("/orders/:id/status", shopController.updateShopOrderStatus);

// Review management routes
router.get("/reviews", shopController.getShopReviews);
router.put("/reviews/:id/reply", shopController.replyShopReview);

// Notification routes
router.get("/notifications", shopController.getShopNotifications);
router.put("/notifications/:id/read", shopController.markNotificationRead);
router.put("/notifications/read-all", shopController.markAllNotificationsRead);

// Revenue analytics routes
router.get("/revenue", shopController.getShopRevenue);

module.exports = router;

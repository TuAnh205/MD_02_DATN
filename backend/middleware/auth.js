const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { syncShopBilling } = require('../services/shopBillingService');

const jwtSecret = process.env.JWT_SECRET || 'secret_jwt_key';

const auth = async (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ message: 'No token provided' });
    const parts = authHeader.split(' ');
    if (parts.length !== 2) return res.status(401).json({ message: 'Token error' });
    const scheme = parts[0];
    const token = parts[1];
    if (!/^Bearer$/i.test(scheme)) return res.status(401).json({ message: 'Token malformatted' });

    try {
        const decoded = jwt.verify(token, jwtSecret);
        req.user = { id: decoded.id, role: decoded.role };
        next();
    } catch (err) {
        return res.status(401).json({ message: 'Token invalid' });
    }
};

const adminAuth = (req, res, next) => {
    if (req.user && req.user.role === 'admin') {
        next();
    } else {
        res.status(403).json({ message: 'Admin access required' });
    }
};

const shopAuth = async (req, res, next) => {
    if (!req.user || req.user.role !== 'shop') {
        return res.status(403).json({ message: 'Shop access required' });
    }

    try {
        const synced = await syncShopBilling(req.user.id, { createNotifications: true });
        if (!synced?.shop) {
            return res.status(404).json({ message: 'Shop not found' });
        }

        req.shop = synced.shop;
        req.shopBillingSummary = synced.summary;
        next();
    } catch (err) {
        return res.status(500).json({ message: 'Cannot load shop state', error: err.message });
    }
};

const ensureShopCanSell = (req, res, next) => {
    if (req.shop && req.shop.shopStatus === 'frozen') {
        return res.status(403).json({
            message: req.shopBillingSummary?.message || 'Shop đang bị đóng băng bán hàng do còn nợ phí nền tảng',
            code: 'SHOP_FROZEN',
            billingSummary: req.shopBillingSummary || null,
        });
    }

    next();
};

module.exports = auth;
module.exports.auth = auth;
module.exports.adminAuth = adminAuth;
module.exports.shopAuth = shopAuth;
module.exports.ensureShopCanSell = ensureShopCanSell;

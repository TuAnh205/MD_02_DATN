const jwt = require('jsonwebtoken');
const User = require('../models/User');

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

const shopAuth = (req, res, next) => {
    if (req.user && req.user.role === 'shop') {
        next();
    } else {
        res.status(403).json({ message: 'Shop access required' });
    }
};

module.exports = auth;
module.exports.auth = auth;
module.exports.adminAuth = adminAuth;
module.exports.shopAuth = shopAuth;

const mongoose = require('mongoose');
const Order = require('../models/Order');
const Product = require('../models/Product');
const User = require('../models/User');
const { settlePaymentAndCreditShops } = require('../controllers/orderController');

const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/md02_datn';

(async () => {
  try {
    await mongoose.connect(MONGO_URI);
    console.log('Connected to DB');

    // find a shop user
    const shop = await User.findOne({ role: 'shop' });
    if (!shop) throw new Error('No shop user found');

    // find a product from that shop
    const product = await Product.findOne({ shopId: shop._id });
    if (!product) throw new Error('No product for shop found');

    // find a normal user
    const user = await User.findOne({ role: 'user' });
    if (!user) throw new Error('No user found');

    const orderData = {
      orderNumber: 'TEST-' + Date.now(),
      user: user._id,
      items: [
        {
          product: product._id,
          shopId: product.shopId,
          name: product.name,
          price: product.price || 100000,
          qty: 1,
          image: product.images && product.images[0] || '',
          sku: '',
          platformFee: { eligible: true, feeAmount: 0, status: 'pending' }
        }
      ],
      subtotal: product.price || 100000,
      total: product.price || 100000,
      shipping: { address: { name: 'Test', phone: '0912345678', address: 'Addr', city: 'Hanoi', district: 'T', ward: 'W' }, method: 'standard', fee: 0 },
      payment: { method: 'card', status: 'paid', paidAt: new Date() }
    };

    const order = await Order.create(orderData);
    console.log('Created order', order._id.toString());

    // run settle
    const res = await settlePaymentAndCreditShops(order._id);
    console.log('Settle result:', !!res);

    const updatedOrder = await Order.findById(order._id);
    console.log('Updated order items:', updatedOrder.items.map(i => ({ name: i.name, platformFee: i.platformFee })));

    const updatedShop = await User.findById(shop._id);
    console.log('Shop wallet balance:', updatedShop.shopWallet);

    process.exit(0);
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
})();

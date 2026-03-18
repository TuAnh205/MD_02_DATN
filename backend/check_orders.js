require('dotenv').config();
const mongoose = require('mongoose');
const Order = require('./models/Order');
const User = require('./models/User'); // Add this line
const Product = require('./models/Product'); // Add this line

const connectDB = async () => {
  try {
    const uri = process.env.MONGO_URI || 'mongodb://localhost:27017/md02_datn';
    await mongoose.connect(uri);
    console.log('✓ Connected to MongoDB');
  } catch (err) {
    console.error('✗ Failed to connect:', err.message);
    process.exit(1);
  }
};

const checkOrders = async () => {
  try {
    const orders = await Order.find().populate('user', 'name email').populate('items.product', 'name price');
    console.log(`Found ${orders.length} orders:`);
    orders.forEach((order, index) => {
      console.log(`${index + 1}. Order ID: ${order._id}`);
      console.log(`   User: ${order.user?.name} (${order.user?.email})`);
      console.log(`   Status: ${order.status}`);
      console.log(`   Total: ${order.total}`);
      console.log(`   Items: ${order.items.length}`);
      console.log('---');
    });
  } catch (error) {
    console.error('Error checking orders:', error);
  }
};

const run = async () => {
  await connectDB();
  await checkOrders();
  process.exit(0);
};

run();
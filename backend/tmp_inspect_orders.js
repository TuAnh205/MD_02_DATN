const connectDB = require('./config/db');
const Order = require('./models/Order');

(async () => {
  try {
    await connectDB();
    const orders = await Order.find().limit(5).lean();
    console.log(orders.map(o => ({ _id: o._id, user: o.user, total: o.total })));
  } catch (err) {
    console.error(err);
  } finally {
    process.exit(0);
  }
})();

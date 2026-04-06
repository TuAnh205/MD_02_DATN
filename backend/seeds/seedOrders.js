require('dotenv').config();
const mongoose = require('mongoose');
const User = require('../models/User');
const Product = require('../models/Product');
const Order = require('../models/Order');

const MONGO_URI = process.env.MONGO_URI || process.env.MONGODB_URI || 'mongodb://localhost:27017/md02_datn';

const FAKE_ADDRESSES = [
  { name: 'Nguyen Van A', phone: '0901234567', address: '123 Le Loi', city: 'TP.HCM', district: 'Quan 1', ward: 'Phuong Ben Nghe' },
  { name: 'Tran Thi B', phone: '0912345678', address: '456 Nguyen Hue', city: 'Ha Noi', district: 'Hoan Kiem', ward: 'Phuong Hang Bac' },
  { name: 'Le Quang C', phone: '0923456789', address: '789 Tran Hung Dao', city: 'Da Nang', district: 'Hai Chau', ward: 'Phuong Thach Thang' },
  { name: 'Pham Bao D', phone: '0934567890', address: '101 Pham Ngu Lao', city: 'TP.HCM', district: 'Quan 1', ward: 'Phuong Pham Ngu Lao' },
  { name: 'Hoang Gia E', phone: '0945678901', address: '202 Ba Trieu', city: 'Ha Noi', district: 'Hai Ba Trung', ward: 'Phuong Nguyen Du' },
];

const generateOrderNumber = (index) => {
  const ts = Date.now().toString().slice(-6);
  return `ORD-FAKE-${ts}-${String(index).padStart(3, '0')}`;
};

const run = async () => {
  try {
    await mongoose.connect(MONGO_URI);
    console.log('✓ Connected to MongoDB');

    // Lấy user thường để làm người mua
    const users = await User.find({ role: 'user' }).limit(5);
    if (users.length === 0) {
      console.error('✗ Không tìm thấy user nào. Hãy chạy seed.js trước.');
      process.exit(1);
    }

    // Lấy products để làm items
    const products = await Product.find().limit(10);
    if (products.length === 0) {
      console.error('✗ Không tìm thấy sản phẩm nào. Hãy chạy seed.js trước.');
      process.exit(1);
    }

    // Helper lấy ngẫu nhiên
    const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
    const randQty = () => Math.floor(Math.random() * 3) + 1;

    // Tạo danh sách 10 đơn theo yêu cầu:
    // 5 đơn: payment paid, status đã nhận
    // 3 đơn: payment pending, status chờ xác nhận
    // 2 đơn: payment paid, status chờ xác nhận
    const orderSpecs = [
      ...Array(5).fill({ payStatus: 'paid',    orderStatus: 'đã nhận' }),
      ...Array(3).fill({ payStatus: 'pending',  orderStatus: 'chờ xác nhận' }),
      ...Array(2).fill({ payStatus: 'paid',     orderStatus: 'chờ xác nhận' }),
    ];

    const payMethods = ['momo', 'vnpay', 'card', 'bank'];
    let created = 0;

    for (let i = 0; i < orderSpecs.length; i++) {
      const spec = orderSpecs[i];
      const user = pick(users);
      const address = pick(FAKE_ADDRESSES);

      // Chọn 1–2 sản phẩm ngẫu nhiên
      const numItems = Math.floor(Math.random() * 2) + 1;
      const chosenProducts = products.slice(0, numItems);

      const items = chosenProducts.map((p) => ({
        product: p._id,
        shopId: p.shopId,
        name: p.name,
        sku: `SKU-${p._id.toString().slice(-4).toUpperCase()}`,
        price: p.price,
        originalPrice: p.price,
        qty: randQty(),
        image: p.images?.[0] || '',
      }));

      const subtotal = items.reduce((sum, it) => sum + it.price * it.qty, 0);
      const shippingFee = 30000;
      const total = subtotal + shippingFee;

      const orderData = {
        orderNumber: generateOrderNumber(i + 1),
        user: user._id,
        items,
        subtotal,
        discount: { amount: 0 },
        shipping: {
          fee: shippingFee,
          method: 'standard',
          address,
        },
        tax: 0,
        total,
        status: spec.orderStatus,
        payment: {
          method: pick(payMethods),
          status: spec.payStatus,
          ...(spec.payStatus === 'paid' && { paidAt: new Date() }),
        },
      };

      await Order.create(orderData);
      created++;
      console.log(
        `  [${i + 1}/10] Tạo đơn #${orderData.orderNumber} | payment: ${spec.payStatus} | status: ${spec.orderStatus}`
      );
    }

    console.log(`\n✓ Hoàn thành: đã tạo ${created} đơn hàng giả`);
    console.log('  - 5 đơn thanh toán thành công (paid) + đã nhận');
    console.log('  - 3 đơn chưa thanh toán (pending) + chờ xác nhận');
    console.log('  - 2 đơn thanh toán thành công (paid) + chờ xác nhận');
    process.exit(0);
  } catch (err) {
    console.error('✗ Lỗi:', err.message);
    process.exit(1);
  }
};

run();

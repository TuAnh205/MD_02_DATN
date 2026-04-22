// Scheduler: Chạy các job định kỳ
const cron = require('node-cron');
const mongoose = require('mongoose');
const notifyExpiringVouchers = require('./voucherExpiryNotifier');

// Kết nối MongoDB (sử dụng URI từ biến môi trường hoặc config)
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/your_db_name';
mongoose.connect(MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => console.log('Connected to MongoDB for jobs'))
  .catch((err) => console.error('MongoDB connection error:', err));

// Chạy job kiểm tra voucher sắp hết hạn mỗi ngày lúc 2h sáng
cron.schedule('0 2 * * *', async () => {
  console.log('[JOB] Running notifyExpiringVouchers...');
  try {
    await notifyExpiringVouchers();
    console.log('[JOB] notifyExpiringVouchers done');
  } catch (err) {
    console.error('[JOB] notifyExpiringVouchers error:', err);
  }
});

// Có thể thêm các job khác tại đây
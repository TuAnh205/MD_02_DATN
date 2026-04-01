require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const Product = require('../models/Product');
const Review = require('../models/Review');
const User = require('../models/User');

const MONGO_URI =
  process.env.MONGO_URI || process.env.MONGODB_URI || 'mongodb://localhost:27017/md02_datn';

const SAMPLE_NAMES = [
  'Nguyen Anh',
  'Tran Minh',
  'Le Quang',
  'Pham Bao',
  'Hoang Kiet',
  'Dang Nhat',
  'Vo Gia',
  'Bui Tuan',
  'Do Thanh',
  'Phan Nam'
];

const REVIEW_COMMENTS_BY_RATING = {
  5: [
    'San pham rat tot, dung on dinh va dang tien.',
    'Chat luong vuot mong doi, giao hang nhanh.',
    'Dong goi ky, may chay muot, se ung ho tiep.'
  ],
  4: [
    'Dung on, gia hop ly, nhin chung hai long.',
    'San pham tot trong tam gia, nen mua.',
    'Hoat dong tot, thiet ke dep.'
  ],
  3: [
    'Tam on, dung duoc cho nhu cau co ban.',
    'Khong qua noi bat nhung van chap nhan duoc.',
    'Can cai thien them mot vai diem nho.'
  ]
};

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pickRandom(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomRating() {
  const weighted = [5, 5, 5, 4, 4, 3];
  return pickRandom(weighted);
}

async function ensureReviewUsers(minCount = 6) {
  let users = await User.find({ role: 'user' }).select('_id name email').lean();

  if (users.length >= minCount) return users;

  const needToCreate = minCount - users.length;
  const basePassword = await bcrypt.hash('123456', 10);

  const payload = [];
  for (let i = 0; i < needToCreate; i += 1) {
    const suffix = Date.now() + i;
    payload.push({
      name: `Reviewer ${suffix}`,
      email: `reviewer_${suffix}@coretech.local`,
      password: basePassword,
      role: 'user'
    });
  }

  await User.insertMany(payload, { ordered: false });
  users = await User.find({ role: 'user' }).select('_id name email').lean();
  return users;
}

async function updateProductRating(productId) {
  const reviews = await Review.find({ product: productId }).select('rating').lean();
  const count = reviews.length;
  const average = count > 0 ? reviews.reduce((sum, item) => sum + item.rating, 0) / count : 0;

  await Product.findByIdAndUpdate(productId, {
    'ratings.average': Number(average.toFixed(2)),
    'ratings.count': count
  });
}

async function seedRandomReviews() {
  await mongoose.connect(MONGO_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
  });

  console.log('Connected to MongoDB');

  const products = await Product.find({}).select('_id name').lean();
  if (products.length === 0) {
    console.log('No products found. Stop.');
    return;
  }

  const users = await ensureReviewUsers(8);
  let createdCount = 0;

  for (const product of products) {
    const target = randomInt(2, 3);
    const existingReviews = await Review.find({ product: product._id }).select('user rating').lean();

    if (existingReviews.length >= target) {
      await updateProductRating(product._id);
      console.log(`Skip ${product.name}: already has ${existingReviews.length} reviews`);
      continue;
    }

    const usedUserIds = new Set(existingReviews.map((r) => String(r.user)));
    const availableUsers = users.filter((u) => !usedUserIds.has(String(u._id)));

    const need = target - existingReviews.length;
    const selectedUsers = availableUsers.slice(0, need);

    if (selectedUsers.length < need) {
      console.log(`Not enough unique users for ${product.name}, only add ${selectedUsers.length}/${need}`);
    }

    const docs = selectedUsers.map((u) => {
      const rating = randomRating();
      const comment = pickRandom(REVIEW_COMMENTS_BY_RATING[rating]);

      return {
        user: u._id,
        product: product._id,
        rating,
        title: `Danh gia ${rating} sao`,
        comment,
        isVerified: false
      };
    });

    if (docs.length > 0) {
      await Review.insertMany(docs, { ordered: false });
      createdCount += docs.length;
    }

    await updateProductRating(product._id);
    const total = existingReviews.length + docs.length;
    console.log(`Updated ${product.name}: ${total} reviews`);
  }

  console.log(`Done. Created ${createdCount} random reviews.`);
}

seedRandomReviews()
  .catch((err) => {
    console.error('Seed random reviews failed:', err.message || err);
    process.exitCode = 1;
  })
  .finally(async () => {
    await mongoose.disconnect();
    console.log('Disconnected MongoDB');
  });

require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const Product = require('../models/Product');

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

const seedUsers = async () => {
  try {
    // Clear existing users
    await User.deleteMany({});
    console.log('✓ Cleared existing users');

    // Create test users
    const hashedPassword = await bcrypt.hash('123456', 10);

    const users = [
      {
        name: 'Admin User',
        email: 'admin@test.com',
        password: hashedPassword,
        role: 'admin',
        avatar: 'https://via.placeholder.com/150?text=Admin'
      },
      {
        name: 'Test User',
        email: 'user@test.com',
        password: hashedPassword,
        role: 'user',
        avatar: 'https://via.placeholder.com/150?text=User'
      },
      {
        name: 'John Doe',
        email: 'john@test.com',
        password: hashedPassword,
        role: 'user',
        avatar: 'https://via.placeholder.com/150?text=John'
      }
    ];

    const createdUsers = await User.insertMany(users);
    console.log(`✓ Created ${createdUsers.length} test users`);
    return createdUsers;
  } catch (err) {
    console.error('✗ Error seeding users:', err.message);
  }
};

const seedProducts = async () => {
  try {
    // Clear existing products
    await Product.deleteMany({});
    console.log('✓ Cleared existing products');

    // Get admin user for createdBy
    const adminUser = await User.findOne({ role: 'admin' });
    if (!adminUser) {
      console.error('✗ No admin user found, skipping product seeding');
      return;
    }

    const products = [
      {
        name: 'Laptop Dell XPS 13',
        price: 1299000,
        category: 'Máy tính',
        description: 'Laptop siêu mỏng, hiệu năng cao',
        detailedDescription: 'Màn hình 13.4" QHD+, CPU Intel Core i7, RAM 16GB, SSD 512GB, pin 12h.',
        images: ['https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60'],
        stock: 15,
        featured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'iPhone 15 Pro',
        price: 999000,
        category: 'Điện thoại',
        description: 'Điện thoại thông minh mới nhất',
        detailedDescription: 'Màn hình OLED 6.1", A17 Bionic, camera 48MP, sạc nhanh 30W.',
        images: ['https://images.unsplash.com/photo-1542751110-97427bbecf20?auto=format&fit=crop&w=800&q=60'],
        stock: 25,
        featured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Samsung Galaxy Watch',
        price: 299000,
        category: 'Đồng hồ',
        description: 'Đồng hồ thông minh cao cấp',
        detailedDescription: 'Màn hình AMOLED, đo nhịp tim, GPS, chống nước 5ATM.',
        images: ['https://images.unsplash.com/photo-1523475496153-3c7cc1d6a55e?auto=format&fit=crop&w=800&q=60'],
        stock: 30,
        featured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Sony WH-1000XM5 Headphones',
        price: 349000,
        category: 'Tai nghe',
        description: 'Tai nghe khử tiếng ồn tuyệt vời',
        detailedDescription: 'Khử ồn chủ động, âm thanh chất lượng cao, pin 30 giờ.',
        images: ['https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=800&q=60'],
        stock: 20,
        featured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'iPad Air',
        price: 599000,
        category: 'Máy tính bảng',
        description: 'Máy tính bảng mạnh mẽ',
        detailedDescription: 'Màn hình Liquid Retina 10.9", chip M1, hỗ trợ Apple Pencil.',
        images: ['https://images.unsplash.com/photo-1555098715-519c13c05a8d?auto=format&fit=crop&w=800&q=60'],
        stock: 18,
        featured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'MacBook Pro M3',
        price: 1999000,
        category: 'Máy tính',
        description: 'Máy tính xách tay chuyên nghiệp',
        detailedDescription: 'CPU M3, 16GB RAM, 1TB SSD, màn hình Retina 14".',
        images: ['https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=60'],
        stock: 10,
        featured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Google Pixel 8',
        price: 799000,
        category: 'Điện thoại',
        description: 'Điện thoại camera tốt nhất',
        detailedDescription: 'Camera AI 50MP, chip Tensor, Android thuần.',
        images: ['https://images.unsplash.com/photo-1521618751442-4cbf0f6b8bd6?auto=format&fit=crop&w=800&q=60'],
        stock: 22,
        featured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'AirPods Pro Max',
        price: 549000,
        category: 'Tai nghe',
        description: 'Tai nghe over-ear cao cấp từ Apple',
        detailedDescription: 'Khử ồn chủ động, âm thanh sống động, pin 20 giờ.',
        images: ['https://images.unsplash.com/photo-1580894894518-5d728c78d432?auto=format&fit=crop&w=800&q=60'],
        stock: 12,
        featured: true,
        hot: false,
        createdBy: adminUser._id
      }
    ];

    const createdProducts = await Product.insertMany(products);
    console.log(`✓ Created ${createdProducts.length} test products`);
    return createdProducts;
  } catch (err) {
    console.error('✗ Error seeding products:', err.message);
  }
};

const seedDatabase = async () => {
  try {
    await connectDB();
    await seedUsers();
    await seedProducts();
    
    console.log('\n✓ Database seeding completed successfully!');
    console.log('\n--- Test Accounts ---');
    console.log('Admin:');
    console.log('  Email: admin@test.com');
    console.log('  Password: 123456');
    console.log('\nUser:');
    console.log('  Email: user@test.com');
    console.log('  Password: 123456');
    console.log('  Email: john@test.com');
    console.log('  Password: 123456');
    
    process.exit(0);
  } catch (err) {
    console.error('✗ Seeding failed:', err.message);
    process.exit(1);
  }
};

seedDatabase();

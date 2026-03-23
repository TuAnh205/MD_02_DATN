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
        detailedDescription: '✨ Tổng quan sản phẩm\nDell XPS 13 là dòng laptop cao cấp hướng đến các doanh nhân, designer và lập trình viên. Với thiết kế ultrabook siêu sang trọng, nó luôn nằm trong top laptop cao cấp thế giới.\n\n🎯 Thiết kế đẳng cấp\nVỏ nhôm nguyên khối CNC chống trầy, trọng lượng chỉ 1.2kg cực kỳ dễ mang theo. Viền màn hình InfinityEdge siêu mỏng tạo cảm giác tràn viền hiện đại. Có 3 màu: Platinum Silver, Graphite, Sky Blue cực đẹp.\n\n📊 Hiệu năng\nCPU Intel Core i7 Gen 13, RAM 16GB LPDDR5, SSD NVMe 512GB. Màn hình 13.4" QHD+ độ sáng 500 nits, pin 73Wh kéo dài 12h.\n\n👉 Nhận xét: Sang xịn như MacBook nhưng mang chất riêng của Dell, giá hợp lý cho quality này.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\niPhone 15 Pro là flagship 2024 của Apple với thiết kế titanium cao cấp, camera 48MP cúp, và chip A17 Pro siêu mạnh. Đây là điện thoại yêu cầu bắt buộc cho các content creator.\n\n🎯 Thiết kế Titanium\nFramework titanium hạng hàng không vũ trụ chống rơi cực tốt. Viền lưng bằng kính Ceramic Shield tăng độ bền. Nút Action thay thế mute switch, có 4 màu: bạc, đen, vàng, tím đầy ấn tượng.\n\n📸 Camera & Hiệu năng\n48MP chính với zoom quang học 5x, A17 Pro xử lý video 4K 120fps. Pin 21 giờ, sạc nhanh 35W, IP69 kháng nước chuẩn.\n\n👉 Nhận xét: Flagship xứng đáng, video/photo quá đỉnh, giá vừa phải.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\nSamsung Galaxy Watch là đồng hồ thông minh hàng đầu, kết hợp hoàn hảo giữa thời trang và công nghệ. Thích hợp cho những người yêu thích theo dõi sức khỏe, thể thao và lối sống lành mạnh.\n\n💎 Thiết kế & Màn hình\nMàn hình AMOLED 1.4 inch cực nét, viền thép không gỉ bóng loáng. Dây đeo da hoặc cao su tùy chọn, pin 5 ngày khi sử dụng tích cực.\n\n🏃 Tính năng Sức khỏe\nCảm biến nhịp tim chuẩn y tế, SpO2, đo căng thẳng, theo dõi giấc ngủ chi tiết. 100+ chế độ tập luyện, GPS chính xác, chống nước 5ATM bơi lội được.\n\n👉 Nhận xét: Companion hoàn hảo cho cuộc sống khỏe mạnh, giá tốt.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\nSony WH-1000XM5 là tai nghe khử ồn số 1 thế giới, được chứng nhận bởi các chuyên gia âm thanh. Dành cho những người thường xuyên du lịch hoặc làm việc ồn.\n\n🎧 Khử Ồn & Âm thanh\nKhử ồn chủ động ANC vô địch, cảm nhận hơn 30,000 lần/giây. Âm thanh LDAC siêu sắc nét, bass sâu mà không chói tai. Chế độ ambient bắt được âm xung quanh khi cần.\n\n⚡ Pin & Thoải mái\nPin liên tục 30 giờ khử ồn, 12 giờ không khử ồn. Đệm tai Memory Foam êm ái, trọng lượng chỉ 250g, Bluetooth 5.3 kết nối ổn định.\n\n👉 Nhận xét: Đáng giá mọi đồng xu, tốt nhất cho chuyến bay dài.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\niPad Air là máy tính bảng vàng giữa iPad thường và iPad Pro - mạnh mẽ nhưng giá hợp lý. Hoàn hảo cho designer, painter, student, hoặc consumption đơn giản.\n\n💫 Màn hình & Thiết kế\nMàn hình Liquid Retina 10.9 inch 120Hz mịn như lụa xem video/vẽ. Vỏ nhôm siêu mỏng chỉ 6.1mm, trọng lượng 613g nhẹ nhàng, có 4 lựa chọn màu sắc.\n\n🚀 Hiệu năng\nChip M1 cấp Pro, 8GB RAM, 256GB lưu trữ nhanh SSD. Hỗ trợ Apple Pencil Gen 2, Magic Keyboard, USB-C Thunderbolt, pin 10 giờ.\n\n👉 Nhận xét: Best tablet for creators dưới 10 triệu, giá hợp lý.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\nMacBook Pro M3 là laptop Pro của Apple cho các developer, video editor, engineer. Hiệu năng ngang ngửa laptop gaming nhưng tiết kiệm điện hơn 80%, pin kéo 20 giờ.\n\n⚡ Chip M3 & Hiệu năng\nChip Apple Silicon 8-core CPU, 10-core GPU xử lý video/photo siêu mượt. 16GB RAM LPDDR5X, 1TB SSD NVMe thunder-fast 7GB/s. Hệ thống làm mát yên tĩnh, không quạt.\n\n🎨 Màn hình & Công nghệ\nMàn hình Liquid Retina XDR 14 inch 3200x2234, 1000 nits sáng, ProMotion 120Hz. 3 Thunderbolt 4, MagSafe charging, HDMI 2.1, pin thật 20 giờ, tặng kèm AppleCare.\n\n👉 Nhận xét: Nếu code/edít video, productive lên 5 lần, ROI của pro rõ ràng.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\nGoogle Pixel 8 nổi bật nhất là xử lý ảnh bằng AI - mang lại chất lượng ảnh chuyên nghiệp cho mọi người. Đây là điện thoại cho những người yêu thích photo tự nhiên, không filter.\n\n🤖 AI & Camera\n50MP chính f/1.7, Ultra Wide 48MP, zoom digital AI 8x không mất chất lượng. Magic Eraser xoá đối tượng thần kỳ, Best Take chọn biểu cảm tốt nhất, Face Unblur làm nét khuôn mặt.\n\n🎬 Video & Hiệu năng\nChip Tensor Gen 3 mạnh mẽ, Cinematic Pan video mượt như Hollywood. Pin 24 giờ, sạc nhanh 30W, IP68 chống nước, Gorilla Glass Victus 2.\n\n👉 Nhận xét: Tensor hơn về AI photo - chọn Pixel nếu quan tâm ảnh.',
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
        detailedDescription: '✨ Tổng quan sản phẩm\nAirPods Pro Max là tai nghe chùm cao cấp của Apple - perfect blend giữa chất lượng âm thanh audiophile và sự tiện lợi của Apple. Dành cho Apple fan hoặc audiophile.\n\n🎧 Âm thanh Lossless\nCông nghệ Spatial Audio với dynamic head tracking tạo cảm giác 3D chân thực. Khử ồn chủ động Level cực cao, Transparency mode sắc nét, âm thanh ALAC lossless siêu sạch.\n\n💎 Wearable & Pin\nKhung nhôm + vỏ nhựa cao cấp siêu thoải mái, có thể đeo 24/7. Pin 20 giờ, sạc nhanh 15 phút để nghe 1 giờ. Tích hợp mic giống conference room, isolation tuyệt vời.\n\n👉 Nhận xét: Mắc nhất nhưng quality vô địch, dành cho kẻ yêu âm nhạc thực sự.',
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

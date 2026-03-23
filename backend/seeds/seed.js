require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const Product = require('../models/Product');
const Promotion = require('../models/Promotion');

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
        images: ['https://picsum.photos/800/600?random=1'],
        stock: 15,
        isFeatured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'iPhone 15 Pro',
        price: 999000,
        category: 'Điện thoại',
        description: 'Điện thoại thông minh mới nhất',
        detailedDescription: '✨ Tổng quan sản phẩm\niPhone 15 Pro là flagship 2024 của Apple với thiết kế titanium cao cấp, camera 48MP cúp, và chip A17 Pro siêu mạnh. Đây là điện thoại yêu cầu bắt buộc cho các content creator.\n\n🎯 Thiết kế Titanium\nFramework titanium hạng hàng không vũ trụ chống rơi cực tốt. Viền lưng bằng kính Ceramic Shield tăng độ bền. Nút Action thay thế mute switch, có 4 màu: bạc, đen, vàng, tím đầy ấn tượng.\n\n📸 Camera & Hiệu năng\n48MP chính với zoom quang học 5x, A17 Pro xử lý video 4K 120fps. Pin 21 giờ, sạc nhanh 35W, IP69 kháng nước chuẩn.\n\n👉 Nhận xét: Flagship xứng đáng, video/photo quá đỉnh, giá vừa phải.',
        images: ['https://picsum.photos/800/600?random=2'],
        stock: 25,
        isFeatured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Samsung Galaxy Watch',
        price: 299000,
        category: 'Đồng hồ',
        description: 'Đồng hồ thông minh cao cấp',
        detailedDescription: '✨ Tổng quan sản phẩm\nSamsung Galaxy Watch là đồng hồ thông minh hàng đầu, kết hợp hoàn hảo giữa thời trang và công nghệ. Thích hợp cho những người yêu thích theo dõi sức khỏe, thể thao và lối sống lành mạnh.\n\n💎 Thiết kế & Màn hình\nMàn hình AMOLED 1.4 inch cực nét, viền thép không gỉ bóng loáng. Dây đeo da hoặc cao su tùy chọn, pin 5 ngày khi sử dụng tích cực.\n\n🏃 Tính năng Sức khỏe\nCảm biến nhịp tim chuẩn y tế, SpO2, đo căng thẳng, theo dõi giấc ngủ chi tiết. 100+ chế độ tập luyện, GPS chính xác, chống nước 5ATM bơi lội được.\n\n👉 Nhận xét: Companion hoàn hảo cho cuộc sống khỏe mạnh, giá tốt.',
        images: ['https://picsum.photos/800/600?random=3'],
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
        isFeatured: true,
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
        createdBy: adminUser._id      },
      {
        name: 'Bàn phím cơ Logitech MX Keys',
        price: 189000,
        category: 'Phụ kiện',
        description: 'Bàn phím cơ không dây cao cấp',
        detailedDescription: '✨ Tổng quan sản phẩm\nLogitech MX Keys là bàn phím cơ không dây cao cấp dành cho người dùng văn phòng và creator. Thiết kế ergonomic, kết nối đa thiết bị, pin kéo dài 10 ngày.\n\n⌨️ Thiết kế & Cảm giác gõ\nLayout full-size với switch cơ chất lượng cao, hành trình phím 1.8mm. Thiết kế cong nhẹ ergonomic, chống mỏi tay khi gõ lâu. Backlit RGB có thể điều chỉnh.\n\n🔋 Pin & Kết nối\nPin lithium-polymer kéo dài 10 ngày, sạc qua USB-C. Kết nối Bluetooth 5.0 + USB receiver, có thể kết nối đồng thời 3 thiết bị và chuyển đổi tức thì.\n\n👉 Nhận xét: Bàn phím office đỉnh cao, đáng đồng tiền bát gạo.',
        images: ['https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=800&q=60'],
        stock: 25,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Chuột Logitech MX Master 3S',
        price: 149000,
        category: 'Phụ kiện',
        description: 'Chuột không dây đa năng cao cấp',
        detailedDescription: '✨ Tổng quan sản phẩm\nLogitech MX Master 3S là chuột không dây đỉnh cao cho productivity. Thiết kế ergonomic hoàn hảo, cảm biến precision, cuộn đa hướng, kết nối đa thiết bị.\n\n🖱️ Ergonomic & Precision\nThiết kế ergonomic cho cả tay trái và phải, cảm biến Darkfield 8000 DPI siêu chính xác. Cuộn đa hướng MagSpeed cho tốc độ cuộn nhanh như bay.\n\n🔋 Kết nối & Pin\nKết nối Bluetooth + USB receiver, chuyển đổi giữa 3 thiết bị tức thì. Pin lithium-polymer kéo dài 70 ngày, sạc nhanh 3 phút cho 1 ngày sử dụng.\n\n👉 Nhận xét: Chuột productivity số 1 thế giới, investment xứng đáng.',
        images: ['https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=60'],
        stock: 30,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Màn hình LG 27UK650-W 4K',
        price: 699000,
        category: 'Màn hình',
        description: 'Màn hình 4K UHD cao cấp',
        detailedDescription: '✨ Tổng quan sản phẩm\nLG 27UK650-W là màn hình 4K UHD cao cấp với công nghệ IPS, màu sắc chính xác, HDR10. Hoàn hảo cho designer, photographer, và người dùng cần màu sắc chuẩn.\n\n🖥️ Màn hình & Hiển thị\nKích thước 27 inch, độ phân giải 4K UHD (3840x2160), tấm nền IPS. Công nghệ HDR10, dải màu 95% DCI-P3, độ sáng 350 nits, contrast ratio 1000:1.\n\n🔌 Kết nối & Tính năng\n2x HDMI 2.0, 1x DisplayPort 1.4, 2x USB 3.0. Công nghệ AMD FreeSync, Flicker-free, Blue Light Filter. Chân đế có thể điều chỉnh độ cao và nghiêng.\n\n👉 Nhận xét: Màn hình 4K giá hợp lý, màu sắc đẹp, đáng mua.',
        images: ['https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=60'],
        stock: 15,
        isFeatured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Loa Bluetooth JBL GO 3',
        price: 59000,
        category: 'Loa',
        description: 'Loa Bluetooth di động nhỏ gọn',
        detailedDescription: '✨ Tổng quan sản phẩm\nJBL GO 3 là loa Bluetooth di động nhỏ gọn với âm thanh JBL signature. Chống nước IPX7, pin 5 giờ, màu sắc đa dạng. Hoàn hảo cho người yêu thích âm nhạc di động.\n\n🔊 Âm thanh & Thiết kế\nDriver 40mm với âm trầm mạnh mẽ, âm treble sáng rõ. Thiết kế nhỏ gọn (7.6 x 8.7 x 3.1 cm), trọng lượng chỉ 184g. Chống nước IPX7 có thể ngâm dưới nước 1m trong 30 phút.\n\n🔋 Pin & Kết nối\nPin lithium-ion kéo dài 5 giờ, sạc qua micro USB. Kết nối Bluetooth 4.1, có thể kết nối 2 thiết bị cùng lúc. Có jack 3.5mm aux.\n\n👉 Nhận xét: Loa nhỏ nhưng âm to, chất lượng JBL chuẩn, giá rẻ.',
        images: ['https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=800&q=60'],
        stock: 40,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Ổ cứng SSD Samsung 970 EVO 1TB',
        price: 249000,
        category: 'Lưu trữ',
        description: 'Ổ cứng SSD NVMe tốc độ cao',
        detailedDescription: '✨ Tổng quan sản phẩm\nSamsung 970 EVO là ổ SSD NVMe tốc độ cao với công nghệ V-NAND 3-bit MLC. Tốc độ đọc lên đến 3400MB/s, ghi 2500MB/s. Hoàn hảo để nâng cấp máy tính.\n\n💾 Tốc độ & Hiệu năng\nTốc độ đọc tuần tự 3400MB/s, ghi 2500MB/s. Random read 500K IOPS, write 480K IOPS. Công nghệ Intelligent TurboWrite tối ưu hóa hiệu năng.\n\n🔧 Tương thích & Bảo hành\nForm factor M.2 2280, tương thích với PCIe Gen 3.0 x4. Bảo hành 5 năm hoặc 600TBW. Phần mềm Samsung Magician để quản lý.\n\n👉 Nhận xét: SSD tốc độ cao, đáng tin cậy, giá hợp lý.',
        images: ['https://images.unsplash.com/photo-1591488320449-011701bb6704?auto=format&fit=crop&w=800&q=60'],
        stock: 20,
        isFeatured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Router WiFi TP-Link Archer AX55',
        price: 189000,
        category: 'Mạng',
        description: 'Router WiFi 6 tốc độ cao',
        detailedDescription: '✨ Tổng quan sản phẩm\nTP-Link Archer AX55 là router WiFi 6 với tốc độ lên đến 2402Mbps. Công nghệ OFDMA, MU-MIMO, beamforming. Hoàn hảo cho gia đình nhiều thiết bị.\n\n📡 WiFi 6 & Tốc độ\nWiFi 6 (802.11ax), băng tần 2.4GHz 574Mbps + 5GHz 2402Mbps. Công nghệ OFDMA cho hiệu quả mạng cao, MU-MIMO cho nhiều thiết bị cùng lúc.\n\n🔒 Bảo mật & Tính năng\nWPA3 bảo mật cao, parental controls, guest network. 4 cổng Gigabit LAN, 1 WAN. App Tether để quản lý dễ dàng.\n\n👉 Nhận xét: Router WiFi 6 giá tốt, tốc độ nhanh, ổn định.',
        images: ['https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=60'],
        stock: 18,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Webcam Logitech C920 HD',
        price: 89000,
        category: 'Camera',
        description: 'Webcam HD 1080p cho streaming',
        detailedDescription: '✨ Tổng quan sản phẩm\nLogitech C920 HD là webcam phổ biến nhất cho streaming, video call. Độ phân giải 1080p, góc rộng 78°, autofocus, mic stereo. Hoàn hảo cho content creator.\n\n📹 Chất lượng hình ảnh\nĐộ phân giải 1080p 30fps, 720p 60fps. Góc nhìn 78°, autofocus nhanh, low-light correction. Màu sắc tự nhiên, chi tiết cao.\n\n🎙️ Âm thanh & Kết nối\n2 mic stereo với noise reduction, RightSound technology. Kết nối USB 2.0, tương thích universal. Tripod mount và clip để gắn màn hình.\n\n👉 Nhận xét: Webcam phổ biến nhất, chất lượng ổn định, giá rẻ.',
        images: ['https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?auto=format&fit=crop&w=800&q=60'],
        stock: 22,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Tai nghe gaming Razer BlackShark V2',
        price: 229000,
        category: 'Tai nghe',
        description: 'Tai nghe gaming với mic rời',
        detailedDescription: '✨ Tổng quan sản phẩm\nRazer BlackShark V2 là tai nghe gaming cao cấp với driver 50mm, mic rời, khử ồn. Thiết kế nhẹ, thoải mái cho gaming marathon.\n\n🎮 Gaming Features\nDriver 50mm với âm thanh surround 7.1 (virtual). Mic rời với noise cancellation, có thể gập lại. Khử ồn môi trường, âm thanh game rõ ràng.\n\n💎 Comfort & Design\nThiết kế over-ear với đệm tai memory foam, trọng lượng 262g. Dây 1.8m braided, jack 3.5mm. Logo Razer Chroma RGB.\n\n👉 Nhận xét: Tai nghe gaming chất lượng, mic tốt, thoải mái.',
        images: ['https://images.unsplash.com/photo-1599669454699-248893623440?auto=format&fit=crop&w=800&q=60'],
        stock: 16,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Bàn phím gaming Corsair K57 RGB',
        price: 159000,
        category: 'Phụ kiện',
        description: 'Bàn phím gaming cơ RGB',
        detailedDescription: '✨ Tổng quan sản phẩm\nCorsair K57 RGB là bàn phím gaming cơ với switch Cherry MX Red, RGB per-key, aluminum frame. Compact 75% layout, hoàn hảo cho gaming và typing.\n\n⌨️ Switch & Layout\nSwitch Cherry MX Red (linear, không click), hành trình 2mm. Layout 75% compact, thiếu numpad. RGB per-key với 16.8 triệu màu.\n\n🎮 Gaming Features\nAnti-ghosting 100%, polling rate 1000Hz. Phím media dedicated, detachable USB-C cable. Software Corsair iCUE để customize.\n\n👉 Nhận xét: Bàn phím gaming compact, RGB đẹp, typing tốt.',
        images: ['https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=800&q=60'],
        stock: 14,
        isFeatured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Chuột gaming Logitech G305',
        price: 59000,
        category: 'Phụ kiện',
        description: 'Chuột gaming không dây siêu nhẹ',
        detailedDescription: '✨ Tổng quan sản phẩm\nLogitech G305 là chuột gaming không dây siêu nhẹ chỉ 99g. Pin kéo dài 250 giờ, HERO sensor 12000 DPI. Hoàn hảo cho FPS gaming.\n\n🖱️ Sensor & Performance\nHERO sensor thế hệ mới, DPI 200-12000, IPS 400. Polling rate 1000Hz, zero smoothing. Trọng lượng chỉ 99g, thiết kế ambidextrous.\n\n🔋 Pin & Kết nối\nPin AA kéo dài 250 giờ, có thể thay pin. Kết nối Lightspeed wireless, range 10m. On-board memory cho 5 profiles.\n\n👉 Nhận xét: Chuột gaming không dây nhẹ nhất, pin trâu, giá rẻ.',
        images: ['https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=60'],
        stock: 28,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Màn hình gaming Samsung Odyssey G7',
        price: 1299000,
        category: 'Màn hình',
        description: 'Màn hình gaming cong 144Hz QHD',
        detailedDescription: '✨ Tổng quan sản phẩm\nSamsung Odyssey G7 là màn hình gaming đỉnh cao với tấm nền VA cong 1000R, 144Hz, QHD. HDR1000, Quantum Dot. Hoàn hảo cho gaming enthusiast.\n\n🖥️ Gaming Specs\nKích thước 27 inch cong 1000R, độ phân giải 1440p QHD. Tần số quét 144Hz, response time 1ms. Công nghệ HDR1000, dải màu 125% sRGB.\n\n🎮 Tính năng\nAMD FreeSync Premium Pro, G-Sync compatible. Quantum Dot, Core Lighting. Cổng HDMI 2.1, DisplayPort 1.4. Chân đế có thể điều chỉnh.\n\n👉 Nhận xét: Màn hình gaming đỉnh cao, hình ảnh tuyệt đẹp.',
        images: ['https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=60'],
        stock: 8,
        isFeatured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Loa soundbar Samsung HW-K360',
        price: 299000,
        category: 'Loa',
        description: 'Soundbar 2.1 kênh với subwoofer',
        detailedDescription: '✨ Tổng quan sản phẩm\nSamsung HW-K360 là soundbar 2.1 kênh với subwoofer wireless. Công nghệ Dolby Digital, DTS. Tăng cường âm thanh cho TV, hoàn hảo cho phòng khách.\n\n🔊 Âm thanh\n2 kênh chính + subwoofer wireless, công suất 300W. Công nghệ Dolby Digital, DTS decoder. Bass reflex design cho âm trầm mạnh mẽ.\n\n📺 Tính năng\nKết nối HDMI ARC, optical, Bluetooth. Điều khiển bằng remote, app Samsung SmartThings. Chế độ âm thanh: Standard, Music, Movie, Voice.\n\n👉 Nhận xét: Soundbar giá tốt, âm thanh cải thiện rõ rệt.',
        images: ['https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=800&q=60'],
        stock: 12,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Ổ cứng di động WD My Passport 2TB',
        price: 149000,
        category: 'Lưu trữ',
        description: 'Ổ cứng di động 2TB chống sốc',
        detailedDescription: '✨ Tổng quan sản phẩm\nWD My Passport là ổ cứng di động 2.5 inch với dung lượng 2TB. Thiết kế nhỏ gọn, chống sốc, bảo mật. Hoàn hảo để backup và di chuyển dữ liệu.\n\n💾 Dung lượng & Tốc độ\nDung lượng 2TB, tốc độ USB 3.0 lên đến 5Gbps. Tương thích với USB 2.0. Phần mềm WD Backup và WD Security để bảo mật.\n\n🛡️ Bảo mật & Độ bền\nMã hóa AES 256-bit, password protection. Thiết kế chống sốc, rơi từ độ cao 1.22m. Bảo hành 3 năm.\n\n👉 Nhận xét: Ổ cứng di động đáng tin cậy, dung lượng lớn.',
        images: ['https://images.unsplash.com/photo-1591488320449-011701bb6704?auto=format&fit=crop&w=800&q=60'],
        stock: 25,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Switch mạng TP-Link TL-SG108',
        price: 249000,
        category: 'Mạng',
        description: 'Switch Gigabit 8 cổng không quản lý',
        detailedDescription: '✨ Tổng quan sản phẩm\nTP-Link TL-SG108 là switch Gigabit 8 cổng không quản lý. Tốc độ 1000Mbps, plug-and-play. Hoàn hảo để mở rộng mạng gia đình hoặc văn phòng nhỏ.\n\n🌐 Tính năng\n8 cổng Gigabit Ethernet, tốc độ 10/100/1000Mbps. Auto-negotiation, auto-MDI/MDIX. Chế độ Energy Efficient Ethernet tiết kiệm điện.\n\n🔌 Thiết kế\nThiết kế desktop nhỏ gọn, quạt tản nhiệt yên tĩnh. Đèn LED hiển thị trạng thái. Bảo hành 3 năm.\n\n👉 Nhận xét: Switch giá rẻ, ổn định, dễ dùng.',
        images: ['https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=60'],
        stock: 15,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Microphone Blue Yeti USB',
        price: 349000,
        category: 'Microphone',
        description: 'Microphone USB đa hướng cho streaming',
        detailedDescription: '✨ Tổng quan sản phẩm\nBlue Yeti USB là microphone USB đa hướng với 4 polar patterns. Chất lượng âm thanh studio, hoàn hảo cho podcast, streaming, recording.\n\n🎙️ Chất lượng âm thanh\nCapsule condenser 192kHz/24-bit, 4 polar patterns: Cardioid, Omni, Bidirectional, Stereo. Tần số đáp ứng 20Hz-20kHz.\n\n🔧 Tính năng\nGain control, mute button, headphone jack 3.5mm. Zero-latency monitoring. Tương thích macOS, Windows, Linux.\n\n👉 Nhận xét: Microphone phổ biến cho content creator, âm thanh tốt.',
        images: ['https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?auto=format&fit=crop&w=800&q=60'],
        stock: 10,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Bàn phím Apple Magic Keyboard',
        price: 229000,
        category: 'Phụ kiện',
        description: 'Bàn phím không dây cho Mac',
        detailedDescription: '✨ Tổng quan sản phẩm\nApple Magic Keyboard là bàn phím không dây cho Mac với thiết kế mỏng, kết nối Bluetooth. Pin kéo dài 1 tháng, layout optimized cho macOS.\n\n⌨️ Thiết kế & Layout\nThiết kế mỏng chỉ 0.41 inch, trọng lượng 231g. Layout optimized cho macOS với các phím đặc biệt. Cảm giác gõ yên tĩnh, hành trình phím thấp.\n\n🔋 Pin & Kết nối\nPin lithium-ion kéo dài khoảng 1 tháng. Kết nối Bluetooth, có thể kết nối nhiều thiết bị. Sạc qua Lightning.\n\n👉 Nhận xét: Bàn phím Mac chuẩn, mỏng đẹp, pin trâu.',
        images: ['https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=800&q=60'],
        stock: 20,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Chuột Apple Magic Mouse 2',
        price: 149000,
        category: 'Phụ kiện',
        description: 'Chuột không dây cảm ứng cho Mac',
        detailedDescription: '✨ Tổng quan sản phẩm\nApple Magic Mouse 2 là chuột không dây cảm ứng cho Mac. Thiết kế một mảnh kính, Multi-Touch surface. Pin kéo dài 1 tháng.\n\n🖱️ Multi-Touch\nBề mặt kính Multi-Touch, hỗ trợ gestures quen thuộc: scroll, swipe, zoom. Thiết kế một mảnh liền lạc, không có moving parts.\n\n🔋 Pin & Kết nối\nPin lithium-ion kéo dài 1 tháng. Kết nối Bluetooth, sạc qua Lightning. Tự động kết nối khi mở nắp Mac.\n\n👉 Nhận xét: Chuột Mac chuẩn, gestures mượt mà, thiết kế đẹp.',
        images: ['https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=60'],
        stock: 18,
        isFeatured: false,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Màn hình Apple Studio Display',
        price: 3999000,
        category: 'Màn hình',
        description: 'Màn hình 5K Retina cho Mac',
        detailedDescription: '✨ Tổng quan sản phẩm\nApple Studio Display là màn hình 5K Retina cao cấp cho Mac. Độ phân giải 5120x2880, True Tone, camera 12MP. Hoàn hảo cho creative professionals.\n\n🖥️ Retina 5K\nKích thước 27 inch, độ phân giải 5K (5120x2880), 218 ppi. Tấm nền IPS, độ sáng 600 nits, dải màu P3. Công nghệ True Tone, ProMotion 60Hz.\n\n📷 Camera & Audio\nCamera 12MP Ultra Wide với Center Stage. 3 mic studio quality, 4 loa với Spatial Audio. Jack 3.5mm headphone.\n\n👉 Nhận xét: Màn hình Mac đỉnh cao, camera tốt, audio tuyệt vời.',
        images: ['https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=60'],
        stock: 5,
        isFeatured: true,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Loa Apple HomePod mini',
        price: 299000,
        category: 'Loa',
        description: 'Loa thông minh với Siri',
        detailedDescription: '✨ Tổng quan sản phẩm\nApple HomePod mini là loa thông minh nhỏ gọn với Siri. Âm thanh 360°, Spatial Audio, HomeKit. Hoàn hảo cho smart home.\n\n🔊 Âm thanh\nDriver full-range, passive radiator. Công nghệ Spatial Audio, Adaptive Audio. Tương thích với Apple Music Lossless.\n\n🤖 Smart Features\nSiri voice assistant, HomeKit hub. Nhận diện giọng nói, multi-room audio. Điều khiển smart home devices.\n\n👉 Nhận xét: Loa smart home tốt, âm thanh bất ngờ, Siri hữu ích.',
        images: ['https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=800&q=60'],
        stock: 15,
        isFeatured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Ổ cứng SSD WD Black SN850 1TB',
        price: 329000,
        category: 'Lưu trữ',
        description: 'Ổ SSD NVMe gaming tốc độ cao',
        detailedDescription: '✨ Tổng quan sản phẩm\nWD Black SN850 là ổ SSD NVMe dành cho gaming với tốc độ lên đến 7000MB/s. Công nghệ WD Black technology, hoàn hảo cho gaming PC.\n\n💾 Tốc độ Gaming\nTốc độ đọc 7000MB/s, ghi 5300MB/s. Game Mode technology, WD Black Dashboard. Tương thích với PS5.\n\n🔧 Tính năng\nHeatsink aluminum sẵn có, bảo hành 5 năm. Phần mềm WD Black Dashboard để monitoring. Form factor M.2 2280.\n\n👉 Nhận xét: SSD gaming tốc độ cao, heatsink tốt, đáng mua.',
        images: ['https://images.unsplash.com/photo-1591488320449-011701bb6704?auto=format&fit=crop&w=800&q=60'],
        stock: 12,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id
      },
      {
        name: 'Router mesh TP-Link Deco X20',
        price: 899000,
        category: 'Mạng',
        description: 'Hệ thống router mesh WiFi 6',
        detailedDescription: '✨ Tổng quan sản phẩm\nTP-Link Deco X20 là hệ thống router mesh WiFi 6 với 2 pack. Bao phủ toàn nhà, tốc độ 1200Mbps, EasyMesh. Hoàn hảo cho nhà lớn.\n\n🏠 Mesh Network\nCông nghệ EasyMesh, seamless roaming. 2 pack bao phủ lên đến 600m². Tốc độ 1200Mbps trên băng tần 5GHz.\n\n🔒 Bảo mật & Quản lý\nWPA3 bảo mật, parental controls. App Deco để quản lý dễ dàng. Alexa và Google Assistant tương thích.\n\n👉 Nhận xét: Router mesh tốt, setup dễ, bao phủ rộng.',
        images: ['https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=60'],
        stock: 8,
        isFeatured: false,
        hot: true,
        createdBy: adminUser._id
      },
      {
        name: 'Webcam Sony ZV-1',
        price: 189000,
        category: 'Camera',
        description: 'Webcam 4K cho content creator',
        detailedDescription: '✨ Tổng quan sản phẩm\nSony ZV-1 là webcam 4K với cảm biến 1 inch, autofocus nhanh, mic stereo. Thiết kế cho vlogger, hoàn hảo cho streaming chất lượng cao.\n\n📹 Chất lượng 4K\nCảm biến 1 inch stacked CMOS, độ phân giải 4K 30fps, 1080p 60fps. Autofocus nhanh, eye AF. Góc nhìn 84°.\n\n🎙️ Audio & Tính năng\n3 mic capsule stereo, noise reduction. Background blur, beauty effect. Kết nối USB-C, tripod mount.\n\n👉 Nhận xét: Webcam chất lượng cao, màu sắc Sony đẹp.',
        images: ['https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?auto=format&fit=crop&w=800&q=60'],
        stock: 10,
        isFeatured: true,
        hot: false,
        createdBy: adminUser._id      }
    ];

    const createdProducts = await Product.insertMany(products);
    console.log(`✓ Created ${createdProducts.length} test products`);
    return createdProducts;
  } catch (err) {
    console.error('✗ Error seeding products:', err.message);
  }
};

const seedPromotions = async () => {
  try {
    // Clear existing promotions
    await Promotion.deleteMany({});
    console.log('✓ Cleared existing promotions');

    // Get admin user for createdBy
    const adminUser = await User.findOne({ role: 'admin' });
    if (!adminUser) {
      console.error('✗ No admin user found, skipping promotion seeding');
      return;
    }

    const promotions = [
      {
        name: 'Giảm 10% cho đơn hàng từ 1 triệu',
        description: 'Giảm 10% cho đơn hàng từ 1.000.000₫',
        type: 'percentage',
        value: 10,
        code: 'SALE10',
        conditions: {
          minOrderValue: 1000000,
          maxDiscount: 500000
        },
        isActive: true,
        startDate: new Date(),
        endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
        createdBy: adminUser._id
      },
      {
        name: 'Giảm 50.000₫ cho đơn từ 500.000₫',
        description: 'Giảm cố định 50.000₫ cho đơn hàng từ 500.000₫',
        type: 'fixed',
        value: 50000,
        code: 'FIXED50',
        conditions: {
          minOrderValue: 500000
        },
        isActive: true,
        startDate: new Date(),
        endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
        createdBy: adminUser._id
      },
      {
        name: 'Flash Sale - Giảm 20%',
        description: 'Giảm 20% cho tất cả sản phẩm Laptop',
        type: 'percentage',
        value: 20,
        code: 'LAPTOP20',
        conditions: {
          applicableCategories: ['Máy tính'],
          maxDiscount: 2000000
        },
        isActive: true,
        startDate: new Date(),
        endDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
        createdBy: adminUser._id
      }
    ];

    const createdPromotions = await Promotion.insertMany(promotions);
    console.log(`✓ Created ${createdPromotions.length} test promotions`);
    return createdPromotions;
  } catch (err) {
    console.error('✗ Error seeding promotions:', err.message);
  }
};

const seedDatabase = async () => {
  try {
    await connectDB();
    await seedUsers();
    await seedProducts();
    await seedPromotions();
    
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

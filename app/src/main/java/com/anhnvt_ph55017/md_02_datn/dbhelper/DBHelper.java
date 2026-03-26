package com.anhnvt_ph55017.md_02_datn.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anhnvt_ph55017.md_02_datn.R;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "coretech2.db";
    private static final int DB_VERSION = 29; // 🔥 v29: Thêm rating + comment cho đánh giá đơn hàng

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* ================= USERS ================= */
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "fullname TEXT," +
                        "email TEXT UNIQUE," +
                        "password TEXT," +
                        "phone TEXT," +
                        "address TEXT," +
                        "dateOfBirth TEXT," +
                        "isActive INTEGER DEFAULT 1," +
                        "profilePicture TEXT," +
                        "createdAt TEXT," +
                        "updatedAt TEXT" +
                        ")"
        );

        /* ================= CATEGORIES ================= */
        db.execSQL(
                "CREATE TABLE categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "description TEXT," +
                        "image INTEGER," +
                        "status INTEGER DEFAULT 1," +
                        "createdAt TEXT," +
                        "updatedAt TEXT" +
                        ")"
        );

        /* ================= PRODUCTS ================= */
        db.execSQL(
                "CREATE TABLE products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "categoryId INTEGER," +
                        "name TEXT," +
                        "price REAL," +
                        "oldPrice REAL," +
                        "image INTEGER," +
                        "description TEXT," +
                        "stock INTEGER," +
                        "rating REAL DEFAULT 0," +
                        "reviewCount INTEGER DEFAULT 0," +
                        "isFavorite INTEGER DEFAULT 0," +
                        "status INTEGER DEFAULT 1," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(categoryId) REFERENCES categories(id)" +
                        ")"
        );
        //-------------------review
        db.execSQL(
                "CREATE TABLE reviews (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "productId INTEGER," +
                        "userId INTEGER," +
                        "rating REAL," +
                        "comment TEXT," +
                        "createdAt TEXT," +
                        "FOREIGN KEY(productId) REFERENCES products(id)," +
                        "FOREIGN KEY(userId) REFERENCES users(id)" +
                        ")"
        );
        // ===== DATA MẪU =====
        db.execSQL("INSERT INTO categories(name,image) VALUES " + "('Laptop'," + R.drawable.ic_laptop + ")," + "('Điện thoại'," + R.drawable.ic_phone + ")," + "('Tai nghe'," + R.drawable.ic_headphone + ")," + "('Phụ kiện'," + R.drawable.ic_box + ")," + "('Đồng hồ'," + R.drawable.ic_notifications + ")" );
        db.execSQL( "INSERT INTO products(categoryId,name,price,oldPrice,image,description,stock,rating,reviewCount,isFavorite) VALUES " + "(1,'MacBook Pro M3',2500,3000," + R.drawable.anh1 + ",'Laptop mạnh mẽ với chip Apple M3, RAM 16GB và màn hình Retina tuyệt đẹp. Hoàn hảo cho lập trình viên và nhà sáng tạo.',10,4.8,120,0)," + "(1,'Dell XPS 15',2400,2800," + R.drawable.laptop2 + ",'Laptop Windows cao cấp với hiệu suất mạnh mẽ, thiết kế sang trọng và màn hình tuyệt vời.',10,4.7,110,0)," + "(2,'iPhone 15',1200,1400," + R.drawable.anh22 + ",'Smartphone Apple mới nhất với chip A17, dynamic island, camera tốt hơn và pin cả ngày.',20,4.6,95,0)," + "(2,'Samsung Galaxy S25',1250,1450," + R.drawable.anh23 + ",'Điện thoại Samsung flagship với xử lý nhanh, hệ thống camera nâng cao và màn hình AMOLED đẹp.',20,4.7,100,0)," + "(3,'Sony WH-1000XM5',500,650," + R.drawable.anh31 + ",'Tai nghe khử tiếng ồn hàng đầu ngành với âm thanh sống động và pin 30 tiếng.',15,4.9,210,0)," + "(3,'AirPods Pro 2',450,550," + R.drawable.anh32 + ",'Tai nghe không dây Apple cao cấp với khử tiếng ồn chủ động và âm thanh không gian.',18,4.8,180,0)," + "(2,'Google Pixel 8 Pro',1100,1300," + R.drawable.anh24 + ",'Điện thoại flagship Google với tính năng camera AI và trải nghiệm Android nguyên bản.',22,4.6,90,0)," + "(3,'JBL Tune 760NC',300,400," + R.drawable.anh33 + ",'Tai nghe không dây với khử tiếng ồn chủ động và âm bass sâu.',25,4.5,75,0)," + "(4,'Cáp sạc iPhone',25,35," + R.drawable.anh41 + ",'Cáp Lightning chuẩn Apple, bền bỉ và an toàn cho mọi thiết bị.',50,4.7,145,0)," + "(4,'Ốp lưng Samsung',15,25," + R.drawable.anh42 + ",'Ốp lưng chống sốc cao cấp, bảo vệ điện thoại khỏi va đập.',40,4.6,110,0)," + "(4,'Pin dự phòng 20000mAh',35,50," + R.drawable.anh43 + ",'Pin dự phòng dung lượng lớn, sạc nhanh và an toàn.',35,4.8,190,0)," + "(5,'Apple Watch Series 9',399,499," + R.drawable.anh51 + ",'Đồng hồ thông minh Apple mới nhất với thiết kế thanh lịch và tính năng sức khỏe toàn diện.',12,4.9,230,0)," + "(5,'Samsung Galaxy Watch 6',299,399," + R.drawable.anh52 + ",'Đồng hồ thông minh Samsung với màn hình AMOLED sắc nét và pin lâu dài.',15,4.7,165,0)," + "(5,'Fitbit Charge 6',199,279," + R.drawable.anh53 + ",'Vòng theo dõi sức khỏe Fitbit với cảm biến hiện đại và ứng dụng thông minh.',20,4.6,125,0)" );

        /* ================= CARTS ================= */
        db.execSQL(
                "CREATE TABLE carts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "userId INTEGER," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(userId) REFERENCES users(id)" +
                        ")"
        );

        /* ================= CART ITEMS ================= */
        db.execSQL(
                "CREATE TABLE cart_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "cartId INTEGER," +
                        "productId INTEGER," +
                        "quantity INTEGER," +
                        "FOREIGN KEY(cartId) REFERENCES carts(id)," +
                        "FOREIGN KEY(productId) REFERENCES products(id)" +
                        ")"
        );

        /* ================= VOUCHERS ================= */
        db.execSQL(
                "CREATE TABLE vouchers (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "sellerId INTEGER," +
                        "type TEXT," +
                        "discount REAL," +
                        "condition REAL," +
                        "limitCount INTEGER," +
                        "stock INTEGER," +
                        "startAt TEXT," +
                        "endAt TEXT," +
                        "isDisable INTEGER DEFAULT 0," +
                        "updatedAt TEXT" +
                        ")"
        );

        /* ================= ORDERS ================= */
        db.execSQL(
                "CREATE TABLE orders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "userId INTEGER," +
                        "voucherId INTEGER," +
                        "payment TEXT," +
                        "note TEXT," +
                        "cancellationReason TEXT," +
                        "address TEXT," +
                        "total REAL," +
                        "status TEXT," +
                        "shipDiscount REAL," +
                        "shipCost REAL," +
                        "imageRes INTEGER DEFAULT 0," +
                        "rating REAL DEFAULT 0," +
                        "reviewComment TEXT," +
                        "reviewedAt TEXT," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(userId) REFERENCES users(id)," +
                        "FOREIGN KEY(voucherId) REFERENCES vouchers(id)" +
                        ")"
        );

        /* ================= ADDRESSES ================= */
        db.execSQL(
                "CREATE TABLE addresses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "userId INTEGER," +
                        "name TEXT," +
                        "phone TEXT," +
                        "address TEXT," +
                        "isDefault INTEGER DEFAULT 0," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(userId) REFERENCES users(id)" +
                        ")"
        );

        /* ================= ORDER ITEMS ================= */
        db.execSQL(
                "CREATE TABLE order_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "orderId INTEGER," +
                        "productId INTEGER," +
                        "quantity INTEGER," +
                        "price REAL," +
                        "variant TEXT," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(orderId) REFERENCES orders(id)," +
                        "FOREIGN KEY(productId) REFERENCES products(id)" +
                        ")"
        );

        /* ================= SEARCH HISTORY ================= */
        db.execSQL(
                "CREATE TABLE search_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "keyword TEXT UNIQUE," +
                        "createdAt TEXT" +
                        ")"
        );

        /* ================= SAMPLE ORDERS (cho Orders Fragment) ================= */
        // Đơn hàng 1: Đã nhận + ĐÃ ĐÁNH GIÁ (DELIVERED + REVIEWED)
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Thanh toán khi nhận hàng','Giao hàng chặn đầu','123 Đường Lê Lợi, Quận 1, TP.HCM',2750.0,'Đã nhận'," + R.drawable.anh1 + ",NULL,4.5,'Sản phẩm rất tốt, giao hàng nhanh, đóng gói chắc chắn. Rất hài lòng!','2025-03-19 15:30:00','2025-03-15 10:30:00','2025-03-18 14:20:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(1,1,1,2500.0,'Space Gray','2025-03-15 10:30:00','2025-03-15 10:30:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(1,4,1,250.0,'Trắng','2025-03-15 10:30:00','2025-03-15 10:30:00')");

        // Đơn hàng 2: Đang giao hàng (SHIPPING)
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Thanh toán khi nhận hàng','','456 Đường Nguyễn Huệ, Quận 1, TP.HCM',1320.0,'Đang giao hàng'," + R.drawable.anh22 + ",NULL,0,NULL,NULL,'2025-03-20 09:15:00','2025-03-22 08:45:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(2,3,1,1200.0,'Đen','2025-03-20 09:15:00','2025-03-20 09:15:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(2,10,1,120.0,'Đỏ','2025-03-20 09:15:00','2025-03-20 09:15:00')");

        // Đơn hàng 3: Đang xử lý (PROCESSING)
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Sử dụng thẻ','Ưu tiên giao','789 Đường Hàm Nghi, Quận 5, TP.HCM',550.0,'Đang xử lý'," + R.drawable.anh31 + ",NULL,0,NULL,NULL,'2025-03-23 14:45:00','2025-03-23 16:00:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(3,5,1,500.0,'Bạc','2025-03-23 14:45:00','2025-03-23 14:45:00')");

        // Đơn hàng 4: Chưa thanh toán (NOT PAID)
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Thanh toán khi nhận hàng','Liên hệ trước khi giao','321 Đường Trần Hung Đạo, Quận 1, TP.HCM',478.0,'Chưa thanh toán'," + R.drawable.anh32 + ",NULL,0,NULL,NULL,'2025-03-24 11:20:00','2025-03-24 11:20:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(4,6,1,450.0,'Trắng','2025-03-24 11:20:00','2025-03-24 11:20:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(4,11,1,28.0,'Đen','2025-03-24 11:20:00','2025-03-24 11:20:00')");

        // Đơn hàng 5: Đã hủy (CANCELLED) - với lý do hủy
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Thanh toán khi nhận hàng','','000 Đường Test, TP.HCM',1210.0,'Đã hủy'," + R.drawable.anh24 + ",'Khách hủy đơn - lý do cá nhân',0,NULL,NULL,'2025-03-10 08:30:00','2025-03-11 10:15:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(5,7,1,1100.0,'Xám','2025-03-10 08:30:00','2025-03-10 08:30:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(5,9,1,110.0,'Xanh','2025-03-10 08:30:00','2025-03-10 08:30:00')");

        // Đơn hàng 6: Đã nhận - nhiều items + ĐÃ ĐÁNH GIÁ
        db.execSQL("INSERT INTO orders(userId,payment,note,address,total,status,imageRes,cancellationReason,rating,reviewComment,reviewedAt,createdAt,updatedAt) VALUES " +
                "(1,'Thanh toán khi nhận hàng','Giao sáng sớm','555 Đường Pasteur, Quận 3, TP.HCM',2310.0,'Đã nhận'," + R.drawable.anh52 + ",NULL,5.0,'Tuyệt vời! Tất cả sản phẩm đúng như mô tả, chất lượng cao. Sẽ mua lại lần nữa.','2025-03-08 11:00:00','2025-03-05 15:00:00','2025-03-08 09:30:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(6,13,1,299.0,'Đen','2025-03-05 15:00:00','2025-03-05 15:00:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(6,14,1,199.0,'Đỏ','2025-03-05 15:00:00','2025-03-05 15:00:00')");
        db.execSQL("INSERT INTO order_items(orderId,productId,quantity,price,variant,createdAt,updatedAt) VALUES " +
                "(6,8,2,150.0,'Xám','2025-03-05 15:00:00','2025-03-05 15:00:00')");

        /* ================= SAMPLE ADDRESSES ================= */
        db.execSQL("INSERT INTO addresses(userId,name,phone,address,isDefault,createdAt,updatedAt) VALUES " +
                "(1,'Nguyễn Văn A','0123456789','123 Đường Lê Lợi, Quận 1, TP.HCM',1,'2025-01-01 10:00:00','2025-03-01 10:00:00')");
        db.execSQL("INSERT INTO addresses(userId,name,phone,address,isDefault,createdAt,updatedAt) VALUES " +
                "(1,'Nguyễn Văn A','0123456789','456 Đường Nguyễn Huệ, Quận 1, TP.HCM',0,'2025-02-15 11:00:00','2025-02-15 11:00:00')");
        db.execSQL("INSERT INTO addresses(userId,name,phone,address,isDefault,createdAt,updatedAt) VALUES " +
                "(1,'Nguyễn Văn A','0987654321','789 Đường Hàm Nghi, Quận 5, TP.HCM',0,'2025-03-01 12:00:00','2025-03-01 12:00:00')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // For older versions, drop and recreate (dev mode)

            db.execSQL("DROP TABLE IF EXISTS search_history");
            db.execSQL("DROP TABLE IF EXISTS order_items");
            db.execSQL("DROP TABLE IF EXISTS orders");
            db.execSQL("DROP TABLE IF EXISTS vouchers");
            db.execSQL("DROP TABLE IF EXISTS cart_items");
            db.execSQL("DROP TABLE IF EXISTS carts");
            db.execSQL("DROP TABLE IF EXISTS products");
            db.execSQL("DROP TABLE IF EXISTS reviews");
            db.execSQL("DROP TABLE IF EXISTS categories");
            db.execSQL("DROP TABLE IF EXISTS users");
            onCreate(db);

    }
}
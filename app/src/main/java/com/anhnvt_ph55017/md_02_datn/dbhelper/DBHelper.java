package com.anhnvt_ph55017.md_02_datn.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anhnvt_ph55017.md_02_datn.R;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "coretech.db";
    private static final int DB_VERSION = 7; // 🔥 tăng version

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
        db.execSQL("INSERT INTO categories(name,image) VALUES " +
                "('Laptop'," + R.drawable.ic_laptop + ")," +
                "('Phone'," + R.drawable.ic_phone + ")," +
                "('Headphone'," + R.drawable.ic_headphone + ")"
        );

        db.execSQL(
                "INSERT INTO products(categoryId,name,price,oldPrice,image,stock,rating,reviewCount,isFavorite) VALUES " +
                        "(1,'MacBook Pro',2500,3000," + R.drawable.anh1 + ",10,4.8,120,0)," +
                        "(2,'iPhone 15',1200,1400," + R.drawable.anh2 + ",20,4.6,95,0)," +
                        "(3,'Sony WH-1000XM5',500,650," + R.drawable.anh3 + ",15,4.9,210,0)"
        );

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
                        "address TEXT," +
                        "total REAL," +
                        "status TEXT," +
                        "shipDiscount REAL," +
                        "shipCost REAL," +
                        "createdAt TEXT," +
                        "updatedAt TEXT," +
                        "FOREIGN KEY(userId) REFERENCES users(id)," +
                        "FOREIGN KEY(voucherId) REFERENCES vouchers(id)" +
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
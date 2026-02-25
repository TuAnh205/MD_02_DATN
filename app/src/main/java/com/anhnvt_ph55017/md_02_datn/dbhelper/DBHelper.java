package com.anhnvt_ph55017.md_02_datn.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anhnvt_ph55017.md_02_datn.R;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "coretech.db";
    private static final int DB_VERSION = 2; // 🔥 BẮT BUỘC > 1

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // USERS
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "fullname TEXT," +
                        "email TEXT UNIQUE," +
                        "phone TEXT," +
                        "password TEXT)"
        );

        // CATEGORY
        db.execSQL(
                "CREATE TABLE category (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "icon INTEGER)"
        );

        // PRODUCT
        db.execSQL(
                "CREATE TABLE product (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "price REAL," +
                        "image INTEGER)"
        );

        // DỮ LIỆU MẪU CATEGORY
        db.execSQL(
                "INSERT INTO category(name, icon) VALUES " +
                        "('Laptop'," + R.drawable.ic_laptop + ")," +
                        "('Phone'," + R.drawable.ic_phone + ")," +
                        "('Headphone'," + R.drawable.ic_headphone + ")"
        );

        // DỮ LIỆU MẪU PRODUCT
        db.execSQL(
                "INSERT INTO product(name, price, image) VALUES " +
                        "('Macbook Pro',2499," + R.drawable.anh1 + ")," +
                        "('iPhone 15',1999," + R.drawable.anh2 + ")," +
                        "('AirPods Pro',399," + R.drawable.anh3 + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS product");
        onCreate(db);
    }
}
package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;

public class UserDAO {

    private DBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // Đăng ký
    public boolean register(String fullname, String email, String phone, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);

        long result = db.insert("users", null, values);
        return result != -1;
    }

    // Kiểm tra đăng nhập
    public boolean login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password}
        );

        boolean ok = cursor.getCount() > 0;
        cursor.close();
        return ok;
    }

    // Check email tồn tại chưa
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ?",
                new String[]{email}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}

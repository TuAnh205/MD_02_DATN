package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.User;

public class UserDAO {

    private DBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // REGISTER
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

    // LOGIN
    public boolean login(String identifier, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Allow login by email or phone
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE (email = ? OR phone = ?) AND password = ?",
                new String[]{identifier, identifier, password}
        );

        boolean ok = cursor.moveToFirst();
        cursor.close();
        return ok;
    }

    // CHECK EMAIL
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE email = ?",
                new String[]{email}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
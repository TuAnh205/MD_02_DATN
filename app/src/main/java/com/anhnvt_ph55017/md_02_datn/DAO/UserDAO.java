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

        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", normalizedEmail);
        values.put("phone", phone);
        values.put("password", password);

        long result = db.insert("users", null, values);
        return result != -1;
    }

    // LOGIN
    public boolean login(String identifier, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        String normalizedEmail = normalizedIdentifier.toLowerCase();

        // Allow login by email (case-insensitive) or phone
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE ((LOWER(email) = ? OR phone = ?) AND password = ?)",
                new String[]{normalizedEmail, normalizedIdentifier, password}
        );

        boolean ok = cursor.moveToFirst();
        cursor.close();
        return ok;
    }

    // CHECK EMAIL
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE LOWER(email) = ?",
                new String[]{normalizedEmail}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
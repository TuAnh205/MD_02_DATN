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

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // CHECK EMAIL
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE LOWER(email) = ?",
                new String[]{normalizedEmail}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // INSERT OR UPDATE USER FROM FIREBASE/SERVER
    public boolean insertOrUpdateUser(String fullname, String email, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        // Check if user exists
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE LOWER(email) = ?",
                new String[]{normalizedEmail}
        );

        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", normalizedEmail);
        values.put("phone", phone);

        long result;
        try {
            if (cursor.moveToFirst()) {
                // Update existing
                int id = cursor.getInt(0);
                result = db.update("users", values, "id = ?", new String[]{String.valueOf(id)});
            } else {
                // Insert new
                result = db.insert("users", null, values);
            }
        } finally {
            cursor.close();
        }

        return result != -1;
    }

    // GET USER BY EMAIL
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        Cursor cursor = db.rawQuery(
                "SELECT id, fullname, email, phone, password FROM users WHERE LOWER(email) = ?",
                new String[]{normalizedEmail}
        );

        User user = null;
        try {
            if (cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
            }
        } finally {
            cursor.close();
        }

        return user;
    }
}
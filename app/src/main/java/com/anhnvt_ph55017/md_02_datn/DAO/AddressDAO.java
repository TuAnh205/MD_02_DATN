package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    private final DBHelper dbHelper;

    public AddressDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // ─── Thêm địa chỉ mới ───────────────────────────────────────────────────
    public void addAddress(int userId, String name, String phone, String address, boolean isDefault) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Nếu địa chỉ mới là mặc định → bỏ default của tất cả địa chỉ cũ
        if (isDefault) {
            clearDefault(db, userId);
        }

        ContentValues values = new ContentValues();
        values.put("user_id",    userId);
        values.put("name",       name);
        values.put("phone",      phone);
        values.put("address",    address);
        values.put("is_default", isDefault ? 1 : 0);

        db.insert("addresses", null, values);
        db.close();
    }

    // ─── Lấy tất cả địa chỉ của user ────────────────────────────────────────
    public List<Address> getAddresses(int userId) {
        List<Address> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "addresses",
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                "is_default DESC"   // địa chỉ mặc định lên đầu
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToAddress(cursor));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    // ─── Lấy địa chỉ mặc định ───────────────────────────────────────────────
    public Address getDefaultAddress(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Address address = null;

        Cursor cursor = db.query(
                "addresses",
                null,
                "user_id = ? AND is_default = 1",
                new String[]{String.valueOf(userId)},
                null, null, null,
                "1"     // chỉ lấy 1 dòng
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                address = cursorToAddress(cursor);
            }
            cursor.close();
        }
        db.close();
        return address;
    }

    // ─── Cập nhật địa chỉ ───────────────────────────────────────────────────
    public void updateAddress(Address addr) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (addr.isDefault()) {
            clearDefault(db, addr.getUserId());
        }

        ContentValues values = new ContentValues();
        values.put("name",       addr.getName());
        values.put("phone",      addr.getPhone());
        values.put("address",    addr.getAddress());
        values.put("is_default", addr.isDefault() ? 1 : 0);

        db.update("addresses", values, "id = ?",
                new String[]{String.valueOf(addr.getId())});
        db.close();
    }

    // ─── Đặt địa chỉ làm mặc định ───────────────────────────────────────────
    @SuppressWarnings("unused")
    public void setDefault(int addressId, int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        clearDefault(db, userId);

        ContentValues values = new ContentValues();
        values.put("is_default", 1);
        db.update("addresses", values, "id = ?",
                new String[]{String.valueOf(addressId)});
        db.close();
    }

    // ─── Xóa địa chỉ ────────────────────────────────────────────────────────
    @SuppressWarnings("unused")
    public void deleteAddress(int addressId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("addresses", "id = ?", new String[]{String.valueOf(addressId)});
        db.close();
    }

    // ─── Helper: bỏ default tất cả địa chỉ của user ────────────────────────
    private void clearDefault(SQLiteDatabase db, int userId) {
        ContentValues values = new ContentValues();
        values.put("is_default", 0);
        db.update("addresses", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    // ─── Helper: đọc cursor → Address ───────────────────────────────────────
    private Address cursorToAddress(Cursor cursor) {
        return new Address(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1
        );
    }
}
//    package com.anhnvt_ph55017.md_02_datn.DAO;
//
//    import android.content.ContentValues;
//    import android.content.Context;
//    import android.database.Cursor;
//    import android.database.sqlite.SQLiteDatabase;
//
//    import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
//    import com.anhnvt_ph55017.md_02_datn.models.Address;
//
//    import java.util.ArrayList;
//    import java.util.List;
//
//    public class AddressDAO {
//
//        private final SQLiteDatabase db;
//
//        public AddressDAO(Context context) {
//            DBHelper helper = new DBHelper(context);
//            db = helper.getWritableDatabase();
//        }
//
//        // lấy danh sách
//        public List<Address> getAddresses(int userId) {
//            List<Address> list = new ArrayList<>();
//
//            Cursor cursor = db.rawQuery(
//                    "SELECT id,userId,name,phone,address,isDefault FROM addresses WHERE userId=?",
//                    new String[]{String.valueOf(userId)}
//            );
//
//            if (cursor.moveToFirst()) {
//                do {
//                    list.add(new Address(
//                            cursor.getInt(0),
//                            cursor.getInt(1),
//                            cursor.getString(2),
//                            cursor.getString(3),
//                            cursor.getString(4),
//                            cursor.getInt(5) == 1
//                    ));
//                } while (cursor.moveToNext());
//            }
//
//            cursor.close();
//            return list;
//        }
//        //dia chi mac dinh
//        public Address getDefaultAddress(int userId) {
//            Cursor cursor = db.rawQuery(
//                    "SELECT id,userId,name,phone,address,isDefault FROM addresses WHERE userId=? AND isDefault=1 LIMIT 1",
//                    new String[]{String.valueOf(userId)}
//            );
//
//            if (cursor.moveToFirst()) {
//                Address a = new Address(
//                        cursor.getInt(0),
//                        cursor.getInt(1),
//                        cursor.getString(2),
//                        cursor.getString(3),
//                        cursor.getString(4),
//                        cursor.getInt(5) == 1
//                );
//                cursor.close();
//                return a;
//            }
//
//            cursor.close();
//            return null;
//        }
//
//        public long addAddress(int userId, String name, String phone, String address, boolean isDefault) {
//            if (isDefault) {
//                clearDefault(userId);
//            }
//
//            ContentValues values = new ContentValues();
//            values.put("userId", userId);
//            values.put("name", name);
//            values.put("phone", phone);
//            values.put("address", address);
//            values.put("isDefault", isDefault ? 1 : 0);
//
//            return db.insert("addresses", null, values);
//        }
//        // cap nhat dia chi
//        public void updateAddress(int id, String name, String phone, String address, boolean isDefault) {
//            if (isDefault) {
//
//                Cursor c = db.rawQuery("SELECT userId FROM addresses WHERE id=?", new String[]{String.valueOf(id)});
//                if (c.moveToFirst()) {
//                    int userId = c.getInt(0);
//                    clearDefault(userId);
//                }
//                c.close();
//            }
//
//            ContentValues values = new ContentValues();
//            values.put("name", name);
//            values.put("phone", phone);
//            values.put("address", address);
//            values.put("isDefault", isDefault ? 1 : 0);
//
//            db.update("addresses", values, "id=?", new String[]{String.valueOf(id)});
//        }
//
//        public void deleteAddress(int id) {
//            db.delete("addresses", "id=?", new String[]{String.valueOf(id)});
//        }
//
//        public void setDefault(int userId, int addressId) {
//            clearDefault(userId);
//            ContentValues values = new ContentValues();
//            values.put("isDefault", 1);
//            db.update("addresses", values, "id=?", new String[]{String.valueOf(addressId)});
//        }
//
//        private void clearDefault(int userId) {
//            ContentValues values = new ContentValues();
//            values.put("isDefault", 0);
//            db.update("addresses", values, "userId=?", new String[]{String.valueOf(userId)});
//        }
//    }

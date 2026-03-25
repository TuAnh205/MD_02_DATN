package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    SQLiteDatabase db;
    Context context;

    public OrderDAO(Context context) {
        this.context = context;
        DBHelper helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    // Thêm đơn hàng mới
    public long addOrder(String orderId, int userId, double total, String status, 
                        String address, String payment, String note, int imageRes) {
        try {
            ContentValues values = new ContentValues();
            values.put("orderId", orderId);
            values.put("userId", userId);
            values.put("total", total);
            values.put("status", status);
            values.put("address", address);
            values.put("payment", payment);
            values.put("note", note);
            values.put("imageRes", imageRes);
            values.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            values.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            return db.insert("orders", null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Lấy tất cả đơn hàng của user (mới nhất ở trên)
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT id, total, status, createdAt, imageRes FROM orders WHERE userId=? ORDER BY createdAt DESC",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    double total = cursor.getDouble(1);
                    String status = cursor.getString(2);
                    String createdAt = cursor.getString(3);
                    int imageRes = cursor.getInt(4);

                    // Parse date
                    String[] dateParts = createdAt.split(" ");
                    String dateStr = dateParts[0]; // "yyyy-MM-dd"

                    // Convert to "MMM dd yyyy" format
                    String date = convertDateFormat(dateStr);
                    
                    Order order = new Order("OD-" + id, date, total, status, "TBD", 1, imageRes);
                    orders.add(order);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Lấy tất cả đơn hàng (cho admin, hoặc dev testing)
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT id, total, status, createdAt, imageRes FROM orders ORDER BY createdAt DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    double total = cursor.getDouble(1);
                    String status = cursor.getString(2);
                    String createdAt = cursor.getString(3);
                    int imageRes = cursor.getInt(4);

                    String date = convertDateFormat(createdAt.split(" ")[0]);
                    
                    Order order = new Order("OD-" + id, date, total, status, "TBD", 1, imageRes);
                    orders.add(order);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Cập nhật trạng thái đơn hàng
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            ContentValues values = new ContentValues();
            values.put("status", newStatus);
            values.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            int rowsAffected = db.update("orders", values, "id=?", new String[]{String.valueOf(orderId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper: chuyển đổi định dạng ngày từ "yyyy-MM-dd" sang "MMM dd yyyy"
    private String convertDateFormat(String dateStr) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd yyyy");
            java.util.Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr; // return as is if parsing fails
        }
    }
}

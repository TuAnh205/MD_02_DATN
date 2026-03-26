package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.models.OrderItem;

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

    // Lấy chi tiết đơn hàng (kèm tất cả items)
    public Order getOrderById(int orderId) {
        Order order = null;
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT id, userId, total, status, address, payment, createdAt, note, imageRes, cancellationReason, rating, reviewComment, reviewedAt FROM orders WHERE id=?",
                    new String[]{String.valueOf(orderId)}
            );

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                int userId = cursor.getInt(1);
                double total = cursor.getDouble(2);
                String status = cursor.getString(3);
                String address = cursor.getString(4);
                String payment = cursor.getString(5);
                String createdAt = cursor.getString(6);
                String note = cursor.getString(7);
                int imageRes = cursor.getInt(8);
                String cancellationReason = cursor.getString(9);
                double rating = cursor.getDouble(10);
                String reviewComment = cursor.getString(11);
                String reviewedAt = cursor.getString(12);

                String date = convertDateFormat(createdAt.split(" ")[0]);
                order = new Order("OD-" + id, date, total, status, "TBD", 1, imageRes);
                order.setShippingAddress(address);
                order.setPaymentMethod(payment);
                order.setCreatedAt(createdAt);  // Lưu createdAt để tính ngày dự kiến
                order.setCancellationReason(cancellationReason);  // Lưu lý do hủy
                order.setRating(rating);  // Lưu rating
                order.setReviewComment(reviewComment);  // Lưu review comment
                order.setReviewedAt(reviewedAt);  // Lưu thời gian đánh giá
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return order;
    }

    // Lấy tất cả items của 1 đơn hàng (kèm product info)
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT oi.productId, oi.quantity, oi.price, p.name, p.image " +
                    "FROM order_items oi " +
                    "JOIN products p ON oi.productId = p.id " +
                    "WHERE oi.orderId = ? " +
                    "ORDER BY oi.id ASC",
                    new String[]{String.valueOf(orderId)}
            );

            if (cursor.moveToFirst()) {
                do {
                    int productId = cursor.getInt(0);
                    int quantity = cursor.getInt(1);
                    double price = cursor.getDouble(2);
                    String productName = cursor.getString(3);
                    int imageRes = cursor.getInt(4);

                    OrderItem item = new OrderItem(productName, price, quantity, imageRes);
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // Cập nhật review (rating + comment) cho đơn hàng
    public boolean updateOrderReview(int orderId, double rating, String reviewComment) {
        try {
            ContentValues values = new ContentValues();
            values.put("rating", rating);
            values.put("reviewComment", reviewComment);
            values.put("reviewedAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            values.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            int rowsAffected = db.update("orders", values, "id=?", new String[]{String.valueOf(orderId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật lý do hủy đơn hàng
    public boolean updateOrderCancellation(int orderId, String cancellationReason) {
        try {
            ContentValues values = new ContentValues();
            values.put("status", "Đã hủy");
            values.put("cancellationReason", cancellationReason);
            values.put("updatedAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            int rowsAffected = db.update("orders", values, "id=?", new String[]{String.valueOf(orderId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tính ngày dự kiến nhận hàng dựa trên status
    public String calculateExpectedDelivery(String createdAt, String status) {
        try {
            // Nếu chưa xác nhận (Chưa thanh toán, Đang xử lý) => chưa có ngày dự kiến
            if ("Chưa thanh toán".equals(status) || "Đang xử lý".equals(status)) {
                return "Chưa xác định";
            }

            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd yyyy");
            java.util.Date orderDate = inputFormat.parse(createdAt);

            // Tính ngày dự kiến dựa trên status
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(orderDate);

            if ("Đang giao hàng".equals(status)) {
                // Nếu đang giao => + 3-4 ngày (chọn 3)
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 3);
            } else if ("Đã nhận".equals(status)) {
                // Nếu đã nhận => + 3 ngày (ngày xác nhận admin + 3 ngày)
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 3);
            } else if ("Đã hủy".equals(status)) {
                return "Đã hủy";
            }

            return outputFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "Chưa xác định";
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

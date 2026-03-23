package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.SearchHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchHistoryDAO {

    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public SearchHistoryDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * Thêm keyword vào lịch sử tìm kiếm
     * Nếu keyword đã tồn tại, không thêm (tránh trùng lặp)
     */
    public void add(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Kiểm tra keyword đã tồn tại hay chưa
            Cursor cursor = db.query(
                    "search_history",
                    null,
                    "keyword = ?",
                    new String[]{keyword},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() > 0) {
                // Nếu đã tồn tại, cập nhật thời gian tìm kiếm lại
                cursor.close();
                updateSearchTime(keyword);
            } else {
                // Nếu chưa tồn tại, thêm mới
                cursor.close();
                ContentValues values = new ContentValues();
                values.put("keyword", keyword);
                values.put("createdAt", dateFormat.format(new Date()));

                db.insert("search_history", null, values);
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật thời gian tìm kiếm lại
     */
    private void updateSearchTime(String keyword) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("createdAt", dateFormat.format(new Date()));

            db.update(
                    "search_history",
                    values,
                    "keyword = ?",
                    new String[]{keyword}
            );

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả lịch sử tìm kiếm (sắp xếp theo thời gian mới nhất)
     */
    public List<SearchHistory> getAll() {
        List<SearchHistory> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    "search_history",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "createdAt DESC"
            );

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String keyword = cursor.getString(cursor.getColumnIndexOrThrow("keyword"));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"));

                list.add(new SearchHistory(id, keyword, createdAt));
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy Top N lịch sử tìm kiếm gần đây
     */
    public List<String> getTopRecent(int limit) {
        List<String> list = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    "search_history",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "createdAt DESC",
                    String.valueOf(limit)
            );

            while (cursor.moveToNext()) {
                String keyword = cursor.getString(cursor.getColumnIndexOrThrow("keyword"));
                list.add(keyword);
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Xóa một keyword khỏi lịch sử
     */
    public void delete(int id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("search_history", "id = ?", new String[]{String.valueOf(id)});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa tất cả lịch sử tìm kiếm
     */
    public void deleteAll() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("search_history", null, null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách các keyword (string) gần đây
     */
    public List<String> getRecentKeywords(int limit) {
        return getTopRecent(limit);
    }
}

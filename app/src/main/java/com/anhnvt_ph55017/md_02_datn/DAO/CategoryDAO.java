package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    DBHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM category", null);

        if (c.moveToFirst()) {
            do {
                list.add(new Category(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getInt(c.getColumnIndexOrThrow("icon"))
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }
}
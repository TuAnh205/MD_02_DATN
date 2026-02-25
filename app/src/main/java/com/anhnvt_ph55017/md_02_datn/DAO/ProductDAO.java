package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    DBHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM product", null);

        if (c.moveToFirst()) {
            do {
                list.add(new Product(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("price")),
                        c.getInt(c.getColumnIndexOrThrow("image"))
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }
}
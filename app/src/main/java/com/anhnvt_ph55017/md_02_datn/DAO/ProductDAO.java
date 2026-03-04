package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.R;
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

        Cursor c = db.rawQuery(
                "SELECT id, name, price, image, description, stock FROM products",
                null
        );

        if (c.moveToFirst()) {
            do {
                list.add(new Product(
                        c.getInt(0),      // id
                        c.getString(1),   // name
                        c.getDouble(2),   // price
                        c.getInt(3),      // image
                        c.getString(4),   // description
                        c.getInt(5)       // stock
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }
    public Product getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, name, price, image, description, stock FROM products WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        Product product = null;

        if (c.moveToFirst()) {
            product = new Product(
                    c.getInt(0),
                    c.getString(1),
                    c.getDouble(2),
                    c.getInt(3),
                    c.getString(4),
                    c.getInt(5)
            );
        }

        c.close();
        db.close();
        return product;
    }
}
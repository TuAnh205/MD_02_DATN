package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.R;
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
        
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor c = db.rawQuery("SELECT id,name,image FROM categories", null);

            try {
                if (c.moveToFirst()) {
                    do {
                        list.add(new Category(
                                c.getInt(0),
                                c.getString(1),
                                c.getInt(2)
                        ));
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
}
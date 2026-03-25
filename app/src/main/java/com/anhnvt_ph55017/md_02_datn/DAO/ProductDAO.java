package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProductDAO {

    DBHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    // Lấy tất cả sản phẩm
    public List<Product> getAll(){

        List<Product> list = new ArrayList<>();

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor c = db.rawQuery(
                    "SELECT id,name,price,image,description,stock FROM products",
                    null
            );

            try {
                if(c.moveToFirst()){
                    do{

                        list.add(new Product(
                                c.getInt(0),
                                c.getString(1),
                                c.getDouble(2),
                                c.getInt(3),
                                c.getString(4),
                                c.getInt(5)
                        ));

                    }while(c.moveToNext());
                }
            } finally {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // Lọc theo nhiều category
    public List<Product> getByCategories(Set<Integer> categoryIds){

        List<Product> list = new ArrayList<>();

        if(categoryIds.isEmpty()){
            return getAll();
        }

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            StringBuilder query = new StringBuilder(
                    "SELECT id,name,price,image,description,stock FROM products WHERE categoryId IN ("
            );

            for(int i = 0; i < categoryIds.size(); i++){
                query.append("?");
                if(i < categoryIds.size() - 1){
                    query.append(",");
                }
            }

            query.append(")");

            String[] args = new String[categoryIds.size()];
            int index = 0;

            for(Integer id : categoryIds){
                args[index++] = String.valueOf(id);
            }

            Cursor c = db.rawQuery(query.toString(), args);

            try {
                if(c.moveToFirst()){
                    do{

                        list.add(new Product(
                                c.getInt(0),
                                c.getString(1),
                                c.getDouble(2),
                                c.getInt(3),
                                c.getString(4),
                                c.getInt(5)
                        ));

                    }while(c.moveToNext());
                }
            } finally {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo tên sản phẩm (like)
    public List<Product> searchByName(String keyword) {
        List<Product> list = new ArrayList<>();

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT id,name,price,image,description,stock FROM products WHERE name LIKE ?",
                    new String[]{"%" + keyword + "%"}
            );

            if (c.moveToFirst()) {
                do {
                    list.add(new Product(
                            c.getInt(0),
                            c.getString(1),
                            c.getDouble(2),
                            c.getInt(3),
                            c.getString(4),
                            c.getInt(5)
                    ));
                } while (c.moveToNext());
            }

            c.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
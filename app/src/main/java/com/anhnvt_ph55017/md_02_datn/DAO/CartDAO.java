package com.anhnvt_ph55017.md_02_datn.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anhnvt_ph55017.md_02_datn.dbhelper.DBHelper;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    SQLiteDatabase db;

    public CartDAO(Context context){
        DBHelper helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    /* ================= ADD TO CART ================= */

    public void addToCart(int userId,int productId){

        Cursor cursor = db.rawQuery(
                "SELECT id FROM carts WHERE userId=?",
                new String[]{String.valueOf(userId)}
        );

        int cartId;

        if(cursor.moveToFirst()){
            cartId = cursor.getInt(0);
        }else{

            ContentValues values = new ContentValues();
            values.put("userId",userId);

            cartId = (int) db.insert("carts",null,values);
        }

        Cursor c = db.rawQuery(
                "SELECT quantity FROM cart_items WHERE cartId=? AND productId=?",
                new String[]{String.valueOf(cartId),String.valueOf(productId)}
        );

        if(c.moveToFirst()){

            int quantity = c.getInt(0)+1;

            ContentValues v = new ContentValues();
            v.put("quantity",quantity);

            db.update(
                    "cart_items",
                    v,
                    "cartId=? AND productId=?",
                    new String[]{String.valueOf(cartId),String.valueOf(productId)}
            );

        }else{

            ContentValues v = new ContentValues();
            v.put("cartId",cartId);
            v.put("productId",productId);
            v.put("quantity",1);

            db.insert("cart_items",null,v);
        }
    }

    /* ================= GET CART PRODUCTS ================= */

    public List<Product> getCartProducts(int userId){

        List<Product> list = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT p.id,p.name,p.price,p.image,p.description,p.stock,c.quantity " +
                        "FROM cart_items c " +
                        "JOIN carts ca ON c.cartId = ca.id " +
                        "JOIN products p ON c.productId = p.id " +
                        "WHERE ca.userId=?",
                new String[]{String.valueOf(userId)}
        );

        if(cursor.moveToFirst()){
            do{
                Product p = new Product(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getInt(5)
                );
                p.setQty(cursor.getInt(6));
                list.add(p);
            }while(cursor.moveToNext());
        }

        return list;
    }

    /* ================= REMOVE ITEM ================= */

    public void removeItem(int productId){

        db.delete(
                "cart_items",
                "productId=?",
                new String[]{String.valueOf(productId)}
        );
    }

    /* ================= UPDATE QUANTITY ================= */

    public void updateQuantity(int productId,int quantity){

        ContentValues v = new ContentValues();
        v.put("quantity",quantity);

        db.update(
                "cart_items",
                v,
                "productId=?",
                new String[]{String.valueOf(productId)}
        );
    }

    /* ================= TOTAL PRICE ================= */

    public double getTotalPrice(int userId){

        double total = 0;

        Cursor cursor = db.rawQuery(
                "SELECT p.price,c.quantity " +
                        "FROM cart_items c " +
                        "JOIN carts ca ON c.cartId = ca.id " +
                        "JOIN products p ON c.productId = p.id " +
                        "WHERE ca.userId=?",
                new String[]{String.valueOf(userId)}
        );

        if(cursor.moveToFirst()){
            do{

                double price = cursor.getDouble(0);
                int quantity = cursor.getInt(1);

                total += price * quantity;

            }while(cursor.moveToNext());
        }

        return total;
    }

    /* ================= CLEAR ALL CART ================= */

    public void clearCart(int userId){
        
        Cursor cursor = db.rawQuery(
                "SELECT id FROM carts WHERE userId=?",
                new String[]{String.valueOf(userId)}
        );

        if(cursor.moveToFirst()){
            int cartId = cursor.getInt(0);
            db.delete(
                    "cart_items",
                    "cartId=?",
                    new String[]{String.valueOf(cartId)}
            );
        }
        
        cursor.close();
    }
}
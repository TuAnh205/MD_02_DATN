package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.anhnvt_ph55017.md_02_datn.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartLocalManager {
    private static final String PREF_NAME = "cart_local";
    private static final String KEY_CART = "cart_items";

    public static void saveCart(Context context, List<Product> cartList) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Product p : cartList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("_id", p.getId());
                obj.put("name", p.getName());
                obj.put("price", p.getPrice());
                obj.put("imageUrl", p.getImageUrl());
                obj.put("description", p.getDescription());
                obj.put("stock", p.getStock());
                obj.put("selected", p.isSelected());
                obj.put("quantity", p.getQuantity());
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_CART, arr.toString()).apply();
    }

    public static List<Product> loadCart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CART, "[]");
        List<Product> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Product p = new Product(
                        obj.getString("_id"),
                        obj.getString("name"),
                        obj.getDouble("price"),
                        obj.getString("imageUrl"),
                        obj.optString("description", ""),
                        obj.optInt("stock", 0)
                );
                p.setSelected(obj.optBoolean("selected", false));
                p.setQuantity(obj.optInt("quantity", 1));
                list.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void clearCart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_CART).apply();
    }
}

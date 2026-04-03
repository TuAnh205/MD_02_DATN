package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.models.Category;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoryApiService {

    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String error);
    }

    public static void fetchCategories(Context context, CategoryCallback callback) {
        String url = NetworkConstants.API_BASE_URL + "/api/products/categories";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Category> categories = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            String name = response.optString(i);
                            categories.add(new Category(i, name, 0)); // No image from backend
                        }
                        callback.onSuccess(categories);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    callback.onError("Network error: " + error.getMessage());
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}

package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ProductApiService {

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public static void fetchProducts(Context context, String query, ProductCallback callback) {
        String url = NetworkConstants.API_BASE_URL + "/api/products";
        if (query != null && !query.isEmpty()) {
            try {
                url += "?q=" + URLEncoder.encode(query, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Product> products = new ArrayList<>();
                        JSONArray data = response.optJSONArray("data");
                        if (data == null) {
                            callback.onError("Null data from server");
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String id = item.optString("_id", "");
                            String name = item.optString("name", "Unknown");
                            double price = item.optDouble("price", 0);
                            String description = item.optString("description", "");
                            int stock = item.optInt("stock", 0);
                            String imageUrl = item.optString("image", "");

                            Product product = new Product(id, name, price, imageUrl, description, stock);

                            JSONObject ratings = item.optJSONObject("ratings");
                            if (ratings != null) {
                                product.setRating((float) ratings.optDouble("average", 0));
                                product.setReviewCount(ratings.optInt("count", 0));
                            }

                            products.add(product);
                        }

                        callback.onSuccess(products);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("ProductApiService", "fetchProducts error", error);
                    if (error instanceof VolleyError && error.networkResponse != null) {
                        callback.onError("Server error code: " + error.networkResponse.statusCode);
                    } else {
                        callback.onError(error.getMessage() != null ? error.getMessage() : "Unknown error");
                    }
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}

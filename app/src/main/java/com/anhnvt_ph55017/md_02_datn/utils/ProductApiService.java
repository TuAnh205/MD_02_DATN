package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
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
                            callback.onError("No data from server");
                            return;
                        }

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);

                            String id = item.optString("_id");
                            String name = item.optString("name");
                            double price = item.optDouble("price");
                            String description = item.optString("description");
                            int stock = item.optInt("stock");

                            JSONArray images = item.optJSONArray("images");
                            String imageUrl = "";

                            if (images != null && images.length() > 0) {
                                imageUrl = images.optString(0);
                            }

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
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("API_ERROR", error.toString());
                    callback.onError("API error");
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
    public interface ProductDetailCallback {
        void onSuccess(JSONObject productJson);
        void onError(String error);
    }

    public static void fetchProductById(Context context, String id, ProductDetailCallback callback) {
        String url = NetworkConstants.API_BASE_URL + "/api/products/" + id;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("API_ERROR", error.toString());
                    callback.onError("API error");
                }
        );
        Volley.newRequestQueue(context).add(request);
    }
}
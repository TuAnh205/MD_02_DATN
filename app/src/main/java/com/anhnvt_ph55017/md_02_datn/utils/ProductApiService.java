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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductApiService {

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public static void fetchProducts(Context context, String query, String category, ProductCallback callback) {
        final String[] urlHolder = {NetworkConstants.API_BASE_URL + "/api/products"};
        boolean hasQuery = query != null && !query.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        try {
            StringBuilder sb = new StringBuilder(urlHolder[0]);
            boolean first = true;
            if (hasQuery) {
                sb.append(first ? "?" : "&");
                sb.append("q=").append(URLEncoder.encode(query, "UTF-8"));
                first = false;
            }
            if (hasCategory) {
                sb.append(first ? "?" : "&");
                sb.append("category=").append(URLEncoder.encode(category, "UTF-8"));
            }
            urlHolder[0] = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lấy danh sách sản phẩm yêu thích trước
        String token = SessionManager.getToken(context);
        com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService.getFavorites(context, token, new com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService.FavoritesCallback() {
            @Override
            public void onSuccess(JSONArray favoritesJson) {
                Set<String> favoriteIds = new HashSet<>();
                for (int i = 0; i < favoritesJson.length(); i++) {
                    org.json.JSONObject fav = favoritesJson.optJSONObject(i);
                    if (fav != null && fav.has("product")) {
                        Object prod = fav.opt("product");
                        if (prod instanceof org.json.JSONObject) {
                            String pid = ((org.json.JSONObject) prod).optString("_id");
                            if (pid != null) favoriteIds.add(pid);
                        } else if (prod instanceof String) {
                            favoriteIds.add((String) prod);
                        }
                    }
                }

                final String url = urlHolder[0];
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

                                    // Đánh dấu sản phẩm yêu thích
                                    product.setFavorite(favoriteIds.contains(id));

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

            @Override
            public void onError(String error) {
                // Nếu lỗi lấy danh sách yêu thích, vẫn load sản phẩm bình thường
                final String url = urlHolder[0];
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
                        error2 -> {
                            Log.e("API_ERROR", error2.toString());
                            callback.onError("API error");
                        }
                );
                Volley.newRequestQueue(context).add(request);
            }
        });
    }

    // Backward compatibility
    public static void fetchProducts(Context context, String query, ProductCallback callback) {
        fetchProducts(context, query, "", callback);
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
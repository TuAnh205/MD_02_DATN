package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.models.Review;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewApiService {
    public interface ReviewCallback {
        void onSuccess(List<Review> reviews);
        void onError(String error);
    }
    public interface PostReviewCallback {
        void onSuccess(Review review);
        void onError(String error);
    }

    // Lấy danh sách đánh giá cho sản phẩm
    public static void fetchReviews(Context context, String productId, ReviewCallback callback) {
        String url = NetworkConstants.API_BASE_URL + "/api/reviews?productId=" + productId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Review> reviews = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject item = response.getJSONObject(i);
                            String id = item.optString("_id");
                            String userName = item.optJSONObject("user") != null ? item.optJSONObject("user").optString("name", "Ẩn danh") : "Ẩn danh";
                            String userId = item.optJSONObject("user") != null ? item.optJSONObject("user").optString("_id", "") : "";
                            String content = item.optString("content");
                            float rating = (float) item.optDouble("rating", 0);
                            String createdAt = item.optString("createdAt", "");
                            reviews.add(new Review(id, userName, userId, content, rating, createdAt));
                        }
                        callback.onSuccess(reviews);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("API_REVIEW", error.toString());
                    callback.onError("API error");
                }
        );
        Volley.newRequestQueue(context).add(request);
    }

    // Gửi đánh giá mới
    public static void postReview(Context context, String productId, String userId, String userName, float rating, String content, PostReviewCallback callback) {
        String url = NetworkConstants.API_BASE_URL + "/api/reviews";
        try {
            JSONObject body = new JSONObject();
            body.put("productId", productId);
            body.put("userId", userId);
            body.put("userName", userName);
            body.put("rating", rating);
            body.put("content", content);
        
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                    response -> {
                        try {
                            String id = response.optString("_id");
                            String createdAt = response.optString("createdAt", "");
                            Review review = new Review(id, userName, userId, content, rating, createdAt);
                            callback.onSuccess(review);
                        } catch (Exception e) {
                            callback.onError("Parse error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e("API_REVIEW", error.toString());
                        callback.onError("API error");
                    }
            );
            Volley.newRequestQueue(context).add(request);
        } catch (Exception e) {
            callback.onError("Build body error: " + e.getMessage());
        }
    }
}

package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReviewApiService {

    private static RequestQueue requestQueue;

    private static RequestQueue getQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    // ================== CALLBACK ==================
    public interface ReviewListCallback {
        void onSuccess(JSONArray reviews);
        void onError(String error);
    }

    public interface ReviewPostCallback {
        void onSuccess(JSONObject review);
        void onError(String error);
    }

    // ================== GET REVIEW ==================
    public static void fetchReviewsByProduct(Context context, String productId, ReviewListCallback callback) {

        String url = NetworkConstants.API_BASE_URL + "/api/reviews/product/" + productId;
        Log.d("API_URL", url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("GET_SUCCESS", response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String message = parseError(error, "Lỗi tải review");
                    Log.e("GET_ERROR", message);
                    callback.onError(message);
                }
        );

        getQueue(context).add(request);
    }

    // ================== POST REVIEW ==================
    public static void postReview(Context context, String token, String productId, int rating, String comment, ReviewPostCallback callback) {

        String url = NetworkConstants.API_BASE_URL + "/api/reviews";

        JSONObject body = new JSONObject();
        try {
            body.put("productId", productId);
            body.put("rating", rating);
            body.put("comment", comment);
        } catch (Exception e) {
            callback.onError("Data lỗi");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d("POST_SUCCESS", response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String message = parseError(error, "Gửi review thất bại");
                    Log.e("POST_ERROR", message);
                    callback.onError(message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };

        getQueue(context).add(request);
    }

    // ================== UPDATE REVIEW ==================
    public static void updateReview(Context context, String token, String reviewId, int rating, String comment, ReviewPostCallback callback) {

        String url = NetworkConstants.API_BASE_URL + "/api/reviews/" + reviewId;

        JSONObject body = new JSONObject();
        try {
            body.put("rating", rating);
            body.put("comment", comment);
        } catch (Exception e) {
            callback.onError("Data lỗi");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Log.d("UPDATE_SUCCESS", response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String message = parseError(error, "Update thất bại");
                    Log.e("UPDATE_ERROR", message);
                    callback.onError(message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };

        getQueue(context).add(request);
    }

    // ================== DELETE REVIEW ==================
    public static void deleteReview(Context context, String token, String reviewId, ReviewPostCallback callback) {

        String url = NetworkConstants.API_BASE_URL + "/api/reviews/" + reviewId;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("success", true);
                        obj.put("message", "Xóa thành công");

                        Log.d("DELETE_SUCCESS", obj.toString());
                        callback.onSuccess(obj);
                    } catch (Exception e) {
                        callback.onError("Parse lỗi");
                    }
                },
                error -> {
                    String message = parseError(error, "Xóa thất bại");
                    Log.e("DELETE_ERROR", message);
                    callback.onError(message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };

        getQueue(context).add(request);
    }

    // ================== COMMON ==================
    private static Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "application/json");

        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }

        return headers;
    }

    private static String parseError(com.android.volley.VolleyError error, String defaultMsg) {
        String message = defaultMsg;

        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String json = new String(error.networkResponse.data);
                JSONObject obj = new JSONObject(json);

                if (obj.has("message")) {
                    message = obj.getString("message");
                } else {
                    message = json;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }
}
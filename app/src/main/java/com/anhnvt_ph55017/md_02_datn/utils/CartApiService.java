package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CartApiService {

    private static final String BASE_URL = "http://10.0.2.2:5000/api/cart";

    public interface CartCallback {
        void onSuccess(JSONObject cartJson);
        void onError(String error);
    }

    // ✅ ADD TO CART
    public static void addToCart(Context context, String token, String productId, int qty, CartCallback callback) {
        new Thread(() -> {
            try {
                Log.d("ADD_CART_DEBUG", "Token=" + token + " | productId=" + productId);

                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("productId", productId);
                body.put("qty", qty);

                Log.d("ADD_CART_BODY", body.toString());

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("ADD_CART_RESPONSE_CODE", String.valueOf(responseCode));

                // Log all response headers for debugging
                try {
                    for (int i = 1;; i++) {
                        String headerKey = conn.getHeaderFieldKey(i);
                        String headerValue = conn.getHeaderField(i);
                        if (headerKey == null && headerValue == null) break;
                        Log.d("ADD_CART_HEADER", headerKey + ": " + headerValue);
                    }
                } catch (Exception ex) {
                    Log.e("ADD_CART_HEADER_ERROR", ex.getMessage(), ex);
                }

                Scanner scanner = new Scanner(
                        responseCode >= 200 && responseCode < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream()
                ).useDelimiter("\\A");

                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                Log.d("ADD_CART_RESPONSE", "code=" + responseCode + " | body=" + response);

                if (responseCode >= 200 && responseCode < 300) {
                    try {
                        JSONObject json = new JSONObject(response);
                        callback.onSuccess(json);
                    } catch (Exception parseEx) {
                        Log.e("ADD_CART_JSON_ERROR", "Parse error: " + parseEx.getMessage() + " | body=" + response, parseEx);
                        callback.onError("JSON parse error: " + parseEx.getMessage() + " | body=" + response);
                    }
                } else {
                    Log.e("ADD_CART_API_ERROR", "API error: code=" + responseCode + " | body=" + response);
                    callback.onError(response);
                }

            } catch (Exception e) {
                Log.e("ADD_CART_ERROR", e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ✅ GET CART
    public static void getCart(Context context, String token, CartCallback callback) {
        new Thread(() -> {
            try {
                Log.d("GET_CART_DEBUG", "Token=" + token);

                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();

                Scanner scanner = new Scanner(
                        responseCode >= 200 && responseCode < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream()
                ).useDelimiter("\\A");

                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                Log.d("GET_CART_RESPONSE", "code=" + responseCode + " | body=" + response);

                if (responseCode >= 200 && responseCode < 300) {
                    callback.onSuccess(new JSONObject(response));
                } else {
                    callback.onError(response);
                }

            } catch (Exception e) {
                Log.e("GET_CART_ERROR", e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ✅ UPDATE
    public static void updateCartItem(Context context, String token, String productId, int qty, CartCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("qty", qty);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                Log.d("UPDATE_CART", res);

                if (code >= 200 && code < 300) {
                    callback.onSuccess(new JSONObject(res));
                } else callback.onError(res);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // ✅ DELETE
    public static void removeFromCart(Context context, String token, String productId, CartCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                Log.d("DELETE_CART", res);

                if (code >= 200 && code < 300) {
                    callback.onSuccess(new JSONObject(res));
                } else callback.onError(res);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
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

    // ================= SAFE CALLBACK =================
    private static void safeSuccess(CartCallback callback, JSONObject res) {
        if (callback != null) callback.onSuccess(res);
    }

    private static void safeError(CartCallback callback, String err) {
        if (callback != null) callback.onError(err);
    }

    // ================= CLEAR CART =================
    public static void clearCart(Context context, String token, CartCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/clear");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                Log.d("CLEAR_CART", res);

                if (code >= 200 && code < 300) {
                    safeSuccess(callback, new JSONObject(res));
                } else {
                    safeError(callback, res);
                }

            } catch (Exception e) {
                safeError(callback, e.getMessage());
            }
        }).start();
    }

    // ================= ADD TO CART =================
    public static void addToCart(Context context, String token, String productId, int qty, CartCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("productId", productId);
                body.put("qty", qty);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                Log.d("ADD_CART", "code=" + code + " | body=" + res);

                if (code >= 200 && code < 300) {
                    safeSuccess(callback, new JSONObject(res));
                } else {
                    safeError(callback, res);
                }

            } catch (Exception e) {
                safeError(callback, e.getMessage());
            }
        }).start();
    }

    // ================= GET CART =================
    public static void getCart(Context context, String token, CartCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                Log.d("GET_CART", "code=" + code + " | body=" + res);

                if (code >= 200 && code < 300) {
                    safeSuccess(callback, new JSONObject(res));
                } else {
                    safeError(callback, res);
                }

            } catch (Exception e) {
                safeError(callback, e.getMessage());
            }
        }).start();
    }

    // ================= UPDATE =================
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

                Log.d("UPDATE_CART", "code=" + code + " | body=" + res);

                if (code >= 200 && code < 300) {
                    safeSuccess(callback, new JSONObject(res));
                } else {
                    safeError(callback, res);
                }

            } catch (Exception e) {
                safeError(callback, e.getMessage());
            }
        }).start();
    }

    // ================= DELETE =================
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

                Log.d("DELETE_CART", "id=" + productId + " | code=" + code + " | body=" + res);

                if (code >= 200 && code < 300) {
                    safeSuccess(callback, new JSONObject(res));
                } else {
                    safeError(callback, res);
                }

            } catch (Exception e) {
                safeError(callback, e.getMessage());
            }
        }).start();
    }
}
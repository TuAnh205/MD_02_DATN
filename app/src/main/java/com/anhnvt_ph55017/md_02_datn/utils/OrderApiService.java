
    package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class OrderApiService {
    private static final String BASE_URL = "http://10.0.2.2:5000/api/orders";

    // Tạo đơn hàng mới
    public interface CreateOrderCallback {
        void onSuccess(JSONObject orderJson);
        void onError(String error);
    }

    public static void createOrder(Context context, String token, JSONObject orderBody, CreateOrderCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                String bodyStr = orderBody.toString();
                conn.getOutputStream().write(bodyStr.getBytes("UTF-8"));

                int responseCode = conn.getResponseCode();
                InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                Log.d("CREATE_ORDER_RESPONSE", "code=" + responseCode + " | body=" + response);

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject obj = new JSONObject(response);
                    callback.onSuccess(obj);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                Log.e("CREATE_ORDER_ERROR", e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public interface OrdersCallback {
        void onSuccess(JSONArray ordersJson);
        void onError(String error);
    }

    // Lấy danh sách đơn hàng của user hiện tại
    public static void getOrders(Context context, String token, OrdersCallback callback) {
        new Thread(() -> {
            try {
                Log.d("GET_ORDERS_DEBUG", "Token=" + token);
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                Log.d("GET_ORDERS_RESPONSE", "code=" + responseCode + " | body=" + response);

                if (responseCode >= 200 && responseCode < 300) {
                    JSONArray arr = new JSONArray(response);
                    callback.onSuccess(arr);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                Log.e("GET_ORDERS_ERROR", e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
    // Hủy đơn hàng
    public interface CancelOrderCallback {
        void onSuccess(JSONObject orderJson);
        void onError(String error);
    }

    public static void cancelOrder(Context context, String token, String orderId, String reason, CancelOrderCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + orderId + "/cancel");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("reason", reason);
                String bodyStr = body.toString();
                conn.getOutputStream().write(bodyStr.getBytes("UTF-8"));

                int responseCode = conn.getResponseCode();
                InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                Log.d("CANCEL_ORDER_RESPONSE", "code=" + responseCode + " | body=" + response);

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject obj = new JSONObject(response);
                    callback.onSuccess(obj);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                Log.e("CANCEL_ORDER_ERROR", e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
}

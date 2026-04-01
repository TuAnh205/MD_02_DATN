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
}

package com.anhnvt_ph55017.md_02_datn.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AddressApiService {

    private static final String BASE_URL = "http://10.0.2.2:5000/api/user/addresses";

    public interface AddressListCallback {
        void onSuccess(JSONArray data);
        void onError(String error);
    }

    public interface AddressCallback {
        void onSuccess(JSONObject data);
        void onError(String error);
    }

    public static void getAddresses(String token, AddressListCallback callback) {
        new Thread(() -> {
            try {
                if (token == null || token.isEmpty()) {
                    callback.onError("Token rỗng");
                    return;
                }

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

                Log.d("API_ADDRESS", "code=" + code + " | " + res);

                if (code >= 200 && code < 300) {
                    JSONArray arr;

                    if (res.trim().startsWith("[")) {
                        arr = new JSONArray(res);
                    } else {
                        JSONObject obj = new JSONObject(res);
                        arr = obj.optJSONArray("addresses");
                    }

                    callback.onSuccess(arr);
                } else {
                    callback.onError(res);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void addAddress(String token, JSONObject body, AddressCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();

                int code = conn.getResponseCode();

                Scanner sc = new Scanner(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ).useDelimiter("\\A");

                String res = sc.hasNext() ? sc.next() : "";
                sc.close();

                if (code >= 200 && code < 300) {
                    callback.onSuccess(new JSONObject(res));
                } else {
                    callback.onError(res);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
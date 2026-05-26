package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LocationApiService {
    private static final String API_URL = "https://provinces.open-api.vn/api/?depth=3";
    private static final String LOCAL_FILE = "vietnam_provinces.json";

    public interface LocationCallback {
        void onSuccess(JSONArray locations);
        void onError(String error);
    }

    public static void getLocations(Context context, LocationCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                Scanner sc = new Scanner(is).useDelimiter("\\A");
                String res = sc.hasNext() ? sc.next() : "";
                sc.close();
                if (code >= 200 && code < 300) {
                    JSONArray arr = new JSONArray(res);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(arr));
                } else {
                    loadLocalLocations(context, callback, "API lỗi: " + res);
                }
            } catch (Exception e) {
                loadLocalLocations(context, callback, e.getMessage());
            }
        }).start();
    }

    private static void loadLocalLocations(Context context, LocationCallback callback, String fallbackReason) {
        try (InputStream is = context.getAssets().open(LOCAL_FILE);
             Scanner sc = new Scanner(is).useDelimiter("\\A")) {
            String res = sc.hasNext() ? sc.next() : "";
            JSONArray arr = new JSONArray(res);
            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(arr));
        } catch (Exception e) {
            String message = "Không thể tải dữ liệu địa lý: " + fallbackReason;
            new Handler(Looper.getMainLooper()).post(() -> callback.onError(message));
        }
    }
}

package com.anhnvt_ph55017.md_02_datn.utils;

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

    public interface LocationCallback {
        void onSuccess(JSONArray locations);
        void onError(String error);
    }

    public static void getLocations(LocationCallback callback) {
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
                    // API trả về 1 JSONArray các tỉnh/thành, mỗi tỉnh có districts, mỗi district có wards
                    JSONArray arr = new JSONArray(res);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(arr));
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(res));
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}

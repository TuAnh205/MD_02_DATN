package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;

import com.anhnvt_ph55017.md_02_datn.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoriteApiService {
    public interface FavoritesCallback {
        void onSuccess(JSONArray favoritesJson);
        void onError(String error);
    }

    public static void getFavorites(Context context, String token, FavoritesCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://10.0.2.2:5000/api/favorites");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                if (responseCode >= 200 && responseCode < 300) {
                    JSONArray arr = new JSONArray(response);
                    callback.onSuccess(arr);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}

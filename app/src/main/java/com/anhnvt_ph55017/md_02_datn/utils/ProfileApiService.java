package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import org.json.JSONObject;

public class ProfileApiService {
    public interface ProfileCallback {
        void onSuccess(JSONObject userJson);
        void onError(String error);
    }

    // Fetch profile info from backend
    public static void fetchProfile(Context context, String token, ProfileCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(NetworkConstants.API_BASE_URL + "/api/auth/me");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject userJson = new JSONObject(response);
                    callback.onSuccess(userJson);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Update profile info to backend
    public static void updateProfile(Context context, String token, String name, String phone, ProfileCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(NetworkConstants.API_BASE_URL + "/api/auth/me");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("name", name);
                body.put("phone", phone);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject userJson = jsonResponse.optJSONObject("user");
                    if (userJson == null) {
                        userJson = jsonResponse;
                    }
                    callback.onSuccess(userJson);
                } else {
                    callback.onError(response);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}

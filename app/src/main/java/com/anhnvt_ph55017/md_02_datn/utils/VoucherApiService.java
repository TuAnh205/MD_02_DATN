package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.util.Log;

import com.anhnvt_ph55017.md_02_datn.models.Voucher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VoucherApiService {
    public interface VoucherListCallback {
        void onSuccess(List<Voucher> vouchers);
        void onError(String error);
    }

    public interface ClaimCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void getVouchers(Context context, VoucherListCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(NetworkConstants.API_BASE_URL + "/api/vouchers");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                android.util.Log.d("VOUCHER_API", "Response: " + response);
                if (responseCode >= 200 && responseCode < 300) {
                    List<Voucher> vouchers = new ArrayList<>();
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONArray arr = null;
                        if (obj.has("vouchers")) arr = obj.getJSONArray("vouchers");
                        else if (obj.has("data")) arr = obj.getJSONArray("data");
                        else if (obj.has("items")) arr = obj.getJSONArray("items");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject v = arr.getJSONObject(i);
                                android.util.Log.d("VOUCHER_API", "Parsing voucher object: " + v.toString());
                                Voucher voucher = new Voucher();
                                java.lang.reflect.Field[] fields = Voucher.class.getDeclaredFields();
                                for (java.lang.reflect.Field f : fields) {
                                    f.setAccessible(true);
                                    if (v.has(f.getName())) {
                                        Object value = v.get(f.getName());
                                        f.set(voucher, value);
                                    }
                                }
                                vouchers.add(voucher);
                            }
                            callback.onSuccess(vouchers);
                            return;
                        }
                    } catch (Exception e) {
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject v = arr.getJSONObject(i);
                                android.util.Log.d("VOUCHER_API", "Parsing voucher object (array): " + v.toString());
                                Voucher voucher = new Voucher();
                                java.lang.reflect.Field[] fields = Voucher.class.getDeclaredFields();
                                for (java.lang.reflect.Field f : fields) {
                                    f.setAccessible(true);
                                    if (v.has(f.getName())) {
                                        Object value = v.get(f.getName());
                                        f.set(voucher, value);
                                    }
                                }
                                vouchers.add(voucher);
                            }
                            callback.onSuccess(vouchers);
                            return;
                        } catch (Exception ignored) {}
                        android.util.Log.e("VOUCHER_API", "Parse error (object/array): " + e.getMessage(), e);
                    }
                    android.util.Log.e("VOUCHER_API", "Không tìm thấy danh sách voucher trong response!");
                    callback.onError("Không tìm thấy danh sách voucher trong response!");
                } else {
                    android.util.Log.e("VOUCHER_API", "API error response: " + response);
                    callback.onError(response);
                }
            } catch (Exception e) {
                android.util.Log.e("VOUCHER_API", "Exception: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void getMyVouchers(Context context, VoucherListCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(NetworkConstants.API_BASE_URL + "/api/vouchers/my");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getToken(context));
                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                android.util.Log.d("VOUCHER_API", "MyVouchers Response: " + response);
                if (responseCode >= 200 && responseCode < 300) {
                    List<Voucher> vouchers = new ArrayList<>();
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONArray arr = null;
                        if (obj.has("vouchers")) arr = obj.getJSONArray("vouchers");
                        else if (obj.has("data")) arr = obj.getJSONArray("data");
                        else if (obj.has("items")) arr = obj.getJSONArray("items");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject v = arr.getJSONObject(i);
                                android.util.Log.d("VOUCHER_API", "Parsing my voucher object: " + v.toString());
                                Voucher voucher = new Voucher();
                                java.lang.reflect.Field[] fields = Voucher.class.getDeclaredFields();
                                for (java.lang.reflect.Field f : fields) {
                                    f.setAccessible(true);
                                    if (v.has(f.getName())) {
                                        Object value = v.get(f.getName());
                                        f.set(voucher, value);
                                    }
                                }
                                vouchers.add(voucher);
                            }
                            callback.onSuccess(vouchers);
                            return;
                        }
                    } catch (Exception e) {
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject v = arr.getJSONObject(i);
                                Voucher voucher = new Voucher();
                                java.lang.reflect.Field[] fields = Voucher.class.getDeclaredFields();
                                for (java.lang.reflect.Field f : fields) {
                                    f.setAccessible(true);
                                    if (v.has(f.getName())) {
                                        Object value = v.get(f.getName());
                                        f.set(voucher, value);
                                    }
                                }
                                vouchers.add(voucher);
                            }
                            callback.onSuccess(vouchers);
                            return;
                        } catch (Exception ignored) {}
                        android.util.Log.e("VOUCHER_API", "Parse error (my vouchers): " + e.getMessage(), e);
                    }
                    android.util.Log.e("VOUCHER_API", "Không tìm thấy danh sách voucher trong response!");
                    callback.onError("Không tìm thấy danh sách voucher trong response!");
                } else {
                    android.util.Log.e("VOUCHER_API", "API error response: " + response);
                    callback.onError(response);
                }
            } catch (Exception e) {
                android.util.Log.e("VOUCHER_API", "Exception: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void claimVoucher(Context context, String code, ClaimCallback callback) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(NetworkConstants.API_BASE_URL + "/api/vouchers/claim");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getToken(context));
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("code", code);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                if (responseCode >= 200 && responseCode < 300) {
                    String successMessage = "Nhận voucher thành công";
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.has("message")) {
                            successMessage = json.optString("message", successMessage);
                        }
                    } catch (Exception ignored) {}
                    callback.onSuccess(successMessage);
                } else {
                    String errorMessage = response;
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.has("message")) {
                            errorMessage = json.optString("message", errorMessage);
                        }
                    } catch (Exception ignored) {}
                    android.util.Log.e("VOUCHER_API", "Claim error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            } catch (Exception e) {
                android.util.Log.e("VOUCHER_API", "Exception: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
}

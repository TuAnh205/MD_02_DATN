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

    public static void getVouchers(Context context, VoucherListCallback callback) {
        new Thread(() -> {
            try {
                // Đổi sang endpoint public cho user
                java.net.URL url = new java.net.URL("http://10.0.2.2:5000/api/vouchers");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // Endpoint public, không cần token
                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                android.util.Log.d("VOUCHER_API", "Response: " + response);
                if (responseCode >= 200 && responseCode < 300) {
                    List<Voucher> vouchers = new ArrayList<>();
                    try {
                        // Thử parse object chứa mảng
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
                        // Nếu không phải object, thử parse trực tiếp mảng
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
}

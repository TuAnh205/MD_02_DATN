package com.anhnvt_ph55017.md_02_datn.utils;

import org.json.JSONObject;

public class ApiUtils {

    public static String parseErrorMessage(String error, String fallback) {
        if (error == null || error.trim().isEmpty()) {
            return fallback;
        }
        String trimmed = error.trim();
        try {
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                JSONObject obj = new JSONObject(trimmed);
                if (obj.has("message")) {
                    String msg = obj.optString("message", fallback);
                    return localizeStockError(msg);
                }
            }
        } catch (Exception ignored) {
            // ignore parse errors and fall back to raw string
        }
        return localizeStockError(trimmed);
    }

    private static String localizeStockError(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        String lower = message.toLowerCase();
        if (lower.contains("insufficient stock")
                || lower.contains("stock") && (lower.contains("insufficient") || lower.contains("out of") || lower.contains("hết"))) {
            return "Sản phẩm đã hết hàng hoặc không đủ tồn kho.";
        }
        return message;
    }
}

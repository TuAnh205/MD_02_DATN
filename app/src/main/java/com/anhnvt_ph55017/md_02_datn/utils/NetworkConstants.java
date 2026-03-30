package com.anhnvt_ph55017.md_02_datn.utils;

import android.os.Build;

public class NetworkConstants {
    // Emulator Android: gọi localhost máy host qua 10.0.2.2
    private static final String EMULATOR_BASE_URL = "http://10.0.2.2:5000";

    // Máy thật cắm USB: dùng adb reverse tcp:5000 tcp:5000 để gọi localhost
    private static final String USB_REVERSE_BASE_URL = "http://127.0.0.1:5000";

    // Máy thật: IP LAN hiện tại của máy chạy backend (Wi-Fi)
    private static final String DEVICE_BASE_URL = "http://10.24.10.178:5000";

    // Bật true khi test bằng điện thoại cắm USB + adb reverse
    private static final boolean USE_USB_REVERSE = false;

    public static final String API_BASE_URL = getApiBaseUrl();

    public static String getApiBaseUrl() {
        if (isProbablyEmulator()) {
            return EMULATOR_BASE_URL;
        }

        return USE_USB_REVERSE ? USB_REVERSE_BASE_URL : DEVICE_BASE_URL;
    }

    private static boolean isProbablyEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }
}

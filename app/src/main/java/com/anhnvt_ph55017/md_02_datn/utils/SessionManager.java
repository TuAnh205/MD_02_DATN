package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "AppSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_ID_STR = "userIdStr";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_TOKEN = "token";

    private static final int DEFAULT_USER_ID = -1;

    // ===== SAVE USER SESSION (INT ID - LOCAL) =====
    public static void saveUserSession(Context context, int userId, String email, String fullname) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_ID_STR, String.valueOf(userId));
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, fullname);

        editor.apply();
    }

    // ===== SAVE USER SESSION (STRING ID - MONGO) =====
    public static void saveUserSession(Context context, String userId, String email, String fullname) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(KEY_USER_ID_STR, userId);

        // convert sang int để dùng cho code cũ
        int legacyId = DEFAULT_USER_ID;
        try {
            legacyId = Integer.parseInt(userId);
        } catch (Exception e) {
            if (userId != null) {
                legacyId = Math.abs(userId.hashCode());
            }
        }

        editor.putInt(KEY_USER_ID, legacyId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, fullname);

        editor.apply();
    }

    // ===== SAVE TOKEN =====
    public static void saveToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_TOKEN, token).apply();
    }

    // ===== GET TOKEN =====
    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TOKEN, "");
    }

    // ===== GET USER ID =====
    public static int getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_USER_ID, DEFAULT_USER_ID);
    }

    public static String getUserIdString(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USER_ID_STR, "");
    }

    // ===== GET USER INFO =====
    public static String getUserEmail(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public static String getUserName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USER_NAME, "");
    }

    // ===== CHECK LOGIN =====
    public static boolean isLoggedIn(Context context) {
        return getUserId(context) != DEFAULT_USER_ID;
    }

    // ===== LOGOUT =====
    public static void clearSession(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}
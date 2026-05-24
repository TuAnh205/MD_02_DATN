package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationManager {

    private static final String PREF_NAME = "notifications_pref";
    private static final String KEY_NOTIFICATION_COUNT = "notification_count";
    private static final String KEY_READ_COUNT = "notification_read_count";

    public static void incrementNotification(Context context) {
        int count = getStoredNotificationCount(context);
        saveNotificationCount(context, count + 1);
    }

    public static void decrementNotification(Context context) {
        int count = getStoredNotificationCount(context);
        if (count > 0) {
            saveNotificationCount(context, count - 1);
        }
    }

    public static int getNotificationCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int total = prefs.getInt(KEY_NOTIFICATION_COUNT, 0);
        int read = prefs.getInt(KEY_READ_COUNT, 0);
        return Math.max(0, total - read);
    }

    public static void saveNotificationCount(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int read = prefs.getInt(KEY_READ_COUNT, 0);
        if (read > count) {
            read = count;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NOTIFICATION_COUNT, count);
        editor.putInt(KEY_READ_COUNT, read);
        editor.apply();
    }

    public static void markAllNotificationsAsRead(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int total = prefs.getInt(KEY_NOTIFICATION_COUNT, 0);
        prefs.edit().putInt(KEY_READ_COUNT, total).apply();
    }

    public static void clearNotifications(Context context) {
        markAllNotificationsAsRead(context);
    }

    private static int getStoredNotificationCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_NOTIFICATION_COUNT, 0);
    }
}

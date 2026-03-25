package com.anhnvt_ph55017.md_02_datn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationManager {

    private static final String PREF_NAME = "notifications_pref";
    private static final String KEY_NOTIFICATION_COUNT = "notification_count";

    public static void incrementNotification(Context context) {
        int count = getNotificationCount(context);
        saveNotificationCount(context, count + 1);
    }

    public static void decrementNotification(Context context) {
        int count = getNotificationCount(context);
        if (count > 0) {
            saveNotificationCount(context, count - 1);
        }
    }

    public static int getNotificationCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_NOTIFICATION_COUNT, 0);
    }

    public static void saveNotificationCount(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NOTIFICATION_COUNT, count);
        editor.apply();
    }

    public static void clearNotifications(Context context) {
        saveNotificationCount(context, 0);
    }
}

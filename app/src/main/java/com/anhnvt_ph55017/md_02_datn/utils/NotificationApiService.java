package com.anhnvt_ph55017.md_02_datn.utils;

import com.anhnvt_ph55017.md_02_datn.models.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface NotificationApiService {

    @GET("api/notifications")
    Call<List<Notification>> getNotifications(
            @Header("Authorization") String token
    );
}
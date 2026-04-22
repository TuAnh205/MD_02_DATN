package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.NotificationAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Notification;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.NotificationApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rcvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> list;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rcvNotifications = findViewById(R.id.rcvNotifications);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        list = new ArrayList<>();
        adapter = new NotificationAdapter(list, this);

        rcvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rcvNotifications.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        String rawToken = SessionManager.getToken(this);

        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + rawToken;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkConstants.API_BASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NotificationApiService api = retrofit.create(NotificationApiService.class);

        api.getNotifications(token).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Code: " + response.code());
                    Toast.makeText(NotificationActivity.this, "Lỗi API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API_FAIL", t.getMessage());
                Toast.makeText(NotificationActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
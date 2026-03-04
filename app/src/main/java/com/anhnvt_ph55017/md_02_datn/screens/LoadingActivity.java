package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;

public class LoadingActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView txtPercent;

    int progress = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);
        txtPercent = findViewById(R.id.txtPercent);

        startLoading();
    }

    private void startLoading() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress < 100) {
                    progress++;
                    progressBar.setProgress(progress);
                    txtPercent.setText(progress + "%");
                    handler.postDelayed(this, 30);
                } else {
                    // Sang màn hình chính
                    startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }, 30);
    }
}

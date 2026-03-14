package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;
import com.google.firebase.auth.FirebaseAuth;

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
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    Intent nextIntent;
                    if (mAuth.getCurrentUser() != null) {
                        nextIntent = new Intent(LoadingActivity.this, MainActivity.class);
                    } else {
                        nextIntent = new Intent(LoadingActivity.this, LoginActivity.class);
                    }
                    nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(nextIntent);
                    finish();
                }
            }
        }, 30);
    }
}

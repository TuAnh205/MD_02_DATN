package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.anhnvt_ph55017.md_02_datn.utils.ProfileApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
public class EditProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFullName, etEmail, etPhone;
    private AppCompatButton btnSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        loadProfile();

        btnBack.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        String token = SessionManager.getToken(this);

        ProfileApiService.fetchProfile(this, token, new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(org.json.JSONObject userJson) {
                runOnUiThread(() -> {
                    etFullName.setText(userJson.optString("name"));
                    etEmail.setText(userJson.optString("email"));
                    etPhone.setText(userJson.optString("phone"));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveProfile() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SessionManager.getToken(this);

        ProfileApiService.updateProfile(this, token, name, phone,
                new ProfileApiService.ProfileCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject userJson) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditProfileActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }
}
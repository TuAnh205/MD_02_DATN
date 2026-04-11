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
    private ImageView imgAvatar;
    private EditText etFullName, etEmail, etPhone, etBio;
    private AppCompatButton btnSaveChanges, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBio = findViewById(R.id.etBio);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);

        loadCurrentProfile();

        btnBack.setOnClickListener(v -> onBackPressed());
        btnCancel.setOnClickListener(v -> onBackPressed());

        btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        String token = SessionManager.getToken(this);
        ProfileApiService.fetchProfile(this, token, new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(org.json.JSONObject userJson) {
                runOnUiThread(() -> {
                    etFullName.setText(userJson.optString("name", ""));
                    etEmail.setText(userJson.optString("email", ""));
                    etPhone.setText(userJson.optString("phone", ""));
                    etEmail.setEnabled(false);
                    etEmail.setTextIsSelectable(false);
                    // Nếu backend có bio/avatar thì set thêm
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tên", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = SessionManager.getToken(this);
        ProfileApiService.updateProfile(this, token, fullName, phone, new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(org.json.JSONObject responseJson) {
                runOnUiThread(() -> {
                    org.json.JSONObject userJson = responseJson.optJSONObject("user");
                    if (userJson == null) {
                        userJson = responseJson;
                    }
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại session nếu cần
                    SessionManager.saveUserSession(EditProfileActivity.this,
                            userJson.optString("_id", ""),
                            userJson.optString("email", ""),
                            userJson.optString("name", "")
                    );
                    Intent result = new Intent();
                    result.putExtra("name", userJson.optString("name", ""));
                    result.putExtra("email", userJson.optString("email", ""));
                    result.putExtra("phone", userJson.optString("phone", ""));
                    setResult(RESULT_OK, result);
                    finish();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.ProfileApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etOldPass, etNewPass, etConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirmPass = findViewById(R.id.etConfirmPass);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnChangePass).setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirm = etConfirmPass.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải >= 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SessionManager.getToken(this);

        ProfileApiService.changePassword(this, token, oldPass, newPass,
                new ProfileApiService.ProfileCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject res) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Đổi mật khẩu thành công",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(ChangePasswordActivity.this,
                                        error,
                                        Toast.LENGTH_SHORT).show());
                    }
                });
    }
}
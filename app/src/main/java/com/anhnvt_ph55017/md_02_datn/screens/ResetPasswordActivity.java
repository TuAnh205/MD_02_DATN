package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;

import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText edtEmail;
    EditText edtToken;
    EditText edtNewPassword;
    EditText edtConfirmPassword;
    AppCompatButton btnResetPassword;
    ImageView btnBack;
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        edtEmail = findViewById(R.id.edtResetEmail);
        edtToken = findViewById(R.id.edtResetToken);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        tvMessage = findViewById(R.id.tvResetMessage);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String token = intent.getStringExtra("token");

        if (email != null) {
            edtEmail.setText(email);
            edtEmail.setEnabled(false);
        }

        if (token != null) {
            edtToken.setText(token);
        }

        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = edtEmail.getText().toString().trim();
        String token = edtToken.getText().toString().trim();
        String password = edtNewPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        if (email.isEmpty() || token.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnResetPassword.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("token", token);
            body.put("password", password);

            String url = NetworkConstants.getApiBaseUrl() + "/api/auth/reset-password";
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        btnResetPassword.setEnabled(true);
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        btnResetPassword.setEnabled(true);
                        String message = "Không thể đổi mật khẩu. Vui lòng thử lại.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            message = new String(error.networkResponse.data);
                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            btnResetPassword.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hệ thống", Toast.LENGTH_LONG).show();
        }
    }
}

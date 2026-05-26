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

public class ResetPass extends AppCompatActivity {
    EditText edtResetEmail;
    AppCompatButton btnReset;
    TextView tvBackLogin;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_pass);

        edtResetEmail = findViewById(R.id.edtResetEmail);
        btnReset = findViewById(R.id.btnReset);
        tvBackLogin = findViewById(R.id.tvBackLogin);
        btnBack = findViewById(R.id.btnBack);

        btnReset.setOnClickListener(view -> sendForgotPassword());
        tvBackLogin.setOnClickListener(view -> finish());
        btnBack.setOnClickListener(view -> finish());
    }

    private void sendForgotPassword() {
        String email = edtResetEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email hoặc tên tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        btnReset.setEnabled(false);

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            String url = NetworkConstants.getApiBaseUrl() + "/api/auth/forgot-password";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        btnReset.setEnabled(true);
                        String resetToken = response.optString("resetPasswordToken", "");
                        String emailResponse = response.optString("email", email);

                        if (resetToken.isEmpty()) {
                            Toast.makeText(this, "Không nhận được mã đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent intent = new Intent(this, ResetPasswordActivity.class);
                        intent.putExtra("email", emailResponse);
                        intent.putExtra("token", resetToken);
                        startActivity(intent);
                    },
                    error -> {
                        btnReset.setEnabled(true);
                        String message = "Không thể gửi yêu cầu. Vui lòng thử lại.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            message = new String(error.networkResponse.data);
                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            btnReset.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hệ thống", Toast.LENGTH_LONG).show();
        }
    }
}

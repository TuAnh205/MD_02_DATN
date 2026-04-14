package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPass, edtConfirm;
    AppCompatButton btnRegister;
    ImageView btnBack;
    TextView tvLogin;

    private static final String BASE_URL = NetworkConstants.API_BASE_URL + "/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass1);
        edtConfirm = findViewById(R.id.edtPass2);
        btnRegister = findViewById(R.id.btnRegister);

        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> gotoLogin());
        }

        tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> gotoLogin());
        }

        btnRegister.setOnClickListener(v -> register());
    }

    private void gotoLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void register() {

        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String pass = edtPass.getText().toString();
        String confirm = edtConfirm.getText().toString();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải ≥ 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 GỌI THẲNG API (KHÔNG FIREBASE)
        registerToServer(name, email, pass);
    }

    private void registerToServer(String name, String email, String password) {

        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);

            String url = BASE_URL + "/auth/register";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {

                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("prefill_email", email);
                        intent.putExtra("prefill_pass", password);
                        startActivity(intent);
                        finish();
                    },
                    error -> {

                        String message = "Có lỗi xảy ra, thử lại";

                        if (error.networkResponse != null) {

                            String err = new String(error.networkResponse.data);
                            int statusCode = error.networkResponse.statusCode;

                            android.util.Log.e("REGISTER_ERROR", err);

                            if (statusCode == 400) {
                                // lỗi client (email trùng)
                                if (err.contains("Email already exists") || err.contains("duplicate")) {
                                    message = "Email đã tồn tại";
                                } else {
                                    message = "Dữ liệu không hợp lệ";
                                }

                            } else if (statusCode == 500) {
                                message = "Lỗi server";
                            }

                        } else {
                            message = "Không có kết nối mạng";
                        }

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
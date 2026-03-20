package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.anhnvt_ph55017.md_02_datn.R;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPhone, edtPass, edtConfirm;
    Button btnRegister;

    // 🔥 dùng IP máy bạn (KHÔNG dùng 10.0.2.2)
    private static final String BASE_URL = "http://192.168.1.10:5000/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        edtPass = findViewById(R.id.edtPass1);
        edtConfirm = findViewById(R.id.edtPass2);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {

        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String phone = edtPhone.getText().toString().trim();
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

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 BƯỚC 1: TẠO FIREBASE ACCOUNT
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // 🔥 BƯỚC 2: LƯU MONGO
                        registerToServer(name, email, phone, pass);

                    } else {
                        Toast.makeText(this, "Firebase lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 🔥 CALL API BACKEND
    private void registerToServer(String name, String email, String phone, String password) {

        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
            body.put("phone", phone);

            String url = BASE_URL + "/auth/register";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {

                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        // 🔥 CHUYỂN SANG LOGIN + ĐIỀN SẴN
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("prefill_email", email);
                        intent.putExtra("prefill_pass", password);
                        startActivity(intent);
                        finish();
                    },
                    error -> {

                        if (error.networkResponse != null) {
                            String err = new String(error.networkResponse.data);
                            android.util.Log.e("REGISTER_ERROR", err);
                        }

                        Toast.makeText(this, "Email đã tồn tại hoặc lỗi server", Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
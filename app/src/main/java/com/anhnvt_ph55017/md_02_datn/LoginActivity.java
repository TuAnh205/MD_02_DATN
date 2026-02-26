package com.anhnvt_ph55017.md_02_datn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPass;
    private Button btnLogin;
    private TextView tvSignUp, tvReset;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ánh xạ
        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvReset = findViewById(R.id.tvResetPass);

        userDAO = new UserDAO(this);

        btnLogin.setOnClickListener(v -> login());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        tvReset.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPass.class))
        );
    }

    private void login() {
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String password = edtPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = userDAO.login(email, password);

        if (success) {
            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
}
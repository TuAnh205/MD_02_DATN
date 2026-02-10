package com.anhnvt_ph55017.md_02_datn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    EditText edtEmail,edtPass;
    TextView tvSignUp,tvReset;
    UserDAO userDAO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        btnLogin = findViewById(R.id.btn_login);
        tvReset = findViewById(R.id.tvResetPass);
        tvSignUp = findViewById(R.id.tvSignUp);

        userDAO = new UserDAO(this);
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPass.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDAO.login(email, pass)) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                 startActivity(new Intent(this, MainActivity.class));
            } else {
                Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ResetPass.class);
                startActivity(intent);
            }
        });

    }
}
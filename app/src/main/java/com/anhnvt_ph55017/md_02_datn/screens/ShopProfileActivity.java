package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

public class ShopProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserRole;
    private TextView tvUserEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_profile);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);

        String name = SessionManager.getUserName(this);
        String email = SessionManager.getUserEmail(this);
        String role = SessionManager.getUserRole(this);

        tvUserName.setText(name != null && !name.isEmpty() ? name : "Admin User");
        tvUserEmail.setText(email != null && !email.isEmpty() ? email : "Không có email");
        tvUserRole.setText(role != null && !role.isEmpty() ? role : "Quản trị viên");

        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}

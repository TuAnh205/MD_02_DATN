package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText edtCategoryName;
    private Button btnCancel, btnSave;
    private ImageView ivBack;
    private String categoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Initialize views
        edtCategoryName = findViewById(R.id.edtCategoryName);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        ivBack = findViewById(R.id.ivBack);

        // Check if editing
        if (getIntent().hasExtra("categoryId")) {
            categoryId = getIntent().getStringExtra("categoryId");
            String categoryName = getIntent().getStringExtra("categoryName");
            edtCategoryName.setText(categoryName);
            btnSave.setText("Cập nhật");
        }

        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Cancel button
        btnCancel.setOnClickListener(v -> onBackPressed());

        // Save button
        btnSave.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = edtCategoryName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Creating/updating categories is not supported by the current backend API
        // Show informational message and close the activity
        Toast.makeText(this, "Thao tác thêm/cập nhật danh mục chưa được hỗ trợ trên backend", Toast.LENGTH_LONG).show();
        finish();
    }
}

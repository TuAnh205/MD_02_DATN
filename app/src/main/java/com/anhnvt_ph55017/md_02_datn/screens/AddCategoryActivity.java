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

        String url;
        int method;

        if (categoryId != null) {
            // Update
            url = NetworkConstants.getApiBaseUrl() + "/api/shop/categories/" + categoryId;
            method = Request.Method.PUT;
        } else {
            // Create
            url = NetworkConstants.getApiBaseUrl() + "/api/shop/categories";
            method = Request.Method.POST;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                method,
                url,
                body,
                response -> {
                    Toast.makeText(this, categoryId != null ? "Cập nhật thành công" : "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Lỗi lưu danh mục", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}

package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.Adapters.CategoryAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Category;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories;
    private EditText edtSearchCategory;
    private ImageView ivBack;
    private Button btnAddCategory;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private CategoryAdapter adapter;
    private final List<Category> allCategories = new ArrayList<>();
    private final List<Category> filteredCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        edtSearchCategory = findViewById(R.id.edtSearchCategory);
        recyclerCategories = findViewById(R.id.recyclerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        adapter = new CategoryAdapter(this, filteredCategories, this::onCategoryAction);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategories.setAdapter(adapter);

        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Add category button
        btnAddCategory.setOnClickListener(v ->
                startActivity(new Intent(this, AddCategoryActivity.class)));

        // Search functionality
        edtSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCategories(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load categories
        loadCategories();
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        String token = SessionManager.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/categories";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allCategories.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            Category category = new Category(
                                    item.optString("_id", ""),
                                    item.optString("name", ""),
                                    item.optInt("productCount", 0)
                            );
                            allCategories.add(category);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    filteredCategories.clear();
                    filteredCategories.addAll(allCategories);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    updateEmptyState();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void searchCategories(String query) {
        filteredCategories.clear();

        if (query.isEmpty()) {
            filteredCategories.addAll(allCategories);
        } else {
            for (Category category : allCategories) {
                if (category.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredCategories.add(category);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCategories.isEmpty()) {
            recyclerCategories.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerCategories.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void onCategoryAction(Category category, String action) {
        if ("delete".equals(action)) {
            deleteCategory(category);
        } else if ("edit".equals(action)) {
            Intent intent = new Intent(this, AddCategoryActivity.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            startActivity(intent);
        }
    }

    private void deleteCategory(Category category) {
        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            return;
        }

        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/categories/" + category.getId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Xóa danh mục thành công", Toast.LENGTH_SHORT).show();
                    loadCategories();
                },
                error -> {
                    Toast.makeText(this, "Lỗi xóa danh mục", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}

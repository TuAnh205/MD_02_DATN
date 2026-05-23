package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    // ✅ Các biến khớp đúng với ID trong XML
    private RecyclerView recyclerViewProducts;
    private EditText edtSearch;           // cần thêm id này vào XML
    private ImageView btnMenu;
    private ImageView btnNotification;
    private Button btnAddProduct;
    private Button btnLoadMore;

    private CategoryAdapter adapter;
    private final List<Category> allCategories = new ArrayList<>();
    private final List<Category> filteredCategories = new ArrayList<>();

    // Phân trang
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories); // ← đổi tên layout nếu cần

        // ✅ Ánh xạ View theo đúng ID trong XML
        btnMenu           = findViewById(R.id.btnMenu);
        btnNotification   = findViewById(R.id.btnNotification);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        btnAddProduct     = findViewById(R.id.btnAddProduct);
        btnLoadMore       = findViewById(R.id.btnLoadMore);
        edtSearch         = findViewById(R.id.edtSearch); // ← thêm id này vào EditText trong XML

        // Setup RecyclerView
        adapter = new CategoryAdapter(this, filteredCategories, this::onCategoryAction);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(adapter);

        // ✅ btnMenu → quay lại
        btnMenu.setOnClickListener(v -> onBackPressed());

        // ✅ btnNotification → có thể mở màn thông báo (tuỳ chỉnh)
        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show());

        // ✅ btnAddProduct → mở màn thêm danh mục
        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AddCategoryActivity.class)));

        // ✅ btnLoadMore → tải thêm trang tiếp theo
        btnLoadMore.setOnClickListener(v -> {
            if (!isLoading) {
                currentPage++;
                loadCategories(currentPage);
            }
        });

        // ✅ Tìm kiếm realtime
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchCategories(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Tải trang đầu tiên
        loadCategories(currentPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Làm mới danh sách khi quay lại từ màn thêm/sửa
        currentPage = 1;
        allCategories.clear();
        filteredCategories.clear();
        adapter.notifyDataSetChanged();
        loadCategories(currentPage);
    }

    // ✅ Tải danh mục có hỗ trợ phân trang
    private void loadCategories(int page) {
        isLoading = true;
        btnLoadMore.setEnabled(false);
        btnLoadMore.setText("Đang tải...");

        String url = NetworkConstants.getApiBaseUrl()
                + "/api/products/categories?page=" + page + "&limit=" + PAGE_SIZE;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (page == 1) {
                        allCategories.clear();
                        filteredCategories.clear();
                    }

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

                    // Ẩn btnLoadMore nếu không còn dữ liệu
                    boolean hasMore = response.length() == PAGE_SIZE;
                    btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
                    btnLoadMore.setText("Xem thêm sản phẩm");
                    btnLoadMore.setEnabled(true);
                    isLoading = false;
                },
                error -> {
                    Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                    btnLoadMore.setText("Xem thêm sản phẩm");
                    btnLoadMore.setEnabled(true);
                    isLoading = false;
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    // ✅ Lọc danh sách theo từ khoá
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

        // Ẩn/hiện btnLoadMore khi đang tìm kiếm
        btnLoadMore.setVisibility(query.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ✅ Xử lý action từ Adapter (edit / delete)
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

    // ✅ Xoá danh mục
    private void deleteCategory(Category category) {
        String token = SessionManager.getToken(this);
        String url = NetworkConstants.getApiBaseUrl() + "/api/categories/" + category.getId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    allCategories.remove(category);
                    filteredCategories.remove(category);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Lỗi xóa danh mục", Toast.LENGTH_SHORT).show()
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
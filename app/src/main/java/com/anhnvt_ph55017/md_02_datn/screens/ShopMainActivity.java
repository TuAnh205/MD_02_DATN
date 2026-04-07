package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.Adapters.ShopProductAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopMainActivity extends AppCompatActivity {

    private RecyclerView recyclerShopProducts;
    private ProgressBar progressLoading;
    private TextView tvNoProducts;
    private TextView tvShopTitle;
    private View sidebarContainer;
    private View mainContentArea;
    private View layoutShopUserCard;
    private ImageView ivToggleSidebar;
    private Button btnAddProduct;
    private Button btnLoadMoreProducts;
    private EditText edtSearchProduct;
    private ShopProductAdapter adapter;
    private final List<Product> allShopProducts = new ArrayList<>();
    private final List<Product> shopProducts = new ArrayList<>();
    private static final int PREVIEW_PRODUCT_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_main);

        recyclerShopProducts = findViewById(R.id.recyclerShopProducts);
        progressLoading = findViewById(R.id.progress_bar_shop);
        tvNoProducts = findViewById(R.id.tv_no_products_shop);
        tvShopTitle = findViewById(R.id.tvShopTitle);
        sidebarContainer = findViewById(R.id.sidebarContainer);
        mainContentArea = findViewById(R.id.mainContentArea);
        layoutShopUserCard = findViewById(R.id.layoutShopUserCard);
        ivToggleSidebar = findViewById(R.id.ivToggleSidebar);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);

        tvShopTitle.setText("Quản lý sản phẩm");

        ivToggleSidebar.setVisibility(View.GONE);
        ivToggleSidebar.setOnClickListener(v -> {
            sidebarContainer.setVisibility(View.VISIBLE);
            ivToggleSidebar.setVisibility(View.GONE);
        });

        layoutShopUserCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopProfileActivity.class));
        });

        // Categories menu item
        View menuCategories = findViewById(R.id.menuCategories);
        menuCategories.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoriesActivity.class));
        });

        // Orders menu item
        View menuOrders = findViewById(R.id.menuOrders);
        menuOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });

        btnLoadMoreProducts = findViewById(R.id.btnLoadMoreProducts);
        btnLoadMoreProducts.setOnClickListener(v -> {
            showAllProducts();
        });

        // Setup search functionality
        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim().toLowerCase();
                searchProducts(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mainContentArea.setOnClickListener(v -> {
            if (sidebarContainer.getVisibility() == View.VISIBLE) {
                sidebarContainer.setVisibility(View.GONE);
                ivToggleSidebar.setVisibility(View.VISIBLE);
            }
        });

        adapter = new ShopProductAdapter(this, shopProducts);
        recyclerShopProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerShopProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));

        String role = SessionManager.getUserRole(this);
        if (!"shop".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Tài khoản không có quyền shop", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        loadShopProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh product list when returning to this activity
        loadShopProducts();
    }

    private void loadShopProducts() {
        progressLoading.setVisibility(View.VISIBLE);
        tvNoProducts.setVisibility(View.GONE);

        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            progressLoading.setVisibility(View.GONE);
            return;
        }

        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/products";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allShopProducts.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject item = response.optJSONObject(i);
                        if (item != null) {
                            Product product = parseProduct(item);
                            allShopProducts.add(product);
                        }
                    }
                    refreshProductPreview();
                    progressLoading.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(allShopProducts.isEmpty() ? View.VISIBLE : View.GONE);
                },
                error -> {
                    progressLoading.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(View.VISIBLE);
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Không thể kết nối server", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void refreshProductPreview() {
        shopProducts.clear();
        int count = Math.min(PREVIEW_PRODUCT_COUNT, allShopProducts.size());
        for (int i = 0; i < count; i++) {
            shopProducts.add(allShopProducts.get(i));
        }
        adapter.notifyDataSetChanged();
        btnLoadMoreProducts.setVisibility(allShopProducts.size() > PREVIEW_PRODUCT_COUNT ? View.VISIBLE : View.GONE);
    }

    private void showAllProducts() {
        shopProducts.clear();
        shopProducts.addAll(allShopProducts);
        adapter.notifyDataSetChanged();
        btnLoadMoreProducts.setVisibility(View.GONE);
    }

    private Product parseProduct(JSONObject item) {
        String id = item.optString("_id", "");
        String name = item.optString("name", "");
        double price = item.optDouble("price", 0);
        String imageUrl = item.optString("image", "");

        if ((imageUrl == null || imageUrl.isEmpty()) && item.has("images")) {
            JSONArray images = item.optJSONArray("images");
            if (images != null && images.length() > 0) {
                imageUrl = images.optString(0, "");
            }
        }

        String description = item.optString("description", "");
        int stock = item.optInt("stock", 0);
        return new Product(id, name, price, imageUrl, description, stock);
    }

    private void searchProducts(String searchQuery) {
        if (searchQuery.isEmpty()) {
            // Nếu search rỗng, hiển thị preview lại
            refreshProductPreview();
            return;
        }

        // Filter sản phẩm từ allShopProducts
        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : allShopProducts) {
            if (product.getName().toLowerCase().contains(searchQuery) ||
                product.getDescription().toLowerCase().contains(searchQuery)) {
                filteredProducts.add(product);
            }
        }

        // Cập nhật shopProducts với kết quả tìm kiếm
        shopProducts.clear();
        shopProducts.addAll(filteredProducts);
        adapter.notifyDataSetChanged();

        // Ẩn button "Xem thêm" khi đang search
        btnLoadMoreProducts.setVisibility(View.GONE);

        // Hiển thị thông báo nếu không tìm thấy
        tvNoProducts.setVisibility(filteredProducts.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

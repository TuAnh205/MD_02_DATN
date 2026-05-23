package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.TranslateAnimation;
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
import com.anhnvt_ph55017.md_02_datn.Adapters.ShopProductAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMainActivity extends AppCompatActivity {

    private RecyclerView recyclerShopProducts;
    private ProgressBar progressLoading;
    private TextView tvNoProducts;
    private TextView tvShopTitle;
    private View sidebarContainer;
    private View dimOverlay;
    private View mainContentArea;
    private View layoutShopUserCard;
    private ImageView ivToggleSidebar;
    private ImageView ivCloseSidebar;
    private Button btnAddProduct;
    private Button btnLoadMoreProducts;
    private EditText edtSearchProduct;
    private ShopProductAdapter adapter;
    private final List<Product> allShopProducts = new ArrayList<>();
    private final List<Product> shopProducts    = new ArrayList<>();
    private static final int PREVIEW_PRODUCT_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_main);

        // ── Ánh xạ view ──────────────────────────────────────────────────────
        recyclerShopProducts = findViewById(R.id.recyclerShopProducts);
        progressLoading      = findViewById(R.id.progress_bar_shop);
        tvNoProducts         = findViewById(R.id.tv_no_products_shop);
        tvShopTitle          = findViewById(R.id.tvShopTitle);
        sidebarContainer     = findViewById(R.id.sidebarContainer);
        dimOverlay           = findViewById(R.id.dimOverlay);
        mainContentArea      = findViewById(R.id.mainContentArea);
        layoutShopUserCard   = findViewById(R.id.layoutShopUserCard);
        ivToggleSidebar      = findViewById(R.id.ivToggleSidebar);
        ivCloseSidebar       = findViewById(R.id.ivCloseSidebar);
        btnAddProduct        = findViewById(R.id.btnAddProduct);
        btnLoadMoreProducts  = findViewById(R.id.btnLoadMoreProducts);
        edtSearchProduct     = findViewById(R.id.edtSearchProduct);

        // ── Sidebar logic ─────────────────────────────────────────────────────
        // Mở sidebar khi bấm nút hamburger
        ivToggleSidebar.setOnClickListener(v -> openSidebar());

        // Đóng sidebar khi bấm nút X
        ivCloseSidebar.setOnClickListener(v -> closeSidebar());

        // Đóng sidebar khi bấm vùng mờ bên ngoài
        dimOverlay.setOnClickListener(v -> closeSidebar());

        // ── Menu sidebar ──────────────────────────────────────────────────────
        layoutShopUserCard.setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopProfileActivity.class));
        });

        findViewById(R.id.menuDashboard).setOnClickListener(v -> closeSidebar());

        findViewById(R.id.menuCategories).setOnClickListener(v -> {
            closeSidebar();

        });

        findViewById(R.id.menuOrders).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, OrdersActivity.class));
        });

        findViewById(R.id.menuRevenue).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, RevenueActivity.class));
        });

        // ── Load more ─────────────────────────────────────────────────────────
        btnLoadMoreProducts.setOnClickListener(v -> showAllProducts());

        // ── Search ────────────────────────────────────────────────────────────
        edtSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString().trim().toLowerCase());
            }
        });

        // ── Adapter ───────────────────────────────────────────────────────────
        adapter = new ShopProductAdapter(this, shopProducts, new ShopProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product, int position) {
                Intent intent = new Intent(ShopMainActivity.this, AddProductActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("productName", product.getName());
                startActivity(intent);
            }

            @Override
            public void onToggleVisibility(Product product, int position) {
                toggleProductVisibility(product, position);
            }

            @Override
            public void onDelete(Product product, int position) {
                deleteProduct(product, position);
            }
        });

        recyclerShopProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerShopProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));

        // ── Kiểm tra quyền ───────────────────────────────────────────────────
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
        loadShopProducts();
    }

    // ── Mở / Đóng sidebar với animation ──────────────────────────────────────
    private void openSidebar() {
        sidebarContainer.setVisibility(View.VISIBLE);
        dimOverlay.setVisibility(View.VISIBLE);

        TranslateAnimation anim = new TranslateAnimation(-sidebarContainer.getWidth(), 0, 0, 0);
        anim.setDuration(250);
        sidebarContainer.startAnimation(anim);
    }

    private void closeSidebar() {
        TranslateAnimation anim = new TranslateAnimation(0, -sidebarContainer.getWidth(), 0, 0);
        anim.setDuration(200);
        anim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation a) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
            @Override public void onAnimationEnd(android.view.animation.Animation a) {
                sidebarContainer.setVisibility(View.GONE);
                dimOverlay.setVisibility(View.GONE);
            }
        });
        sidebarContainer.startAnimation(anim);
    }

    // ── Load sản phẩm từ API ──────────────────────────────────────────────────
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
                Request.Method.GET, url, null,
                response -> {
                    allShopProducts.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject item = response.optJSONObject(i);
                        if (item != null) allShopProducts.add(parseProduct(item));
                    }
                    refreshProductPreview();
                    progressLoading.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(allShopProducts.isEmpty() ? View.VISIBLE : View.GONE);
                },
                error -> {
                    progressLoading.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(View.VISIBLE);
                    String msg = (error.networkResponse != null)
                            ? "Lỗi tải dữ liệu: " + error.networkResponse.statusCode
                            : "Không thể kết nối server";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                h.put("Content-Type", "application/json");
                return h;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // ── Ẩn / Hiện sản phẩm ───────────────────────────────────────────────────
    private void toggleProductVisibility(Product product, int position) {
        String token = SessionManager.getToken(this);
        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/products/" + product.getId();
        JSONObject body = new JSONObject();
        try {
            body.put("isActive", !product.isVisible());
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi dữ liệu trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    product.setVisible(!product.isVisible());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this,
                            product.isVisible() ? "Đã hiện sản phẩm" : "Đã ẩn sản phẩm",
                            Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                h.put("Content-Type", "application/json");
                return h;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    // ── Xoá sản phẩm ─────────────────────────────────────────────────────────
    private void deleteProduct(Product product, int position) {
        String token = SessionManager.getToken(this);
        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/products/" + product.getId();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    allShopProducts.remove(product);
                    shopProducts.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, shopProducts.size());
                    tvNoProducts.setVisibility(shopProducts.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(this, "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Lỗi xoá sản phẩm", Toast.LENGTH_SHORT).show()
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                h.put("Content-Type", "application/json");
                return h;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void refreshProductPreview() {
        shopProducts.clear();
        int count = Math.min(PREVIEW_PRODUCT_COUNT, allShopProducts.size());
        for (int i = 0; i < count; i++) shopProducts.add(allShopProducts.get(i));
        adapter.notifyDataSetChanged();
        btnLoadMoreProducts.setVisibility(
                allShopProducts.size() > PREVIEW_PRODUCT_COUNT ? View.VISIBLE : View.GONE);
    }

    private void showAllProducts() {
        shopProducts.clear();
        shopProducts.addAll(allShopProducts);
        adapter.notifyDataSetChanged();
        btnLoadMoreProducts.setVisibility(View.GONE);
    }

    private Product parseProduct(JSONObject item) {
        String id          = item.optString("_id", "");
        String name        = item.optString("name", "");
        double price       = item.optDouble("price", 0);
        String imageUrl    = item.optString("image", "");
        String description = item.optString("description", "");
        int    stock       = item.optInt("stock", 0);
        boolean visible = item.optBoolean("isActive", item.optBoolean("isVisible", true));

        if ((imageUrl == null || imageUrl.isEmpty()) && item.has("images")) {
            JSONArray images = item.optJSONArray("images");
            if (images != null && images.length() > 0) imageUrl = images.optString(0, "");
        }

        Product product = new Product(id, name, price, imageUrl, description, stock);
        product.setVisible(visible);
        return product;
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            refreshProductPreview();
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : allShopProducts) {
            if (p.getName().toLowerCase().contains(query) ||
                    p.getDescription().toLowerCase().contains(query)) {
                filtered.add(p);
            }
        }
        shopProducts.clear();
        shopProducts.addAll(filtered);
        adapter.notifyDataSetChanged();
        btnLoadMoreProducts.setVisibility(View.GONE);
        tvNoProducts.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CartAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.utils.CartApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView tvSubtotal, tvTax, tvTotal;
    CheckBox cbAll;
    AppCompatButton btnCheckOut;

    List<Product> cartList;
    CartAdapter adapter;
    // com.anhnvt_ph55017.md_02_datn.DAO.CartDAO cartDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isLoggedIn(this)) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            Log.d("CartActivity", "CartActivity opened without login, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_cart);

        // ánh xạ view
        rvCart = findViewById(R.id.rvCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        cbAll = findViewById(R.id.cbAll);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        cartList = new ArrayList<>();

        setupAdapter();
        setupListeners();
        loadCartFromBackend();
    }

    private void setupAdapter() {
        adapter = new CartAdapter(this, cartList, this::calculateTotal, new CartAdapter.CartActionListener() {
            @Override
            public void onUpdateQuantity(Product product, int newQty) {
                updateCartItem(product, newQty);
            }
            @Override
            public void onRemove(Product product) {
                removeCartItem(product);
            }
        });
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);
    }

    private void setupListeners() {
        // select all
        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
            calculateTotal();
        });

        // nút checkout
        btnCheckOut.setOnClickListener(v -> {
            int userId = SessionManager.getUserId(this);
            if (userId <= 0) {
                Toast.makeText(this, "Please login to checkout", Toast.LENGTH_SHORT).show();
                Log.d("CartActivity", "Checkout clicked without login, redirecting to LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            ArrayList<Product> selected = new ArrayList<>();
            for (Product p : cartList) {
                if (p.isSelected()) selected.add(p);
            }
            if (selected.isEmpty()) {
                Toast.makeText(this, "Chọn sản phẩm trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckOutActivity.class);
            intent.putExtra("cart", selected);
            startActivity(intent);
        });
    }

    // ================= BACKEND CART ACTIONS =================
    private void updateCartItem(Product product, int newQty) {
        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) return;
        // Truyền cartItemId thay vì productId
        CartApiService.updateCartItem(this, token, product.getCartItemId(), newQty, new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                runOnUiThread(() -> loadCartFromBackend());
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, "Lỗi cập nhật số lượng: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void removeCartItem(Product product) {
        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) return;
        // Truyền cartItemId thay vì productId
        CartApiService.removeFromCart(this, token, product.getCartItemId(), new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                runOnUiThread(() -> loadCartFromBackend());
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, "Lỗi xoá sản phẩm: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateTotal();
    }

    // ================= LOAD API =================
    private void loadCartFromBackend() {
        String token = SessionManager.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        CartApiService.getCart(this, token, new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                runOnUiThread(() -> {
                    android.util.Log.d("CART_PARSE_DEBUG", "Raw cart json: " + cartJson.toString());
                    cartList.clear();

                    org.json.JSONArray items = cartJson.optJSONArray("items");
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            org.json.JSONObject obj = items.optJSONObject(i);
                            if (obj == null) {
                                android.util.Log.d("CART_PARSE_DEBUG", "Item obj null at i=" + i);
                                continue;
                            }

                            org.json.JSONObject prod = obj.optJSONObject("product");
                            if (prod == null) {
                                android.util.Log.d("CART_PARSE_DEBUG", "Product null at i=" + i + ", obj=" + obj.toString());
                                continue;
                            }

                            // ===== LẤY DATA =====
                            String id = prod.optString("_id");
                            String name = prod.optString("name");
                            double price = prod.optDouble("price");

                            // ===== FIX ẢNH (QUAN TRỌNG NHẤT) =====
                            String imageUrl = "";

                            // lấy từ images[]
                            org.json.JSONArray images = prod.optJSONArray("images");
                            if (images != null && images.length() > 0) {
                                imageUrl = images.optString(0);
                            }

                            // fallback nếu có image
                            if ((imageUrl == null || imageUrl.isEmpty()) && prod.has("image")) {
                                imageUrl = prod.optString("image");
                            }

                            // log check ảnh
                            android.util.Log.d("IMG_URL", imageUrl);

                            String desc = prod.optString("description");
                            int stock = prod.optInt("stock", 0);
                            int qty = obj.optInt("qty", 1);

                                // ===== TẠO PRODUCT =====
                                Product p = new Product(id, name, price, imageUrl, desc, stock);
                                p.setQty(qty);
                                // Lưu _id của item trong cart
                                String cartItemId = obj.optString("_id");
                                p.setCartItemId(cartItemId);

                                cartList.add(p);

                                android.util.Log.d("CART_PARSE_DEBUG",
                                    "Added: " + name + " | img=" + imageUrl + " | cartItemId=" + cartItemId);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    calculateTotal();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(CartActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // ================= TÍNH TIỀN =================
    public void calculateTotal() {
        double subtotal = 0;

        for (Product p : cartList) {
            if (p.isSelected()) {
                subtotal += p.getPrice() * p.getQty();
            }
        }

        double tax = subtotal * 0.1;
        double total = subtotal + tax;

        tvSubtotal.setText("Tạm tính: " + String.format("%,.0f đ", subtotal));
        tvTax.setText("Thuế: " + String.format("%,.0f đ", tax));
        tvTotal.setText("Tổng cộng: " + String.format("%,.0f đ", total));
    }
}
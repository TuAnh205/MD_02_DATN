package com.anhnvt_ph55017.md_02_datn.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CartAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.screens.CheckOutActivity;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.utils.CartApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    RecyclerView rvCart;
    TextView tvSubtotal, tvTax, tvTotal;
    CheckBox cbAll;
    AppCompatButton btnCheckOut;

    List<Product> cartList;
    CartAdapter adapter;

    public CartFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_cart, container, false);

        // check login
        if (!SessionManager.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        // ánh xạ view
        rvCart = view.findViewById(R.id.rvCart);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvTax = view.findViewById(R.id.tvTax);
        tvTotal = view.findViewById(R.id.tvTotal);
        cbAll = view.findViewById(R.id.cbAll);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);

        cartList = new ArrayList<>();

        setupAdapter();
        setupListeners();
        loadCartFromBackend();

        return view;
    }

    private void setupAdapter() {
        adapter = new CartAdapter(requireContext(), cartList, this::calculateTotal, new CartAdapter.CartActionListener() {
            @Override
            public void onUpdateQuantity(Product product, int newQty) {
                updateCartItem(product, newQty);
            }

            @Override
            public void onRemove(Product product) {
                removeCartItem(product);
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCart.setAdapter(adapter);
    }

    private void setupListeners() {

        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
            calculateTotal();
        });

        btnCheckOut.setOnClickListener(v -> {
            int userId = SessionManager.getUserId(requireContext());

            if (userId <= 0) {
                Toast.makeText(requireContext(), "Please login to checkout", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }

            ArrayList<Product> selected = new ArrayList<>();
            for (Product p : cartList) {
                if (p.isSelected()) selected.add(p);
            }

            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Chọn sản phẩm trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), CheckOutActivity.class);
            intent.putExtra("cart", selected);
            startActivity(intent);
        });
    }

    private void updateCartItem(Product product, int newQty) {
        String token = SessionManager.getToken(requireContext());
        if (token == null || token.isEmpty()) return;

        CartApiService.updateCartItem(requireContext(), token, product.getCartItemId(), newQty, new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                requireActivity().runOnUiThread(() -> loadCartFromBackend());
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void removeCartItem(Product product) {
        String token = SessionManager.getToken(requireContext());
        if (token == null || token.isEmpty()) return;

        CartApiService.removeFromCart(requireContext(), token, product.getCartItemId(), new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                requireActivity().runOnUiThread(() -> loadCartFromBackend());
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi xoá: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        calculateTotal();
    }

    private void loadCartFromBackend() {
        String token = SessionManager.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        CartApiService.getCart(requireContext(), token, new CartApiService.CartCallback() {
            @Override
            public void onSuccess(org.json.JSONObject cartJson) {
                requireActivity().runOnUiThread(() -> {
                    cartList.clear();

                    org.json.JSONArray items = cartJson.optJSONArray("items");
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            org.json.JSONObject obj = items.optJSONObject(i);
                            if (obj == null) continue;

                            org.json.JSONObject prod = obj.optJSONObject("product");
                            if (prod == null) continue;

                            String id = prod.optString("_id");
                            String name = prod.optString("name");
                            double price = prod.optDouble("price");

                            String imageUrl = "";
                            org.json.JSONArray images = prod.optJSONArray("images");
                            if (images != null && images.length() > 0) {
                                imageUrl = images.optString(0);
                            }

                            if ((imageUrl == null || imageUrl.isEmpty()) && prod.has("image")) {
                                imageUrl = prod.optString("image");
                            }

                            String desc = prod.optString("description");
                            int stock = prod.optInt("stock", 0);
                            int qty = obj.optInt("qty", 1);

                            Product p = new Product(id, name, price, imageUrl, desc, stock);
                            p.setQty(qty);
                            p.setCartItemId(obj.optString("_id"));

                            cartList.add(p);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    calculateTotal();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

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
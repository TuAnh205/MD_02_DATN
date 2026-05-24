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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CustomerAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Customer;
import com.anhnvt_ph55017.md_02_datn.utils.OrderApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShopCustomersActivity extends AppCompatActivity {
    private View sidebarContainer;
    private View dimOverlay;
    private View layoutShopUserCard;
    private RecyclerView rvCustomers;
    private CustomerAdapter adapter;
    private List<Customer> allCustomers = new ArrayList<>();
    private List<Customer> filteredCustomers = new ArrayList<>();
    private EditText edtSearch;
    private Button btnAll;
    private ImageView btnMenu, imgProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_customers);

        sidebarContainer = findViewById(R.id.sidebarContainer);
        dimOverlay = findViewById(R.id.dimOverlay);
        layoutShopUserCard = findViewById(R.id.layoutShopUserCard);
        rvCustomers = findViewById(R.id.rvCustomers);
        edtSearch = findViewById(R.id.edtSearch);
        btnAll = findViewById(R.id.btnAll);
        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);

        adapter = new CustomerAdapter(filteredCustomers, customer -> {
            Intent intent = new Intent(ShopCustomersActivity.this, CustomerManagementActivity.class);
            intent.putExtra(CustomerManagementActivity.EXTRA_CUSTOMER, customer);
            startActivity(intent);
        });
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(adapter);

        btnMenu.setOnClickListener(v -> openSidebar());
        dimOverlay.setOnClickListener(v -> closeSidebar());
        findViewById(R.id.ivCloseSidebar).setOnClickListener(v -> closeSidebar());

        layoutShopUserCard.setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopProfileActivity.class));
        });

        findViewById(R.id.menuDashboard).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuCategories).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuOrders).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, OrdersActivity.class));
        });

        findViewById(R.id.menuCustomers).setOnClickListener(v -> closeSidebar());

        findViewById(R.id.menuVoucher).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopVoucherListActivity.class));
        });

        findViewById(R.id.menuReviews).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopReviewsActivity.class));
        });

        findViewById(R.id.menuRevenue).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, RevenueActivity.class));
        });

        imgProfile.setOnClickListener(v -> startActivity(new Intent(this, ShopProfileActivity.class)));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAll.setOnClickListener(v -> {
            edtSearch.setText("");
            filterCustomers("");
        });

        loadCustomersFromApi();
    }

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
            @Override public void onAnimationStart(android.view.animation.Animation animation) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
            @Override public void onAnimationEnd(android.view.animation.Animation animation) {
                sidebarContainer.setVisibility(View.GONE);
                dimOverlay.setVisibility(View.GONE);
            }
        });
        sidebarContainer.startAnimation(anim);
    }

    private void loadCustomersFromApi() {
        String token = SessionManager.getToken(this);
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tải danh sách khách hàng", Toast.LENGTH_LONG).show();
            return;
        }

        OrderApiService.getShopOrders(this, token, new OrderApiService.OrdersCallback() {
            @Override
            public void onSuccess(JSONArray ordersJson) {
                runOnUiThread(() -> {
                    allCustomers.clear();
                    allCustomers.addAll(parseCustomersFromOrders(ordersJson));
                    filteredCustomers.clear();
                    filteredCustomers.addAll(allCustomers);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(ShopCustomersActivity.this, "Không tải được khách hàng: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private List<Customer> parseCustomersFromOrders(JSONArray ordersJson) {
        Map<String, CustomerSummary> customerMap = new HashMap<>();

        for (int i = 0; i < ordersJson.length(); i++) {
            JSONObject order = ordersJson.optJSONObject(i);
            if (order == null) continue;

            JSONObject user = order.optJSONObject("user");
            if (user == null) continue;

            String userId = user.optString("_id", user.optString("id", ""));
            if (userId.isEmpty()) continue;

            CustomerSummary summary = customerMap.get(userId);
            if (summary == null) {
                String name = user.optString("name", "Khách hàng");
                String email = user.optString("email", "");
                String phone = user.optString("phone", "");

                if (phone.isEmpty()) {
                    JSONObject shipping = order.optJSONObject("shipping");
                    if (shipping != null) {
                        JSONObject address = shipping.optJSONObject("address");
                        if (address != null) {
                            phone = address.optString("phone", "");
                        }
                    }
                }

                summary = new CustomerSummary(userId, name, email, phone, 0);
                customerMap.put(userId, summary);
            }

            summary.orderCount += 1;
        }

        List<Customer> customers = new ArrayList<>();
        for (CustomerSummary summary : customerMap.values()) {
            customers.add(new Customer(summary.id, summary.name, summary.email, summary.phone, summary.orderCount));
        }

        return customers;
    }

    private void filterCustomers(String query) {
        filteredCustomers.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredCustomers.addAll(allCustomers);
        } else {
            String q = query.toLowerCase(Locale.getDefault());
            for (Customer c : allCustomers) {
                if (c.getName().toLowerCase().contains(q)
                        || c.getEmail().toLowerCase().contains(q)
                        || c.getPhone().toLowerCase().contains(q)) {
                    filteredCustomers.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class CustomerSummary {
        private final String id;
        private final String name;
        private final String email;
        private final String phone;
        private int orderCount;

        private CustomerSummary(String id, String name, String email, String phone, int orderCount) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.orderCount = orderCount;
        }
    }
}

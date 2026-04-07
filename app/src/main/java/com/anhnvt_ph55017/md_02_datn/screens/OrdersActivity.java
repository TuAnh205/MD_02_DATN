package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.anhnvt_ph55017.md_02_datn.Adapters.OrderAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private ProgressBar progressLoadingOrders;
    private TextView tvNoOrders;
    private TextView tabAll, tabPreparing, tabShipping, tabDelivered;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> displayOrders = new ArrayList<>();
    private String currentFilter = "all"; // all, preparing, shipping, delivered

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressLoadingOrders = findViewById(R.id.progressLoadingOrders);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        ImageView btnBackOrders = findViewById(R.id.btnBackOrders);

        tabAll = findViewById(R.id.tabAll);
        tabPreparing = findViewById(R.id.tabPreparing);
        tabShipping = findViewById(R.id.tabShipping);
        tabDelivered = findViewById(R.id.tabDelivered);

        // Back button
        btnBackOrders.setOnClickListener(v -> onBackPressed());

        // Tab listeners
        tabAll.setOnClickListener(v -> setTab("all"));
        tabPreparing.setOnClickListener(v -> setTab("preparing"));
        tabShipping.setOnClickListener(v -> setTab("shipping"));
        tabDelivered.setOnClickListener(v -> setTab("delivered"));

        // Setup adapter
        adapter = new OrderAdapter(this, displayOrders, order -> {
            // Navigate to order detail
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);

        // Load orders
        loadOrders();
    }

    private void setTab(String filter) {
        currentFilter = filter;
        tabAll.setTextColor(getResources().getColor(filter.equals("all") ? android.R.color.holo_blue_light : android.R.color.darker_gray, null));
        tabPreparing.setTextColor(getResources().getColor(filter.equals("preparing") ? android.R.color.holo_blue_light : android.R.color.darker_gray, null));
        tabShipping.setTextColor(getResources().getColor(filter.equals("shipping") ? android.R.color.holo_blue_light : android.R.color.darker_gray, null));
        tabDelivered.setTextColor(getResources().getColor(filter.equals("delivered") ? android.R.color.holo_blue_light : android.R.color.darker_gray, null));
        
        filterOrders();
    }

    private void filterOrders() {
        displayOrders.clear();
        
        if (currentFilter.equals("all")) {
            displayOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                String status = order.getStatus();
                if (status == null) status = "pending";
                
                switch (currentFilter) {
                    case "preparing":
                        if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("processing")) {
                            displayOrders.add(order);
                        }
                        break;
                    case "shipping":
                        if (status.equalsIgnoreCase("shipping")) {
                            displayOrders.add(order);
                        }
                        break;
                    case "delivered":
                        if (status.equalsIgnoreCase("delivered")) {
                            displayOrders.add(order);
                        }
                        break;
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        tvNoOrders.setVisibility(displayOrders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadOrders() {
        progressLoadingOrders.setVisibility(View.VISIBLE);
        tvNoOrders.setVisibility(View.GONE);

        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            progressLoadingOrders.setVisibility(View.GONE);
            return;
        }

        String url = NetworkConstants.getApiBaseUrl() + "/api/orders";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allOrders.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject item = response.optJSONObject(i);
                        if (item != null) {
                            Order order = parseOrder(item);
                            allOrders.add(order);
                        }
                    }
                    filterOrders();
                    progressLoadingOrders.setVisibility(View.GONE);
                },
                error -> {
                    progressLoadingOrders.setVisibility(View.GONE);
                    tvNoOrders.setVisibility(View.VISIBLE);
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

    private Order parseOrder(JSONObject item) {
        String id = item.optString("_id", "");
        String date = item.optString("createdAt", "");
        double total = item.optDouble("totalPrice", 0);
        String status = item.optString("status", "pending");
        String arrivalDate = item.optString("arrivalDate", "");
        int itemCount = item.optInt("itemCount", 0);
        String productName = item.optString("customerName", "");

        // Format date to Vietnamese format
        String formattedDate = formatDate(date);
        
        // Extract order number from ID
        String orderId = "#CT-" + id.substring(Math.max(0, id.length() - 5));

        return new Order(orderId, formattedDate, total, status, arrivalDate, itemCount, 0, productName, 0, "", "");
    }

    private String formatDate(String date) {
        // Convert ISO date to Vietnamese format (DD/MM/YYYY • HH:MM)
        try {
            if (date == null || date.isEmpty()) return "";
            // Parse ISO format: 2023-10-14T14:30:00Z
            String[] parts = date.split("T");
            String datePart = parts[0];
            String timePart = parts.length > 1 ? parts[1].substring(0, 5) : "00:00";
            
            String[] dateParts = datePart.split("-");
            if (dateParts.length == 3) {
                return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " • " + timePart;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}

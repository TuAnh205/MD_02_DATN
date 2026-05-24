package com.anhnvt_ph55017.md_02_datn.screens;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.anhnvt_ph55017.md_02_datn.Adapters.UserOrderAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.models.OrderItem;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderStatusChangeListener {

    private RecyclerView recyclerOrders;
    private ProgressBar progressLoadingOrders;
    private TextView tvNoOrders;
    private TextView tabPending, tabConfirmed, tabShipping, tabDelivered, tabCancelled;
    private View tabIndicator;
    private TextView[] tabViews;
    private UserOrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> displayOrders = new ArrayList<>();
    private String currentFilter = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressLoadingOrders = findViewById(R.id.progressLoadingOrders);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        ImageView btnBackOrders = findViewById(R.id.btnBackOrders);

        tabPending = findViewById(R.id.tabPending);
        tabConfirmed = findViewById(R.id.tabConfirmed);
        tabShipping = findViewById(R.id.tabShipping);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);
        tabIndicator = findViewById(R.id.tabIndicator);
        tabViews = new TextView[]{tabPending, tabConfirmed, tabShipping, tabDelivered, tabCancelled};

        btnBackOrders.setOnClickListener(v -> onBackPressed());

        tabPending.setOnClickListener(v -> setTab("pending", 0));
        tabConfirmed.setOnClickListener(v -> setTab("confirmed", 1));
        tabShipping.setOnClickListener(v -> setTab("shipping", 2));
        tabDelivered.setOnClickListener(v -> setTab("delivered", 3));
        tabCancelled.setOnClickListener(v -> setTab("cancelled", 4));

        adapter = new UserOrderAdapter(this, displayOrders, order -> {
            Intent intent = new Intent(this, UserOrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("orderStatus", order.getStatus());
            intent.putExtra("orderDate", order.getDate());
            intent.putExtra("orderTotal", order.getTotal());
            intent.putExtra("itemCount", order.getItemCount());
            intent.putExtra("arrivalDate", order.getArrivalDate());
            intent.putExtra("shippingAddress", order.getShippingAddress());
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                intent.putExtra("orderItems", new ArrayList<>(order.getItems()));
            }
            startActivity(intent);
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);

        setTab("pending", 0);
        loadOrders();
    }

    private void setTab(String filter, int tabIndex) {
        currentFilter = filter;

        int selectedColor = 0xFF0A6ED8;
        int normalColor = 0xFF64748B;

        for (int i = 0; i < tabViews.length; i++) {
            tabViews[i].setTextColor(i == tabIndex ? selectedColor : normalColor);
            tabViews[i].setTypeface(null, i == tabIndex ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        moveIndicatorToTab(tabIndex);
        filterOrders();
    }

    private void moveIndicatorToTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabViews.length) {
            return;
        }

        tabViews[tabIndex].post(() -> {
            int[] tabLocation = new int[2];
            tabViews[tabIndex].getLocationOnScreen(tabLocation);

            int[] parentLocation = new int[2];
            ((View) tabViews[tabIndex].getParent()).getLocationOnScreen(parentLocation);

            int targetX = tabLocation[0] - parentLocation[0];
            int targetWidth = tabViews[tabIndex].getWidth();

            ViewGroup.LayoutParams params = tabIndicator.getLayoutParams();
            params.width = targetWidth;
            tabIndicator.setLayoutParams(params);

            ObjectAnimator.ofFloat(tabIndicator, "translationX", targetX)
                    .setDuration(220)
                    .start();
        });
    }

    private void filterOrders() {
        displayOrders.clear();

        for (Order order : allOrders) {
            if (matchesFilter(order.getStatus(), currentFilter)) {
                displayOrders.add(order);
            }
        }

        adapter.notifyDataSetChanged();
        tvNoOrders.setVisibility(displayOrders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesFilter(String status, String filter) {
        if (filter == null) {
            return true;
        }

        String normalizedStatus = normalizeStatus(status);
        return normalizedStatus.equals(filter);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }

        String normalized = status.trim().toLowerCase();
        switch (normalized) {
            case "pending":
            case "processing":
            case "chờ xác nhận":
            case "cho xac nhan":
                return "pending";
            case "confirmed":
            case "đã xác nhận":
            case "da xac nhan":
                return "confirmed";
            case "shipping":
            case "shipped":
            case "đang giao":
            case "dang giao":
                return "shipping";
            case "delivered":
            case "đã nhận":
            case "da nhan":
            case "đã giao":
            case "da giao":
                return "delivered";
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return "cancelled";
            default:
                return normalized;
        }
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

        String url = getOrdersEndpoint();

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

    private String getOrdersEndpoint() {
        String role = SessionManager.getUserRole(this);
        if ("shop".equalsIgnoreCase(role) || "seller".equalsIgnoreCase(role)) {
            return NetworkConstants.getApiBaseUrl() + "/api/shop/orders";
        }
        return NetworkConstants.getApiBaseUrl() + "/api/orders";
    }

    @Override
    public void onOrderStatusChanged() {
        loadOrders();
    }

    private Order parseOrder(JSONObject item) {
        String id = item.optString("_id", item.optString("id", ""));
        String orderNumber = item.optString("orderNumber", "");
        String createdAt = item.optString("createdAt", item.optString("date", ""));
        String status = item.optString("status", "chờ xác nhận");
        double total = item.optDouble("total", item.optDouble("totalPrice", 0));
        String arrivalDate = item.optString("arrivalDate", "");

        JSONObject userObj = item.optJSONObject("user");
        String customerName = userObj != null ? userObj.optString("name", userObj.optString("fullname", userObj.optString("email", ""))) : "";

        List<OrderItem> parsedItems = parseItems(item.optJSONArray("items"));
        int itemCount = item.optInt("itemCount", parsedItems.size());
        String firstImage = extractFirstImage(parsedItems, item);
        String summary = buildSummary(parsedItems, itemCount);
        String shippingAddress = buildShippingAddress(item.optJSONObject("shipping"), item.optString("shippingAddress", ""));

        String formattedDate = formatDate(createdAt);
        String orderCode = buildOrderCode(orderNumber, id);

        Order order = new Order(id, orderCode, formattedDate, total, status, customerName, summary, firstImage, itemCount);
        order.setArrivalDate(arrivalDate);
        order.setItems(parsedItems);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(createdAt);
        order.setProductImageUrl(firstImage);
        return order;
    }

    private List<OrderItem> parseItems(JSONArray itemsArray) {
        List<OrderItem> items = new ArrayList<>();
        if (itemsArray == null) {
            return items;
        }

        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject item = itemsArray.optJSONObject(i);
            if (item == null) {
                continue;
            }

            JSONObject product = item.optJSONObject("product");
            String name = item.optString("name", product != null ? product.optString("name", "Sản phẩm") : "Sản phẩm");
            double price = item.optDouble("price", product != null ? product.optDouble("price", 0) : 0);
            int qty = item.optInt("qty", 1);
            String imageUrl = item.optString("image", "");
            if (imageUrl.isEmpty() && product != null) {
                imageUrl = product.optString("image", "");
                if (imageUrl.isEmpty() && product.optJSONArray("images") != null && product.optJSONArray("images").length() > 0) {
                    imageUrl = product.optJSONArray("images").optString(0, "");
                }
            }

            items.add(new OrderItem(name, price, qty, 0, imageUrl));
        }

        return items;
    }

    private String extractFirstImage(List<OrderItem> parsedItems, JSONObject item) {
        if (parsedItems != null && !parsedItems.isEmpty() && parsedItems.get(0).getImageUrl() != null && !parsedItems.get(0).getImageUrl().isEmpty()) {
            return parsedItems.get(0).getImageUrl();
        }

        JSONArray itemsArray = item.optJSONArray("items");
        if (itemsArray == null || itemsArray.length() == 0) {
            return "";
        }

        JSONObject firstItem = itemsArray.optJSONObject(0);
        if (firstItem == null) {
            return "";
        }

        JSONObject product = firstItem.optJSONObject("product");
        if (product != null) {
            String image = product.optString("image", "");
            if (!image.isEmpty()) {
                return image;
            }
            if (product.optJSONArray("images") != null && product.optJSONArray("images").length() > 0) {
                return product.optJSONArray("images").optString(0, "");
            }
        }

        return firstItem.optString("image", "");
    }

    private String buildSummary(List<OrderItem> parsedItems, int itemCount) {
        if (parsedItems == null || parsedItems.isEmpty()) {
            return itemCount + " sản phẩm";
        }

        int totalItems = parsedItems.size();
        String firstName = parsedItems.get(0).getName();
        if (firstName == null || firstName.trim().isEmpty()) {
            firstName = "Sản phẩm";
        }

        if (totalItems == 1) {
            return totalItems + " sản phẩm • " + firstName.trim();
        }

        StringBuilder names = new StringBuilder(firstName.trim());
        if (totalItems >= 2) {
            String secondName = parsedItems.get(1).getName();
            if (secondName == null || secondName.trim().isEmpty()) {
                secondName = "Sản phẩm";
            }
            names.append(", ").append(secondName.trim());
        }

        if (totalItems > 2) {
            names.append(" + ").append(totalItems - 2).append(" sản phẩm khác");
        }

        return totalItems + " sản phẩm • " + names;
    }

    private String buildShippingAddress(JSONObject shippingObj, String fallbackAddress) {
        if (shippingObj != null) {
            JSONObject address = shippingObj.optJSONObject("address");
            if (address != null) {
                StringBuilder builder = new StringBuilder();
                appendIfNotEmpty(builder, address.optString("address", ""));
                appendIfNotEmpty(builder, address.optString("district", ""));
                appendIfNotEmpty(builder, address.optString("city", ""));
                String result = builder.toString().replaceAll("^,\\s*|,\\s*$", "");
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return fallbackAddress;
    }

    private void appendIfNotEmpty(StringBuilder builder, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value.trim());
        }
    }

    private String buildOrderCode(String orderNumber, String id) {
        if (orderNumber != null && !orderNumber.isEmpty()) {
            return orderNumber;
        }

        if (id == null || id.isEmpty()) {
            return "#CT-00000";
        }

        String suffix = id.length() > 5 ? id.substring(id.length() - 5) : id;
        return "#CT-" + suffix;
    }

    private String formatDate(String date) {
        try {
            if (date == null || date.isEmpty()) {
                return "";
            }

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

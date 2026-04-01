package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.OrderItemAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.OrderItem;
import com.anhnvt_ph55017.md_02_datn.utils.OrderApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvStatusDescription;
    Button btnCancel, btnBuyAgain, btnSubmitRating;
    RatingBar ratingBar;
    EditText edtRatingComment;
    RecyclerView rvOrderItems;

    String orderId, orderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        try {
            // ===== INIT VIEW =====
            tvOrderId = findViewById(R.id.tvOrderId);
            tvOrderDate = findViewById(R.id.tvOrderDate);
            tvOrderStatus = findViewById(R.id.tvOrderStatus);
            tvOrderTotal = findViewById(R.id.tvOrderTotal);
            tvStatusDescription = findViewById(R.id.tvStatusDescription);
            btnCancel = findViewById(R.id.btnCancel);
            btnBuyAgain = findViewById(R.id.btnBuyAgain);
            btnSubmitRating = findViewById(R.id.btnSubmitRating);
            ratingBar = findViewById(R.id.ratingBar);
            edtRatingComment = findViewById(R.id.edtRatingComment);
            rvOrderItems = findViewById(R.id.rvOrderItems);

            // ===== GET DATA =====
            Intent intent = getIntent();
            orderId = intent.getStringExtra("orderId");
            orderStatus = intent.getStringExtra("orderStatus");
            String orderDate = intent.getStringExtra("orderDate");
            double total = intent.getDoubleExtra("orderTotal", 0);

            if (orderId == null) orderId = "N/A";
            if (orderStatus == null) orderStatus = "pending";

            Log.d("STATUS_DEBUG", "Status backend: " + orderStatus);

            // ===== SET UI =====
            tvOrderId.setText("Đơn #" + orderId);
            tvOrderDate.setText(orderDate);
            tvOrderStatus.setText(getStatusVietnamese(orderStatus));
            tvOrderTotal.setText("$" + total);

            setStatusColor(tvOrderStatus, orderStatus);
            tvStatusDescription.setText(getStatusDescription(orderStatus));

            // ===== LIST ITEM =====
            List<OrderItem> list = (List<OrderItem>) intent.getSerializableExtra("orderItems");
            if (list == null) list = new ArrayList<>();

            OrderItemAdapter adapter = new OrderItemAdapter(list);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(adapter);

            // ===== CANCEL BUTTON =====
            if (isCancelled(orderStatus) || orderStatus.equalsIgnoreCase("delivered")) {
                btnCancel.setVisibility(android.view.View.GONE);
                btnBuyAgain.setVisibility(android.view.View.VISIBLE);
            } else {
                btnCancel.setVisibility(android.view.View.VISIBLE);
                btnBuyAgain.setVisibility(android.view.View.GONE);
            }

            btnCancel.setOnClickListener(v -> cancelOrder());

            // ===== RATING =====
            if (orderStatus.equalsIgnoreCase("delivered")) {
                btnSubmitRating.setVisibility(android.view.View.VISIBLE);
            } else {
                btnSubmitRating.setVisibility(android.view.View.GONE);
            }

        } catch (Exception e) {
            Log.e("ORDER_DETAIL", "Error", e);
            Toast.makeText(this, "Lỗi màn chi tiết", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ================= STATUS =================

    private String getStatusVietnamese(String status) {
        if (status == null) return "";

        switch (status.toLowerCase()) {
            case "pending":
                return "Chưa thanh toán";
            case "processing":
                return "Đang xử lý";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Đã nhận";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private String getStatusDescription(String status) {
        if (status == null) return "";

        switch (status.toLowerCase()) {
            case "pending":
                return "Đơn đang chờ xử lý";
            case "processing":
                return "Đang chuẩn bị hàng";
            case "shipping":
                return "Đang giao đến bạn";
            case "delivered":
                return "Giao thành công";
            case "cancelled":
                return "Đơn đã bị hủy";
            default:
                return "";
        }
    }

    private boolean isCancelled(String status) {
        return status != null && status.equalsIgnoreCase("cancelled");
    }

    private void setStatusColor(TextView tv, String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "pending":
                tv.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                break;
            case "processing":
                tv.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "shipping":
                tv.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                break;
            case "delivered":
                tv.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "cancelled":
                tv.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
        }
    }

    // ================= CANCEL ORDER =================

    private void cancelOrder() {

        String token = SessionManager.getToken(this);

        OrderApiService.cancelOrder(this, token, orderId, "User cancel", new OrderApiService.CancelOrderCallback() {
            @Override
            public void onSuccess(org.json.JSONObject json) {
                runOnUiThread(() -> {
                    orderStatus = "cancelled";

                    tvOrderStatus.setText(getStatusVietnamese(orderStatus));
                    setStatusColor(tvOrderStatus, orderStatus);
                    tvStatusDescription.setText("Đơn đã bị hủy");

                    btnCancel.setVisibility(android.view.View.GONE);
                    btnBuyAgain.setVisibility(android.view.View.VISIBLE);

                    Toast.makeText(OrderDetailActivity.this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String err) {
                runOnUiThread(() ->
                        Toast.makeText(OrderDetailActivity.this, "Lỗi: " + err, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
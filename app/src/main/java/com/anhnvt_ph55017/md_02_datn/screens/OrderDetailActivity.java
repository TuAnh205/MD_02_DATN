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

    TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvStatusDescription, tvShippingAddress, tvItemCount, tvArrivalDate;
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
            // Ẩn các view đánh giá (ratingCard đã bị xóa khỏi layout, không cần xử lý)
                        // Địa chỉ giao hàng
                        tvShippingAddress = findViewById(R.id.tvShippingAddress);
                        // Lấy địa chỉ từ 3 trường nếu có
                        String address = getIntent().getStringExtra("address");
                        String district = getIntent().getStringExtra("district");
                        String city = getIntent().getStringExtra("city");
                        String shippingAddress = getIntent().getStringExtra("shippingAddress");
                        String fullAddress = "";
                        if (address != null && !address.isEmpty()) fullAddress += address;
                        if (district != null && !district.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + district;
                        if (city != null && !city.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + city;
                        if (fullAddress.isEmpty() && shippingAddress != null && !shippingAddress.isEmpty()) {
                            fullAddress = shippingAddress;
                        }
                        if (fullAddress.isEmpty()) fullAddress = "Không có thông tin";
                        tvShippingAddress.setText(fullAddress);

                        // Số lượng sản phẩm
                        tvItemCount = findViewById(R.id.tvItemCount);
                        int itemCount = getIntent().getIntExtra("itemCount", 0);
                        tvItemCount.setText(String.valueOf(itemCount));

                        // Dự kiến nhận
                        tvArrivalDate = findViewById(R.id.tvArrivalDate);
                        String arrivalDate = getIntent().getStringExtra("arrivalDate");
                        // Nếu chưa có thì hiển thị "Chưa có"
                        if (arrivalDate == null || arrivalDate.isEmpty()) {
                            tvArrivalDate.setText("Chưa có");
                        } else {
                            // Nếu trạng thái là đã xác nhận thì cộng thêm 4 ngày
                            if (orderStatus.equalsIgnoreCase("processing") || orderStatus.equalsIgnoreCase("xác nhận") || orderStatus.equalsIgnoreCase("confirmed")) {
                                try {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                                    java.util.Calendar cal = java.util.Calendar.getInstance();
                                    cal.setTime(sdf.parse(arrivalDate));
                                    cal.add(java.util.Calendar.DATE, 4);
                                    String newDate = sdf.format(cal.getTime());
                                    tvArrivalDate.setText(newDate);
                                } catch (Exception e) {
                                    tvArrivalDate.setText(arrivalDate);
                                }
                            } else {
                                tvArrivalDate.setText(arrivalDate);
                            }
                        }
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
            List<OrderItem> list = (List<OrderItem>) getIntent().getSerializableExtra("orderItems");
            if (list == null) list = new ArrayList<>();
            OrderItemAdapter adapter = new OrderItemAdapter(list);
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(adapter);

            // ===== CANCEL BUTTON =====
            if (btnCancel != null && btnBuyAgain != null) {
                // Chỉ cho phép hủy khi trạng thái là 'pending' hoặc tương đương
                boolean canCancel = false;
                if (orderStatus != null) {
                    String s = orderStatus.trim().toLowerCase();
                    canCancel = s.equals("pending") || s.equals("chờ xác nhận") || s.equals("cho xac nhan") || s.equals("chua xac nhan");
                }
                if (canCancel) {
                    btnCancel.setVisibility(android.view.View.VISIBLE);
                    btnBuyAgain.setVisibility(android.view.View.GONE);
                    btnCancel.setOnClickListener(v -> showCancelDialog());
                } else {
                    btnCancel.setVisibility(android.view.View.GONE);
                    btnBuyAgain.setVisibility(android.view.View.VISIBLE);
                    btnBuyAgain.setOnClickListener(v -> {
                        Intent intentHome = new Intent(OrderDetailActivity.this, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentHome);
                        finish();
                    });
                }
            }
    // Hiện dialog xác nhận hủy đơn hàng


            // ===== RATING =====
            if (btnSubmitRating != null) {
                if (orderStatus.equalsIgnoreCase("delivered")) {
                    btnSubmitRating.setVisibility(android.view.View.VISIBLE);
                } else {
                    btnSubmitRating.setVisibility(android.view.View.GONE);
                }
            }

        } catch (Exception e) {
            Log.e("ORDER_DETAIL", "Error", e);
            Toast.makeText(this, "Lỗi màn chi tiết", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ================= STATUS =================
    private void showCancelDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", (dialog, which) -> cancelOrder())
                .setNegativeButton("Không", null)
                .show();
    }
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
                    // Trả kết quả về fragment để reload danh sách
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("orderCancelled", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
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
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
    TextView tvPaymentMethod, tvVoucherDiscount;
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
            tvShippingAddress = findViewById(R.id.tvShippingAddress);
            tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
            tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
            rvOrderItems = findViewById(R.id.rvOrderItems);

            // ===== GET DATA =====
            Intent intent = getIntent();
            orderId = intent.getStringExtra("orderId");
            orderStatus = intent.getStringExtra("orderStatus");
            String orderDate = intent.getStringExtra("orderDate");
            double total = intent.getDoubleExtra("orderTotal", 0);
            String paymentMethod = intent.getStringExtra("paymentMethod");
            double voucherDiscount = intent.getDoubleExtra("voucherDiscount", 0);

            if (orderId == null) orderId = "N/A";
            if (orderStatus == null) orderStatus = "pending";

            String address = intent.getStringExtra("address");
            String district = intent.getStringExtra("district");
            String city = intent.getStringExtra("city");
            String shippingAddress = intent.getStringExtra("shippingAddress");
            String fullAddress = "";
            if (address != null && !address.isEmpty()) fullAddress += address;
            if (district != null && !district.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + district;
            if (city != null && !city.isEmpty()) fullAddress += (fullAddress.isEmpty() ? "" : ", ") + city;
            if (fullAddress.isEmpty() && shippingAddress != null && !shippingAddress.isEmpty()) {
                fullAddress = shippingAddress;
            }
            if (fullAddress.isEmpty()) fullAddress = "Không có thông tin";
            tvShippingAddress.setText(fullAddress);

            tvItemCount = findViewById(R.id.tvItemCount);
            int itemCount = intent.getIntExtra("itemCount", 0);
            tvItemCount.setText(String.valueOf(itemCount));

            tvArrivalDate = findViewById(R.id.tvArrivalDate);
            String arrivalDate = intent.getStringExtra("arrivalDate");
            if (arrivalDate == null || arrivalDate.isEmpty()) {
                tvArrivalDate.setText("Chưa có");
            } else {
                if (shouldAddEstimatedDelivery(orderStatus)) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(sdf.parse(arrivalDate));
                        cal.add(java.util.Calendar.DATE, 4);
                        tvArrivalDate.setText(sdf.format(cal.getTime()));
                    } catch (Exception e) {
                        tvArrivalDate.setText(arrivalDate);
                    }
                } else {
                    tvArrivalDate.setText(arrivalDate);
                }
            }

            Log.d("STATUS_DEBUG", "Status backend: " + orderStatus);

            // ===== SET UI =====
            tvOrderId.setText("Đơn #" + orderId);
            tvOrderDate.setText(formatOrderDate(orderDate));
            tvOrderStatus.setText(getStatusVietnamese(orderStatus));
            tvOrderTotal.setText("$" + total);
            tvPaymentMethod.setText(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod : "Thanh toán khi nhận hàng");
            tvVoucherDiscount.setText(voucherDiscount > 0 ? formatPrice(voucherDiscount) : "-");

            setStatusColor(tvOrderStatus, orderStatus);
            tvStatusDescription.setText(getStatusDescription(orderStatus));

            // ===== LIST ITEM =====
            List<OrderItem> list = (List<OrderItem>) intent.getSerializableExtra("orderItems");
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

    private boolean shouldAddEstimatedDelivery(String status) {
        if (status == null) return false;
        String normalized = status.trim().toLowerCase();
        return normalized.equals("processing") || normalized.equals("xác nhận") || normalized.equals("confirmed")
                || normalized.equals("đã xác nhận") || normalized.equals("da xac nhan")
                || normalized.equals("chờ xác nhận") || normalized.equals("cho xac nhan");
    }

    private String formatOrderDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        if (date.contains("/") && date.contains("•")) {
            return date;
        }
        try {
            String[] parts = date.split("T");
            String datePart = parts[0];
            String timePart = parts.length > 1 ? parts[1].substring(0, Math.min(5, parts[1].length())) : "00:00";
            String[] dateParts = datePart.split("-");
            if (dateParts.length == 3) {
                return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " • " + timePart;
            }
        } catch (Exception ignored) {
        }
        return date;
    }

    private String formatPrice(double price) {
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return fmt.format((long) price) + "đ";
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
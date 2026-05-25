package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class UserOrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvStatusDescription;
    private TextView tvItemCount, tvArrivalDate, tvPaymentMethod, tvVoucherDiscount, tvOrderTotal, tvShippingAddress;
    private RecyclerView rvOrderItems;
    private Button btnCancelOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_user);

        ImageButton btnBack = findViewById(R.id.btnBackUserOrderDetail);
        btnBack.setOnClickListener(v -> finish());

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvStatusDescription = findViewById(R.id.tvStatusDescription);

        tvItemCount = findViewById(R.id.tvItemCount);
        tvArrivalDate = findViewById(R.id.tvArrivalDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);

        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));

        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        // Read intent extras
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String orderDate = intent.getStringExtra("orderDate");
        String status = intent.getStringExtra("orderStatus");
        String arrival = intent.getStringExtra("arrivalDate");
        String payment = intent.getStringExtra("paymentMethod");
        double voucherDiscount = readVoucherDiscount(intent);
        double total = intent.getDoubleExtra("orderTotal", 0);
        int itemCount = intent.getIntExtra("itemCount", 0);

        ArrayList<OrderItem> items = (ArrayList<OrderItem>) intent.getSerializableExtra("orderItems");
        if (items == null) items = new ArrayList<>();

        tvOrderId.setText(orderId != null ? orderId : "");
        tvOrderDate.setText(orderDate != null ? orderDate : "");
        tvOrderStatus.setText(status != null ? status : "");
        tvStatusDescription.setText("");

        tvItemCount.setText(String.valueOf(itemCount));
        tvArrivalDate.setText(arrival != null ? arrival : "");
        tvPaymentMethod.setText(payment != null && !payment.trim().isEmpty() ? payment : "Thanh toán khi nhận hàng");
        tvVoucherDiscount.setText(voucherDiscount > 0 ? formatPrice(voucherDiscount) : "-");
        tvOrderTotal.setText(formatPrice(total));
        tvShippingAddress.setText(intent.getStringExtra("shippingAddress") != null ? intent.getStringExtra("shippingAddress") : "");

        OrderItemAdapter adapter = new OrderItemAdapter(items);
        rvOrderItems.setAdapter(adapter);

        // Determine if cancel should be shown: allow when status is not accepted/delivered/shipping/cancelled
        boolean canCancel = true;
        if (status != null) {
            String s = status.toLowerCase();
            if (s.contains("accept") || s.contains("accepted") || s.contains("đã") || s.contains("giao") || s.contains("đang") || s.contains("delivered") || s.contains("shipping") || s.contains("cancelled")) {
                canCancel = false;
            }
        }

        if (canCancel) {
            btnCancelOrder.setVisibility(View.VISIBLE);
            btnCancelOrder.setOnClickListener(v -> doCancelOrder(orderId));
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void doCancelOrder(String orderId) {
        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để hủy đơn", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        btnCancelOrder.setEnabled(false);
        OrderApiService.cancelOrder(this, token, orderId, "Hủy bởi người dùng", new OrderApiService.CancelOrderCallback() {
            @Override
            public void onSuccess(org.json.JSONObject orderJson) {
                runOnUiThread(() -> {
                    Toast.makeText(UserOrderDetailActivity.this, "Đã hủy đơn", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnCancelOrder.setEnabled(true);
                    Toast.makeText(UserOrderDetailActivity.this, "Hủy đơn thất bại: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private double readVoucherDiscount(Intent intent) {
        if (intent == null || !intent.hasExtra("voucherDiscount")) {
            return 0;
        }

        Object extra = intent.getExtras() != null ? intent.getExtras().get("voucherDiscount") : null;
        if (extra instanceof Number) {
            return ((Number) extra).doubleValue();
        }

        if (extra instanceof String) {
            String rawValue = ((String) extra).replaceAll("[^\\d.]", "");
            if (rawValue.isEmpty()) {
                return 0;
            }
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    private String formatPrice(double price) {
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return fmt.format((long) price) + "đ";
    }
}

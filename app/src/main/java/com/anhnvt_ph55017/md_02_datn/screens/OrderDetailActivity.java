package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvOrderTotal;
    private TextView tvStatusDescription;
    private TextView tvShippingAddress;
    private TextView tvItemCount;
    private TextView tvArrivalDate;
    private TextView tvPaymentMethod;
    private TextView tvVoucherDiscount;

    private Button btnCancel;
    private Button btnBuyAgain;

    private RecyclerView rvOrderItems;

    private String orderId;
    private String orderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        loadData();
        setupBackButton();
    }

    // ================= INIT VIEW =================

    private void initViews() {

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvStatusDescription = findViewById(R.id.tvStatusDescription);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvArrivalDate = findViewById(R.id.tvArrivalDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);

        btnCancel = findViewById(R.id.btnCancel);
        btnBuyAgain = findViewById(R.id.btnBuyAgain);

        rvOrderItems = findViewById(R.id.rvOrderItems);
    }

    // ================= LOAD DATA =================

    private void loadData() {

        try {

            Intent intent = getIntent();

            orderId = intent.getStringExtra("orderId");
            orderStatus = intent.getStringExtra("orderStatus");

            String orderDate = intent.getStringExtra("orderDate");
            double total = intent.getDoubleExtra("orderTotal", 0);

            String shippingAddress = intent.getStringExtra("shippingAddress");
            String paymentMethod = mapPaymentMethod(intent.getStringExtra("paymentMethod"));
            String voucherDiscount = intent.getStringExtra("voucherDiscount");

            String arrivalDate = intent.getStringExtra("arrivalDate");

            int itemCount = intent.getIntExtra("itemCount", 0);

            List<OrderItem> list =
                    (List<OrderItem>) intent.getSerializableExtra("orderItems");

            // ===== DEFAULT VALUE =====

            if (orderId == null) orderId = "N/A";

            if (orderStatus == null) orderStatus = "pending";

            if (orderDate == null) orderDate = "";

            if (shippingAddress == null || shippingAddress.isEmpty()) {
                shippingAddress = "Không có thông tin";
            }

            paymentMethod = mapPaymentMethod(paymentMethod);

            if (voucherDiscount == null || voucherDiscount.isEmpty()) {
                voucherDiscount = "0đ";
            }

            if (arrivalDate == null || arrivalDate.isEmpty()) {
                arrivalDate = "Chưa có";
            }

            if (list == null) {
                list = new ArrayList<>();
            }

            // ================= UI =================

            tvOrderId.setText("#" + orderId);

            tvOrderDate.setText(
                    formatDate(orderDate)
            );

            tvOrderStatus.setText(
                    getStatusVietnamese(orderStatus)
            );

            tvOrderTotal.setText(
                    String.format(Locale.getDefault(),
                            "%,.0fđ",
                            total)
            );

            tvShippingAddress.setText(shippingAddress);
            tvPaymentMethod.setText(paymentMethod);
            tvVoucherDiscount.setText(voucherDiscount);

            tvItemCount.setText(String.valueOf(itemCount));

            tvArrivalDate.setText(arrivalDate);

            tvStatusDescription.setText(
                    getStatusDescription(orderStatus)
            );

            setModernStatusStyle(tvOrderStatus, orderStatus);

            // ================= RECYCLERVIEW =================

            OrderItemAdapter adapter =
                    new OrderItemAdapter(list);

            rvOrderItems.setLayoutManager(
                    new LinearLayoutManager(this)
            );

            rvOrderItems.setAdapter(adapter);

            // ================= BUTTON =================

            setupButtons();

        } catch (Exception e) {

            Log.e("ORDER_DETAIL", "Error", e);

            Toast.makeText(
                    this,
                    "Lỗi hiển thị chi tiết đơn hàng",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    // ================= BACK =================

    private void setupBackButton() {

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {

            btnBack.setOnClickListener(v -> finish());
        }
    }

    private String mapPaymentMethod(String rawMethod) {
        if (rawMethod == null || rawMethod.trim().isEmpty()) {
            return "Thanh toán khi nhận hàng";
        }

        String value = rawMethod.trim();
        if (value.equalsIgnoreCase("COD")
                || value.equalsIgnoreCase("cod")
                || value.equalsIgnoreCase("cash_on_delivery")
                || value.equalsIgnoreCase("cash on delivery")
                || value.equalsIgnoreCase("cashondelivery")) {
            return "Thanh toán khi nhận hàng";
        }

        return value;
    }

    // ================= BUTTON ACTION =================

    private void setupButtons() {

        boolean canCancel = false;

        if (orderStatus != null) {

            String s = orderStatus.trim().toLowerCase();

            canCancel =
                    s.equals("pending")
                            || s.equals("chờ xác nhận")
                            || s.equals("cho xac nhan");
        }

        if (canCancel) {

            btnCancel.setVisibility(android.view.View.VISIBLE);

            btnBuyAgain.setVisibility(android.view.View.GONE);

            btnCancel.setOnClickListener(v ->
                    showCancelDialog()
            );

        } else {

            btnCancel.setVisibility(android.view.View.GONE);

            btnBuyAgain.setVisibility(android.view.View.VISIBLE);

            btnBuyAgain.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                OrderDetailActivity.this,
                                MainActivity.class
                        );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(intent);

                finish();
            });
        }
    }

    // ================= CANCEL =================

    private void showCancelDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn muốn hủy đơn hàng này?")
                .setPositiveButton("Hủy đơn", (dialog, which) ->
                        cancelOrder()
                )
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void cancelOrder() {

        String token = SessionManager.getToken(this);

        OrderApiService.cancelOrder(
                this,
                token,
                orderId,
                "User cancel",

                new OrderApiService.CancelOrderCallback() {

                    @Override
                    public void onSuccess(org.json.JSONObject json) {

                        runOnUiThread(() -> {

                            orderStatus = "cancelled";

                            tvOrderStatus.setText(
                                    getStatusVietnamese(orderStatus)
                            );

                            tvStatusDescription.setText(
                                    "Đơn hàng đã bị hủy"
                            );

                            setModernStatusStyle(
                                    tvOrderStatus,
                                    orderStatus
                            );

                            btnCancel.setVisibility(android.view.View.GONE);

                            btnBuyAgain.setVisibility(android.view.View.VISIBLE);

                            Toast.makeText(
                                    OrderDetailActivity.this,
                                    "Đã hủy đơn hàng",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent resultIntent = new Intent();

                            resultIntent.putExtra(
                                    "orderCancelled",
                                    true
                            );

                            setResult(RESULT_OK, resultIntent);

                            finish();
                        });
                    }

                    @Override
                    public void onError(String err) {

                        runOnUiThread(() ->

                                Toast.makeText(
                                        OrderDetailActivity.this,
                                        "Lỗi: " + err,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    // ================= STATUS =================

    private String getStatusVietnamese(String status) {

        if (status == null) return "";

        switch (status.toLowerCase()) {

            case "pending":
                return "Chờ xác nhận";

            case "processing":
                return "Đang xử lý";

            case "shipping":
                return "Đang giao";

            case "delivered":
                return "Đã giao";

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
                return "Đơn hàng đang chờ xác nhận";

            case "processing":
                return "Shop đang chuẩn bị hàng";

            case "shipping":
                return "Đơn hàng đang được giao";

            case "delivered":
                return "Đơn hàng đã giao thành công";

            case "cancelled":
                return "Đơn hàng đã bị hủy";

            default:
                return "";
        }
    }

    // ================= STATUS STYLE =================

    private void setModernStatusStyle(TextView tv, String status) {

        if (status == null) return;

        switch (status.toLowerCase()) {

            case "pending":

                tv.setTextColor(getColor(R.color.orange));

                tv.setBackgroundResource(
                        R.drawable.bg_status_pending
                );

                break;

            case "processing":

                tv.setTextColor(getColor(R.color.blue));

                tv.setBackgroundResource(
                        R.drawable.bg_status_processing
                );

                break;

            case "shipping":

                tv.setTextColor(getColor(R.color.primary));

                tv.setBackgroundResource(
                        R.drawable.bg_status_shipping
                );

                break;

            case "delivered":

                tv.setTextColor(getColor(R.color.green));

                tv.setBackgroundResource(
                        R.drawable.bg_status_delivered
                );

                break;

            case "cancelled":

                tv.setTextColor(getColor(R.color.red));

                tv.setBackgroundResource(
                        R.drawable.bg_status_cancelled
                );

                break;
        }

        tv.setPadding(32, 16, 32, 16);
    }

    // ================= FORMAT DATE =================

    private String formatDate(String date) {

        try {

            return date
                    .replace("T", " ")
                    .replace(".000Z", "")
                    .replace(".695Z", "");

        } catch (Exception e) {

            return date;
        }
    }
}
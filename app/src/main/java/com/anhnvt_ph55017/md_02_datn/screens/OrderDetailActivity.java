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
    private TextView tvCancellationReason;
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
        tvCancellationReason = findViewById(R.id.tvCancellationReason);
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

            if (isCancelledStatus(orderStatus)) {
                tvStatusDescription.setVisibility(android.view.View.GONE);

                String cancellationReason = intent.getStringExtra("cancellationReason");
                if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                    cancellationReason = intent.getStringExtra("reason");
                }

                if (cancellationReason != null && !cancellationReason.trim().isEmpty()) {
                    tvCancellationReason.setText("Lý do hủy: " + cancellationReason);
                } else {
                    tvCancellationReason.setText("Lý do hủy: Chưa có lý do hủy");
                }
                tvCancellationReason.setVisibility(android.view.View.VISIBLE);
            } else {
                tvStatusDescription.setVisibility(android.view.View.VISIBLE);
                tvStatusDescription.setText(getStatusDescription(orderStatus));
                tvCancellationReason.setText("");
                tvCancellationReason.setVisibility(android.view.View.GONE);
            }

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

            // Fetch fresh order details from API to display voucher and latest info
            fetchOrderDetails();

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

        String[] cancelReasons = new String[]{
                "Đổi ý không muốn mua nữa",
                "Đặt nhầm sản phẩm",
                "Muốn thay đổi địa chỉ nhận hàng",
                "Muốn thay đổi sản phẩm / số lượng",
                "Thời gian giao hàng quá lâu",
                "Tìm được giá tốt hơn",
                "Không đủ khả năng thanh toán",
                "Sản phẩm không còn nhu cầu",
                "Muốn đặt lại đơn mới",
                "Lý do khác"
        };

        final int[] selectedPosition = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn có chắc chắn hủy đơn?")
                .setSingleChoiceItems(cancelReasons, 0, (dialog, which) -> selectedPosition[0] = which)
                .setNegativeButton("Đóng", null)
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    int sel = selectedPosition[0];
                    String chosen = cancelReasons[sel];
                    if (sel == cancelReasons.length - 1) {
                        // Lý do khác -> yêu cầu nhập text
                        showCustomReasonDialog();
                    } else {
                        cancelOrder(chosen);
                    }
                });

        builder.show();
    }

    private void showCustomReasonDialog() {
        android.widget.EditText et = new android.widget.EditText(this);
        et.setHint("Nhập lý do...");
        et.setSingleLine(false);
        et.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle("Lý do khác")
                .setView(et)
                .setPositiveButton("Gửi", (d, w) -> {
                    String text = et.getText() != null ? et.getText().toString().trim() : "";
                    if (text.isEmpty()) {
                        // Nếu rỗng, vẫn cho phép hủy với nhãn chung
                        cancelOrder("Lý do khác");
                    } else {
                        cancelOrder(text);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void cancelOrder(String reason) {

        String token = SessionManager.getToken(this);

        final String sentReason = reason;

        OrderApiService.cancelOrder(
            this,
            token,
            orderId,
            reason,

            new OrderApiService.CancelOrderCallback() {

                @Override
                public void onSuccess(org.json.JSONObject json) {

                runOnUiThread(() -> {

                    orderStatus = "cancelled";

                    tvOrderStatus.setText(
                        getStatusVietnamese(orderStatus)
                    );

                    // try extract cancellation reason from response
                    String displayReason = "";
                    if (json != null) {
                    displayReason = json.optString("cancellationReason", "");
                    if (displayReason.isEmpty()) {
                        displayReason = json.optString("reason", "");
                    }
                    if (displayReason.isEmpty()) {
                        org.json.JSONObject orderObj = json.optJSONObject("order");
                        if (orderObj != null) {
                        displayReason = orderObj.optString("cancellationReason", "");
                        }
                    }
                    }

                    if (displayReason == null || displayReason.trim().isEmpty()) {
                    displayReason = sentReason != null ? sentReason : "";
                    }

                    tvStatusDescription.setText(
                        "Đơn hàng đã bị hủy"
                    );

                    if (displayReason != null && !displayReason.trim().isEmpty()) {
                    tvCancellationReason.setText("Lý do hủy: " + displayReason);
                    tvCancellationReason.setVisibility(android.view.View.VISIBLE);
                    }

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

                    resultIntent.putExtra("cancellationReason", displayReason);

                    setResult(RESULT_OK, resultIntent);

                    // update local DB if needed via DAO - keep existing behavior: finish activity
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

    // ================= FETCH DETAILS =================

    private void fetchOrderDetails() {

        String token = SessionManager.getToken(this);

        if (token == null || orderId == null) return;

        OrderApiService.getOrderById(
                this,
                token,
                orderId,

                new OrderApiService.OrderCallback() {

                    @Override
                    public void onSuccess(org.json.JSONObject orderJson) {

                        try {

                            Log.d("ORDER_DETAIL_RESPONSE", orderJson.toString());

                            // ================= VOUCHER =================

                            double voucherDiscount = 0;

                            if (orderJson.has("voucher")) {

                                org.json.JSONObject v =
                                        orderJson.optJSONObject("voucher");

                                if (v != null) {

                                    voucherDiscount = v.optDouble(
                                            "discount",
                                            v.optDouble(
                                                    "amount",
                                                    v.optDouble("value", 0)
                                            )
                                    );
                                }
                            }

                            if (voucherDiscount == 0
                                    && orderJson.has("discount")) {

                                org.json.JSONObject discountObj =
                                        orderJson.optJSONObject("discount");

                                if (discountObj != null) {

                                    voucherDiscount = discountObj.optDouble(
                                            "amount",
                                            discountObj.optDouble(
                                                    "discount",
                                                    discountObj.optDouble(
                                                            "value",
                                                            0
                                                    )
                                            )
                                    );

                                } else {

                                    voucherDiscount =
                                            orderJson.optDouble(
                                                    "discount",
                                                    0
                                            );
                                }
                            }

                            if (voucherDiscount == 0) {

                                voucherDiscount =
                                        orderJson.optDouble(
                                                "voucherDiscount",
                                                orderJson.optDouble(
                                                        "discount",
                                                        0
                                                )
                                        );
                            }

                            // ================= CANCEL REASON =================

                            String displayReason = "";

                            if (orderJson.has("cancellationReason")) {

                                displayReason =
                                        orderJson.optString(
                                                "cancellationReason",
                                                ""
                                        );
                            }

                            if (displayReason.isEmpty()) {

                                displayReason =
                                        orderJson.optString(
                                                "reason",
                                                ""
                                        );
                            }

                            if (displayReason.isEmpty()) {

                                org.json.JSONObject orderObj =
                                        orderJson.optJSONObject("order");

                                if (orderObj != null) {

                                    displayReason =
                                            orderObj.optString(
                                                    "cancellationReason",
                                                    ""
                                            );
                                }
                            }

                            final double finalVoucher =
                                    voucherDiscount;

                            final String finalReason =
                                    displayReason;

                            runOnUiThread(() -> {

                                // ===== Voucher =====

                                if (finalVoucher > 0) {

                                    tvVoucherDiscount.setText(
                                            String.format(
                                                    Locale.getDefault(),
                                                    "%,.0fđ",
                                                    finalVoucher
                                            )
                                    );

                                } else {

                                    String cur =
                                            tvVoucherDiscount.getText() != null
                                                    ? tvVoucherDiscount
                                                    .getText()
                                                    .toString()
                                                    : "";

                                    if (cur.isEmpty()
                                            || cur.equals("0đ")) {

                                        tvVoucherDiscount.setText("0đ");
                                    }
                                }

                                // ===== Lý do hủy =====

                                if (!finalReason.isEmpty()) {

                                    tvCancellationReason.setText(
                                            "Lý do hủy: " + finalReason
                                    );

                                    tvCancellationReason.setVisibility(
                                            android.view.View.VISIBLE
                                    );

                                } else {

                                    tvCancellationReason.setVisibility(
                                            android.view.View.GONE
                                    );
                                }
                            });

                        } catch (Exception e) {

                            Log.e(
                                    "FETCH_ORDER_DETAIL",
                                    "err",
                                    e
                            );
                        }
                    }

                    @Override
                    public void onError(String error) {

                        Log.e(
                                "FETCH_ORDER_DETAIL",
                                error
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
            case "đã hủy":
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
            case "đã hủy":
                return "Đơn hàng đã bị hủy";

            default:
                return "";
        }
    }

    private boolean isCancelledStatus(String status) {
        if (status == null) return false;
        String normalized = status.trim().toLowerCase();
        return normalized.equals("cancelled") || normalized.equals("đã hủy") || normalized.contains("hủy");
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
            case "đã hủy":

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
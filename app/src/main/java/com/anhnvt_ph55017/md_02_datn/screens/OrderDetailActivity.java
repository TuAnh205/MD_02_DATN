package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.OrderItemAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.OrderDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal;
    TextView tvArrivalDate, tvItemCount, tvStatusDescription;
    TextView tvProductName, tvProductPrice, tvProductDesc;
    TextView tvShippingAddress, tvOrderCode, tvPaymentMethod, tvOrderTime, tvDeliveryTime;
    Button  btnCancel, btnChat, btnCall, btnSubmitRating, btnSupportCenter, btnRefund;
    ImageButton btnBack;
    AppCompatButton btnBuyAgain;
    RatingBar ratingBar;
    EditText edtRatingComment;
    ImageView ivExpandIcon;
    RecyclerView rvOrderItems;
    String orderId, orderStatus;
    android.view.View ratingCard, messageCard, productInfoCard, itemsCard, expandableContent;
    OrderDAO orderDAO;
    OrderItemAdapter orderItemAdapter;
    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_order_detail);

            // Initialize views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvArrivalDate = findViewById(R.id.tvArrivalDate);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvStatusDescription = findViewById(R.id.tvStatusDescription);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDesc = findViewById(R.id.tvProductDesc);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvOrderTime = findViewById(R.id.tvOrderTime);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.btnCancel);
        btnBuyAgain = findViewById(R.id.btnBuyAgain);
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        btnSupportCenter = findViewById(R.id.btnSupportCenter);
        btnRefund = findViewById(R.id.btnRefund);
        ratingBar = findViewById(R.id.ratingBar);
        edtRatingComment = findViewById(R.id.edtRatingComment);
        ratingCard = findViewById(R.id.ratingCard);
        messageCard = findViewById(R.id.messageCard);
        productInfoCard = findViewById(R.id.productInfoCard);
        itemsCard = findViewById(R.id.itemsCard);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        ivExpandIcon = findViewById(R.id.ivExpandIcon);
        expandableContent = findViewById(R.id.expandableContent);

        orderDAO = new OrderDAO(this);

        // Get data from intent
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        if (orderId == null) orderId = "#N/A";
        String orderDate = intent.getStringExtra("orderDate");
        if (orderDate == null) orderDate = "Chưa xác định";
        orderStatus = intent.getStringExtra("orderStatus");
        if (orderStatus == null) orderStatus = "Chưa thanh toán";
        double orderTotal = intent.getDoubleExtra("orderTotal", 0);
        String arrivalDate = intent.getStringExtra("arrivalDate");
        if (arrivalDate == null) arrivalDate = "Chưa xác định";
        int itemCount = intent.getIntExtra("itemCount", 0);
        int imageRes = intent.getIntExtra("imageRes", R.drawable.bg_image);
        String productName = intent.getStringExtra("productName");
        double productPrice = intent.getDoubleExtra("productPrice", 0);
        String productDesc = intent.getStringExtra("productDesc");
        String shippingAddress = intent.getStringExtra("shippingAddress");
        if (shippingAddress == null) shippingAddress = "123 Đường ABC, Quận XYZ, TP.HCM";
        String paymentMethod = intent.getStringExtra("paymentMethod");
        if (paymentMethod == null || paymentMethod.isEmpty()) paymentMethod = "Thanh toán khi nhận hàng";

        // Set data to views
        tvOrderId.setText("Đơn #" + orderId);
        tvOrderDate.setText("Đặt hàng vào: " + orderDate);
        tvOrderStatus.setText(getStatusVietnamese(orderStatus));
        tvOrderTotal.setText("$" + orderTotal);
        tvArrivalDate.setText("Dự kiến nhận: " + arrivalDate);
        tvItemCount.setText(itemCount + " sản phẩm");
        tvStatusDescription.setText(getStatusDescription(orderStatus));

        // Shipping address
        if (shippingAddress != null && !shippingAddress.isEmpty()) {
            tvShippingAddress.setText(shippingAddress);
        } else {
            tvShippingAddress.setText("123 Đường ABC, Quận XYZ, TP.HCM");
        }
        // Expandable section data
        tvOrderCode.setText(orderId);
        tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "Thanh toán khi nhận hàng");
        tvOrderTime.setText(orderDate);
        tvDeliveryTime.setText(arrivalDate);
        // Setup items list
        List<OrderItem> orderItems = new ArrayList<>();
        // Always show list mode
        productInfoCard.setVisibility(android.view.View.GONE);
        itemsCard.setVisibility(android.view.View.VISIBLE);


        // Sample items for demo
        orderItems.add(new OrderItem("Laptop Gaming XYZ", 1200.00, 1, R.drawable.bg_image));
        orderItems.add(new OrderItem("Chuột Gaming ABC", 50.00, 2, R.drawable.bg_image));
        orderItems.add(new OrderItem("Bàn phím Mechanical", 150.00, 1, R.drawable.bg_image));

        // Setup RecyclerView
        orderItemAdapter = new OrderItemAdapter(orderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);

        // Set status color
        setStatusColor(tvOrderStatus, orderStatus);

        // Setup expandable section
        findViewById(R.id.expandableHeader).setOnClickListener(v -> toggleExpandable());

        // Show/Hide rating card based on status
        if ("Đã nhận".equals(orderStatus)) {
            ratingCard.setVisibility(android.view.View.VISIBLE);
        } else {
            ratingCard.setVisibility(android.view.View.GONE);
        }

        // cancel vs buyAgain visibility
        if ("Đã nhận".equals(orderStatus) || "Đã hủy".equals(orderStatus)) {
            btnCancel.setVisibility(android.view.View.GONE);
            btnBuyAgain.setVisibility(android.view.View.VISIBLE);
        } else {
            btnCancel.setVisibility(android.view.View.VISIBLE);
            btnBuyAgain.setVisibility(android.view.View.GONE);
        }

        // Enable/Disable cancel button based on status
        if ("Đang giao hàng".equals(orderStatus) || "Đã nhận".equals(orderStatus)) {
            btnCancel.setEnabled(false);
            btnCancel.setAlpha(0.5f);
        } else {
            btnCancel.setEnabled(true);
            btnCancel.setAlpha(1f);
        }

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Cancel button with confirmation and reason
        btnCancel.setOnClickListener(v -> {
            if ("Đang giao hàng".equals(orderStatus) || "Đã nhận".equals(orderStatus)) {
                Toast.makeText(OrderDetailActivity.this, "Không thể hủy đơn hàng ở trạng thái hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
            // build dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(OrderDetailActivity.this);
            builder.setTitle("Xác nhận hủy đơn");
            builder.setMessage("Bạn có chắc muốn hủy đơn hàng #" + orderId + " không?");

            // custom view for reason selection
            android.widget.LinearLayout container = new android.widget.LinearLayout(OrderDetailActivity.this);
            container.setOrientation(android.widget.LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            container.setPadding(padding, padding, padding, padding);

            android.widget.RadioGroup radioGroup = new android.widget.RadioGroup(OrderDetailActivity.this);
            android.widget.RadioButton rb1 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb1.setText("Tôi đổi ý không muốn mua nữa");
            rb1.setId(android.view.View.generateViewId());
            android.widget.RadioButton rb2 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb2.setText("Đặt nhầm sản phẩm");
            rb2.setId(android.view.View.generateViewId());
            android.widget.RadioButton rb3 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb3.setText("Muốn thay đổi địa chỉ giao hàng");
            rb3.setId(android.view.View.generateViewId());
            android.widget.RadioButton rb4 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb4.setText("Muốn thay đổi sản phẩm / số lượng");
            rb4.setId(android.view.View.generateViewId());
            android.widget.RadioButton rb5 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb5.setText("Tìm được giá rẻ hơn ở nơi khác");
            rb5.setId(android.view.View.generateViewId());
            android.widget.RadioButton rb6 = new android.widget.RadioButton(OrderDetailActivity.this);
            rb6.setText("Lý do khác");
            rb6.setId(android.view.View.generateViewId());

            radioGroup.addView(rb1);
            radioGroup.addView(rb2);
            radioGroup.addView(rb3);
            radioGroup.addView(rb4);
            radioGroup.addView(rb5);
            radioGroup.addView(rb6);

            android.widget.EditText otherReasonInput = new android.widget.EditText(OrderDetailActivity.this);
            otherReasonInput.setHint("Nhập lý do khác (nếu chọn 'Lý do khác')");
            otherReasonInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            otherReasonInput.setVisibility(android.view.View.GONE);

            rb6.setOnCheckedChangeListener((buttonView, isChecked) -> {
                otherReasonInput.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
            });

            container.addView(radioGroup);
            container.addView(otherReasonInput);

            builder.setView(container);
            builder.setPositiveButton("Đồng ý", null); // set later to validate
            builder.setNegativeButton("Không", (dialog, which) -> dialog.cancel());
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
            // override positive button to enforce selection
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v2 -> {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String reason = "";
                if (selectedId == rb1.getId()) {
                    reason = "Tôi đổi ý không muốn mua nữa";
                } else if (selectedId == rb2.getId()) {
                    reason = "Đặt nhầm sản phẩm";
                } else if (selectedId == rb3.getId()) {
                    reason = "Muốn thay đổi địa chỉ giao hàng";
                } else if (selectedId == rb4.getId()) {
                    reason = "Muốn thay đổi sản phẩm / số lượng";
                } else if (selectedId == rb5.getId()) {
                    reason = "Tìm được giá rẻ hơn ở nơi khác";
                } else if (selectedId == rb6.getId()) {
                    reason = otherReasonInput.getText().toString().trim();
                    if (reason.isEmpty()) {
                        otherReasonInput.setError("Vui lòng nhập lý do");
                        return;
                    }
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Vui lòng chọn lý do hủy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // update status in database
                int orderIdInt = -1;
                try {
                    String cleanedId = orderId.replace("OD-", "").replace("CT-", "").replace("#", "").trim();
                    orderIdInt = Integer.parseInt(cleanedId);
                } catch (Exception ex) {
                    Log.w("OrderDetailActivity", "Cannot parse orderId for DB update: " + orderId, ex);
                }
                boolean success = (orderIdInt > 0) ? orderDAO.updateOrderStatus(orderIdInt, "Đã hủy") : false;

                if (success) {
                    // update status locally
                    orderStatus = "Đã hủy";
                    tvOrderStatus.setText(getStatusVietnamese(orderStatus));
                    tvStatusDescription.setText("Đơn hàng đã bị hủy");
                    btnCancel.setVisibility(android.view.View.GONE);
                    btnBuyAgain.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(OrderDetailActivity.this, "Đơn hàng #" + orderId + " đã bị hủy\nLý do: " + reason, Toast.LENGTH_LONG).show();
                    // return result so fragment can refresh
                    Intent data = new Intent();
                    data.putExtra("orderId", orderId);
                    data.putExtra("newStatus", orderStatus);
                    setResult(RESULT_OK, data);
                    dialog.dismiss();
                    onBackPressed();
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Lỗi khi hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Chat button
        btnChat.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Mở chat với shop", Toast.LENGTH_SHORT).show();
            // TODO: Implement chat functionality
        });

        // Buy again button
        btnBuyAgain.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Mua lại đơn hàng", Toast.LENGTH_SHORT).show();
            // TODO: Implement reorder logic
        });

        // Call button
        btnCall.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Gọi shop", Toast.LENGTH_SHORT).show();
            // TODO: Implement call functionality
            // Intent callIntent = new Intent(Intent.ACTION_DIAL);
            // callIntent.setData(Uri.parse("tel:0123456789"));
            // startActivity(callIntent);
        });

        // Support Center button
        btnSupportCenter.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Mở trung tâm hỗ trợ", Toast.LENGTH_SHORT).show();
            // TODO: Open support center activity or web page
        });

        // Refund button
        btnRefund.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Yêu cầu hoàn tiền", Toast.LENGTH_SHORT).show();
            // TODO: Implement refund request functionality
        });

        // Submit rating button
        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = edtRatingComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(OrderDetailActivity.this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            } else if (comment.isEmpty()) {
                Toast.makeText(OrderDetailActivity.this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OrderDetailActivity.this, "Cảm ơn đánh giá của bạn (" + (int)rating + " sao)", Toast.LENGTH_SHORT).show();
                ratingBar.setRating(0);
                edtRatingComment.setText("");
                // TODO: Save rating to database
            }
        });
        
        } catch (Exception e) {
            Log.e("OrderDetailActivity", "Error in onCreate", e);
            String message = e.getMessage() != null ? e.getMessage() : "Unknown";
            Toast.makeText(this, "Lỗi khi mở chi tiết đơn hàng: " + message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // status đã là tiếng Việt nên trả về trực tiếp
        private String getStatusVietnamese(String status) {
            return status;
        }

    private String getStatusDescription(String status) {
        if (status == null) return "";
        if ("Chưa thanh toán".equals(status)) {
            return "Đơn hàng của bạn đang chờ thanh toán. Vui lòng hoàn tất thanh toán để tiếp tục xử lý.";
        } else if ("Đang xử lý".equals(status)) {
            return "Đơn hàng của bạn đang được xử lý. Chúng tôi sẽ chuẩn bị hàng hóa của bạn.";
        } else if ("Đang giao hàng".equals(status)) {
            return "Đơn hàng của bạn đang được vận chuyển. Vui lòng chờ đợi.";
        } else if ("Đã nhận".equals(status)) {
            return "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua hàng!";
        } else if ("Đã hủy".equals(status)) {
            return "Đơn hàng này đã bị hủy. Liên hệ shop nếu có thắc mắc.";
        }
        return "";
    }

    private void toggleExpandable() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            expandableContent.setVisibility(android.view.View.VISIBLE);
            ivExpandIcon.setRotation(180);
        } else {
            expandableContent.setVisibility(android.view.View.GONE);
            ivExpandIcon.setRotation(0);
        }
    }

    private void setStatusColor(TextView tvStatus, String status) {
        if (status == null) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            return;
        }
        if ("Chưa thanh toán".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
        } else if ("Đang xử lý".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
        } else if ("Đang giao hàng".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_light, null));
        } else if ("Đã nhận".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
        } else if ("Đã hủy".equals(status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
        } else {
            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }
}
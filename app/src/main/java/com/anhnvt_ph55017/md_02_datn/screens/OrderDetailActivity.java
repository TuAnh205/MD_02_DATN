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
import com.anhnvt_ph55017.md_02_datn.models.Order;
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
    Order order;
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

        // Parse order ID từ "OD-1" sang số 1
        int orderIdNum = 1;
        try {
            if (orderId.startsWith("OD-")) {
                orderIdNum = Integer.parseInt(orderId.substring(3));
            } else if (orderId.startsWith("OD")) {
                orderIdNum = Integer.parseInt(orderId.substring(2));
            } else {
                orderIdNum = Integer.parseInt(orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            orderIdNum = 1;
        }

        // Lấy full order data từ database
        order = orderDAO.getOrderById(orderIdNum);
        if (order == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy order items từ database
        List<OrderItem> orderItems = orderDAO.getOrderItemsByOrderId(orderIdNum);

        // Lấy dữ liệu từ order object
        String orderDate = order.getDate();
        orderStatus = order.getStatus();
        double orderTotal = 0;
        
        // Tính tổng tiền từ all order items
        for (OrderItem item : orderItems) {
            orderTotal += item.getTotal();
        }

        // Tính ngày dự kiến nhận hàng
        String arrivalDate = orderDAO.calculateExpectedDelivery(
            order.getCreatedAt(),
            orderStatus
        );

        int itemCount = orderItems.size();
        int imageRes = intent.getIntExtra("imageRes", R.drawable.bg_image);
        String shippingAddress = order.getShippingAddress();
        if (shippingAddress == null || shippingAddress.isEmpty()) {
            shippingAddress = "123 Đường ABC, Quận XYZ, TP.HCM";
        }
        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "Thanh toán khi nhận hàng";
        }

        // Set data to views
        tvOrderId.setText("Đơn #" + orderId);
        tvOrderDate.setText("Đặt hàng vào: " + orderDate);
        tvOrderStatus.setText(getStatusVietnamese(orderStatus));
        tvOrderTotal.setText("$" + String.format("%.2f", orderTotal));
        tvArrivalDate.setText("Dự kiến nhận: " + arrivalDate);
        tvItemCount.setText(itemCount + " sản phẩm");
        
        // Hiển thị lý do hủy nếu đơn hàng đã hủy
        String statusDesc = getStatusDescription(orderStatus);
        if ("Đã hủy".equals(orderStatus) && order.getCancellationReason() != null && !order.getCancellationReason().isEmpty()) {
            statusDesc += "\n💔 Lý do: " + order.getCancellationReason();
        }
        tvStatusDescription.setText(statusDesc);

        // Shipping address
        tvShippingAddress.setText(shippingAddress);

        // Expandable section data
        tvOrderCode.setText(orderId);
        tvPaymentMethod.setText(paymentMethod);
        tvOrderTime.setText(orderDate);
        tvDeliveryTime.setText(arrivalDate);

        // Setup items list
        // Always show list mode
        productInfoCard.setVisibility(android.view.View.GONE);
        itemsCard.setVisibility(android.view.View.VISIBLE);

        // Setup RecyclerView với real data từ database
        orderItemAdapter = new OrderItemAdapter(orderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderItemAdapter);

        // Set status color
        setStatusColor(tvOrderStatus, orderStatus);

        // Setup expandable section
        findViewById(R.id.expandableHeader).setOnClickListener(v -> toggleExpandable());

        // Show/Hide rating card based on status and review status
        if ("Đã nhận".equals(orderStatus)) {
            if (order.getRating() > 0) {
                // Already reviewed - show only review comment, hide input
                ratingCard.setVisibility(android.view.View.VISIBLE);
                ratingBar.setVisibility(android.view.View.GONE);
                edtRatingComment.setVisibility(android.view.View.GONE);
                btnSubmitRating.setVisibility(android.view.View.GONE);
                
                // Show review comment
                TextView tvReviewedComment = ratingCard.findViewById(R.id.tvReviewedComment);
                if (tvReviewedComment != null) {
                    tvReviewedComment.setText(order.getReviewComment());
                    tvReviewedComment.setVisibility(android.view.View.VISIBLE);
                }
            } else {
                // Not reviewed yet - show rating input form
                ratingCard.setVisibility(android.view.View.VISIBLE);
                ratingBar.setVisibility(android.view.View.VISIBLE);
                edtRatingComment.setVisibility(android.view.View.VISIBLE);
                btnSubmitRating.setVisibility(android.view.View.VISIBLE);
                
                // Hide already reviewed display
                TextView tvReviewedStars = ratingCard.findViewById(R.id.tvReviewedStars);
                TextView tvReviewedComment = ratingCard.findViewById(R.id.tvReviewedComment);
                if (tvReviewedStars != null) tvReviewedStars.setVisibility(android.view.View.GONE);
                if (tvReviewedComment != null) tvReviewedComment.setVisibility(android.view.View.GONE);
            }
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

        // Create final copy for lambda expression
        final int finalOrderIdNum = orderIdNum;

        // Submit rating button
        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = edtRatingComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(OrderDetailActivity.this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            } else if (comment.isEmpty()) {
                Toast.makeText(OrderDetailActivity.this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            } else {
                // Save rating to database
                OrderDAO orderDAO = new OrderDAO(OrderDetailActivity.this);
                boolean success = orderDAO.updateOrderReview(finalOrderIdNum, rating, comment);
                
                if (success) {
                    // Update local order object
                    order.setRating(rating);
                    order.setReviewComment(comment);
                    order.setReviewedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
                    
                    Toast.makeText(OrderDetailActivity.this, "Cảm ơn đánh giá của bạn (" + (int)rating + " sao)", Toast.LENGTH_SHORT).show();
                    
                    // Update UI - hide rating input, show review
                    ratingBar.setVisibility(android.view.View.GONE);
                    edtRatingComment.setVisibility(android.view.View.GONE);
                    btnSubmitRating.setVisibility(android.view.View.GONE);
                    
                    // Refresh display
                    updateRatingDisplay();
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Lỗi khi lưu đánh giá. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
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

    private void updateRatingDisplay() {
        // Update status to show rating stars
        String statusText = getStatusVietnamese(orderStatus);
        if (order.getRating() > 0) {
            statusText += " • ⭐ " + String.format("%.1f", order.getRating()) + " | ✅ Đã đánh giá";
            tvOrderStatus.setText(statusText);
        }
        
        // Hide rating input
        ratingBar.setVisibility(android.view.View.GONE);
        edtRatingComment.setVisibility(android.view.View.GONE);
        btnSubmitRating.setVisibility(android.view.View.GONE);
        
        // Show review comment
        TextView tvReviewedComment = ratingCard.findViewById(R.id.tvReviewedComment);
        if (tvReviewedComment != null) {
            tvReviewedComment.setText(order.getReviewComment());
            tvReviewedComment.setVisibility(android.view.View.VISIBLE);
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
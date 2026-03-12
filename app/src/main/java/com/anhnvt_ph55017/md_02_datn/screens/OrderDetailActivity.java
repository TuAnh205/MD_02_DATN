package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.anhnvt_ph55017.md_02_datn.R;

public class OrderDetailActivity extends AppCompatActivity {

    TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal;
    TextView tvArrivalDate, tvItemCount, tvStatusDescription;
    TextView tvProductName, tvProductPrice, tvProductDesc;
    Button  btnCancel, btnChat, btnCall, btnSubmitRating;
    ImageButton btnBack;
    AppCompatButton btnBuyAgain;
    RatingBar ratingBar;
    EditText edtRatingComment;
    ImageView imgProduct;
    String orderId, orderStatus;
    android.view.View ratingCard, messageCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.btnCancel);
        btnBuyAgain = findViewById(R.id.btnBuyAgain);
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);
        ratingBar = findViewById(R.id.ratingBar);
        edtRatingComment = findViewById(R.id.edtRatingComment);
        ratingCard = findViewById(R.id.ratingCard);
        messageCard = findViewById(R.id.messageCard);
        imgProduct = findViewById(R.id.imgProduct);

        // Get data from intent
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        String orderDate = intent.getStringExtra("orderDate");
        orderStatus = intent.getStringExtra("orderStatus");
        double orderTotal = intent.getDoubleExtra("orderTotal", 0);
        String arrivalDate = intent.getStringExtra("arrivalDate");
        int itemCount = intent.getIntExtra("itemCount", 0);
        int imageRes = intent.getIntExtra("imageRes", R.drawable.bg_image);
        String productName = intent.getStringExtra("productName");
        double productPrice = intent.getDoubleExtra("productPrice", 0);
        String productDesc = intent.getStringExtra("productDesc");

        // Set data to views
        tvOrderId.setText("Đơn #" + orderId);
        tvOrderDate.setText("Đặt hàng vào: " + orderDate);
        tvOrderStatus.setText(getStatusVietnamese(orderStatus));
        tvOrderTotal.setText("$" + orderTotal);
        tvArrivalDate.setText("Dự kiến nhận: " + arrivalDate);
        tvItemCount.setText(itemCount + " sản phẩm");
        tvStatusDescription.setText(getStatusDescription(orderStatus));

        // product info
        if (productName != null) tvProductName.setText(productName);
        tvProductPrice.setText("$" + productPrice);
        if (productDesc != null) tvProductDesc.setText(productDesc);

        // Set status color
        setStatusColor(tvOrderStatus, orderStatus);

        // Load product image resource id
        imgProduct.setImageResource(imageRes);

        // Show/Hide rating card based on status
        if (orderStatus.equals("Đã nhận")) {
            ratingCard.setVisibility(android.view.View.VISIBLE);
        } else {
            ratingCard.setVisibility(android.view.View.GONE);
        }

        // cancel vs buyAgain visibility
        if (orderStatus.equals("Đã nhận") || orderStatus.equals("Đã hủy")) {
            btnCancel.setVisibility(android.view.View.GONE);
            btnBuyAgain.setVisibility(android.view.View.VISIBLE);
        } else {
            btnCancel.setVisibility(android.view.View.VISIBLE);
            btnBuyAgain.setVisibility(android.view.View.GONE);
        }

        // Enable/Disable cancel button based on status
        if (orderStatus.equals("Đang giao hàng") || orderStatus.equals("Đã nhận")) {
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
            if (orderStatus.equals("Đang giao hàng") || orderStatus.equals("Đã nhận")) {
                Toast.makeText(OrderDetailActivity.this, "Không thể hủy đơn hàng ở trạng thái hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
            // build dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(OrderDetailActivity.this);
            builder.setTitle("Xác nhận hủy đơn");
            builder.setMessage("Bạn có chắc muốn hủy đơn hàng #" + orderId + " không?");
            // custom view for reason
            android.widget.EditText input = new android.widget.EditText(OrderDetailActivity.this);
            input.setHint("Lý do hủy (tùy chọn)");
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Đồng ý", null); // set later to validate
            builder.setNegativeButton("Không", (dialog, which) -> dialog.cancel());
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
            // override positive button to enforce non-empty reason
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v2 -> {
                String reason = input.getText().toString().trim();
                if (reason.isEmpty()) {
                    input.setError("Vui lòng nhập lý do hủy");
                } else {
                    // update status locally
                    orderStatus = "Đã hủy";
                    tvOrderStatus.setText(getStatusVietnamese(orderStatus));
                    tvStatusDescription.setText("Đơn hàng đã bị hủy");
                    btnCancel.setVisibility(android.view.View.GONE);
                    btnBuyAgain.setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(OrderDetailActivity.this, "Đơn hàng #" + orderId + " đã bị hủy\nLý do: " + reason, Toast.LENGTH_LONG).show();
                    // optionally return result so fragment can refresh
                    Intent data = new Intent();
                    data.putExtra("orderId", orderId);
                    data.putExtra("newStatus", orderStatus);
                    setResult(RESULT_OK, data);
                    dialog.dismiss();
                    onBackPressed();
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
    }

// status đã là tiếng Việt nên trả về trực tiếp
        private String getStatusVietnamese(String status) {
            return status;
        }

    private String getStatusDescription(String status) {
        if (status.equals("Chưa thanh toán")) {
            return "Đơn hàng của bạn đang chờ thanh toán. Vui lòng hoàn tất thanh toán để tiếp tục xử lý.";
        } else if (status.equals("Đang xử lý")) {
            return "Đơn hàng của bạn đang được xử lý. Chúng tôi sẽ chuẩn bị hàng hóa của bạn.";
        } else if (status.equals("Đang giao hàng")) {
            return "Đơn hàng của bạn đang được vận chuyển. Vui lòng chờ đợi.";
        } else if (status.equals("Đã nhận")) {
            return "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua hàng!";
        } else if (status.equals("Đã hủy")) {
            return "Đơn hàng này đã bị hủy. Liên hệ shop nếu có thắc mắc.";
        }
        return "";
    }

    private void setStatusColor(TextView tvStatus, String status) {
        if (status.equals("Chưa thanh toán")) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
        } else if (status.equals("Đang xử lý")) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
        } else if (status.equals("Đang giao hàng")) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_light, null));
        } else if (status.equals("Đã nhận")) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
        } else if (status.equals("Đã hủy")) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
        }
    }
}
package com.anhnvt_ph55017.md_02_datn.screens;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.ReviewApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.bumptech.glide.Glide;
public class ReviewDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvReviewerName;
    private TextView tvReviewDate;
    private TextView tvReviewContent;
    private RatingBar ratingBarReviewDetail;
    private TextView tvCurrentReplyHeader;
    private TextView tvCurrentReplyContent;
    private TextView tvCurrentReplyInfo;
    private LinearLayout layoutReplySection;
    private EditText edtReplyText;
    private Button btnSaveReply;
    private TextView tvReplyNotice;
    private String reviewId;
    private String currentReplyText;
    private String currentReplyByName;
    private String currentReplyDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);
        bindViews();
        reviewId = getIntent().getStringExtra("reviewId");
        String reviewerName = getIntent().getStringExtra("reviewerName");
        String reviewContent = getIntent().getStringExtra("reviewContent");
        String reviewDate = getIntent().getStringExtra("reviewDate");
        float reviewRating = getIntent().getFloatExtra("reviewRating", 0f);
        String productName = getIntent().getStringExtra("productName");
        String productImageUrl = getIntent().getStringExtra("productImageUrl");
        currentReplyText = getIntent().getStringExtra("responseText");
        currentReplyByName = getIntent().getStringExtra("responseByName");
        currentReplyDate = getIntent().getStringExtra("responseDate");
        if (TextUtils.isEmpty(reviewId)) {
            Toast.makeText(this, "Không tìm thấy đánh giá để mở", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String safeProductName = TextUtils.isEmpty(productName) ? "Sản phẩm" : productName;
        tvProductName.setText(safeProductName);
        Glide.with(this)
                .load(productImageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(ivProductImage);
        tvReviewerName.setText(TextUtils.isEmpty(reviewerName) ? "Ẩn danh" : reviewerName);
        tvReviewContent.setText(TextUtils.isEmpty(reviewContent) ? "Không có nội dung đánh giá" : reviewContent);
        tvReviewDate.setText(TextUtils.isEmpty(reviewDate) ? "" : reviewDate);
        ratingBarReviewDetail.setRating(reviewRating);
        boolean isShop = "shop".equalsIgnoreCase(SessionManager.getUserRole(this));
        if (!isShop) {
            layoutReplySection.setVisibility(View.GONE);
            tvReplyNotice.setVisibility(View.VISIBLE);
            tvReplyNotice.setText("Chỉ tài khoản shop mới có quyền phản hồi đánh giá.");
        } else {
            layoutReplySection.setVisibility(View.VISIBLE);
            tvReplyNotice.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(currentReplyText)) {
                tvCurrentReplyHeader.setVisibility(View.VISIBLE);
                tvCurrentReplyContent.setVisibility(View.VISIBLE);
                tvCurrentReplyInfo.setVisibility(View.VISIBLE);
                tvCurrentReplyContent.setText(currentReplyText);
                String info = "";
                if (!TextUtils.isEmpty(currentReplyByName)) {
                    info += "Trả lời bởi " + currentReplyByName;
                }
                if (!TextUtils.isEmpty(currentReplyDate)) {
                    if (!TextUtils.isEmpty(info)) {
                        info += " • ";
                    }
                    info += currentReplyDate;
                }
                tvCurrentReplyInfo.setText(TextUtils.isEmpty(info) ? "Đã phản hồi" : info);
                edtReplyText.setText(currentReplyText);
                btnSaveReply.setText("Cập nhật phản hồi");
            } else {
                tvCurrentReplyHeader.setVisibility(View.GONE);
                tvCurrentReplyContent.setVisibility(View.GONE);
                tvCurrentReplyInfo.setVisibility(View.GONE);
                edtReplyText.setText("");
                btnSaveReply.setText("Gửi phản hồi");
            }
        }
        btnBack.setOnClickListener(v -> finish());
        btnSaveReply.setOnClickListener(v -> {
            if (!isShop) {
                Toast.makeText(this, "Chỉ tài khoản shop mới có quyền phản hồi.", Toast.LENGTH_SHORT).show();
                return;
            }
            String replyText = edtReplyText.getText().toString().trim();
            if (TextUtils.isEmpty(replyText)) {
                Toast.makeText(this, "Vui lòng nhập nội dung phản hồi.", Toast.LENGTH_SHORT).show();
                return;
            }
            String token = SessionManager.getToken(this);
            if (TextUtils.isEmpty(token)) {
                Toast.makeText(this, "Bạn cần đăng nhập để phản hồi đánh giá.", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSaveReply.setEnabled(false);
            ReviewApiService.replyShopReview(this, token, reviewId, replyText, new ReviewApiService.ReviewReplyCallback() {
                @Override
                public void onSuccess(org.json.JSONObject review) {
                    runOnUiThread(() -> {
                        btnSaveReply.setEnabled(true);
                        Toast.makeText(ReviewDetailActivity.this, "Đã lưu phản hồi.", Toast.LENGTH_SHORT).show();
                        currentReplyText = replyText;
                        currentReplyByName = SessionManager.getUserName(ReviewDetailActivity.this);
                        currentReplyDate = "Vừa xong";
                        tvCurrentReplyHeader.setVisibility(View.VISIBLE);
                        tvCurrentReplyContent.setVisibility(View.VISIBLE);
                        tvCurrentReplyInfo.setVisibility(View.VISIBLE);
                        tvCurrentReplyContent.setText(replyText);
                        String info = "Trả lời bởi " + currentReplyByName;
                        if (!TextUtils.isEmpty(currentReplyDate)) {
                            info += " • " + currentReplyDate;
                        }
                        tvCurrentReplyInfo.setText(info);
                        btnSaveReply.setText("Cập nhật phản hồi");
                        setResult(RESULT_OK);
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSaveReply.setEnabled(true);
                        Toast.makeText(ReviewDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvReviewerName = findViewById(R.id.tvReviewerName);
        tvReviewDate = findViewById(R.id.tvReviewDate);
        tvReviewContent = findViewById(R.id.tvReviewContent);
        ratingBarReviewDetail = findViewById(R.id.ratingBarReviewDetail);
        tvCurrentReplyHeader = findViewById(R.id.tvCurrentReplyHeader);
        tvCurrentReplyContent = findViewById(R.id.tvCurrentReplyContent);
        tvCurrentReplyInfo = findViewById(R.id.tvCurrentReplyInfo);
        layoutReplySection = findViewById(R.id.layoutReplySection);
        edtReplyText = findViewById(R.id.edtReplyText);
        btnSaveReply = findViewById(R.id.btnSaveReply);
        tvReplyNotice = findViewById(R.id.tvReplyNotice);
    }
}
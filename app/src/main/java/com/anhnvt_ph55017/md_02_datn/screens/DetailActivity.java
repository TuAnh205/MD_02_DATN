package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.anhnvt_ph55017.md_02_datn.Adapters.ReviewAdapter;
import com.anhnvt_ph55017.md_02_datn.models.Review;
import com.anhnvt_ph55017.md_02_datn.utils.ReviewApiService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
// import com.anhnvt_ph55017.md_02_datn.DAO.CartDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.ProductApiService;
import com.anhnvt_ph55017.md_02_datn.utils.CartApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;


public class DetailActivity extends AppCompatActivity {
    ImageView imgProduct;
    ImageButton btnBack;
    TextView tvName, tvPrice, tvRating, tvDesc, tvDetailedDesc, tvNewBadge;
    TextView tvRatingAverage, tvRatingCount, tvCount1, tvCount2, tvCount3, tvCount4, tvCount5;
    ProgressBar progress1, progress2, progress3, progress4, progress5;
    EditText edtReview;
    Button btnSendReview;
    RatingBar ratingBar;
    RecyclerView rvReviews;
    ReviewAdapter reviewAdapter;
    List<Review> reviewList = new java.util.ArrayList<>();
    int selectedRating = 5;
    AppCompatButton btnAddCart;
    RecyclerView rvRelated;
    // CartDAO cartDAO;
    String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        imgProduct = findViewById(R.id.imgProduct);
        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvRating = findViewById(R.id.tvRating);
        tvDesc = findViewById(R.id.tvDesc);
        rvRelated = findViewById(R.id.rvRelated);
        btnAddCart = findViewById(R.id.btnAddCart);
        tvDetailedDesc = findViewById(R.id.tvDetailedDesc);
        tvNewBadge = findViewById(R.id.tvNewBadge);

        // Rating breakdown UI
        tvRatingAverage = findViewById(R.id.tvRatingAverage);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        tvCount1 = findViewById(R.id.tvCount1);
        tvCount2 = findViewById(R.id.tvCount2);
        tvCount3 = findViewById(R.id.tvCount3);
        tvCount4 = findViewById(R.id.tvCount4);
        tvCount5 = findViewById(R.id.tvCount5);
        progress1 = findViewById(R.id.progress1);
        progress2 = findViewById(R.id.progress2);
        progress3 = findViewById(R.id.progress3);
        progress4 = findViewById(R.id.progress4);
        progress5 = findViewById(R.id.progress5);

        // cartDAO = new CartDAO(this);

        edtReview = findViewById(R.id.edtReview);
        btnSendReview = findViewById(R.id.btnSendReview);
        ratingBar = findViewById(R.id.ratingBar);
        rvReviews = findViewById(R.id.rvReviews);

        // Setup review list
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> selectedRating = (int) rating);

        btnSendReview.setOnClickListener(v -> {
            String comment = edtReview.getText().toString().trim();
            if (selectedRating < 1) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
                return;
            }
            String token = SessionManager.getToken(this);
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Bạn cần đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
                redirectToLogin("review");
                return;
            }
            btnSendReview.setEnabled(false);
            ReviewApiService.postReview(this, token, productId, selectedRating, comment, new ReviewApiService.ReviewPostCallback() {
                @Override
                public void onSuccess(org.json.JSONObject reviewJson) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                        edtReview.setText("");
                        ratingBar.setRating(5);
                        btnSendReview.setEnabled(true);
                        loadReviews();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "Lỗi gửi đánh giá: " + error, Toast.LENGTH_SHORT).show();
                        btnSendReview.setEnabled(true);
                    });
                }
            });
        });


        // Lấy productId đúng chuẩn (giống Home/Search/Favorite)
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            // fallback nếu truyền key khác
            productId = intent.getStringExtra("id");
        }

        btnBack.setOnClickListener(v -> finish());

        loadProductDetail();
        loadReviews();
        loadRelatedProducts();
    }

    private void redirectToLogin(String reason) {
        Log.d("DetailActivity", "Redirecting to LoginActivity because: " + reason);
        startActivity(new Intent(DetailActivity.this, LoginActivity.class));
    }

    private void loadProductDetail() {
        ProductApiService.fetchProductById(this, productId, new ProductApiService.ProductDetailCallback() {
            @Override
            public void onSuccess(JSONObject item) {
                runOnUiThread(() -> {
                    try {
                        final String name = item.optString("name");
                        final double price = item.optDouble("price");
                        final String description = item.optString("description");
                        final String detailedDescription = item.optString("detailedDescription");
                        final boolean isNew = item.optBoolean("isNew", false);
                        final JSONObject ratings = item.optJSONObject("ratings");
                        final float rating;
                        final int reviewCount;
                        if (ratings != null) {
                            rating = (float) ratings.optDouble("average", 0);
                            reviewCount = ratings.optInt("count", 0);
                        } else {
                            rating = 0;
                            reviewCount = 0;
                        }

                        final JSONArray imagesArr = item.optJSONArray("images");
                        final String[] imageUrlArr = { item.optString("image") };
                        if ((imageUrlArr[0] == null || imageUrlArr[0].isEmpty()) && imagesArr != null && imagesArr.length() > 0) {
                            imageUrlArr[0] = imagesArr.optString(0);
                        }

                        // Hiển thị ảnh sản phẩm
                        Glide.with(DetailActivity.this)
                                .load(imageUrlArr[0])
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(imgProduct);

                        tvName.setText(name);
                        tvPrice.setText("$" + price);
                        tvDesc.setText(description);
                        tvDetailedDesc.setText(detailedDescription);
                        tvRating.setText("⭐ " + rating + " (" + reviewCount + ")");

                        // Badge New Release
                        if (isNew) {
                            tvNewBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvNewBadge.setVisibility(View.GONE);
                        }

                        // Hiển thị block rating breakdown
                        tvRatingAverage.setText(String.format("%.1f", rating));
                        tvRatingCount.setText(reviewCount + " đánh giá");
                        // Số lượng breakdown sẽ cập nhật ở loadReviews()

                        btnAddCart.setOnClickListener(v -> {
                            String token = SessionManager.getToken(DetailActivity.this);
                            if (token == null || token.isEmpty()) {
                                Toast.makeText(DetailActivity.this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(DetailActivity.this, LoginActivity.class));
                                return;
                            }
                            Product product = new Product(productId, name, price, imageUrlArr[0], description, 100);
                            product.setRating(rating);
                            product.setReviewCount(reviewCount);
                            BottomSheetProductOptions sheet =
                                    BottomSheetProductOptions.newInstance(product, (selectedProduct, qty) -> {
                                        CartApiService.addToCart(DetailActivity.this, token, productId, qty, new CartApiService.CartCallback() {
                                            @Override
                                            public void onSuccess(org.json.JSONObject cartJson) {
                                                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show());
                                            }
                                            @Override
                                            public void onError(String error) {
                                                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Lỗi thêm vào giỏ hàng: " + error, Toast.LENGTH_SHORT).show());
                                            }
                                        });
                                    });
                            sheet.show(getSupportFragmentManager(), "sheet");
                        });
                    } catch (Exception e) {
                        Toast.makeText(DetailActivity.this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Đặt loadReviews() ngoài onCreate và các method khác, chỉ giữ 1 bản duy nhất
    private void loadReviews() {
        ReviewApiService.fetchReviewsByProduct(this, productId, new ReviewApiService.ReviewListCallback() {
            @Override
            public void onSuccess(org.json.JSONArray arr) {
                runOnUiThread(() -> {
                    reviewList.clear();
                    int[] starCounts = new int[6];
                    String myUserId = SessionManager.getUserIdString(DetailActivity.this);
                    boolean hasMyReview = false;
                    String myReviewId = null;
                    String myReviewContent = null;
                    float myReviewRating = 5;
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject obj = arr.optJSONObject(i);
                        if (obj == null) continue;
                        String id = obj.optString("_id");
                        String userName = obj.optJSONObject("user") != null ? obj.optJSONObject("user").optString("name", "Ẩn danh") : "Ẩn danh";
                        String userId = obj.optJSONObject("user") != null ? obj.optJSONObject("user").optString("_id", "") : "";
                        String content = obj.optString("comment");
                        float rating = (float) obj.optDouble("rating", 0);
                        String createdAt = obj.optString("createdAt");
                        reviewList.add(new Review(id, userName, userId, content, rating, createdAt));
                        int r = Math.round(rating);
                        if (r >= 1 && r <= 5) starCounts[r]++;
                        if (userId.equals(myUserId)) {
                            hasMyReview = true;
                            myReviewId = id;
                            myReviewContent = content;
                            myReviewRating = rating;
                        }
                    }
                    reviewAdapter.notifyDataSetChanged();

                    int total = arr.length();
                    tvCount5.setText(String.valueOf(starCounts[5]));
                    tvCount4.setText(String.valueOf(starCounts[4]));
                    tvCount3.setText(String.valueOf(starCounts[3]));
                    tvCount2.setText(String.valueOf(starCounts[2]));
                    tvCount1.setText(String.valueOf(starCounts[1]));
                    progress5.setProgress(total > 0 ? (starCounts[5] * 100 / total) : 0);
                    progress4.setProgress(total > 0 ? (starCounts[4] * 100 / total) : 0);
                    progress3.setProgress(total > 0 ? (starCounts[3] * 100 / total) : 0);
                    progress2.setProgress(total > 0 ? (starCounts[2] * 100 / total) : 0);
                    progress1.setProgress(total > 0 ? (starCounts[1] * 100 / total) : 0);

                    LinearLayout layoutReviewForm = findViewById(R.id.layoutReviewForm);
                    Button btnSendReview = findViewById(R.id.btnSendReview);
                    if (hasMyReview) {
                        // Ẩn form đánh giá, hiện nút sửa
                        layoutReviewForm.setVisibility(View.GONE);
                        // Đảm bảo id nút sửa review luôn nhất quán trong method
                        int editReviewBtnId;
                        int deleteReviewBtnId;
                        if (layoutReviewForm.getTag() != null && layoutReviewForm.getTag() instanceof int[]) {
                            int[] ids = (int[]) layoutReviewForm.getTag();
                            editReviewBtnId = ids[0];
                            deleteReviewBtnId = ids[1];
                        } else {
                            editReviewBtnId = View.generateViewId();
                            deleteReviewBtnId = View.generateViewId();
                            layoutReviewForm.setTag(new int[]{editReviewBtnId, deleteReviewBtnId});
                        }
                        View btnEditView = findViewById(editReviewBtnId);
                        View btnDeleteView = findViewById(deleteReviewBtnId);
                        if (btnEditView == null || btnDeleteView == null) {
                            LinearLayout parent = (LinearLayout) layoutReviewForm.getParent();
                            LinearLayout btnLayout = new LinearLayout(DetailActivity.this);
                            btnLayout.setOrientation(LinearLayout.HORIZONTAL);
                            btnLayout.setPadding(0, 0, 0, 24);
                            btnLayout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

                            Button btnEdit = new Button(DetailActivity.this, null, android.R.attr.buttonBarButtonStyle);
                            btnEdit.setId(editReviewBtnId);
                            btnEdit.setText("Sửa đánh giá");
                            btnEdit.setBackgroundResource(R.drawable.bg_btn_blue);
                            btnEdit.setTextColor(getResources().getColor(android.R.color.white));
                            btnEdit.setAllCaps(false);
                            btnEdit.setPadding(48, 16, 48, 16);
                            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            editParams.setMarginEnd(24);
                            btnEdit.setLayoutParams(editParams);

                            Button btnDelete = new Button(DetailActivity.this, null, android.R.attr.buttonBarButtonStyle);
                            btnDelete.setId(deleteReviewBtnId);
                            btnDelete.setText("Xóa đánh giá");
                            btnDelete.setBackgroundResource(android.R.color.holo_red_dark);
                            btnDelete.setTextColor(getResources().getColor(android.R.color.white));
                            btnDelete.setAllCaps(false);
                            btnDelete.setPadding(48, 16, 48, 16);
                            btnDelete.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            final String reviewContentFinal = myReviewContent;
                            final float reviewRatingFinal = myReviewRating;
                            final String myReviewIdFinal = myReviewId;
                            btnEdit.setOnClickListener(v -> {
                                layoutReviewForm.setVisibility(View.VISIBLE);
                                EditText edtReview = findViewById(R.id.edtReview);
                                RatingBar ratingBar = findViewById(R.id.ratingBar);
                                edtReview.setText(reviewContentFinal);
                                ratingBar.setRating(reviewRatingFinal);
                                btnSendReview.setText("Cập nhật đánh giá");
                                btnSendReview.setOnClickListener(v2 -> {
                                    String comment = edtReview.getText().toString().trim();
                                    int selectedRating = (int) ratingBar.getRating();
                                    if (selectedRating < 1) {
                                        Toast.makeText(DetailActivity.this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (comment.isEmpty()) {
                                        Toast.makeText(DetailActivity.this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String token = SessionManager.getToken(DetailActivity.this);
                                    if (token == null || token.isEmpty()) {
                                        Toast.makeText(DetailActivity.this, "Bạn cần đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    btnSendReview.setEnabled(false);
                                    ReviewApiService.updateReview(DetailActivity.this, token, myReviewIdFinal, selectedRating, comment, new ReviewApiService.ReviewPostCallback() {
                                        @Override
                                        public void onSuccess(org.json.JSONObject reviewJson) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(DetailActivity.this, "Đã cập nhật đánh giá!", Toast.LENGTH_SHORT).show();
                                                edtReview.setText("");
                                                ratingBar.setRating(5);
                                                btnSendReview.setEnabled(true);
                                                layoutReviewForm.setVisibility(View.GONE);
                                                btnSendReview.setText("Gửi đánh giá");
                                                loadReviews();
                                            });
                                        }
                                        @Override
                                        public void onError(String error) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(DetailActivity.this, "Lỗi cập nhật đánh giá: " + error, Toast.LENGTH_SHORT).show();
                                                btnSendReview.setEnabled(true);
                                            });
                                        }
                                    });
                                });
                                btnEdit.setVisibility(View.GONE);
                            });

                            btnDelete.setOnClickListener(v -> {
                                String token = SessionManager.getToken(DetailActivity.this);
                                if (token == null || token.isEmpty()) {
                                    Toast.makeText(DetailActivity.this, "Bạn cần đăng nhập để xóa đánh giá", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                btnDelete.setEnabled(false);
                                ReviewApiService.deleteReview(DetailActivity.this, token, myReviewIdFinal, new ReviewApiService.ReviewPostCallback() {
                                    @Override
                                    public void onSuccess(org.json.JSONObject reviewJson) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(DetailActivity.this, "Đã xóa đánh giá!", Toast.LENGTH_SHORT).show();
                                            btnDelete.setEnabled(true);
                                            loadReviews();
                                        });
                                    }
                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(DetailActivity.this, "Lỗi xóa đánh giá: " + error, Toast.LENGTH_SHORT).show();
                                            btnDelete.setEnabled(true);
                                        });
                                    }
                                });
                            });

                            btnLayout.addView(btnEdit);
                            btnLayout.addView(btnDelete);
                            parent.addView(btnLayout, parent.indexOfChild(layoutReviewForm) + 1);
                        } else {
                            btnEditView.setVisibility(View.VISIBLE);
                            btnDeleteView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        layoutReviewForm.setVisibility(View.VISIBLE);
                        int[] ids2 = (layoutReviewForm.getTag() instanceof int[]) ? (int[]) layoutReviewForm.getTag() : null;
                        if (ids2 != null) {
                            View btnEditView2 = findViewById(ids2[0]);
                            View btnDeleteView2 = findViewById(ids2[1]);
                            if (btnEditView2 != null) btnEditView2.setVisibility(View.GONE);
                            if (btnDeleteView2 != null) btnDeleteView2.setVisibility(View.GONE);
                        }
                        btnSendReview.setText("Gửi đánh giá");
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Lỗi tải đánh giá: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void loadRelatedProducts() {
        ProductApiService.fetchProducts(this, "", new ProductApiService.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    rvRelated.setLayoutManager(new GridLayoutManager(DetailActivity.this, 2));
                    rvRelated.setAdapter(new ProductAdapter(DetailActivity.this, products));
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
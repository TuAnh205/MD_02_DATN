package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.ReviewAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Review;
import com.anhnvt_ph55017.md_02_datn.utils.ReviewApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopReviewsActivity extends AppCompatActivity {

    private View sidebarContainer;
    private View dimOverlay;
    private View mainContentArea;
    private View layoutShopUserCard;
    private ImageView ivToggleSidebar;
    private ImageView ivCloseSidebar;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvShopReviews;
    private ReviewAdapter reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        sidebarContainer = findViewById(R.id.sidebarContainer);
        dimOverlay = findViewById(R.id.dimOverlay);
        mainContentArea = findViewById(R.id.mainContentArea);
        layoutShopUserCard = findViewById(R.id.layoutShopUserCard);
        ivToggleSidebar = findViewById(R.id.ivToggleSidebar);
        ivCloseSidebar = findViewById(R.id.ivCloseSidebar);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvShopReviews = findViewById(R.id.rvShopReviews);

        reviewAdapter = new ReviewAdapter(this, reviewList);
        reviewAdapter.setOnReviewClickListener(review -> {
            Intent reviewIntent = new Intent(ShopReviewsActivity.this, ReviewDetailActivity.class);
            reviewIntent.putExtra("reviewId", review.getId());
            reviewIntent.putExtra("reviewerName", review.getUserName());
            reviewIntent.putExtra("reviewContent", review.getContent());
            reviewIntent.putExtra("reviewDate", review.getCreatedAt());
            reviewIntent.putExtra("reviewRating", review.getRating());
            reviewIntent.putExtra("responseText", review.getResponseText());
            reviewIntent.putExtra("responseByName", review.getResponseByName());
            reviewIntent.putExtra("responseDate", review.getResponseDate());
            reviewIntent.putExtra("productName", review.getProductName());
            reviewIntent.putExtra("productImageUrl", review.getProductImageUrl());
            startActivity(reviewIntent);
        });

        rvShopReviews.setLayoutManager(new LinearLayoutManager(this));
        rvShopReviews.setAdapter(reviewAdapter);

        ivToggleSidebar.setOnClickListener(v -> openSidebar());
        ivCloseSidebar.setOnClickListener(v -> closeSidebar());
        dimOverlay.setOnClickListener(v -> closeSidebar());

        layoutShopUserCard.setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopProfileActivity.class));
        });

        findViewById(R.id.menuDashboard).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuCategories).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuOrders).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, OrdersActivity.class));
        });

        findViewById(R.id.menuCustomers).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopCustomersActivity.class));
        });

        findViewById(R.id.menuVoucher).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopVoucherListActivity.class));
        });

        findViewById(R.id.menuReviews).setOnClickListener(v -> closeSidebar());

        findViewById(R.id.menuRevenue).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, RevenueActivity.class));
        });

        loadShopReviews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShopReviews();
    }

    private void openSidebar() {
        sidebarContainer.setVisibility(View.VISIBLE);
        dimOverlay.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(-sidebarContainer.getWidth(), 0, 0, 0);
        anim.setDuration(250);
        sidebarContainer.startAnimation(anim);
    }

    private void closeSidebar() {
        TranslateAnimation anim = new TranslateAnimation(0, -sidebarContainer.getWidth(), 0, 0);
        anim.setDuration(200);
        anim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation animation) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
            @Override public void onAnimationEnd(android.view.animation.Animation animation) {
                sidebarContainer.setVisibility(View.GONE);
                dimOverlay.setVisibility(View.GONE);
            }
        });
        sidebarContainer.startAnimation(anim);
    }

    private void loadShopReviews() {
        String token = SessionManager.getToken(this);
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ReviewApiService.fetchShopReviews(this, token, new ReviewApiService.ReviewListCallback() {
            @Override
            public void onSuccess(JSONArray reviews) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    reviewList.clear();

                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject obj = reviews.optJSONObject(i);
                        if (obj == null) continue;

                        String id = obj.optString("_id");
                        JSONObject userObj = obj.optJSONObject("user");
                        String userName = userObj != null ? userObj.optString("name", "Ẩn danh") : "Ẩn danh";
                        String userId = userObj != null ? userObj.optString("_id", "") : "";
                        String content = obj.optString("comment", "");
                        float rating = (float) obj.optDouble("rating", 0);
                        String createdAt = obj.optString("createdAt", "");

                        String productName = "Sản phẩm";
                        String productImageUrl = "";
                        JSONObject productObj = obj.optJSONObject("product");
                        if (productObj != null) {
                            productName = productObj.optString("name", productName);
                            productImageUrl = productObj.optString("image", "");
                            if (productImageUrl.isEmpty()) {
                                JSONArray productImages = productObj.optJSONArray("images");
                                if (productImages != null && productImages.length() > 0) {
                                    productImageUrl = productImages.optString(0, "");
                                }
                            }
                        }

                        String responseText = "";
                        String responseByName = "";
                        String responseDate = "";
                        JSONObject responseObj = obj.optJSONObject("response");
                        if (responseObj != null) {
                            responseText = responseObj.optString("text", "");
                            JSONObject respondedByObj = responseObj.optJSONObject("respondedBy");
                            responseByName = respondedByObj != null ? respondedByObj.optString("name", "") : "";
                            responseDate = responseObj.optString("respondedAt", "");
                        }

                        reviewList.add(new Review(id, userName, userId, content, rating, createdAt,
                                responseText, responseByName, responseDate, productName, productImageUrl));
                    }

                    reviewAdapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(ShopReviewsActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

package com.anhnvt_ph55017.md_02_datn.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.anhnvt_ph55017.md_02_datn.Adapters.BannerAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.CategoryAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.CategoryDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.screens.CartActivity;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.screens.NotificationActivity;
import com.anhnvt_ph55017.md_02_datn.utils.NotificationManager;
import com.anhnvt_ph55017.md_02_datn.utils.ProductApiService;

public class HomeFragment extends Fragment {

    RecyclerView rvCategory, rvProduct;
    ImageView imgNotification;
    TextView tvNotificationBadge;
    ViewPager2 vpBanner;
    LinearLayout dotsContainer;
    EditText edtSearchHome;

    List<Product> listProducts;
    List<com.anhnvt_ph55017.md_02_datn.models.Category> listCategories = new java.util.ArrayList<>();
    String selectedCategoryName = "";

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final int bannerIntervalMs = 3500;
    private int bannerCurrentIndex = 0;
    private Runnable bannerRunnable;
    private ImageView[] dots;

    boolean isShowAllCategory = false;
    boolean isShowAllProduct = false;
    TextView tvSeeAllCategory, tvSeeAllProduct;

    // Sửa loadCategoriesFromApi gọi updateCategoryList thay vì setAdapter trực tiếp
    private void loadCategoriesFromApi() {
        if (getContext() == null) return;
        com.anhnvt_ph55017.md_02_datn.utils.CategoryApiService.fetchCategories(getContext(), new com.anhnvt_ph55017.md_02_datn.utils.CategoryApiService.CategoryCallback() {
            @Override
            public void onSuccess(List<com.anhnvt_ph55017.md_02_datn.models.Category> categories) {
                listCategories = categories;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateCategoryList();
                    });
                }
            }
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(getContext(), "Load categories failed: " + error, android.widget.Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            edtSearchHome = (EditText) view.findViewById(R.id.edtSearchHome);
            vpBanner = (ViewPager2) view.findViewById(R.id.vpBanner);
            dotsContainer = (LinearLayout) view.findViewById(R.id.dotsContainer);
            rvCategory = (RecyclerView) view.findViewById(R.id.rvCategory);
            rvProduct = (RecyclerView) view.findViewById(R.id.rvProduct);
            imgNotification = (ImageView) view.findViewById(R.id.imgNotification);
            tvNotificationBadge = (TextView) view.findViewById(R.id.tvNotificationBadge);

            tvSeeAllCategory = view.findViewById(R.id.tvSeeAllCategory);
            tvSeeAllProduct = view.findViewById(R.id.tvSeeAllProduct);

            if (getContext() == null) {
                return view;
            }

            // Update notification badge
            updateNotificationBadge();


            imgNotification.setOnClickListener(v -> {
                int count = NotificationManager.getNotificationCount(getContext());

                if (count > 0) {
                    Toast.makeText(getContext(), "You have " + count + " notification(s)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No new notifications", Toast.LENGTH_SHORT).show();
                }

                // 👉 Chuyển sang màn NotificationActivity
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                startActivity(intent);

                // 👉 Nếu muốn xóa badge sau khi bấm
                NotificationManager.clearNotifications(getContext());
                updateNotificationBadge();
            });

                listProducts = new java.util.ArrayList<>();

                setupBanner();
                setupSearch();

                // Recycler category
                rvCategory.setLayoutManager(
                    new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false)
                );
                rvCategory.setNestedScrollingEnabled(true);

                // Fetch categories from backend
                loadCategoriesFromApi();

                // Recycler product
                rvProduct.setLayoutManager(new GridLayoutManager(getContext(),2));
                rvProduct.setNestedScrollingEnabled(true);

                // Load products from API
                loadProductsFromApi();
            // ...existing code...
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void setupBanner() {
        try {
            // Danh sách ảnh banner (lấy từ drawable)
            BannerAdapter adapter = new BannerAdapter(
                    java.util.Arrays.asList(
                            R.drawable.bannerapp1,
                            R.drawable.bannerapp2,
                            R.drawable.bannerapp3
                    )
            );
            tvSeeAllCategory.setOnClickListener(v -> {
                isShowAllCategory = !isShowAllCategory;
                updateCategoryList();
                tvSeeAllCategory.setText(isShowAllCategory ? "Thu gọn" : "Xem tất cả");
            });
            tvSeeAllProduct.setOnClickListener(v -> {
                isShowAllProduct = !isShowAllProduct;
                updateProductList();
                tvSeeAllProduct.setText(isShowAllProduct ? "Thu gọn" : "Xem tất cả");
            });
            vpBanner.setAdapter(adapter);
            vpBanner.setOffscreenPageLimit(1);

            // Thiết lập các chấm tròn (dots)
            setupDots(adapter.getItemCount());

            bannerRunnable = () -> {
                int itemCount = adapter.getItemCount();
                if (itemCount == 0) return;

                // Tăng vị trí banner hiện tại (tự động lặp lại)
                bannerCurrentIndex = (bannerCurrentIndex + 1) % itemCount;

                // Chuyển sang banner tiếp theo
                vpBanner.setCurrentItem(bannerCurrentIndex, true);

                // Cập nhật chấm tròn
                updateDots(bannerCurrentIndex);

                // Lặp lại sau khoảng thời gian đã set
                bannerHandler.postDelayed(bannerRunnable, bannerIntervalMs);
            };

            vpBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    // Cập nhật vị trí hiện tại khi người dùng vuốt tay
                    bannerCurrentIndex = position;
                    updateDots(position);

                    // Reset thời gian auto khi user vuốt
                    bannerHandler.removeCallbacks(bannerRunnable);
                    bannerHandler.postDelayed(bannerRunnable, bannerIntervalMs);
                }
            });

            // Bắt đầu chạy tự động slide
            bannerHandler.postDelayed(bannerRunnable, bannerIntervalMs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        try {
            edtSearchHome.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER
                                && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    String query = edtSearchHome.getText().toString().trim();
                    if (!query.isEmpty()) {
                        // For now, reload all and filter locally
                        loadProductsFromApi();
                    } else {
                        loadProductsFromApi();
                    }
                    // Hide keyboard
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromApi() {
        if (getContext() == null) return;

        ProductApiService.fetchProducts(getContext(), edtSearchHome.getText().toString().trim(), selectedCategoryName, new ProductApiService.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                listProducts = products;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        updateProductList();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("HomeFragment", "Product load error: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Load products failed: " + error, Toast.LENGTH_LONG).show();
                        // Fallback to local DB
                        loadLocalProducts();
                    });
                }
            }
        });
    }

    private void loadLocalProducts() {
        Log.d("HomeFragment", "Loading fallback local products");
        // TODO: Load from SQLite DBHelper
        listProducts = new ArrayList<>();
        ProductAdapter adapter = new ProductAdapter(getContext(), listProducts);
        rvProduct.setAdapter(adapter);
    }


    private void hideKeyboard() {
        try {
            if (getContext() != null) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(edtSearchHome.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDots(int count) {
        try {
            dots = new ImageView[count];
            dotsContainer.removeAllViews();

            for (int i = 0; i < count; i++) {
                dots[i] = new ImageView(getContext());
                dots[i].setImageResource(R.drawable.bg_circle_gray); // default inactive
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        12, 12
                );
                params.setMargins(4, 0, 4, 0);
                dots[i].setLayoutParams(params);
                dotsContainer.addView(dots[i]);
            }
            updateDots(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDots(int position) {
        try {
            if (dots != null && dots.length > 0 && position >= 0 && position < dots.length) {
                for (int i = 0; i < dots.length; i++) {
                    if (i == position) {
                        dots[i].setImageResource(R.drawable.bg_circle_blue); // active
                    } else {
                        dots[i].setImageResource(R.drawable.bg_circle_gray); // inactive
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (bannerHandler != null && bannerRunnable != null) {
                bannerHandler.removeCallbacks(bannerRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (bannerHandler != null && bannerRunnable != null) {
                bannerHandler.postDelayed(bannerRunnable, bannerIntervalMs);
            }
            // Update notification badge when fragment resumes
            updateNotificationBadge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge() {
        try {
            if (getContext() != null && tvNotificationBadge != null) {
                int count = NotificationManager.getNotificationCount(getContext());
                tvNotificationBadge.setText(String.valueOf(count));
                
                // Show badge only if count > 0
                if (count > 0) {
                    tvNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    tvNotificationBadge.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCategoryList() {
        List<com.anhnvt_ph55017.md_02_datn.models.Category> showList = listCategories;
        if (!isShowAllCategory && listCategories.size() > 6) {
            showList = listCategories.subList(0, 6);
        }
        rvCategory.setAdapter(new com.anhnvt_ph55017.md_02_datn.Adapters.CategoryAdapter(showList, selectedCategory -> {
            // Only allow single selection for backend filtering
            if (selectedCategory.isEmpty()) {
                selectedCategoryName = "";
            } else {
                int idx = selectedCategory.iterator().next();
                if (idx >= 0 && idx < listCategories.size()) {
                    selectedCategoryName = listCategories.get(idx).getName();
                } else {
                    selectedCategoryName = "";
                }
            }
            loadProductsFromApi();
        }));
    }

    private void updateProductList() {
        List<Product> showList = listProducts;
        if (!isShowAllProduct && listProducts != null && listProducts.size() > 10) {
            showList = listProducts.subList(0, 10);
        }
        rvProduct.setAdapter(new ProductAdapter(getContext(), showList));
    }


}

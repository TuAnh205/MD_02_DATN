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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.anhnvt_ph55017.md_02_datn.Adapters.BannerAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.CategoryAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.CategoryDAO;
import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.screens.CartActivity;

public class HomeFragment extends Fragment {

    RecyclerView rvCategory, rvProduct;
    ImageView imgCart;
    ViewPager2 vpBanner;
    LinearLayout dotsContainer;
    EditText edtSearchHome;

    ProductDAO productDAO;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final int bannerIntervalMs = 3500;
    private int bannerCurrentIndex = 0;
    private Runnable bannerRunnable;
    private ImageView[] dots;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            edtSearchHome = view.findViewById(R.id.edtSearchHome);
            vpBanner = view.findViewById(R.id.vpBanner);
            dotsContainer = view.findViewById(R.id.dotsContainer);
            rvCategory = view.findViewById(R.id.rvCategory);
            rvProduct = view.findViewById(R.id.rvProduct);
            imgCart = view.findViewById(R.id.imgCart);

            if (getContext() == null) {
                return view;
            }

            imgCart.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);
            });

            CategoryDAO categoryDAO = new CategoryDAO(getContext());
            productDAO = new ProductDAO(getContext());

            setupBanner();
            setupSearch();

            // Recycler category
            rvCategory.setLayoutManager(
                    new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false)
            );

            rvCategory.setAdapter(
                    new CategoryAdapter(categoryDAO.getAll(), selectedCategory -> {
                        rvProduct.setAdapter(
                                new ProductAdapter(
                                        getContext(),
                                        productDAO.getByCategories(selectedCategory)
                                )
                        );
                    })
            );

            // Recycler product
            rvProduct.setLayoutManager(new GridLayoutManager(getContext(),2));

            rvProduct.setAdapter(
                    new ProductAdapter(getContext(), productDAO.getAll())
            );
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
                        rvProduct.setAdapter(new ProductAdapter(getContext(), productDAO.searchByName(query)));
                    } else {
                        rvProduct.setAdapter(new ProductAdapter(getContext(), productDAO.getAll()));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
